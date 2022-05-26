package com.tinker.utils;

import java.util.function.Function;

/**
 * Transforms SplitData into a valid HistogramData object.
 */
public final class DataToHistogramConverter<T> implements Function<SplitData<T>, HistogramData<T>> {

  @Override
  public HistogramData<T> apply(SplitData<T> in) {
    return new HistogramData<>(in.index(), in.content().stream().collect(new HistogramCollector<>()));
  }
}
