package com.tinker.utils;

/**
 * Holds a histogram representation of some data.
 * Holds the index the data started at (zero based).
 */
public record HistogramData<T>(int index, Histogram<T> content) {
}
