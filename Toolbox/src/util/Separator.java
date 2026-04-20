package util;

import java.util.Objects;

/**
 * <p>
 * Provides a stateful utility for joining textual elements into a single delimited string. It
 * eliminates the "trailing delimiter" problem by managing its own internal state.
 * </p>
 *
 * <p>
 * This class is designed to be used directly within {@code StringBuilder.append()} calls. The first
 * call to {@code toString()} returns an empty string, while subsequent calls return the specified
 * delimiter. This allows for a clean loop implementation without conditional logic for the first or
 * last element.
 * </p>
 * 
 * <pre>
 * Separator sep = new Separator(", ");
 * StringBuilder sb = new StringBuilder();
 * for (String item : list)
 * {
 *     sb.append(sep).append(item);
 * }
 * </pre>
 * 
 * <p>
 * <b>Change Log:</b>
 * </p>
 * 
 * <ul>
 * <li>Created by Trevor Maggs on 1 June 2017</li>
 * <li>Refined for Java 8 standards on 29 March 2026</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.2
 * @since 29 March 2026
 */
public final class Separator
{
    private String delimiter;
    private boolean skipFirst;

    /**
     * Constructs a new Separator with the default comma-space (", ") delimiter.
     */
    public Separator()
    {
        this(", ");
    }

    /**
     * Constructs a new Separator with a custom delimiter.
     * 
     * @param delimiter
     *        the string to use as a separator
     */
    public Separator(final String delimiter)
    {
        this.delimiter = Objects.requireNonNull(delimiter, "Delimiter cannot be null");
        this.skipFirst = true;
    }

    /**
     * Resets the state so the next call to {@link #toString()} returns an empty string.
     */
    public void reset()
    {
        this.skipFirst = true;
    }

    /**
     * Resets the state and updates the delimiter.
     * 
     * @param delimiter
     *        the new delimiter to use
     */
    public void reset(final String delimiter)
    {
        this.delimiter = Objects.requireNonNull(delimiter, "Delimiter cannot be null");
        reset();
    }

    /**
     * Joins the elements of the specified {@link Iterable} into a single string.
     * 
     * @param items
     *        the elements to join
     * @return a delimited string representation of the elements
     */
    public String join(Iterable<?> items)
    {
        if (items == null)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        reset();

        for (Object item : items)
        {
            if (item != null)
            {
                sb.append(this).append(item);
            }
        }

        return sb.toString();
    }

    /**
     * Returns the separator string. The first call after instantiation or reset returns an empty
     * string; all subsequent calls return the delimiter.
     * 
     * @return the separator string
     */
    @Override
    public String toString()
    {
        if (skipFirst)
        {
            skipFirst = false;
            return "";
        }

        return delimiter;
    }
}