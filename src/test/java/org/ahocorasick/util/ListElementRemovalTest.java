package org.ahocorasick.util;

import java.util.ArrayList;
import java.util.List;

import org.ahocorasick.util.ListElementRemoval.RemoveElementPredicate;
import org.junit.Test;

import junit.framework.Assert;

import static java.util.Arrays.asList;
public class ListElementRemovalTest {

    @Test
    public void removeNone() {
        List<String> list = new ArrayList<>(asList("a", "b", "c"));
        RemoveElementPredicate<String> matchNothing = new RemoveElementPredicate<String>() {

            @Override
            public boolean remove(String t) {
                return false;
            }
            
        };
        
        ListElementRemoval.removeIf(list, matchNothing);
        
        Assert.assertEquals(3, list.size());
    }
    
    @Test
    public void removeAll() {
        List<String> list = new ArrayList<>(asList("a", "b", "c"));
        RemoveElementPredicate<String> matchNothing = new RemoveElementPredicate<String>() {

            @Override
            public boolean remove(String t) {
                return true;
            }
            
        };
        
        ListElementRemoval.removeIf(list, matchNothing);
        
        Assert.assertEquals(0, list.size());
    }
    
    @Test
    public void removeSome() {
        List<String> list = new ArrayList<>(asList("a", "b", "c"));
        RemoveElementPredicate<String> matchNothing = new RemoveElementPredicate<String>() {

            @Override
            public boolean remove(String t) {
                return "a".equals(t) || "c".equals(t);
            }
            
        };
        
        ListElementRemoval.removeIf(list, matchNothing);
        
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("b", list.get(0));
    }
}
