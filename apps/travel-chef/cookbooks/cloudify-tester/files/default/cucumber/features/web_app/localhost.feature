Feature: local web application
  It should have a nice html page

  Scenario: Checking index page 
    When I go to "localhost:80"
    Then I should see "hello"
