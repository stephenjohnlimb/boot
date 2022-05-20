Feature: Avro for java8 glue classes

  Scenario Outline: AVRO serialisation
    Given User avro type exists
    When serialization is required
    Then the user can be serialized and deserialized
    Examples:
      | User Data is Serialised  |