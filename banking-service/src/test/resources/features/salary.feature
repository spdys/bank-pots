Feature: Deposit Salary to Account

  Background:
    Given I have a valid JWT token for a user
    When I create a "MAIN" account
    And I store the returned account ID

  Scenario: Successfully deposit salary to account
    And I have a valid JWT token for an admin
    Given I have a valid deposit salary request with amount 1000.00
    Then the response status code should be 200
    And the response body should contain the transaction details

  Scenario: Fail to deposit salary due to negative number
    Given I have a valid JWT token for an admin
    And I have a valid deposit salary request with amount -1000.00
    Then the response status code should be 400

  Scenario: Fail to deposit salary due to insufficient permissions
    Given I have a valid JWT token for a user
    And I have a valid deposit salary request with amount 1000.00
    Then the response status code should be 403

  Scenario: Fail to deposit salary due to negative number
    Given I have a valid JWT token for a user
    And I have a valid deposit salary request with amount -1000.00
    Then the response status code should be 403