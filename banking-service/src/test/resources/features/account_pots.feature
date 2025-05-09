Feature: User account and pot operations

  Background:
    Given I have a valid JWT token for a user

  Scenario: Successfully create, edit, and retrieve a user account and its pots
    When I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Groceries", allocation type "FIXED", and value 100.0
    And I edit pot ID 1 in the stored account with name "Food", allocation type "PERCENTAGE", and value 0.2
    And I retrieve the summary for the stored account
    Then the response status code should be 200

  Scenario: Successfully create a SAVINGS account
    When I create a "SAVINGS" account
    Then the response status code should be 201

  Scenario: Fail to create a second MAIN account
    When I create a "MAIN" account
    And I create a "MAIN" account
    Then the response status code should be 400

  Scenario: Fail to create a pot with duplicate name
    When I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Savings", allocation type "FIXED", and value 50.0
    And I create a pot in the stored account with name "Savings", allocation type "PERCENTAGE", and value 0.1
    Then the response status code should be 400

  Scenario: Fail to create a pot with zero allocation
    When I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Useless", allocation type "FIXED", and value 0.0
    Then the response status code should be 400

  Scenario: Fail to create a pot with negative allocation
    When I create a "MAIN" account
    And I store the returned account ID
    And I create a pot in the stored account with name "Oops", allocation type "PERCENTAGE", and value -0.3
    Then the response status code should be 400

  Scenario: Fail to create a 7th pot
    When I create a "MAIN" account
    And I store the returned account ID
    And I create 6 pots in the stored account
    And I create a pot in the stored account with name "Overflow", allocation type "FIXED", and value 10.0
    Then the response status code should be 400
