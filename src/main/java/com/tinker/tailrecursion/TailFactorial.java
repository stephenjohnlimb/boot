package com.tinker.tailrecursion;


import java.math.BigInteger;

/**
 * The real point to get here, is that both the value of
 * factorial and number are in effect captured (closure).
 * So they can then be used in the next iteration.
 * rather than being built up in the stack.
 * <p>
 * The return value is not a stack type result, but a data structure that
 * contains a lambda so the result can be calculated.
 * <p>
 * Look at "invoke" in TailCall. All it does is iterate through a series of these TailCall
 * objects these are the 'done' or 'call' lambdas.
 * <p>
 * So while, this code looks recursive via a stack, in fact it has been converted to iteration,
 * but the closure over the values being passed in to the next iterative call.
 * Finally, when 'number' have been reduced to '1' the TailCall.done is returned. By this time the
 * actual calculation of the factorial has now been completed.
 *
 * Taken and refactored form the Java lambdas book. A clever use of FunctionalInterfaces.
 */
public class TailFactorial {

    /**
     * This is how you can convert a recursive call from being stack based to iteration based.
     *
     * @param factorial The initial value of the factorial
     * @param number    The number you want to find the factorial of.
     * @return The final tail call - that will hold the actual final factorial
     */
    public static TailCall<BigInteger> calculate(final BigInteger factorial, final BigInteger number) {
        if (number.equals(BigInteger.ONE))
            return TailCall.done(factorial);

        //This is where the real trick is, both final params (factorial and number) are in effect
        //closed over and held (captured) below in a lambda. That lambda is then bound into
        //a data structure called TailCall.
        //Now if you look at TailCall.invoke it calls 'produce()' which is in effect THE
        //() lambda bit below. So 'calculate' with the 'closed over values' gets called again.
        //But the key bit is that the stack is unwound - this is because of the 'lazy' delayed processing
        //of the lambda.
        //Only when 'calculate' is triggered with ONE does a different TailCall data structure get returned
        //This has isComplete true, and 'produce' is not called.
        return TailCall.call(() -> calculate(factorial.multiply(number), number.subtract(BigInteger.ONE)));
    }

    /**
     * Just a simple wrapper on the actual factorial function.
     */
    public static BigInteger calculate(final BigInteger factorialOf) {
        return calculate(BigInteger.ONE, factorialOf).invoke();
    }

    public static void main(String[] argv) {
        final BigInteger number = BigInteger.valueOf(5L);
        var result = calculate(number);
        System.out.printf("Factorial of %d is %d\n", number, result);

        //So now with tail recursion and BigInteger it is possible to calculate the result.
        //And that is a very-very big number.
        final BigInteger notTooLarge = BigInteger.valueOf(20000);
        result = calculate(notTooLarge);
        System.out.printf("Factorial of %d is %d\n", notTooLarge, result);

    }
}