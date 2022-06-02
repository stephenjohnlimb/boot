package com.tinker.memoization;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;


/**
 * This is now the next and final phase of refactoring DevelopedRodCutter5.
 *
 * Let's see if we can simplify this a bit more.
 * We always pass in method maxProfit - so why not capture this in the getFunction.
 * This would then simplify the getOrCallFuncAndStore apply back to a Function rather than a BiFunction.
 *
 * So we have separated two concerns and made the memoization generic and reusable.
 */
public class DevelopedRodCutter6 {

    private final List<Integer> prices;
    private final Function<Integer, Integer> getOrCallFuncAndStore = MemoizerForPhase6.getMemoizeFunction(this::maxProfit);

    public DevelopedRodCutter6(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    public int maxProfit(final int rodLength) {
        int profit = (rodLength <= prices.size()) ? prices.get(rodLength - 1) : 0;
        return IntStream.range(1, rodLength)
                .map(i -> getOrCallFuncAndStore.apply(i) + getOrCallFuncAndStore.apply(rodLength - i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new DevelopedRodCutter6(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
