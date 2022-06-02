package com.tinker.memoization;

import java.util.List;
import java.util.stream.IntStream;

public class SimpleRecursionRodCutter {
    private final List<Integer> prices;

    public SimpleRecursionRodCutter(final List<Integer> usePrices) {
        prices = List.copyOf(usePrices);
    }

    public int maxProfit(final int length) {
        int profit = (length <= prices.size()) ? prices.get(length - 1) : 0;
        //System.out.printf("Start profit %d for length %d ", profit, length);
        for (int i = 1; i < length; i++) {
            final int priceWhenCut = maxProfit(i) + maxProfit(length - i);
            if (priceWhenCut > profit)
                profit = priceWhenCut;
        }
        //System.out.printf("returning %d\n", profit);
        return profit;
    }

    public int theProfit(final int length){
        //Now a Streams approach - but very slow
        int profit = (length <= prices.size()) ? prices.get(length - 1) : 0;
        return IntStream.range(1, length)
                .map(i -> theProfit(i) + theProfit(length-i))
                .filter(value -> value > profit)
                .max()
                .orElse(profit);
    }

    public static void main(String[] args) {
        final var prices = List.of(2, 1, 1, 2, 2, 2, 1, 8, 9, 15);
        final var cutter = new SimpleRecursionRodCutter(prices);

        System.out.println(cutter.theProfit(5));
        //System.out.println(cutter.theProfit(22));

        //expect 10 and a very slow 44
        System.out.println(cutter.maxProfit(5));

        //System.out.println(cutter.maxProfit(22));
    }
}
