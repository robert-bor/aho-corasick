package org.ahocorasick.trie;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * Contains functionality common to tests.
 */
public class TestHelper {
  /**
   * Injects keywords into a string builder.
   *
   * @param source   Should contain a bunch of random data that cannot match
   *                 any keyword.
   * @param keyword  A keyword to inject repeatedly in the text.
   * @param interval How often to inject the keyword.
   */
  @SuppressWarnings( "SameParameterValue" )
  static void injectKeyword(
    final StringBuilder source,
    final String keyword,
    final int interval ) {
    final int length = source.length();
    for( int i = 0; i < length; i += interval ) {
      source.replace( i, i + keyword.length(), keyword );
    }
  }

  /**
   * Generates a random sequence of ASCII numbers.
   *
   * @param count The number of numbers to generate.
   * @return A character sequence filled with random digits.
   */
  @SuppressWarnings( "SameParameterValue" )
  public static StringBuilder randomNumbers( int count ) {
    final StringBuilder sb = new StringBuilder( count );

    while( --count > 0 ) {
      sb.append( current().nextInt( 0, 10 ) );
    }

    return sb;
  }
}
