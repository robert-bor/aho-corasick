package org.ahocorasick.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps removes elements from a list in a efficient way
 * 
 * <p>Removing elements from an ArrayList in a naive way can lead to O(n^3)
 * running time. If the algorithm first creates a list of all the elements
 * to remove, then we for each element in this list (assume n elements) we look 
 * for in element in the list (against n elements) and when found we need to remove
 * the element and move the elements to the right one to the left the size of this
 * operation is at worst n hence O(n^3).</p>
 * 
 * <p>This basically avoids that by making a new list and copying over only elements
 * we want to keep, we then clear the given list and all of the elements this gives us
 * (for ArrayList) O(n) running time.</p>
 * 
 * <p>The performance of this has not been thoroughly tested for linked list but 
 * it probably is not too bad.</p>
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
     * @param list
     * @param predicate
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
