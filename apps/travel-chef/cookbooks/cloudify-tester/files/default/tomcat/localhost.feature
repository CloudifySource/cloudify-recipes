Feature: local tomcat instance
  It should be exposing jmx attributes on port 11099

  Scenario: check the thread pool
    Given I have JMX exposed locally on port 11099
    When I examine the object "Catalina:name=http-8080,type=ThreadPool"
    Then the attribute "maxThreads" is equal to 200
    And the attribute "currentThreadsBusy" is less than 150
    
  Scenario: check the garbage collector
    Given I have JMX exposed locally on port 11099
    When I examine the object "java.lang:name=Copy,type=GarbageCollector"
    Then the attribute "CollectionCount" is between 10 and 50
    
  Scenario: check the request processor
    Given I have JMX exposed locally on port 11099
    When I examine the object "Catalina:name=http-8080,type=GlobalRequestProcessor"
    Then the attribute "maxTime" is between 5 and 20
    And the attribute "errorCount" is equal to 0
