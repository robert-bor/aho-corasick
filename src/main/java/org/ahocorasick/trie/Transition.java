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
 * Enables the trie to model transitions on whole words or characters
 * ... or whatever!
 * @author doug.lovell
 * @param <T>
 */
public abstract class Transition<T> {
    protected final T token;
    public Transition(T token) {
        this.token = token;
    }
    public T transitionToken() {
        return token;
    }
    public abstract void updateMatch(StringBuilder match);
    public abstract boolean isWordSeparator();
}
