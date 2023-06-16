/*
 * Copyright 2007-2017 David Koelle
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.jikoo.planarwrappers.util;

import java.util.Comparator;

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings containing numbers.
 * Instead of sorting numbers in ASCII order like a standard sort, this algorithm sorts
 * numbers in numeric order.
 *
 * <p>This is a version with enhancements made by Daniel Migowski, Andre Bogus, and David Koelle.
 *
 * <p>David Koelle's site is down; the most recent Wayback Machine capture
 * of his Alphanum page can be found
 * <a href=https://web.archive.org/web/20210803201519/http://www.davekoelle.com/alphanum.html>here</a>.
 */
public class AlphanumComparator implements Comparator<String> {

  private final Comparator<String> stringComparator;

  /**
   * Construct a new AlphanumComparator instance with specific non-numeric ordering.
   *
   * @param stringComparator the {@link Comparator} used for ordering
   */
  public AlphanumComparator(Comparator<String> stringComparator) {
    this.stringComparator = stringComparator;
  }

  /**
   * Construct a new AlphanumComparator instance using default non-numeric ordering.
   */
  public AlphanumComparator() {
    this.stringComparator = null;
  }

  /**
   * Check if a character is a digit ({@code [0-9]}).
   * @param ch the character
   * @return true if the character is a digit
   */
  private boolean isDigit(char ch) {
    return '0' <= ch && ch <= '9';
  }

  /**
   * Get the next substring that is comprised entirely by digit either or non-digit characters.
   *
   * @param s the string to substring
   * @param slength the length of the string (passed in to prevent recalculation)
   * @param marker the start of the substring
   * @return a substring comprised entirely by digit either or non-digit characters
   */
  private String getChunk(String s, int slength, int marker) {
    StringBuilder chunk = new StringBuilder();
    char c = s.charAt(marker);
    chunk.append(c);
    ++marker;
    boolean expectingDigit = isDigit(c);
    for (; marker < slength; ++marker) {
      c = s.charAt(marker);
      if (expectingDigit != isDigit(c)) {
        break;
      }
      chunk.append(c);
    }
    return chunk.toString();
  }

  /**
   * Compare two strings containing numbers. Returns a negative integer, zero, or a positive
   * integer as the first argument is less than, equal to, or greater than the second.
   *
   * <p>This comparator does not allow null values. Use {@link Comparator#nullsFirst(Comparator)} or
   * {@link Comparator#nullsLast(Comparator)} to handle null values according to preference.</p>
   *
   * <p>Note: this comparator may impose orderings that are inconsistent with equals.</p>
   *
   * @param s1 the first object to be compared.
   * @param s2 the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *         equal to, or greater than the second.
   * @throws NullPointerException if either argument is null.
   */
  public int compare(String s1, String s2) {
    int thisMarker = 0;
    int thatMarker = 0;
    int s1Length = s1.length();
    int s2Length = s2.length();

    while (thisMarker < s1Length && thatMarker < s2Length) {
      String thisChunk = getChunk(s1, s1Length, thisMarker);
      thisMarker += thisChunk.length();

      String thatChunk = getChunk(s2, s2Length, thatMarker);
      thatMarker += thatChunk.length();

      // If both chunks contain numeric characters, sort them numerically
      int result;
      if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
        // Simple chunk comparison by length.
        int thisChunkLength = thisChunk.length();
        result = thisChunkLength - thatChunk.length();
        // If equal, the first different number counts
        if (result == 0) {
          for (int i = 0; i < thisChunkLength; i++) {
            result = thisChunk.charAt(i) - thatChunk.charAt(i);
            if (result != 0) {
              return result;
            }
          }
        }
      } else if (stringComparator != null) {
        result = stringComparator.compare(thisChunk, thatChunk);
      } else {
        result = thisChunk.compareTo(thatChunk);
      }

      if (result != 0) {
        return result;
      }
    }

    return s1Length - s2Length;
  }
}
