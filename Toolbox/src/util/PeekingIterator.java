package util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Provides an decorated Iterator that supports look-ahead operations via a {@link #peek()} method
 * without advancing the underlying iteration state.
 * 
 * <p>
 * This utility is particularly effective for the left to right advancement parsing or command-line
 * argument processing where the current token determines the handling of the subsequent element.
 * </p>
 *
 * <p>
 * <b>Change History:</b>
 * </p>
 *
 * <ul>
 * <li>Created by Trevor Maggs on 12 October 2021</li>
 * <li>Revised for improved cursor logic and collection support on 29 March 2026</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.3
 * @since 29 March 2026
 * @param <T>
 *        the type of elements returned by this iterator
 */
public final class PeekingIterator<T> implements Iterator<T>
{
    private final Iterator<? extends T> iter;
    private T cursor;
    private boolean exhausted = false;

    /**
     * Constructs a new peeking iterator by wrapping an existing iterator.
     *
     * @param iter
     *        the underlying iterator to wrap
     * @throws NullPointerException
     *         if the provided iterator is null
     */
    public PeekingIterator(Iterator<? extends T> iter)
    {
        if (iter == null)
        {
            throw new NullPointerException("Underlying iterator cannot be null");
        }

        this.iter = iter;

        advance();
    }

    /**
     * Constructs a new peeking iterator from a {@link Collection}.
     *
     * @param collection
     *        the collection to iterate over
     */
    public PeekingIterator(Collection<T> collection)
    {
        this(collection.iterator());
    }

    /**
     * Constructs a new peeking iterator from the values of a {@link Map}.
     *
     * @param <K>
     *        Key component of the specified map instance
     * @param map
     *        the map whose values will be iterated
     */
    public <K> PeekingIterator(Map<K, T> map)
    {
        this(map.values().iterator());
    }

    /**
     * Constructs a new peeking iterator from a generic array.
     *
     * @param arr
     *        the array to iterate over
     */
    public PeekingIterator(T[] arr)
    {
        this(Arrays.asList(arr).iterator());
    }

    /**
     * Caches the next element from the underlying iterator.
     * 
     * Advances the iterator to cache the next element, allowing either the {@link #next} or
     * {@link #peek} method to be called without throwing an exception.
     */
    private void advance()
    {
        if (iter.hasNext())
        {
            cursor = iter.next();
        }

        else
        {
            cursor = null;
            exhausted = true;
        }
    }

    /**
     * Returns the next element in the iteration without advancing the iterator.
     *
     * @return the cached next element
     * 
     * @throws NoSuchElementException
     *         if the iteration has no more elements
     */
    public T peek()
    {
        if (exhausted)
        {
            throw new NoSuchElementException("No more elements to peek");
        }

        return cursor;
    }

    /**
     * Returns whether the iteration has more elements to retrieve.
     *
     * @return boolean true if the iteration has more elements
     */
    @Override
    public boolean hasNext()
    {
        return !exhausted;
    }

    /**
     * Returns the current element and advances the iterator to the next position.
     *
     * @return the element that was at the top of the iteration
     *
     * @throws NoSuchElementException
     *         if the iteration contains no more elements
     */
    @Override
    public T next()
    {
        if (exhausted)
        {
            throw new NoSuchElementException();
        }

        T result = cursor;
        advance();

        return result;
    }

    /**
     * Removal is not supported in this implementation to ensure cursor consistency.
     *
     * @throws UnsupportedOperationException
     *         always, as removing elements from the command-line stream is prohibited
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Removal not supported by PeekingIterator");
    }
}