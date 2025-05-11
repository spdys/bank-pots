Feature: Pot Withdrawal Functionality

  Background:
    Given I have a valid JWT token for a user
    And I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Travel", allocation type "FIXED", and value 100.0
    Then I have a valid JWT token for an admin
    And I have a valid deposit salary request with amount 500.0

  Scenario: Successfully withdraw money from pot to main account
    Given I have a valid JWT token for a user
    When I withdraw 50.0 from the pot to the main account
    Then the response status code should be 200
    And the withdrawal response should contain valid transaction details

  Scenario: Successfully withdraw money from pot to main account
    Given I have a valid JWT token for a user
    When I withdraw 70.0 from the pot to the main account
    Then the response status code should be 200
    And the withdrawal response should contain valid transaction details

  Scenario: Fail to withdraw more money than available in pot
    Given I have a valid JWT token for a user
    When I withdraw 200.0 from the pot to the main account
    Then the response status code should be 400

  Scenario: Fail to withdraw from non-existent pot
    Given I have a valid JWT token for a user
    When I try to withdraw from non-existent pot with ID 9999
    Then the response status code should be 404

  Scenario: Fail to  withdraw money from pot to main account due to negative number
    Given I have a valid JWT token for a user
    When I withdraw -50.0 from the pot to the main account
    Then the response status code should be 400

  Scenario: Fail to  withdraw money from pot to main account due no valid token
    When I withdraw -50.0 from the pot to the main account
    Then the response status code should be 403