Feature: local spring MVC demo application
  The Spring Travel demo application should be fully functioning

  Background: Browsing to our travel website
    When I go to "http://localhost:8080/travel/"
    
  Scenario: The main page comes up nicely
    Then I should see "Welcome to Spring Travel"
      And I should see "Start your Spring Travel experience"
      But I should not see "error"

  Scenario: The search works
    When I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "hilton"
      And I submit the form named "searchCriteria"
    Then I should see "Hilton Tel Aviv"
      And I should see "Hilton Diagonal Mar"

  Scenario: Cannot book a hotel without logging in
    When I follow "Start your Spring Travel experience"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "Hilton Tel Aviv"
      And I submit the form named "searchCriteria"
    Then I should see "Independence Park"
    When I follow "View Hotel"
    Then I should see "Nightly Rate:\s*210"
    When I press "Book Hotel"
    Then I should see "Login Information"

  Scenario: Cannot log in with bad credentials
    When I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "wrong password"
      And I press "Login"
    Then I should not see "Welcome, keith"
      But I should see "Your login attempt was not successful"

  Scenario: Can log in with the right credentials
    When I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I press "Login"
    Then I should see "Welcome, keith"
    
  Scenario: When logged in, can book a hotel
    # login
    When I follow "Login"
    Then I should see "Login Information"
    When I fill in "j_username" with "keith"
      And I fill in "j_password" with "melbourne"
      And I press "Login"
    Then I should see "Welcome, keith"
    # search
    When I follow "Change Search"
    Then I should see "Search Hotels"
    When I fill in "searchString" with "Hilton Tel Aviv"
      And I submit the form named "searchCriteria"
    Then I should see "Independence Park"
    When I follow "View Hotel"
    Then I should see "Nightly Rate:\s*210"
    # book the hotel
    When I press "Book Hotel"
    Then I should see "Check In"
    # HACK warning: spring uses javascript that's too hard for webrat, so we post manualy
    Then I POST to "/travel/hotels/booking?execution=e1s1" the data:
         """
         checkinDate=01-01-9999, checkoutDate=01-02-9999, beds=1, smoking=false, _amenities=on, creditCard=0123456701234567,
         creditCardName=b, creditCardExpiryMonth=1, creditCardExpiryYear=1, _eventId_proceed=_eventId_proceed
         """
    Then I POST to "/travel/hotels/booking?execution=e1s2" the data:
         """
         _eventId_confirm=
         """
    Then I should see "Current Hotel Bookings"
      But I should see "Confirmation Number"
    * I press "Cancel"
    # TODO: hack around to click the exact same booking that I just made and verify that it's gone
