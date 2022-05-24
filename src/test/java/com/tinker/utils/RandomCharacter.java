package com.tinker.utils;

import java.util.Random;

/**
 * Simple utility to give a random character from a known finite set.
 */
public class RandomCharacter {
    private final Random random = new Random();

    public char getRandomChar() {
        String setOfCharacters = "abcdefghxyz1234567";
        return setOfCharacters.charAt(random.nextInt(setOfCharacters.length()));
    }
}
