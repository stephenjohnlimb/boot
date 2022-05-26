package com.tinker.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Consumes incoming values and builds a histogram of the frequency of occurrences.
 */
public final class Histogram<T> implements Consumer<T> {
  private final HashMap<T, AtomicInteger> map = new HashMap<>();

  public Histogram()
  {

  }

  public Histogram(Map<T, AtomicInteger> withResults1, Map<T, AtomicInteger> withResults2)
  {
    map.putAll(withResults1);
    map.putAll(withResults2);
  }

  @Override
  public void accept(T t) {
    map.putIfAbsent(t, new AtomicInteger());
    map.get(t).incrementAndGet();
  }

  public Map<T, AtomicInteger> getResults() {
    return Map.copyOf(map);
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
