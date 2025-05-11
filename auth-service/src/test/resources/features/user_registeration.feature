Feature: User Registration

  Scenario: Successfully registering a user with valid input
    Given I have a user with username "ahjadi" and password "Password123"
    When I register the user
    Then the response status code should be 200

  Scenario: Failing to register a user with invalid input: less than 8 characters
    Given I have a user with username "ahjadi" and password "pass"
    When I register the user
    Then the response status code should be 400

  Scenario: Failing to register a user with invalid input: no digit
    Given I have a user with username "ahjadi" and password "Passwordddd"
    When I register the user
    Then the response status code should be 400

  Scenario: Failing to register a user with invalid input: no uppercase letter
    Given I have a user with username "ahjadi" and password "password123"
    When I register the user
    Then the response status code should be 400
