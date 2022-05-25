Feature: Create histogram of counts of values from a number of incoming values

  Scenario: The Histogram is empty if no values are added
    Given a histogram that can be used to hold Characters
    When I don't add any Characters to it
    Then the histogram is empty

  Scenario: The Histogram counts to number of times a Character is encountered
    Given a histogram that can be used to hold Characters
    When I add "ajkabbjjja"
    Then the histogram has k->1, j->4, a->3 and b->2