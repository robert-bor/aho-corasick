package org.ahocorasick.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps removes elements from a list in a efficient way
 * 
 * <p>Removing elements from an ArrayList in a naive way can lead to O(n^3)
 * running time. If the algorithm first creates a list of all the elements
 * to remove, then we for each element in this list (assume n elements) we look 
 * for the element in the original list (against n elements) and when found we need 
 * to remove the element and move the elements to the right (of the removed element) 
 * to the left by one, the size of this operation is at worst n hence O(n^3).</p>
 * 
 * <p>This instead makes a new list and copies over only elements we want to keep, 
 * we then clear the original list and then add all of the elements to the original 
 * list. This gives us (for ArrayList) a running time of O(n).</p>
 * 
 * <p>The performance of this has not been thoroughly tested for linked list.</p>
 * 
 * <p>This can be completely removed in java 8 as the List#removeIf() method can be used instead
 * as this already is optimised for each list implementation.
 *
 */
public class ListElementRemoval {

    public static interface RemoveElementPredicate<T> {
        public boolean remove(T t);
    }
    
    /**
     * Removes all elements from the list matching the given predicate.
     * 
     * @param list the list from which to remove
     * @param predicate to test for removal
     * @param <T> type of list
     */
    public static <T> void removeIf(final List<T> list, final RemoveElementPredicate<T> predicate) {
        final List<T> newList = new ArrayList<>(list.size());
        
        for(final T element : list) { 
            if (!predicate.remove(element)) {
                newList.add(element);
            }
        }
        
        list.clear();
        list.addAll(newList);
    }
}
