Feature: Flag KYC Endpoint

  Background:
    Given I have a valid JWT token for a user
    And I extract the user ID from the token

  Scenario: Fail to flag a KYC and deny access
    When I call the flag KYC endpoint
    Then the response status code should be 403

  Scenario: Successfully flag a KYC as admin
    Given I have a valid JWT token for an admin
#    And I extract the user ID from the token
    When I call the flag KYC endpoint
    Then the response status code should be 200