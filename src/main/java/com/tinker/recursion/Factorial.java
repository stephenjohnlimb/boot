package com.tinker.recursion;

/**
 * A normal stack blowing recursive algorithm.
 */
public final class Factorial {
    public static int calculate(final int number)
    {
        if(number == 1)
            return 1;
        return number * calculate(number - 1);
    }

    public static void main(String[] argv)
    {
        final int number = 5;
        System.out.printf("Factorial of %d is %d\n", number, calculate(number));
        //We should have 120 as the result.
        //Don't do this - else it will cause a stackoverflow
        /*
        final int tooLarge = 20000;
        System.out.printf("Factorial of %d is %d", number, calculate(tooLarge));
        */
    }
}
