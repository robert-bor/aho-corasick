package org.ahocorasick.trie;

import java.util.Locale;

/**
 * Circular buffer backed by a primitive int array.  This buffer has a start index and an end index.  If the indices
 * are the same the buffer is empty.  If the index after the end is the start, then the buffer is full.
 */
public class CircularBuffer implements CharSequence
{
    ////////////////////////////////////////////////////////////////////////////
    // Fields

    /**
     * The start index.  The character at the start index is always invalid.
     */
    private int start = 0;

    /**
     * The end index.  When empty end == start, then as each item is added it increments, potentially wrapping, until
     * it gets to the index before the start.  The buffer is then full.
     */
    private int end = 0;

    /**
     * The int array.
     */
    private int[] buffer;

    /**
     * Cache of whether buffer is full, which is the most common case.
     */
    private boolean isFull = false;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors

    /**
     * Creates a circular buffer with the given length.
     * @param length the length of the buffer.
     */
    public CircularBuffer(int length)
    {
        buffer = new int[length + 1];
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods

    /**
     * Adds the given value to the buffer
     * @param value the value to add to the buffer.
     * @return the dropped value if the buffer is full, or {@code -1}.
     */
    public int add(int value)
    {
        int droppedValue = -1;
        if (isFull())
        {
            start = getNext(start);
            droppedValue = buffer[start]; // actually references one ahead of the start char.
        }

        end = getNext(end);
        buffer[end] = value;
        return droppedValue;
    }

    /**
     * Checks for the given value in the buffer.
     * @param value the value to look for.
     * @return {@code true} if the value is found.
     */
    public boolean contains(int value)
    {
        if (isEmpty())
        {
            return false;
        }
        if (isFull())
        {
            // Most common case
            for (int i = 0; i < buffer.length; i++)
            {
                if (i == start)
                {
                    continue;
                }
                if (buffer[i] == value)
                {
                    return true;
                }

            }
        }
        else
        {
            // this uses the start and logical index which will be a little slower than just simply iterating over the
            // buffer, but the buffer is usually full, so this isn't usually used.
            for (int i = 0; i < getLength(); i++)
            {
                if (buffer[getNext(start + i)] == value)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * For a non-empty buffer this method removes an element and returns the element that was removed.
     * @return the element that was removed.
     */
    public int remove()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("Removed shouldn't be called when empty.");
        }

        isFull = false;
        start = getNext(start);
        return buffer[start];
    }

    @Override
    public char charAt(int index)
    {
        if (index > getLength())
        {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Can't return char at index %d for buffer of length %d", index, getLength()));
        }

        // first element is after the start.
        return (char) buffer[getNext(start + index)];
    }

    @Override
    public int length()
    {
        return getLength();
    }

    @Override
    public CharSequence subSequence(int subStart, int subEnd)
    {
        return subString(subStart, subEnd);
    }

    /**
     * Gets the length of the string in the buffer.
     * @return the length of the string in the buffer.
     */
    public int getLength()
    {
        if (isFull())
        {
            // always 1 less than full buffer.
            return buffer.length - 1;
        }

        return end - start + (end < start ? (buffer.length) : 0);
    }

    /**
     * This will clear the buffer.
     */
    public void clearBuffer()
    {
        start = 0;
        end = 0;
        isFull = false;
    }

    /**
     * Whether this is empty.
     * @return whether this is empty.
     */
    public boolean isEmpty()
    {
        return start == end;
    }

    /**
     * Whether this is full.
     * @return whether this is full.
     */
    public boolean isFull()
    {
        if (isFull)
        {
            // this is the most common state, so we cache the result.
            return true;
        }
        else if (getNext(end) == start)
        {
            isFull = true;
            return true;
        }

        return false;
    }

    /**
     * Gets the index after that given, wrapping once it is past the end of the buffer.
     * @param current the current index.
     * @return the next valid index.
     */
    private int getNext(int current)
    {
        return (current + 1) % buffer.length;
    }

    /**
     * Returns the string of the given length.
     * @param length the length of the string.
     * @return the string.
     */
    public String toString(int length)
    {
        return subString(0, length);
    }

    /**
     * Creates a sub-string of this buffer.
     * @param subStart the start value (inclusive).
     * @param subEnd the end value (exclusive).
     * @return the sub-string.
     * @throws  IndexOutOfBoundsException
     *          if <tt>start</tt> or <tt>end</tt> are negative,
     *          if <tt>end</tt> is greater than <tt>length()</tt>,
     *          or if <tt>start</tt> is greater than <tt>end</tt>
     */
    public String subString(int subStart, int subEnd)
    {
        int length = subEnd - subStart;
        if (subEnd < 0 || subStart < 0 || subStart > subEnd || length > getLength() )
        {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Given length %d is greater than total length: %d", length, getLength()));
        }

        StringBuilder builder = new StringBuilder(length);

        for (int i = subStart; i < subEnd; i++)
        {
            builder.append((char) (buffer[(getNext(start + i))]));
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return toString(getLength());
    }
}
