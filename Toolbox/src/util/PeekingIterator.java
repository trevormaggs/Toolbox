package util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>
 * Provides an enhanced Iterator interface that allows users to peek at the next element in the
 * iteration without advancing the iterator.
 * </p>
 *
 * <p>
 * This implementation was inspired by another source. For more information, visit
 * <a href="https://medium.com/@harycane/peeking-iterator-ef69ce9ef788">this site</a>.
 * </p>
 *
 * <p>
 * <b>Change logs:</b>
 * </p>
 *
 * <ul>
 * <li>Created by Trevor Maggs on October 12, 2021</li>
 * <li>Updated by Trevor Maggs on February 27, 2026</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.2
 * @since 17 Fenruary 2026
 */
public class PeekingIterator<T> implements Iterator<T>
{
    private T cursor;
    private Iterator<T> iter;
    private boolean noSuchElement;

    /**
     * Constructs a new peeking iterator, caching the first element in advance.
     *
     * @param itr
     *        the Iterator object
     */
    public PeekingIterator(Iterator<T> itr)
    {
        iter = itr;
        advanceIterator();
    }

    /**
     * Constructs a new peeking iterator from the specified Collection object, caching the first
     * element in advance.
     *
     * @param obj
     *        the specified Collection object
     */
    public PeekingIterator(Collection<T> obj)
    {
        this(obj.iterator());
    }

    /**
     * Constructs a new peeking iterator from the specified Map's collection of values, caching the
     * first element in advance.
     *
     * @param <K>
     *        Key component of the specified map instance
     * @param map
     *        the Map object
     */
    public <K> PeekingIterator(Map<K, T> map)
    {
        this(map.values().iterator());
    }

    /**
     * Constructs a new peeking iterator from the specified generic array, caching the first element
     * in advance.
     *
     * @param arr
     *        the generic array
     */
    public PeekingIterator(T[] arr)
    {
        this(Arrays.asList(arr));
    }

    /**
     * Advances the iterator to cache the next element, allowing either the {@link #next} or
     * {@link #peek} method to be called without throwing an exception.
     */
    private void advanceIterator()
    {
        if (iter.hasNext())
        {
            // Cache the next element
            cursor = iter.next();
        }

        else
        {
            noSuchElement = true;
        }
    }
    /**
     * Retrieves the next element in the iteration without advancing the pointer.
     *
     * <p>
     * This allows for lookahead logic, which is essential for determining if a command-line token
     * is a flag or a value before consuming it.
     * </p>
     *
     * @return the cached next element
     *
     * @throws NoSuchElementException
     *         if the iteration has no more elements
     */
    public T peek()
    {
        if (noSuchElement)
        {
            throw new NoSuchElementException("Cannot peek further");
        }

        return cursor;
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
        if (noSuchElement)
        {
            throw new NoSuchElementException();
        }

        T result = cursor;
        advanceIterator();

        return result;
    }

    /**
     * Returns whether the iteration has more elements to retrieve.
     *
     * @return boolean true if the iteration has more elements
     */
    @Override
    public boolean hasNext()
    {
        return !noSuchElement;
    }

    /**
     * This operation is not supported by this implementation.
     *
     * @throws UnsupportedOperationException
     *         always, as removing elements from the command-line stream is prohibited
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Removal is not supported by PeekingIterator");
    }
}