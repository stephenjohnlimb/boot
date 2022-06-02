package com.tinker.memoization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This is now the next phase of refactoring DevelopedRodCutter.
 *
 * We want to pull the store and the getOrCalculate out as a separate Function.
 *
 * So this now bundles the storage memoisation in with the function, but it is tied back to
 * 'maxProfit'.
 */
public class DevelopedRodCutter2 {

    private final List<Integer> prices;

    public DevelopedRodCutter2(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    private final Function<Integer, Integer> getOrCalculateProfit = new Function<>() {
        private final Map<Integer, Integer> store = new HashMap<>();
        @Override
        public Integer apply(Integer rodLength) {
            if(store.containsKey(rodLength))
                return store.get(rodLength);
            var profit = maxProfit(rodLength);
            store.put(rodLength, profit);
            return profit;
        }
    };

    public int maxProfit(final int rodLength) {
        int profit = (rodLength <= prices.size()) ? prices.get(rodLength - 1) : 0;
        return IntStream.range(1, rodLength)
                .map(i -> getOrCalculateProfit.apply(i) + getOrCalculateProfit.apply(rodLength - i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new DevelopedRodCutter2(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
