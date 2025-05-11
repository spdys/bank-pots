Feature: Admin closes a user account

  Background:
    Given I have a valid JWT token for an admin

  Scenario: Successfully close a user's account
    When I create a "MAIN" account
    And I store the returned account ID
    When I close account ID from stored account
    Then the response status code should be 200

  Scenario: Fail to close a nonexistent account
    When I close account ID 9999
    Then the response status code should be 404

  Scenario: Fail to close account without admin privileges
    Given I have a valid JWT token for a user
    When I close account ID 9999
    Then the response status code should be 403
