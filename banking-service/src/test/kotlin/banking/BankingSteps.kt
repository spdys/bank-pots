package banking

import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity


@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingSteps {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    private var jwtToken: String = ""
    private var response: ResponseEntity<String>? = null

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


}