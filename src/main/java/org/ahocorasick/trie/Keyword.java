/*
 * Copyright 2015 Rogue Wave Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ahocorasick.trie;

/**
 * Keyword encapsulates part of a potential match along with the count
 * of prior source tokens consumed to create the potential match.
 *
 * @author doug.lovell
 */
public class Keyword implements Comparable {
    private final String text;
    private final int depth;
  
    /**
     * Create portion of potential match
     * @param text content that matches
     * @param depth count of prior source tokens that comprise the match
     */
    public Keyword(final String text, final int depth) {
        this.text = text;
        this.depth = depth;
    }
    
    public int getDepth() {
        return depth;
    }
    
    public String getText() {
        return text;
    }
    
    public String toString() {
        final String t = getText();
        final int d = getDepth();
        
        return "Keyword '" + t + "' at depth " + d;
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof Keyword) {
            return text.compareTo(((Keyword) o).text);
        }
        throw new IllegalArgumentException("Only supports comparison with other keywords");
    }
}
