package banking

import banking.dto.DepositSalaryResponse
import banking.dto.PotDepositResponse
import banking.dto.PotResponse
import banking.dto.PotTransferResponse
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import kotlin.test.assertTrue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Base64



@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingSteps () {


    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private var jwtToken: String = ""
    private var response: ResponseEntity<String>? = null
    private var storedAccountId: Long = 0

//    @Autowired
//    lateinit var jwt: JwtUtilForTesting
    private var userId: Long = 0
    private var targetUserId : Long = 0
    private var adminUserId: Long = 0

    @Before
    fun cleanDatabaseExceptUsers() {
            transactionTemplate.execute {
                entityManager.createNativeQuery("DELETE FROM transactions").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM cards").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM pots").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM accounts").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM kyc").executeUpdate()

            }
    }
    @After
    fun cleanAccountsTestData() {
        if (userId != 0L) {
            transactionTemplate.execute {
                entityManager.createNativeQuery("DELETE FROM cards WHERE pot_id " +
                        "IN (SELECT id FROM pots WHERE account_id IN (SELECT id FROM accounts WHERE user_id = $userId)) " +
                        "OR account_id IN (SELECT id FROM accounts WHERE user_id = $userId)").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM pots WHERE account_id " +
                        "IN (SELECT id FROM accounts WHERE user_id = $userId)").executeUpdate()
                entityManager.createNativeQuery("DELETE FROM accounts WHERE user_id = $userId").executeUpdate()
            }
        }
    }

    @Given("I have a valid JWT token for a user")
    fun iHaveValidJWTTokenForUser() {
        val loginPayload = """
        {
            "username": "testuser",
            "password": "Password123"
        }
        """
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(loginPayload, headers)

        val loginResponse = testRestTemplate.postForEntity(
            "http://localhost:2222/api/v1/users/auth/login", // auth service URL
            request,
            String::class.java
        )

        val tokenRegex = Regex("\"token\":\"(.*?)\"")
        val match = tokenRegex.find(loginResponse.body ?: "")
        jwtToken = match?.groupValues?.get(1) ?: throw IllegalStateException("Token not found")
    }

