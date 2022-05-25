Feature: Splitter of in incoming values in to Lists of a specific length

  Scenario: The splitter outputs empty lists if the input is less than the split size
    Given a splitter configured to split at size 5
    When the splitter input is less than 5
    Then the output of the splitter is a Stream of empty lists

  Scenario: The splitter outputs empty lists until the split size is met
    Given a splitter configured to split at size 5
    When the splitter input is greater than 5
    Then the output of the splitter first 4 lists are empty and subsequent list contain just the last 5 inputs
