package com.tinker.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Consumes incoming values and builds a histogram of the frequency of occurrences.
 */
public class Histogram<T> implements Consumer<T> {
  private final HashMap<T, AtomicInteger> map = new HashMap<>();

  @Override
  public void accept(T t) {
    map.putIfAbsent(t, new AtomicInteger());
    map.get(t).incrementAndGet();
  }

  public Map<T, AtomicInteger> getResults() {
    return Map.copyOf(map);
  }
}
