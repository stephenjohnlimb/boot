package com.tinker.utils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.Function;

/**
 * A simple splitter that outputs the last N values as a list (with an index counter).
 * As each new character is appended one is taken of the front of the list.
 * Only when the list has 'splitSize' is any list content out put.
 * So for example if the splitSize is 5, then the first 4 outputs will be an empty list.
 * Only after this will there be lists of size 5 output.
 * <p>
 * Designed to work in streams processing.
 */
public final class Splitter<T> implements Function<T, SplitData<T>> {
  private int indexCount = 0;
  private final CircularBuffer<T> buffer;

  public Splitter(int splitSize) {
    if (splitSize < 1)
      throw new InvalidParameterException("SplitSize must be greater than zero");
    buffer = new CircularBuffer<>(splitSize);
  }

  @Override
  public synchronized SplitData<T> apply(T t) {
    buffer.accept(t);
    if (buffer.isFilled())
      return new SplitData<>(indexCount++, buffer.list());
    return new SplitData<>(indexCount, List.of());
  }
}
