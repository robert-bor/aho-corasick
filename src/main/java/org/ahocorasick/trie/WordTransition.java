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
 * Model transitions on words
 * @author doug.lovell
 */
public class WordTransition extends Transition<String> {

    /**
     * Create a transition from a position in the source string
     * @param word to match
     * @param start position of first character within the source string
     */
    public WordTransition(String word, int start) {
        super(word, start, word.length());
    }

    /**
     * Create a transition without regard for position
     * @param word to match
     */
    public WordTransition(String word) {
        this(word, 0);
    }
}
