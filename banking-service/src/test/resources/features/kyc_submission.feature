
Feature: KYC Submission

  Scenario: Submit valid KYC JSON payload
    Given I have a valid JWT token for a user
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
    Then the response status code should be 200

  Scenario: Submit invalid Civil ID
    Given I have a valid JWT token for a user
    When I submit the following KYC JSON:
    """
    {
      "fullName": "Ali Aljadi",
      "phone": "94444398",
      "email": "ali@example.com",
      "civilId": "298041401494",
      "dateOfBirth": "14-04-1998",
      "address": "Kuwait"
    }
    """
    Then the response status code should be 400


  Scenario: Submit invalid kuwaiti phone number
    Given I have a valid JWT token for a user
    When I submit the following KYC JSON:
    """
    {
      "fullName": "Ali Aljadi",
      "phone": "34444398",
      "email": "ali@example.com",
      "civilId": "298041400494",
      "dateOfBirth": "14-04-1998",
      "address": "Kuwait"
    }
    """
    Then the response status code should be 500

  Scenario: Submit invalid name
    Given I have a valid JWT token for a user
    When I submit the following KYC JSON:
    """
    {
      "fullName": "Ali@Aljadi",
      "phone": "94444398",
      "email": "ali@example.com",
      "civilId": "298041400494",
      "dateOfBirth": "14-04-1998",
      "address": "Kuwait"
    }
    """
    Then the response status code should be 500



