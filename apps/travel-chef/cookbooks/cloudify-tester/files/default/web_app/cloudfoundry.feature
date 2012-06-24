Feature: cloudfoundry spring MVC demo application
  The demo application about travel and stuff should work correctly

  Scenario: Checking travel main page 
    When I go to "http://travel.cloudfoundry.com/"
    Then I should see "Welcome to Spring Travel"
      And I should not see "error"

  Scenario: Checking search page
    When I go to "http://travel.cloudfoundry.com/"
      And I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "hilton"
      And I submit the form named "searchCriteria"
    Then I should see "Hilton Tel Aviv"

  Scenario: Checking login
    When I go to "http://travel.cloudfoundry.com/"
      And I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I submit the form named "f"
    Then I should see "Welcome, keith"
    

  Scenario: Checking login 2
    Given I am HTTP digest authenticated with the following credentials:
      |username|password|
      |keith|melbourne|
    When I go to "http://travel.cloudfoundry.com/"
    Then I should see "Welcome, keith"
      
