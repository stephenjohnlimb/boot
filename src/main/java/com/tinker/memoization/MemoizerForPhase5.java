package com.tinker.memoization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Now make this generic, so it does not need to be tied to integers
 * So accomplish this we need to introduce a method.
 */
public class MemoizerForPhase5 {

    /**
     * So declare we will be using some type parameters call T and R.
     *
     * Then state that the function getFunction will return a BiFunction.
     * That BiFunction will accept two incoming parameters the first is
     * Function<T, R>, the second is just of type T.
     * It will return a type of R.
     *
     * The Function above will accept a type T and return a type R (EK9 or any honda).
     *
     */
    public static <T, R> BiFunction<Function<T, R>, T, R> getFunction()
    {
        return new BiFunction<>() {
            private final Map<T, R> store = new HashMap<>();

            @Override
            public R apply(Function<T, R> func, T key) {
                if(!store.containsKey(key))
                    store.put(key, func.apply(key));
                return store.get(key);
            }
        };
    }
}
