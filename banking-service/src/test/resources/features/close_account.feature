Feature: Admin closes a user account

  Background:
    Given I have a valid JWT token for an admin

  Scenario: Successfully close a user's account
    Given I have a valid JWT token for a user
    When I create a "MAIN" account
    And I store the returned account ID
    Given I have a valid JWT token for an admin
    When I close account ID from stored account
    Then the response status code should be 200

  Scenario: Fail to close a nonexistent account
    When I close account ID 9999
    Then the response status code should be 404
