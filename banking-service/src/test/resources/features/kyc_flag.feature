Feature: Flag KYC Endpoint

  Background:
    Given I have a valid JWT token for a user

  Scenario: Fail to flag a KYC and deny access
    When I call the flag KYC endpoint
    Then the response status code should be 400