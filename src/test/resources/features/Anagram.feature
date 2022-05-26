Feature: Report indexes of anagram in an incoming stream of characters
  Scenario: I want to know the indexes of Strings that are an anagram of a known string.
    Given  a known value of "ab"
    When processing an incoming Stream of characters of "abxaba"
    Then 3 anagrams are detected at indexes 0, 3 and 4
