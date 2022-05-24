Feature: CircularBuffer
  As a developer
  I want to be able use a sliding windows of values of a finite size
  So that I can always access the last N values added in an order collection

  Scenario: Check the Circular Buffer reports filled
    Given a circular buffer of size 5
    When I incrementally add characters
    Then the buffer only reports filled on and after 5 characters have been added

  Scenario: Input less than or equal to the window size
    Given a circular buffer of size 5
    When the input is less than or equal to 5
    Then the output is the same as the input

  Scenario: Input less than or equal to the default window size
    Given a default sized circular buffer
    When the input is less than or equal to the default size
    Then the output is the same as the input

  Scenario: Input greater than buffer size
    Given a circular buffer of size 5
    When the input is greater than 5
    Then the output only has the last 5 values from the input

  Scenario: Input greater than default buffer size
    Given a default sized circular buffer
    When the input is greater than the default size
    Then the output only has the last default size values from the input
