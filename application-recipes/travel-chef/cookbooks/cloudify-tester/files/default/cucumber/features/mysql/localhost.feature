Feature: local mysql instance
  It should be working in a cool fashion 

  Scenario: Checking statistics 
    Given I have a MySQL server on localhost
        And I use the username travel
        And I use the password test
    Then it should have less than 5 queries per second
        And it should have less than 10 threads connected

   Scenario: Negatively Checking tables
    Given I have a MySQL server on localhost
        And I use the username travel
        And I use the password test
        And I use the database travel
    Then it should not have the table bats
