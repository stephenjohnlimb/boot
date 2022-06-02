package com.tinker.memoization;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.tinker.memoization.Memoizer.callMemoised;

/**
 * Most of this is straight forward. But the interaction of maxProfit and callMemoised
 * via recursion is a bit tricky.
 *
 * It's tricky because it builds a stack of recursive calls, this only unwinds when the
 * result is calculated, but it puts the memoiser between the calculations, hence the
 * 'function.apply' that moves it back to the memoiser to look up or call back.
 *
 * This keeps going until 'orElse' profit is returned, then the stack starts to unwind.
 * The memoiser then has the results in memory so stack unwinds quickly.
 */
public class MemoisedRodCutter {

    private final List<Integer> prices;

    public MemoisedRodCutter(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    public int maxProfit(final int rodLength) {

        BiFunction<Function<Integer, Integer>, Integer, Integer> compute = (function, length) -> {
            //Moved away from the for loop do this in a more streams way.
            int profit = (length <= prices.size()) ? prices.get(length - 1) : 0;
            return IntStream.range(1, length)
                    .map(i -> function.apply(i) + function.apply(length - i))
                    .filter(value -> value > profit)
                    .max()
                    .orElse(profit);
        };

        return callMemoised(compute, rodLength);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new MemoisedRodCutter(prices);

        //expect 10 and a very fast 44
        System.out.println(cutter.maxProfit(5));

        System.out.println(cutter.maxProfit(22));
    }
}
