package com.tinker.memoization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Generic static method that can accept a BiFunction and return a result.
 * But will store the result. If the result is already present then BiFunction call is
 * not needed. So uses a hash table to hold the results.
 */
public class Memoizer {
    public static <T, R> R callMemoised(final BiFunction<Function<T, R>, T, R> function, final T input) {

        Function<T, R> memoised = new Function<>() {
            //This a very private state variable within the function.
            //Holds all the results for inputs calculated so far.
            private final Map<T, R> store = new HashMap<>();

            @Override
            public R apply(T input) {
                //You cannot use store.computeIfAbsent - because the calls are recursive
                //So you may be in the process of calculating for the value already
                //you will get a concurrent modification exception.
                if(store.containsKey(input))
                    return store.get(input);
                //OK so not present we must calculate it, and then store it.
                var result = function.apply(this, input);
                //System.out.printf("Calculated for %d result %d\n", input, result);
                store.put(input, result) ;
                return result;
            }
        };
        return memoised.apply(input);
    }
}
