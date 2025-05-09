package banking

import io.cucumber.java.After
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


@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingSteps {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private var jwtToken: String = ""
    private var response: ResponseEntity<String>? = null
    private var storedAccountId: Long = 0

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

    @After
    fun cleanAccountsTestData() {
        transactionTemplate.execute {
            val testUserId: Long = 3 // test user's id
            entityManager.createNativeQuery("DELETE FROM pots WHERE account_id IN (SELECT id FROM accounts WHERE user_id = $testUserId)")
                .executeUpdate()
            entityManager.createNativeQuery("DELETE FROM accounts WHERE user_id = $testUserId").executeUpdate()
        }
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
            "/api/v1/kyc/flag",
            HttpMethod.POST,
            request,
            String::class.java
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
        val match = idRegex.find(body) ?: throw IllegalStateException("Account ID not found in response.")
        storedAccountId = match.groupValues[1].toLong()
    }

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
}