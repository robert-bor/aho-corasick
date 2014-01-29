Aho-Corasick
============

Introduction
------------
Nowadays most free-text searching is based on Lucene-like approaches, where the search text is parsed into its
various components. For every keyword a lookup is done to see where it occurs. When looking for a couple of keywords
this approach is great. But what about it if you are not looking for just a couple of keywords, but a 100,000 of
them? Like, for example, checking against a dictionary?

This is where the Aho-Corasick algorithm shines. Instead of chopping up the search text, it uses all the keywords
to build up a construct called a [Trie](http://en.wikipedia.org/wiki/Trie). There are three crucial components
to Aho-Corasick:
* goto
* fail
* output

Every character encountered is presented to a state object within the goto structure. If there is a matching state,
that will be elevated to the new current state.

However, if there is no matching state, the algorithm will fall back to states with less depth (ie, a match less long)
and proceed from there, until it found a matching state, or it has reached the root state.

Whenever a state is reached that matches an entire keyword, it is emitted to an output set which can be read after the
entire scan has completed.

The beauty of the algorithm is that it is O(n). No matter how many keywords you have, or how big the search text is,
the performance will decline in a linear way.

Some examples you could use the Aho-Corasick algorithm for:
* looking for certain words in texts in order to URL link or emphasize them
* adding semantics to plain text
* checking against a dictionary to see if syntactic errors were made

This library is the Java implementation of the afore-mentioned Aho-Corasick algorithm for efficient string matching.
The algorithm is explained in great detail in the white paper written by
[Aho and Corasick](ftp://163.13.200.222/assistant/bearhero/prog/%A8%E4%A5%A6/ac_bm.pdf).

Usage
-----

Setting up the Trie is a piece of cake:
```java
    Trie trie = new Trie();
    trie.addKeyword("hers");
    trie.addKeyword("his");
    trie.addKeyword("she");
    trie.addKeyword("he");
    Collection<Emit> emits = trie.parseText("ushers");
```

You can now read the set. In this case it will find the following:
* "she" at position 3
* "he" at position 3
* "hers" at position 5

License
-------
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
