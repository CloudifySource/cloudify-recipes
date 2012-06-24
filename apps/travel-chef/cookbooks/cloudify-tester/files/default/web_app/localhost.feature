Feature: local spring MVC demo application
  It should have a nice html page about travel and stuff

  Scenario: Checking travel main page 
    When I go to "http://localhost:8080/travel/"
    Then I should see "Welcome to Spring Travel"
      And I should not see "error"

  Scenario: Checking search page
    When I go to "http://localhost:8080/travel/"
      And I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "hilton"
      And I submit the form named "searchCriteria"
    Then I should see "Hilton Tel Aviv"

  Scenario: Checking login
    When I go to "http://localhost:8080/travel/"
      And I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I press "Login"
    Then I should see "Welcome, keith"

