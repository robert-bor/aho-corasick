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
 * Model transitions on characters
 * @author doug.lovell
 */
class CharacterTransition extends Transition<Character> {

    /**
     * Create a character transition from a position in the source string
     * @param c character to match
     * @param start positon of character in source string
     */
    public CharacterTransition(Character c, int start) {
        super(c, start, 1);
    }

    /**
     * Create a character transition without regard for position
     * @param c character to match
     */
    public CharacterTransition(Character c) {
        this(c, 0);
    }

}
