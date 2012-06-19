Feature: local spring MVC demo application
  It should have a nice html page about travel and stuff

  Scenario: Checking trave main page 
    When I go to "localhost:8080/travel"
    Then I should see "Welcome to Spring Travel"
