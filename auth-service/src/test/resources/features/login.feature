Feature: User Authentication

  Scenario: User successfully registers and logs in
    Given I have a user with username "test" and password "Password123"
    When I register the user
    Then the response status code should be 200
    And I login with username "test" and password "Password123"
    Then a token should exist in the response
    And the response status code should be 200

  Scenario: User login with incorrect credentials
    Given I have a user with username "test" and password "Password123"
    When I register the user
    Then the response status code should be 200
    When I login with username "test" and password "WrongPassword"
    Then the response status code should be 403