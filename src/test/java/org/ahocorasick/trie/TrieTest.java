package org.ahocorasick.trie;

import org.ahocorasick.trie.handler.AbstractStatefulEmitHandler;
import org.ahocorasick.trie.handler.EmitHandler;
import org.ahocorasick.trie.handler.StatefulEmitHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.ahocorasick.trie.Trie.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link Trie} class functionality.
 */
public class TrieTest {
  private final static String[] ALPHABET = new String[]{
      "abc", "bcd", "cde"
  };

  private final static String[] PRONOUNS = new String[]{
      "hers", "his", "she", "he"
  };

  private final static String[] FOOD = new String[]{
      "veal", "cauliflower", "broccoli", "tomatoes"
  };

  private final static String[] GREEK_LETTERS = new String[]{
      "Alpha", "Beta", "Gamma"
  };

  private final static String[] UNICODE = new String[]{
      "turning", "once", "again", "börkü"
  };

  @Test
  public void test_KeywordAndTextAreTheSame() {
    final Trie trie = Trie.builder()
                          .addKeyword( ALPHABET[ 0 ] )
                          .build();
    final Collection<Emit> emits = trie.parseText( ALPHABET[ 0 ] );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 0, 2, ALPHABET[ 0 ] );
  }

  @Test
  public void test_KeywordAndTextAreTheSameFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeyword( ALPHABET[ 0 ] )
                          .build();
    final Emit firstMatch = trie.firstMatch( ALPHABET[ 0 ] );
    checkEmit( firstMatch, 0, 2, ALPHABET[ 0 ] );
  }

  @Test
  public void test_TextIsLongerThanKeyword() {
    final Trie trie = Trie.builder()
                          .addKeyword( ALPHABET[ 0 ] )
                          .build();
    final Collection<Emit> emits = trie.parseText( " " + ALPHABET[ 0 ] );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 1, 3, ALPHABET[ 0 ] );
  }

  @Test
  public void test_TextIsLongerThanKeywordFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeyword( ALPHABET[ 0 ] )
                          .build();
    final Emit firstMatch = trie.firstMatch( " " + ALPHABET[ 0 ] );
    checkEmit( firstMatch, 1, 3, ALPHABET[ 0 ] );
  }

  @Test
  public void test_VariousKeywordsOneMatch() {
    final Trie trie = Trie.builder()
                          .addKeywords( ALPHABET )
                          .build();
    final Collection<Emit> emits = trie.parseText( "bcd" );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 0, 2, "bcd" );
  }

  @Test
  public void test_VariousKeywordsFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeywords( ALPHABET )
                          .build();
    final Emit firstMatch = trie.firstMatch( "bcd" );
    checkEmit( firstMatch, 0, 2, "bcd" );
  }

  @Test
  public void test_UshersTestAndStopOnHit() {
    final Trie trie = Trie.builder()
                          .addKeywords( PRONOUNS )
                          .stopOnHit()
                          .build();
    final Collection<Emit> emits = trie.parseText( "ushers" );
    assertEquals( 1, emits.size() ); // she @ 3, he @ 3, hers @ 5
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 2, 3, "he" );
  }

  @Test
  public void test_UshersTestStopOnHitSkipOne() {
    final Trie trie = Trie.builder()
                          .addKeywords( PRONOUNS )
                          .stopOnHit()
                          .build();

    final StatefulEmitHandler testEmitHandler =
        new AbstractStatefulEmitHandler() {
          boolean first = true;

          @Override
          public boolean emit( final Emit emit ) {
            if( first ) {
              // return false for the first element
              first = false;
              return false;
            }
            addEmit( emit );
            return true;
          }
        };

    trie.parseText( "ushers", testEmitHandler );
    final Collection<Emit> emits = testEmitHandler.getEmits();
    assertEquals( 1, emits.size() ); // she @ 3, he @ 3, hers @ 5
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 1, 3, "she" );
  }

  @Test
  public void test_UshersTest() {
    final Trie trie = Trie.builder()
                          .addKeywords( PRONOUNS )
                          .build();
    final Collection<Emit> emits = trie.parseText( "ushers" );
    assertEquals( 3, emits.size() ); // she @ 3, he @ 3, hers @ 5
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 2, 3, "he" );
    checkEmit( iterator.next(), 1, 3, "she" );
    checkEmit( iterator.next(), 2, 5, "hers" );
  }

  @Test
  public void test_UshersTestWithCapitalKeywords() {
    final Trie trie = Trie.builder()
                          .ignoreCase()
                          .addKeyword( "HERS" )
                          .addKeyword( "HIS" )
                          .addKeyword( "SHE" )
                          .addKeyword( "HE" )
                          .build();
    final Collection<Emit> emits = trie.parseText( "ushers" );
    assertEquals( 3, emits.size() ); // she @ 3, he @ 3, hers @ 5
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 2, 3, "HE" );
    checkEmit( iterator.next(), 1, 3, "SHE" );
    checkEmit( iterator.next(), 2, 5, "HERS" );
  }

  @Test
  public void test_UshersTestFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeywords( PRONOUNS )
                          .build();
    final Emit firstMatch = trie.firstMatch( "ushers" );
    checkEmit( firstMatch, 2, 3, "he" );
  }

  @Test
  public void test_UshersTestByCallback() {
    final Trie trie = Trie.builder()
                          .addKeywords( PRONOUNS )
                          .build();

    final List<Emit> emits = new ArrayList<>();
    final EmitHandler emitHandler = emit -> {
      emits.add( emit );
      return true;
    };
    trie.parseText( "ushers", emitHandler );
    assertEquals( 3, emits.size() ); // she @ 3, he @ 3, hers @ 5
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 2, 3, "he" );
    checkEmit( iterator.next(), 1, 3, "she" );
    checkEmit( iterator.next(), 2, 5, "hers" );
  }

  @Test
  public void test_MisleadingTest() {
    final Trie trie = Trie.builder()
                          .addKeyword( "hers" )
                          .build();
    final Collection<Emit> emits = trie.parseText( "h he her hers" );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 9, 12, "hers" );
  }

  @Test
  public void test_MisleadingTestFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeyword( "hers" )
                          .build();
    final Emit firstMatch = trie.firstMatch( "h he her hers" );
    checkEmit( firstMatch, 9, 12, "hers" );
  }

  @Test
  public void test_Recipes() {
    final Trie trie = Trie.builder()
                          .addKeywords( FOOD )
                          .build();
    final Collection<Emit> emits = trie.parseText(
        "2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli" );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 2, 12, "cauliflower" );
    checkEmit( iterator.next(), 18, 25, "tomatoes" );
    checkEmit( iterator.next(), 40, 43, "veal" );
    checkEmit( iterator.next(), 51, 58, "broccoli" );
  }

  @Test
  public void test_RecipesFirstMatch() {
    final Trie trie = Trie.builder()
                          .addKeywords( FOOD )
                          .build();
    final Emit firstMatch = trie.firstMatch(
        "2 cauliflowers, 3 tomatoes, 4 slices of veal, 100g broccoli" );

    checkEmit( firstMatch, 2, 12, "cauliflower" );
  }

  @Test
  public void test_LongAndShortOverlappingMatch() {
    final Trie trie = Trie.builder()
                          .addKeyword( "he" )
                          .addKeyword( "hehehehe" )
                          .build();
    final Collection<Emit> emits = trie.parseText( "hehehehehe" );
    final Iterator<Emit> iterator = emits.iterator();
    checkEmit( iterator.next(), 0, 1, "he" );
    checkEmit( iterator.next(), 2, 3, "he" );
    checkEmit( iterator.next(), 4, 5, "he" );
    checkEmit( iterator.next(), 6, 7, "he" );
    checkEmit( iterator.next(), 0, 7, "hehehehe" );
    checkEmit( iterator.next(), 8, 9, "he" );
    checkEmit( iterator.next(), 2, 9, "hehehehe" );
  }

  @Test
  public void test_NonOverlapping() {
    final Trie trie = Trie.builder().ignoreOverlaps()
                          .addKeyword( "ab" )
                          .addKeyword( "cba" )
                          .addKeyword( "ababc" )
                          .build();
    final Collection<Emit> emits = trie.parseText( "ababcbab" );
    assertEquals( 2, emits.size() );
    final Iterator<Emit> iterator = emits.iterator();
    // With overlaps: ab@1, ab@3, ababc@4, cba@6, ab@7
    checkEmit( iterator.next(), 0, 4, "ababc" );
    checkEmit( iterator.next(), 6, 7, "ab" );
  }

  @Test
  public void test_NonOverlappingFirstMatch() {
    final Trie trie = Trie.builder().ignoreOverlaps()
                          .addKeyword( "ab" )
                          .addKeyword( "cba" )
                          .addKeyword( "ababc" )
                          .build();
    final Emit firstMatch = trie.firstMatch( "ababcbab" );

    checkEmit( firstMatch, 0, 4, "ababc" );
  }

  @Test
  public void test_ContainsMatch() {
    final Trie trie = Trie.builder().ignoreOverlaps()
                          .addKeyword( "ab" )
                          .addKeyword( "cba" )
                          .addKeyword( "ababc" )
                          .build();
    assertTrue( trie.containsMatch( "ababcbab" ) );
  }

  @Test
  public void test_StartOfChurchillSpeech() {
    final Trie trie = Trie.builder().ignoreOverlaps()
                          .addKeyword( "T" )
                          .addKeyword( "u" )
                          .addKeyword( "ur" )
                          .addKeyword( "r" )
                          .addKeyword( "urn" )
                          .addKeyword( "ni" )
                          .addKeyword( "i" )
                          .addKeyword( "in" )
                          .addKeyword( "n" )
                          .addKeyword( "urning" )
                          .build();
    final Collection<Emit> emits = trie.parseText( "Turning" );
    assertEquals( 2, emits.size() );
  }

  @Test
  public void test_PartialMatch() {
    final Trie trie = Trie.builder()
                          .onlyWholeWords()
                          .addKeyword( "sugar" )
                          .build();
    final Collection<Emit> emits = trie.parseText(
        "sugarcane sugarcane sugar canesugar" ); // left, middle, right test
    assertEquals( 1, emits.size() ); // Match must not be made
    checkEmit( emits.iterator().next(), 20, 24, "sugar" );
  }

  @Test
  public void test_PartialMatchFirstMatch() {
    final Trie trie = Trie.builder()
                          .onlyWholeWords()
                          .addKeyword( "sugar" )
                          .build();

    // left, middle, right test
    final Emit firstMatch =
        trie.firstMatch( "sugarcane sugarcane sugar canesugar" );

    checkEmit( firstMatch, 20, 24, "sugar" );
  }

  @Test
  public void test_TokenizeFullSentence() {
    final Trie trie = Trie.builder()
                          .addKeywords( GREEK_LETTERS )
                          .build();
    final Collection<Token> tokens = trie.tokenize(
        "Hear: Alpha team first, Beta from the rear, Gamma in reserve" );
    assertEquals( 7, tokens.size() );
    final Iterator<Token> tokensIt = tokens.iterator();
    assertEquals( "Hear: ", tokensIt.next().getFragment() );
    assertEquals( "Alpha", tokensIt.next().getFragment() );
    assertEquals( " team first, ", tokensIt.next().getFragment() );
    assertEquals( "Beta", tokensIt.next().getFragment() );
    assertEquals( " from the rear, ", tokensIt.next().getFragment() );
    assertEquals( "Gamma", tokensIt.next().getFragment() );
    assertEquals( " in reserve", tokensIt.next().getFragment() );
  }

  /**
   * Test boundary check with case-insensitive matches with whole words.
   */
  @Test
  public void test_StringIndexOutOfBoundsException() {
    final Trie trie = Trie.builder().ignoreCase().onlyWholeWords()
                          .addKeywords( UNICODE )
                          .build();
    final Collection<Emit> emits = trie.parseText( "TurninG OnCe AgAiN BÖRKÜ" );
    assertEquals( 4, emits.size() ); // Match must not be made
    final Iterator<Emit> it = emits.iterator();
    checkEmit( it.next(), 0, 6, "turning" );
    checkEmit( it.next(), 8, 11, "once" );
    checkEmit( it.next(), 13, 17, "again" );
    checkEmit( it.next(), 19, 23, "börkü" );
  }

  @Test
  public void test_IgnoreCase() {
    final Trie trie = Trie.builder().ignoreCase()
                          .addKeywords( UNICODE )
                          .build();
    final Collection<Emit> emits = trie.parseText( "TurninG OnCe AgAiN BÖRKÜ" );
    assertEquals( 4, emits.size() ); // Match must not be made
    final Iterator<Emit> it = emits.iterator();
    checkEmit( it.next(), 0, 6, "turning" );
    checkEmit( it.next(), 8, 11, "once" );
    checkEmit( it.next(), 13, 17, "again" );
    checkEmit( it.next(), 19, 23, "börkü" );
  }

  @Test
  public void test_IgnoreCaseFirstMatch() {
    final Trie trie = Trie.builder().ignoreCase()
                          .addKeywords( UNICODE )
                          .build();
    final Emit firstMatch = trie.firstMatch( "TurninG OnCe AgAiN BÖRKÜ" );

    checkEmit( firstMatch, 0, 6, "turning" );
  }

  @Test
  public void test_TokenizeTokensInSequence() {
    final Trie trie = Trie.builder()
                          .addKeywords( GREEK_LETTERS )
                          .build();
    final Collection<Token> tokens = trie.tokenize( "Alpha Beta Gamma" );
    assertEquals( 5, tokens.size() );
  }

  /**
   * Fix adding a word of size 0 ("") as a dictionary. A bug in the dictionary
   * parsing code (at end of line) caused it to generate words of 0 length,
   * which were being added to the trie. Removing the additional commas
   * resolved the issue.
   */
  @Test
  public void test_ZeroLength() {
    final Trie trie = Trie.builder()
                          .ignoreOverlaps()
                          .onlyWholeWords()
                          .ignoreCase()
                          .addKeyword( "" )
                          .build();
    trie.tokenize(
        "Try a natural lip and subtle bronzer to keep all the focus on those " +
            "big bright eyes with NARS Eyeshadow Duo in Rated R And the " +
            "winner is... Boots No7 Advanced Renewal Anti-ageing Glycolic " +
            "Peel Kit ($25 amazon.com) won most-appealing peel." );
  }

  @Test
  public void test_Emit_PunctuatedKeyword_AllOffsetsFound() {
    final String keyword = "{{var}}";
    final int len = keyword.length() - 1;
    final Trie trie = builder()
        .ignoreOverlaps()
        .addKeyword( keyword )
        .build();

    final Collection<Emit> emits = trie.parseText(
        format( "__%s__ **%s** {{%s}} %s%s",
                keyword, keyword, keyword, keyword, keyword )
    );

    assertEquals( 5, emits.size() );
    final Iterator<Emit> it = emits.iterator();

    checkEmit( it.next(), 2, 2 + len, keyword );
    checkEmit( it.next(), 14, 14 + len, keyword );
    checkEmit( it.next(), 26, 26 + len, keyword );
    checkEmit( it.next(), 36, 36 + len, keyword );
    checkEmit( it.next(), 43, 43 + len, keyword );
  }

  /**
   * Notice the capital I with a dot. The code used to compute the offsets
   * at (6, 9), which caused {@link Trie#tokenize(String)} to crash because
   * 9 is past the end of the string. That character is two bytes wide, so it
   * pushes the offset calculation off.
   */
  @Test
  public void test_Unicode1() {
    // The second character ('İ') is
    // Unicode, which was read by AC as a 2-byte char
    final String target = "LİKE THIS";
    // Java does it the right way
    assertEquals( "THIS",
                  target.substring( 5, 9 ) );
    final Trie trie = Trie.builder().ignoreCase().onlyWholeWords()
                          .addKeyword( "this" )
                          .build();
    final Collection<Emit> emits = trie.parseText( target );
    assertEquals( 1, emits.size() );
    final Iterator<Emit> it = emits.iterator();
    checkEmit( it.next(), 5, 8, "this" );
  }

  /**
   * Notice the capital I with a dot. The code used to compute the offsets
   * at (6, 9), which caused {@link Trie#tokenize(String)} to crash because
   * 9 is past the end of the string. That character is two bytes wide, so it
   * pushes the offset calculation off.
   */
  @Test
  public void test_Unicode2() {
    // The second character ('İ') is
    // Unicode, which was read by AC as a 2-byte char
    final String target = "LİKE THIS";
    final Trie trie = Trie.builder()
                          .ignoreCase()
                          .onlyWholeWords()
                          .addKeyword( "this" )
                          .build();
    // Java does it the right way
    assertEquals( "THIS",
                  target.substring( 5, 9 ) );
    final Emit firstMatch = trie.firstMatch( target );
    checkEmit( firstMatch, 5, 8, "this" );
  }

  @Test
  public void test_PartialMatchWhiteSpaces() {
    final Trie trie = Trie.builder()
                          .onlyWholeWordsWhiteSpaceSeparated()
                          .addKeyword( "#sugar-123" )
                          .build();
    final Collection<Emit> emits =
        trie.parseText( "#sugar-123 #sugar-1234" ); // left, middle, right test
    assertEquals( 1, emits.size() ); // Match must not be made
    checkEmit( emits.iterator().next(), 0, 9, "#sugar-123" );
  }

  @Test
  public void test_LargeString() {
    final int interval = 100;
    final int textSize = 1000000;
    final String keyword = FOOD[ 1 ];
    final StringBuilder text = randomNumbers( textSize );

    injectKeyword( text, keyword, interval );

    final Trie trie = Trie.builder()
                          .onlyWholeWords()
                          .addKeyword( keyword )
                          .build();

    final Collection<Emit> emits = trie.parseText( text );

    assertEquals( textSize / interval, emits.size() );
  }

  @Test
  public void test_UnicodeIssueBug39ReportedByHumanzz() {
    // Problem: "İ".length => 1, "İ".toLowerCase().length => 2. This causes
    // all sorts of unexpected behaviors
    // and bugs where the Emit will have a size different from the original
    // string.
    // Soln: As in issue #8, convert at character level Character.toLowerCase
    // ('İ') => 'i'  + make sure
    // that emit gets the properly cased keyword.
    final String upperLengthOne = "İnt";
    final Trie trie = Trie.builder()
                          .ignoreCase()
                          .onlyWholeWords()
                          .addKeyword( upperLengthOne )
                          .build();
    final Collection<Emit> emits = trie.parseText( "İnt is good" );
    assertEquals( 1, emits.size() );
    checkEmit( emits.iterator().next(), 0, 2, upperLengthOne );
  }

  @Test(timeout = 30_000)
  public void test_ParallelSearch() throws InterruptedException {
    final int interval = 100;
    final int textSize = 1000000;
    final String keyword = FOOD[ 1 ];
    final StringBuilder matchingText = randomNumbers( textSize );
    injectKeyword( matchingText, keyword, interval );
    final StringBuilder nonMatchingText = randomNumbers( textSize );
    injectKeyword( nonMatchingText,
                   keyword.substring( 0, keyword.length() - 1 ),
                   interval );

    final Trie trie = Trie.builder()
                          .onlyWholeWords()
                          .addKeyword( keyword )
                          .build();

    final AtomicInteger matchCount = new AtomicInteger( 0 );
    final Runnable matchingTask = () -> matchCount.set(
        trie.parseText( matchingText ).size() );

    final AtomicInteger nonMatchCount = new AtomicInteger( 0 );
    final Runnable nonMatchingTask = () -> nonMatchCount.set( trie.parseText(
        nonMatchingText ).size() );
    final Thread matchingThread = new Thread( matchingTask );
    final Thread nonMatchingThread = new Thread( nonMatchingTask );
    matchingThread.start();
    nonMatchingThread.start();
    matchingThread.join();
    nonMatchingThread.join();

    assertEquals( textSize / interval, matchCount.get() );
    assertEquals( 0, nonMatchCount.get() );
  }

  /**
   * Generates a random sequence of ASCII numbers.
   *
   * @param count The number of numbers to generate.
   * @return A character sequence filled with random digits.
   */
  private StringBuilder randomNumbers( int count ) {
    final StringBuilder sb = new StringBuilder( count );

    while( --count > 0 ) {
      sb.append( randomInt( 0, 10 ) );
    }

    return sb;
  }

  /**
   * Injects keywords into a string builder.
   *
   * @param source   Should contain a bunch of random data that cannot match
   *                 any keyword.
   * @param keyword  A keyword to inject repeatedly in the text.
   * @param interval How often to inject the keyword.
   */
  private void injectKeyword(
      final StringBuilder source,
      final String keyword,
      final int interval ) {
    final int length = source.length();
    for( int i = 0; i < length; i += interval ) {
      source.replace( i, i + keyword.length(), keyword );
    }
  }

  private int randomInt( final int min, final int max ) {
    return ThreadLocalRandom.current().nextInt( min, max );
  }

  private void checkEmit( Emit next, int expectedStart, int expectedEnd,
                          String expectedKeyword ) {
    assertEquals( "Start of emit should have been " + expectedStart,
                  expectedStart,
                  next.getStart() );
    assertEquals( "End of emit should have been " + expectedEnd,
                  expectedEnd,
                  next.getEnd() );
    assertEquals( expectedKeyword, next.getKeyword() );
  }
}
