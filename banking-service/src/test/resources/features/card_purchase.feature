Feature: Purchasing with card

  Scenario: Successfully purchase using account card
    Given I have a valid JWT token for an admin
    And I create a "MAIN" account
    And I store the returned account ID
    And I have a valid deposit salary request with amount 300.0
    When I retrieve the summary for the stored account
    And I extract the account card number
    And I purchase 100.0 from the card to destination ID 9999
    Then the response status code should be 200

  Scenario: Fail to purchase with non-existent card
    Given I have a valid JWT token for a user
    When I attempt to purchase 100.0 from invalid card "404CARD123" to destination ID 9999
    Then the response status code should be 404

  Scenario: Fail to purchase with insufficient balance
    Given I have a valid JWT token for an admin
    And I create a "MAIN" account
    And I store the returned account ID
    And I retrieve the summary for the stored account
    And I extract the account card number
    When I purchase 9999.0 from the card to destination ID 9999
    Then the response status code should be 400
