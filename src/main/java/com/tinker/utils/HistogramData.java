package com.tinker.utils;

/**
 * Holds a histogram representation of some data.
 * Holds the index the data started at (zero based).
 */
public final record HistogramData<T>(int index, Histogram<T> content) {

  @Override
  public boolean equals(Object o) {
    if(o instanceof HistogramData<?> histogramData)
    {
      var thisResults = this.content.getResults();
      var otherResults = histogramData.content.getResults();

      if(thisResults.size() != otherResults.size())
        return false;

      //Check the atomic integer values are the same.
      return this.content
              .getResults()
              .entrySet()
              .parallelStream()
              .map(e -> otherResults.containsKey(e.getKey()) && e.getValue().get() == otherResults.get(e.getKey()).get())
              .anyMatch(isEqual -> !isEqual);
    }
    return false;
  }
}