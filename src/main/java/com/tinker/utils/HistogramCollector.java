package com.tinker.utils;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Thought I'd have a quick go at doing a collector.
 * <p>
 * Very useful in enabling a streamed type creation of a Histogram.
 */
public final class HistogramCollector<T> implements Collector<T, Histogram<T>, Histogram<T>> {

  @Override
  public Supplier<Histogram<T>> supplier() {
    return Histogram::new;
  }

  @Override
  public BiConsumer<Histogram<T>, T> accumulator() {
    return Histogram::accept;
  }

  @Override
  public BinaryOperator<Histogram<T>> combiner() {
    return (hist1, hist2) -> new Histogram<>(hist1.getResults(), hist2.getResults());
  }

  @Override
  public Function<Histogram<T>, Histogram<T>> finisher() {
    //There is nothing really to finish off here
    return histogram -> histogram;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of(Characteristics.UNORDERED);
  }
}