//    @Then("I extract the user ID from the USER token")
//    fun extractUserIdFromUserToken() {
//        targetUserId = jwt.extractUserId(jwtToken)
//    }


    @Then("I extract the user ID from the USER token")
    fun extractUserIdFromUserToken() {
        val secret = "58+r/jSrNeLUqe06oVQHK5UFC4fV4dozo5uAAjbgpIU=" // optionally load from properties
        val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(jwtToken)
            .body

        val idClaim = claims["id"]
        targetUserId = when (idClaim) {
            is Number -> idClaim.toLong()
            is String -> idClaim.toLong()
            else -> throw IllegalArgumentException("Invalid ID type in token")
        }
    }


    @Given("I have a valid JWT token for an admin")
    fun iHaveValidJWTTokenForAdmin() {
        val loginPayload = """
        {
            "username": "adminuser",
            "password": "Password123"
        }
        """
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(loginPayload, headers)

        val loginResponse = testRestTemplate.postForEntity(
            "http://localhost:2222/api/v1/users/auth/login", //
            request,
            String::class.java
        )

        val tokenRegex = Regex("\"token\":\"(.*?)\"")
        val match = tokenRegex.find(loginResponse.body ?: "")
        jwtToken = match?.groupValues?.get(1) ?: throw IllegalStateException("Token not found")
    }

    @When("I submit the following KYC JSON:")
    fun iSubmitKYCDetailsWithJson(payload: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/api/v1/kyc",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @When("I call the flag KYC endpoint")
    fun iCallTheFlagKYCEndpoint() {
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)
        val request = HttpEntity(null, headers)

        response = testRestTemplate.exchange(
            "/api/v1/kyc/flag/{targetUserId}",
            HttpMethod.POST,
            request,
            String::class.java,
            targetUserId // target userId
        )
    }

    @Then("the response status code should be {int}")
    fun thenKYCStatusShouldBe(statusCode: Int) {
        assertEquals(statusCode, response?.statusCode?.value())
    }


    @When("I create a {string} account")
    fun iCreateAnAccount(accountType: String) {
        val payload = """
    {
        "accountType": "$accountType"
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/accounts/v1/create",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @And("I store the returned account ID")
    fun iStoreTheReturnedAccountId() {
        val body = response?.body ?: throw IllegalStateException("No response body found.")
        val idRegex = Regex("\"id\":\\s*(\\d+)")
        println("Response Body: $body")
        val match = idRegex.find(body) ?: throw IllegalStateException("Account ID not found in response.")
        storedAccountId = match.groupValues[1].toLong()
    }

    private var potId: Long = 0
    @And("I create a pot in the stored account with name {string}, allocation type {string}, and value {double}")
    fun iCreatePotInStoredAccount(name: String, allocationType: String, value: Double) {
        val payload = """
    {
        "name": "$name",
        "allocationType": "$allocationType",
        "allocationValue": $value
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/accounts/v1/$storedAccountId/pots",
            HttpMethod.POST,
            request,
            String::class.java
        )

        // Extract pot ID from the response body
        val body = response?.body ?: throw IllegalStateException("No response body found.")
        println("Pot creation response body: $body")

        // Use Jackson to deserialize the response to PotResponse
        val objectMapper = ObjectMapper().registerKotlinModule()
        try {
            val potResponse = objectMapper.readValue(body, PotResponse::class.java)
            potId = potResponse.potId
            println("Extracted pot ID: $potId")
        } catch (e: Exception) {
            // Fallback to regex extraction if JSON parsing fails
            val idRegex = Regex("\"potId\":\\s*(\\d+)")
            val match = idRegex.find(body)
            potId = match?.groupValues[1]?.toLong() ?: 0L
            println("Extracted pot ID using regex: $potId")
        }
    }

    @And("I edit pot ID {long} in the stored account with name {string}, allocation type {string}, and value {double}")
    fun iEditPotInStoredAccount(potId: Long, name: String, allocationType: String, value: Double) {
        val payload = """
    {
        "name": "$name",
        "allocationType": "$allocationType",
        "allocationValue": $value
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/accounts/v1/$storedAccountId/pots/$potId",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @And("I retrieve the summary for the stored account")
    fun iRetrieveSummaryForStoredAccount() {
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(null, headers)

        response = testRestTemplate.exchange(
            "/accounts/v1/$storedAccountId/summary",
            HttpMethod.GET,
            request,
            String::class.java
        )
    }

    @And("I create 6 pots in the stored account")
    fun iCreateSixPotsInStoredAccount() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val requestTemplate = { i: Int ->
            val payload = """
        {
            "name": "Pot$i",
            "allocationType": "FIXED",
            "allocationValue": 10.0
        }
        """.trimIndent()

            HttpEntity(payload, headers)
        }

        repeat(6) { i ->
            response = testRestTemplate.exchange(
                "/accounts/v1/$storedAccountId/pots",
                HttpMethod.POST,
                requestTemplate(i + 1),
                String::class.java
            )
        }
    }

    @When("I close account ID from stored account")
    fun iCloseStoredAccountAsAdmin() {
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(null, headers)

        response = testRestTemplate.exchange(
            "/admin/v1/accounts/$storedAccountId/close",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @When("I close account ID {long}")
    fun iCloseAccountById(accountId: Long) {
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(null, headers)

        response = testRestTemplate.exchange(
            "/admin/v1/accounts/$accountId/close",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @Given("I have a valid deposit salary request with amount {double}")
    fun iHaveAValidDepositSalaryRequest(amount: Double) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)
        val payload = """
        {
            "destinationId": $storedAccountId,
            "amount": $amount
        }
    """.trimIndent()

        val requestEntity = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/transactions/v1/salary",
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )
        println(response!!.body)
    }

    var depositSalaryResponse: DepositSalaryResponse? = null

    @Then("the response body should contain the transaction details")
    fun theResponseBodyShouldContainTransactionDetails() {
        val body = response?.body ?: throw IllegalStateException("Response body is null")

        val objectMapper = ObjectMapper().registerKotlinModule()
        depositSalaryResponse = objectMapper.readValue(body, DepositSalaryResponse::class.java)

        val parsedResponse = depositSalaryResponse ?: throw IllegalStateException("Response not deserialized")

        assertTrue(parsedResponse.destinationId > 0, "Destination ID should be greater than 0")
        assertTrue(parsedResponse.balanceBefore >= BigDecimal.ZERO, "Balance before should be >= 0")
        assertTrue(parsedResponse.balanceAfter >= parsedResponse.balanceBefore, "Balance after should be >= balance before")
    }


    // pot withdrawal

    private var newPotBalance: BigDecimal = BigDecimal.ZERO
    private var potWithdrawalResponse: PotTransferResponse? = null


    @When("I withdraw {double} from the pot to the main account")
    fun iWithdrawFromPotToMainAccount(amount: Double) {
        val payload = """
    {
        "sourcePotId": $potId,
        "amount": $amount
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/transactions/v1/pot/withdrawal",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @When("I try to withdraw from non-existent pot with ID {long}")
    fun iTryToWithdrawFromNonExistentPot(invalidPotId: Long) {
        val payload = """
    {
        "sourcePotId": $invalidPotId,
        "amount": 50.0
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/transactions/v1/pot/withdrawal",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @And("the withdrawal response should contain valid transaction details")
    fun theWithdrawalResponseShouldContainValidTransactionDetails() {
        val body = response?.body ?: throw IllegalStateException("Response body is null")

        val objectMapper = ObjectMapper().registerKotlinModule()
        potWithdrawalResponse = objectMapper.readValue(body, PotTransferResponse::class.java)

        val parsedResponse = potWithdrawalResponse ?: throw IllegalStateException("Response not deserialized")

        // Verify transaction details
        assertTrue(parsedResponse.newPotBalance >= BigDecimal.ZERO,
            "New pot balance should be a non-negative value")
        println(parsedResponse.newPotBalance)
        assertTrue(parsedResponse.newAccountBalance >= BigDecimal.ZERO,
            "New account balance should be a non-negative value")

        newPotBalance = parsedResponse.newPotBalance

    }

    // pot deposit
    private var potDepositResponse: PotDepositResponse? = null

    @When("I deposit {double} from account to pot ID {long}")
    fun iDepositFromAccountToPot(amount: Double, destinationPotId: Long) {
        // Check the initial pot balance first

        val payload = """
    {
        "sourceAccountId": $storedAccountId,
        "destinationPotId": $destinationPotId,
        "amount": $amount
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/transactions/v1/pot/deposit",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @When("I deposit {double} from account to the created pot")
    fun iDepositFromAccountToCreatedPot(amount: Double) {

        val payload = """
    {
        "sourceAccountId": $storedAccountId,
        "destinationPotId": $potId,
        "amount": $amount
    }
    """.trimIndent()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(jwtToken)

        val request = HttpEntity(payload, headers)

        response = testRestTemplate.exchange(
            "/transactions/v1/pot/deposit",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }

    @And("the deposit response should contain valid transaction details")
    fun theDepositResponseShouldContainValidTransactionDetails() {
        val body = response?.body ?: throw IllegalStateException("Response body is null")
        println("Deposit response: $body")

        val objectMapper = ObjectMapper().registerKotlinModule()
        potDepositResponse = objectMapper.readValue(body, PotDepositResponse::class.java)

        val parsedResponse = potDepositResponse ?: throw IllegalStateException("Response not deserialized")

        // Verify transaction details
        assertTrue(parsedResponse.newPotBalance >= BigDecimal.ZERO,
            "New pot balance should be a non-negative value")
        println("New pot balance from response: ${parsedResponse.newPotBalance}")

        assertTrue(parsedResponse.newAccountBalance >= BigDecimal.ZERO,
            "New account balance should be a non-negative value")
        println("New account balance from response: ${parsedResponse.newAccountBalance}")

        // Store the new pot balance for later assertions
        newPotBalance = parsedResponse.newPotBalance
    }





}