Feature: local spring MVC demo application
  It should have a nice html page about travel and stuff

  Scenario: Checking travel main page 
    When I go to "http://localhost:8080/travel/"
    Then I should see "Welcome to Spring Travel"
      And I should not see "error"
