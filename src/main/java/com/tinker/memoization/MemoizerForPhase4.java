package com.tinker.memoization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Now we've extracted this, we can rename the variables to something less specific.
 *
 * So this can now be used more widely but only with Integers, next phaase is to make this generic.
 */
public class MemoizerForPhase4 {

    public static final BiFunction<Function<Integer, Integer>, Integer, Integer> getOrCallFuncAndStore = new BiFunction<>() {
        private final Map<Integer, Integer> store = new HashMap<>();

        @Override
        public Integer apply(Function<Integer, Integer> func, Integer key) {
            if(store.containsKey(key))
                return store.get(key);
            var value = func.apply(key);
            store.put(key, value);
            return value;
        }
    };
}
