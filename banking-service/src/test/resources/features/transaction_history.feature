Feature: Fetch transaction history by account ID

  Scenario: Fetch transaction history for account
    Given I have a valid JWT token for a user
    And I create a "MAIN" account
    And I store the returned account ID
    And I have a valid JWT token for an admin
    And I have a valid deposit salary request with amount 500.0
    Given I have a valid JWT token for a user
    When I fetch the transaction history for the stored account
    Then the response status code should be 200
    And the response should contain a list of transactions

  Scenario: Fail to retrieve transaction history with no IDs
    Given I have a valid JWT token for a user
    When I fetch transaction history with no IDs
    Then the response status code should be 400

  Scenario: Fail to retrieve transaction history with multiple IDs
    Given I have a valid JWT token for a user
    When I fetch transaction history with both account and pot IDs
    Then the response status code should be 400

  Scenario: Fail to retrieve transaction history for unauthorized or nonexistent account
    Given I have a valid JWT token for a user
    When I fetch transaction history for account ID 99
    Then the response status code should be one of "403, 404"
