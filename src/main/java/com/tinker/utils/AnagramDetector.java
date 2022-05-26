package com.tinker.utils;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Designed to work with either a fix number of incoming items or just a constant stream.
 */
public final class AnagramDetector implements Function<Stream<Character>, Stream<Integer>> {

  private final String toCheck;
  private final HistogramData<Character> checkHistogram;

  public AnagramDetector(String toCheck)
  {
    this.toCheck = toCheck;
    var histogram = toCheck.chars().mapToObj(value -> (char) value).collect(new HistogramCollector<>());
    this.checkHistogram = new HistogramData<>(0, histogram);
  }

  @Override
  public Stream<Integer> apply(Stream<Character> incoming) {

    Function<Character, SplitData<Character>> splitter = new Splitter<>(toCheck.length());

    Function<SplitData<Character>, HistogramData<Character>> converter = in ->
            new HistogramData<>(in.index(), in.content().parallelStream().collect(new HistogramCollector<>()));

    return incoming.map(splitter)
            .filter(splitData -> !splitData.content().isEmpty())
            .map(converter)
            .filter(histogram -> !checkHistogram.equals(histogram))
            .map(HistogramData::index);
  }
}
