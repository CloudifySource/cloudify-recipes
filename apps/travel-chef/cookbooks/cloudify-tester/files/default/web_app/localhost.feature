Feature: local spring MVC demo application
  The Spring Travel demo application should be fully functioning

  Scenario: The main page comes up nicely
    When I go to "http://localhost:8080/travel"
    Then I should see "Welcome to Spring Travel"
      And I should see "Start your Spring Travel experience"
      But I should not see "error"

  Scenario: The search works
    When I go to "http://localhost:8080/travel"
      And I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "hilton"
      And I submit the form named "searchCriteria"
    Then I should see "Hilton Tel Aviv"
      And I should see "Hilton Diagonal Mar"

  Scenario: Cannot book a hotel without logging in
    When I go to "http://localhost:8080/travel"
      And I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "Hilton Tel Aviv"
      And I submit the form named "searchCriteria"
    Then I should see "Independence Park"
    When I follow "View Hotel"
    Then I should see "Nightly Rate:\s*210"
    When I press "Book Hotel"
    Then I should see "Login Information"

  Scenario: Cannot log in with bad credentials
    When I go to "http://localhost:8080/travel"
      And I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "wrong password"
      And I press "Login"
    Then I should not see "Welcome, keith"
      But I should see "Your login attempt was not successful"

  Scenario: Can log in with the right credentials
    When I go to "http://localhost:8080/travel"
      And I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I press "Login"
    Then I should see "Welcome, keith"

  Scenario: When logged in, can book a hotel
    #login
    When I go to "http://localhost:8080/travel"
      And I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I press "Login"
    Then I should see "Welcome, keith"
    #search
    When I follow "Change Search"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "Hilton Tel Aviv"
      And I submit the form named "searchCriteria"
    Then I should see "Independence Park"
    When I follow "View Hotel"
    Then I should see "Nightly Rate:\s*210"
    #booking (and then cancel)
    When I press "Book Hotel"
    Then I should see "Check In"
    When I fill in "creditCard" with "1234567812345678"
      And I fill in "creditCardName" with "credit user"
    #This fails due to a webrat issue with javascript, we'll need to find some hack or try it with selenium
      And I press "proceed"
    Then I should see "Confirm Booking Details"
    When I press "Confirm"
    Then I should see "Current Hotel Bookings"
      But I should not see "No bookings found"
    When I press "Cancel"
    Then I should see "No bookings found"
