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