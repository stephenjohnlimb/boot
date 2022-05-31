package com.tinker.avro;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Fed up with Map entrySet, stream, map e -> e.getKey(), e.getValue() boiler-plate.
 * Let's see if we can do something to wrap that up.
 *
 * @param <K> The key type
 * @param <V> The value type
 */
@FunctionalInterface
public interface Pairs<K, V> {

  /**
   * Create a 'Pairs<K, V>' object from a Map
   */
  static <K, V> Pairs<K, V> from(Map<K, V> map) {
    return from(map.entrySet().stream());
  }

  /**
   * Create a 'Pairs<K, V>' object from a Stream of Map.Entry<K, V>
   */
  static <K, V> Pairs<K, V> from(Stream<Map.Entry<K, V>> s) {
    //This is shorthand for new Pairs<K, V){ and implement entries() };
    return () -> s;
  }

  /**
   * This is the function that must be implemented, which it is in from(Stream<Map.Entry<K, V>> s)
   *
   * @return A Stream of the map entries.
   */
  Stream<Map.Entry<K, V>> entries();

  /**
   * Now this is where the value is added.
   * This is what enables you to simply convert Map contents into a pair and expects
   * a mapper that can accept two parameters and returns a single value.
   *
   * @param mapper The BiFunction that can map from two parameters via a function
   * @param <R>    The return type from the BiFunction
   * @return The actual value of type <R> from the BiFunction.
   */
  default <R> Stream<R> map(BiFunction<? super K, ? super V, ? extends R> mapper) {
    return entries().map(e -> mapper.apply(e.getKey(), e.getValue()));
  }

}