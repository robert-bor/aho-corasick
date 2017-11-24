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

import java.util.Objects;

/**
 * Enables the trie to model transitions on whole words or characters
 * ... or whatever!
 * @author doug.lovell
 * @param <T>
 */
public class Transition<T> {
    protected final T token;
    protected final int start;
    protected final int length;
    
    public Transition(final T token, int start, int length) {
        this.token = token;
        this.start = start;
        this.length = length;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getLength() {
        return length;
    }
    
    @Override
    public String toString() {
        final int s = getStart();
        final int len = getLength();
        
        return "Transition on '" + token + "' start: " + s + ", length: " + len;
    }
    
    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transition<?> other = (Transition<?>) obj;
        return Objects.equals(this.token, other.token);
    }
}
