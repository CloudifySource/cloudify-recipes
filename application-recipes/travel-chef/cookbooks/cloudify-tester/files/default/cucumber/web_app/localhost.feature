Feature: local mysql instance
  It should be working in a cool fashion 

  Scenario: Checking statistics 
    Given I have a MySQL server on localhost
        And I use the username tester
        And I use the password testpass
    Then it should have less than 5 queries per second
        And it should have less than 10 threads connected

  Scenario Outline: Positively Checking tables
    Given I have a MySQL server on localhost
        And I use the username tester
        And I use the password testpass
        And I use the database test
    Then it should have the table <tablename>

        Examples:
            | tablename |
            | cats      |
            | mycats    |

   Scenario: Negatively Checking tables
    Given I have a MySQL server on localhost
        And I use the username tester
        And I use the password testpass
        And I use the database test
    Then it should not have the table bats
