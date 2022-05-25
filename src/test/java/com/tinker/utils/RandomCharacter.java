package com.tinker.utils;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Simple utility to give a random character from a known finite set.
 */
public class RandomCharacter implements Supplier<Character> {
    private final Random random = new Random();

    @Override
    public Character get() {
        String setOfCharacters = "abcdefghxyz1234567";
        return setOfCharacters.charAt(random.nextInt(setOfCharacters.length()));
    }
}
