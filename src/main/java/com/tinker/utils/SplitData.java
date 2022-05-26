package com.tinker.utils;

import java.util.List;

/**
 * Used to create outputs from splitting an input.
 * Holds the index the data started at (zero based).
 */
public final record SplitData<T>(int index, List<T> content) {
}
