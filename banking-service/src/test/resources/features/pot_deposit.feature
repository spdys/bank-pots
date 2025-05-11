Feature: Pot Deposit Functionality

  Background:
    Given I have a valid JWT token for a user
    And I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Savings", allocation type "FIXED", and value 100.0
    And I have a valid JWT token for an admin
    And I have a valid deposit salary request with amount 500.0

  Scenario: Successfully deposit money from main account to pot
    Given I have a valid JWT token for a user
    When I deposit 75.0 from account to the created pot
    Then the response status code should be 200
    And the deposit response should contain valid transaction details


  Scenario: Fail to deposit more money than available in main account
    Given I have a valid JWT token for a user
    When I deposit 1000.0 from account to the created pot
    Then the response status code should be 400

  Scenario: Fail to deposit to non-existent pot
    Given I have a valid JWT token for a user
    When I deposit 50.0 from account to pot ID 9999
    Then the response status code should be 404