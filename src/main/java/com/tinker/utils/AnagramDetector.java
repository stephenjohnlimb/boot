package com.tinker.utils;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Designed to work with either a fix number of incoming Characters or a constant stream of Characters.
 */
public final class AnagramDetector implements Function<Stream<Character>, Stream<Integer>> {

  private final String toCheck;
  private final HistogramData<Character> checkHistogram;

  /**
   * Uses a 'histogram mechanism' to detect anagrams of a word that occur in an incoming stream of characters.
   * <p>
   * It does this by breaking up the incoming stream of characters in to words that are the same length as the
   * word we are trying to detect anagrams of.
   * <p>
   * It then converts each of those words into a histogram of the letters in the word.
   * <p>
   * Finally, it checks if that histogram matches the same histogram of the word we are checking for anagrams of.
   */
  public AnagramDetector(String toCheck) {
    this.toCheck = toCheck;
    this.checkHistogram = new HistogramData<>(
            0,
            toCheck.chars().mapToObj(value -> (char) value).collect(new HistogramCollector<>())
    );
  }

  @Override
  public Stream<Integer> apply(Stream<Character> incoming) {

    /*
     * This is the function that will do the splitting. It will accept
     * characters coming in and produce a split data object.
     */
    Function<Character, SplitData<Character>> splitter = new Splitter<>(toCheck.length());

    /*
     * The function that converts a split data object into a histogram of the characters in that split data.
     */
    Function<SplitData<Character>, HistogramData<Character>> converter = in ->
            new HistogramData<>(in.index(), in.content().parallelStream().collect(new HistogramCollector<>()));

    /*
     * This is the main function that produces the list of indexes the anagrams start at.
     */
    return incoming.map(splitter)
            .filter(splitData -> !splitData.content().isEmpty())
            .map(converter)
            .filter(histogram -> !checkHistogram.equals(histogram))
            .map(HistogramData::index);
  }
}
