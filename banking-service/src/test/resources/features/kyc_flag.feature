Feature: Flag KYC Endpoint

  Background:
    Given I have a valid JWT token for a user
    And I extract the user ID from the USER token
    When I submit the following KYC JSON:
    """
    {
      "fullName": "Ali Aljadi",
      "phone": "94444398",
      "email": "test@ali.com",
      "civilId": "298030700133",
      "dateOfBirth": "14-04-1998",
      "address": "Kuwait"
    }
    """
    And the response status code should be 200

  Scenario: Fail to flag a KYC and deny access
    When I call the flag KYC endpoint
    Then the response status code should be 403

  Scenario: Successfully flag a KYC as admin
    Given I have a valid JWT token for an admin
    When I call the flag KYC endpoint
    Then the response status code should be 200

  Scenario: Fail to flag nonexistent user
    Given I have a valid JWT token for an admin
    When I call the flag KYC endpoint for user ID 99999
    Then the response status code should be 404
