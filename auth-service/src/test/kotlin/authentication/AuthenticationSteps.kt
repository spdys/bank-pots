package authentication

import authentication.security.jwt.JwtService
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.http.HttpHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.util.AssertionErrors.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationSteps {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var jwtService: JwtService


    private var token = ""
    private var headersMap = mutableMapOf<String, List<String>>()
    private var response: ResponseEntity<String>? = null

    // for later
//    @Before
//    fun setup() {
//        val user = UserEntity(username = "test", password = "Password123")
//    }


    @Given("I have a user with username {string} and password {string}")
    fun iHaveAUserWithUsernameAndPassword(username: String, password: String) {
        // You can use this step to set up the user data before making the request
        val userPayload = """
        {
            "username": "$username",
            "password": "$password"
        }
        """
        headersMap["Content-Type"] = listOf("application/json")
        makePostRequest("/api/v1/users/register", userPayload)  // Call the method to make the POST request
    }

    // Helper function to make a POST request
    private fun makePostRequest(endpoint: String, payload: String): ResponseEntity<String>? {
        val headers = HttpHeaders()
        headers.putAll(headersMap)  // Add headers, including Content-Type
        val requestEntity = HttpEntity(payload, headers)  // Prepare the HTTP request with body and headers

        // Make the POST request and capture the response
        response = testRestTemplate.exchange(
            endpoint,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )
        return response

    }

    @When("I register the user")
    fun iRegisterTheUser() {

    }

    @When("I login with username {string} and password {string}")
    fun iLoginWithUsernameAndPassword(username: String, password: String) {
        val loginPayload = """
        {
            "username": "$username",
            "password": "$password"
        }
        """
        headersMap["Content-Type"] = listOf("application/json")
        response = makePostRequest("/api/v1/users/auth/login", loginPayload)
    }

    @Then("a token should exist in the response")
    fun aTokenShouldExistInTheResponse() {
        assertTrue("Token should exist", response?.body?.contains("token") == true)
    }


    @Then("the response status code should be {int}")
    fun theResponseStatusCodeShouldBe(expectedStatusCode: Int) {
        assertEquals(expectedStatusCode, response?.statusCode?.value())  // Compare actual and expected status code
    }

    @When("I check the token from the response")
    fun iCheckTheTokenFromTheResponse() {
        val tokenRegex = """"token"\s*:\s*"([^"]+)"""".toRegex()
        val match = tokenRegex.find(response?.body ?: "")
        token = match?.groupValues?.get(1) ?: throw IllegalStateException("Token not found in response.")

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $token")
        val entity = HttpEntity<String>(headers)

        response = testRestTemplate.exchange(
            "/api/v1/users/auth/check-token",
            HttpMethod.POST,
            entity,
            String::class.java
        )
    }


}