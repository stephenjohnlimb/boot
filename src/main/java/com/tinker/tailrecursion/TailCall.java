package com.tinker.tailrecursion;

import java.util.stream.Stream;

/**
 * Example of doing tail recursion.
 * In effect, takes what looks like a recursive algorithm and converts into iteration.
 * Saves the stack from blowing up.
 * <p>
 * The use of FunctionalInterface with "TailCall<T> produce();" means that
 * "TailCall.call(() -> calculate(factorial.multiply(number), number.subtract(BigInteger.ONE)));"
 * can be used in place of an anonymous class, so syntax is nicer.
 */
@FunctionalInterface
public interface TailCall<T> {

    /**
     * Design to facilitate the next recursive call.
     */
    static <T> TailCall<T> call(final TailCall<T> nextCall) {
        return nextCall;
    }

    /**
     * Designed to be the end point of the recursion.
     */
    static <T> TailCall<T> done(final T value) {
        return new TailCall<>() {
            @Override
            public TailCall<T> produce() {
                //As this is the end of the recursion nothing is produced.
                throw new Error("not implemented");
            }

            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }
        };
    }

    /**
     * This is the critical bit, as it produces the next element from the previous.
     *
     * @return The next tail call.
     */
    TailCall<T> produce();

    /**
     * Ready for implementations.
     * This should return true to trigger end of recursion.
     */
    default boolean isComplete() {
        return false;
    }

    /**
     * Provide the value of this recursive call.
     *
     * @return The actual value that should result
     */
    default T result() {
        throw new Error("not implemented");
    }

    /**
     * Go through a chain of TailCall until one returns
     * true on isComplete. Then return the value from that.
     *
     * @return The value of the complete TailCall.
     */
    default T invoke() {
        return Stream.iterate(this, TailCall::produce)
                .filter(TailCall::isComplete)
                .findFirst()
                .orElseThrow()
                .result();
    }
}
