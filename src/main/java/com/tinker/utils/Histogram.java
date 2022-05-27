package com.tinker.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Consumes incoming values and builds a histogram of the frequency of occurrences of T's.
 */
public final class Histogram<T> implements Consumer<T> {
  /**
   * Use a hashmap to build a count of the number occurrences of T.
   */
  private final HashMap<T, AtomicInteger> map = new HashMap<>();

  public Histogram()
  {

  }

  /**
   * Useful for joining two histogram results together.
   */
  public Histogram(Map<T, AtomicInteger> withResults1, Map<T, AtomicInteger> withResults2)
  {
    map.putAll(withResults1);
    map.putAll(withResults2);
  }

  /**
   * Creates or increments the count of the occurrence of 't'.
   */
  @Override
  public void accept(T t) {
    map.putIfAbsent(t, new AtomicInteger());
    map.get(t).incrementAndGet();
  }

  /**
   * Provides a copy of the map of the results of the occurrences of t's.
   * @return A copy of the map results.
   */
  public Map<T, AtomicInteger> getResults() {
    return Map.copyOf(map);
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
