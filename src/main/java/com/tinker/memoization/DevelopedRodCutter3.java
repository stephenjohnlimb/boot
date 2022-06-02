package com.tinker.memoization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This is now the next phase of refactoring DevelopedRodCutter2.
 * We want the getOrCalculateProfit function to be able to call an arbitrary function/method.
 *
 * So rather than hard wire to 'maxProfit', lets pass in the function we want it to use.
 */
public class DevelopedRodCutter3 {

    private final List<Integer> prices;

    public DevelopedRodCutter3(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    private final BiFunction<Function<Integer, Integer>, Integer, Integer> getOrCalculateProfit = new BiFunction<>() {
        private final Map<Integer, Integer> store = new HashMap<>();

        @Override
        public Integer apply(Function<Integer, Integer> maxProfitFunction, Integer rodLength) {
            if(store.containsKey(rodLength))
                return store.get(rodLength);
            var profit = maxProfitFunction.apply(rodLength);
            store.put(rodLength, profit);
            return profit;
        }
    };

    public int maxProfit(final int rodLength) {
        int profit = (rodLength <= prices.size()) ? prices.get(rodLength - 1) : 0;
        return IntStream.range(1, rodLength)
                .map(i -> getOrCalculateProfit.apply(this::maxProfit, i) + getOrCalculateProfit.apply(this::maxProfit, rodLength - i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new DevelopedRodCutter3(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
