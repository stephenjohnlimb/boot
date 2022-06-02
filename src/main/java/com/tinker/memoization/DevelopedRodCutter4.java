package com.tinker.memoization;

import java.util.List;
import java.util.stream.IntStream;

import static com.tinker.memoization.MemoizerForPhase4.getOrCallFuncAndStore;
/**
 * This is now the next phase of refactoring DevelopedRodCutter3.
 *
 * Lets now pull out the getOrCalculateProfit part to a separate class MemoizerForPhase4.
 */
public class DevelopedRodCutter4 {

    private final List<Integer> prices;

    public DevelopedRodCutter4(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    public int maxProfit(final int rodLength) {
        int profit = (rodLength <= prices.size()) ? prices.get(rodLength - 1) : 0;
        return IntStream.range(1, rodLength)
                .map(i -> getOrCallFuncAndStore.apply(this::maxProfit, i) + getOrCallFuncAndStore.apply(this::maxProfit, rodLength - i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new DevelopedRodCutter4(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
