package org.ahocorasick.util;

import org.ahocorasick.util.ListElementRemoval.RemoveElementPredicate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Responsible for testing that elements can be removed efficiently.
 */
public class ListElementRemovalTest {

  @Test
  public void test_RemoveNone() {
    final List<String> list = createList();
    RemoveElementPredicate<String> matchNothing = t -> false;

    ListElementRemoval.removeIf( list, matchNothing );

    assertEquals( 3, list.size() );
  }

  @Test
  public void test_RemoveAll() {
    final List<String> list = createList();
    RemoveElementPredicate<String> matchNothing = t -> true;

    ListElementRemoval.removeIf( list, matchNothing );

    assertEquals( 0, list.size() );
  }

  @Test
  public void test_RemoveSome() {
    final List<String> list = createList();
    RemoveElementPredicate<String> matchNothing =
        t -> "a".equals( t ) || "c".equals( t );

    ListElementRemoval.removeIf( list, matchNothing );

    assertEquals( 1, list.size() );
    assertEquals( "b", list.get( 0 ) );
  }

  private List<String> createList() {
    return new ArrayList<>( asList( "a", "b", "c" ) );
  }
}
