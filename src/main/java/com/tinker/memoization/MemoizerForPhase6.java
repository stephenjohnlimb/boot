package com.tinker.memoization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Now make this generic, so it does not need to be tied to integers
 * So accomplish this we need to introduce a method.
 */
public class MemoizerForPhase6 {

    /**
     * So declare we will be using some type parameters call T and R.
     *
     * But now we've refactored away the BiFunction, by capturing the function we need
     * to call as a method parameter, now we can close over that in our Function.
     *
     * So the function we capture is the bit of business logic that can do the actual calculation.
     * i.e. given a T it can work out how to produce an R.
     *
     * This class just stores the result of that. i.e. the 'R' in a map keyed on the T.
     *
     * So next time around it checks if an R exists for that T and if so just returns it.
     *
     *
     * This is now pretty tight and focussed.
     */
    public static <T, R> Function<T, R> getMemoizeFunction(final Function<T, R> func)
    {
        return new Function<>() {
            private final Map<T, R> store = new HashMap<>();

            @Override
            public R apply(T key) {
                //Don't use store.putIfAbsent() because if algorithm is threaded or recursive you get
                //a concurrent modification exception.
                if (!store.containsKey(key))
                    store.put(key, func.apply(key));
                return store.get(key);
            }
        };
    }
}
