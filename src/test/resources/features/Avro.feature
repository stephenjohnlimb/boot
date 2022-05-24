Feature: Avro

  Scenario: AVRO serialisation
    Given User avro type exists
    When serialization is required
    Then the user can be serialized and deserialized