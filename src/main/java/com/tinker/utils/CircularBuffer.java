package com.tinker.utils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple implementation of a circular buffer.
 * Just really to try a bit of Cucumber BDD and also
 * for use in some Streams processing, where I want to build up
 * some state and output a sliding window of values.
 *
 * @param <T> The type to use with the circular buffer.
 */
public final class CircularBuffer<T> implements Consumer<T> {

    /**
     * The buffer that holds the items.
     */
    private T[] buffer;

    /**
     * What capacity was the buffer created with
     */
    private final int capacity;

    /**
     * The head of the buffer.
     */
    private int head;

    /**
     * The tail of the buffer.
     */
    private int tail;

    /**
     * Must make a note if the buffer has been filled, because then we have to overwrite.
     */
    private boolean overwrite;

    /**
     * A new buffer with a default size of 10 elements.
     */
    public CircularBuffer() {
        this(10);
    }

    /**
     * A new buffer with a specific capacity - must be greater than zero.
     */
    public CircularBuffer(int capacity) {
        if (capacity <= 0)
            throw new InvalidParameterException("Capacity must be greater than zero");
        this.capacity = capacity;
        clear();
    }

    /**
     * Clear and fully resets the buffer.
     */
    public synchronized CircularBuffer<T> clear() {
        buffer = (T[]) new Object[this.capacity];
        head = 0;
        tail = -1;
        overwrite = false;
        return this;
    }

    @Override
    public void accept(T t) {
        this.append(t);
    }

    public synchronized CircularBuffer<T> append(T value) {

        tail++;
        if (tail == capacity)
            overwrite = true;

        //So we must move the head on and start overwriting
        if (overwrite)
            head++;
        //ensure they both stay within the bounds of the array.
        tail %= capacity;
        head %= capacity;
        buffer[tail] = value;

        return this;
    }

    /**
     * Is the circular buffer full.
     * This basically means it has or will start to overwrite content.
     * It does not mean that is cannot accept more content.
     * @return true if all the slots in the buffer have been written to.
     */
    public synchronized boolean isFilled() {
        if(overwrite)
            return true;
        return (tail + head + 1) == capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Provide the buffer as a list of items.
     * This maybe empty or smaller than the capacity the circular buffer was created with.
     *
     * @return The order list of items.
     */
    public synchronized List<T> list() {
        //If nothing added then return empty list
        if (tail == -1)
            return List.of();

        var values = new ArrayList<T>();
        if (this.overwrite) {
            //As we know the buffer is full, we know the size
            //We also know to start at head, but stay within bounds.

            for (int i = 0; i < capacity; i++) {
                values.add(buffer[(head + i) % capacity]);
            }

        }
        else {
            for (int i = head; i <= tail; i++)
                values.add(buffer[i]);
        }
        return List.copyOf(values);
    }
}
