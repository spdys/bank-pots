Feature: Create Bank Account

  Background:
    Given I have a valid JWT token for a user

  Scenario: Successfully create a MAIN account
    When I create a "MAIN" account
    Then the response status code should be 201

  Scenario: Successfully create a SAVINGS account
    When I create a "SAVINGS" account
    Then the response status code should be 201

  Scenario: Fail to create a second MAIN account
    When I create a "MAIN" account
    And I create a "MAIN" account
    Then the response status code should be 400
