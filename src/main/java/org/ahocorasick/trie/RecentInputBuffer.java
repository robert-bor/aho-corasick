package org.ahocorasick.trie;

/**
 * The recent input buffer allows user's to access recent values by file position.
 */
public class RecentInputBuffer extends CircularBuffer
{
    ////////////////////////////////////////////////////////////////////////////
    // Fields

    /**
     * Gets the file position of the last character that was inserted.
     */
    private long insertedIndex = 0;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors

    /**
     * Creates a circular buffer with the given length.
     * @param length the length of the buffer.
     */
    public RecentInputBuffer(int length)
    {
        super(length);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods


    @Override
    public int add(int value) {
        insertedIndex++;
        return super.add(value);
    }

    /**
     * Gets a value based on the overall file position as long as the requested position is greater than
     * (currentPosition - bufferLength).
     * @param filePosition the file position.
     * @return the value or -1 if the file position is invalid.
     */
    public char getByFilePosition(long filePosition)
    {
        double lowestFileIndex = insertedIndex - getLength();
        if (filePosition > insertedIndex)
        {
            throw new IllegalArgumentException("File position is greater than highest file index in the buffer");
        }
        if (filePosition < lowestFileIndex)
        {
            throw new IllegalArgumentException("File position is less than the lowest file index in the buffer");
        }
        return charAt((int) (filePosition - lowestFileIndex));
    }

    /**
     * Creates a sub-string of this buffer based on the overall file position.
     * @param startFilePosition the start value (inclusive).
     * @param endFilePosition the end value (exclusive).
     * @return the sub-string.
     */
    public String substringByFilePosition(long startFilePosition, long endFilePosition)
    {
        if (startFilePosition > endFilePosition)
        {
            throw new IllegalArgumentException("Start file position is greater than end file position");
        }

        double lowestFileIndex = insertedIndex - getLength();
        if (endFilePosition > insertedIndex)
        {
            throw new IllegalArgumentException("File position is greater than highest file index in the buffer");
        }
        if (startFilePosition < lowestFileIndex)
        {
            throw new IllegalArgumentException("File position is less than the lowest file index in the buffer");
        }
        return subString((int) (startFilePosition - lowestFileIndex), (int) (endFilePosition - lowestFileIndex));
    }
}