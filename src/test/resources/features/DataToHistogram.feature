Feature: Convert SplitData to HistogramData
  Scenario: As data is received it is necessary to convert this to a histogram representation
    Given a split data to histogram converter
    When I provide SplitData of 20, "ajkabbjjja"
    Then the HistogramData will have the value of 20, k->1, j->4, a->3 and b->2