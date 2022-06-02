package com.tinker.memoization;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;


/**
 * This is now the next phase of refactoring DevelopedRodCutter4.
 *
 * Let's now pull out the getOrCalculateProfit part to a separate class MemoizerForPhase5.
 *
 * Now we have a field that is a BiFunction with the correct types we can call.
 *
 * The MemoizerForPhase5
 */
public class DevelopedRodCutter5 {

    private final List<Integer> prices;
    private final BiFunction<Function<Integer, Integer>, Integer, Integer> getOrCallFuncAndStore = MemoizerForPhase5.getFunction();

    public DevelopedRodCutter5(final List<Integer> usePrices) {
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
        final var cutter = new DevelopedRodCutter5(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
