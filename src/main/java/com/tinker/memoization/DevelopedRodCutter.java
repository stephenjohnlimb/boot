package com.tinker.memoization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * This is a sort of staging phase of development before extracting the
 * Generic Memoizer out.
 *
 * So we start out with just a standard recursive algorithm.
 * Now add in a Map called 'store'.
 *
 * So the method 'getOrCalculateProfit' looks in store or does calculation
 * puts result in store for next time.
 *
 * What the extraction of the Memoiser does, is decouple the storage and memoisation
 * from a specific class (this one).
 *
 */
public class DevelopedRodCutter {

    private final List<Integer> prices;

    private final Map<Integer, Integer> store = new HashMap<>();

    public DevelopedRodCutter(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    private int getOrCalculateProfit(final int rodLength)
    {
        if(store.containsKey(rodLength))
            return store.get(rodLength);
        var profit = maxProfit(rodLength);
        store.put(rodLength, profit);
        return profit;
    }
    public int maxProfit(final int rodLength) {

        int profit = (rodLength <= prices.size()) ? prices.get(rodLength - 1) : 0;
        return IntStream.range(1, rodLength)
                .map(i -> getOrCalculateProfit(i) + getOrCalculateProfit(rodLength - i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new DevelopedRodCutter(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
