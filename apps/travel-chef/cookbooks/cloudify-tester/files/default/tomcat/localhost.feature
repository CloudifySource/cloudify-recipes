Feature: local tomcat instance
  It should be exposing jmx attributes on port 11099

  Scenario: check the thread pool
    Given I have JMX exposed locally on port 11099
        And I have the object 'Catalina:name=http-8080,type=ThreadPool'
    Then it should have attribute 'maxThreads' with value '200'

