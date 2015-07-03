Aho-Corasick
============

Dependency
----------
Include this dependency in your POM. Be sure to check for the latest version in Maven Central.
```xml
    <dependency>
        <groupId>org.ahocorasick</groupId>
        <artifactId>ahocorasick</artifactId>
        <version>0.2.3</version>
    </dependency>
```

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

Every character encountered is presented to a state object within the *goto* structure. If there is a matching state,
that will be elevated to the new current state.

However, if there is no matching state, the algorithm will signal a *fail* and fall back to states with less depth
(ie, a match less long) and proceed from there, until it found a matching state, or it has reached the root state.

Whenever a state is reached that matches an entire keyword, it is emitted to an *output* set which can be read after
the entire scan has completed.

The beauty of the algorithm is that it is O(n). No matter how many keywords you have, or how big the search text is,
the performance will decline in a linear way.

Some examples you could use the Aho-Corasick algorithm for:
* looking for certain words in texts in order to URL link or emphasize them
* adding semantics to plain text
* checking against a dictionary to see if syntactic errors were made

This library is the Java implementation of the afore-mentioned Aho-Corasick algorithm for efficient string matching.
The algorithm is explained in great detail in the white paper written by
Aho and Corasick: ftp://163.13.200.222/assistant/bearhero/prog/%A8%E4%A5%A6/ac_bm.pdf

Usage
-----
Setting up the Trie is a piece of cake:
```java
    Trie trie = Trie.builder()
        .addKeyword("hers")
        .addKeyword("his")
        .addKeyword("she")
        .addKeyword("he")
        .build();
    Collection<Emit> emits = trie.parseText("ushers");
```

You can now read the set. In this case it will find the following:
* "she" starting at position 1, ending at position 3
* "he" starting at position 2, ending at position 3
* "hers" starting at position 2, ending at position 5

In normal situations you probably want to remove overlapping instances, retaining the longest and left-most
matches.

```java
    Trie trie = Trie.builder()
        .removeOverlaps()
        .addKeyword("hot")
        .addKeyword("hot chocolate")
        .build();
    Collection<Emit> emits = trie.parseText("hot chocolate");
```

The removeOverlaps method tells the Trie to remove all overlapping matches. For this it relies on the following
conflict resolution rules: 1) longer matches prevail over shorter matches, 2) left-most prevails over right-most.
There is only one result now:
* "hot chocolate" starting at position 0, ending at position 12

If you want the algorithm to only check for whole words, you can tell the Trie to do so:

```java
    Trie trie = Trie.builder()
        .onlyWholeWords()
        .addKeyword("sugar")
        .build();
    Collection<Emit> emits = trie.parseText("sugarcane sugarcane sugar canesugar");
```

In this case, it will only find one match, whereas it would normally find four. The sugarcane/canesugar words
are discarded because they are partial matches.

Some text are WrItTeN in combinations of lowercase and uppercase and therefore hard to identify. You can instruct
the Trie to lowercase the entire searchtext to ease the matching process.

```java
    Trie trie = Trie.builder()
        .caseInsensitive()
        .addKeyword("casing")
        .build();
    Collection<Emit> emits = trie.parseText("CaSiNg");
```

Normally, this match would not be found. With the caseInsensitive settings the entire search text is lowercased
before the matching begins. Therefore it will find exactly one match. Since you still have control of the original
search text and you will know exactly where the match was, you can still utilize the original casing.

In many cases you may want to do useful stuff with both the non-matching and the matching text. In this case, you
might be better served by using the Trie.tokenize(). It allows you to loop over the entire text and deal with
matches as soon as you encounter them. Let's look at an example where we want to highlight words from HGttG in HTML:

```java
    String speech = "The Answer to the Great Question... Of Life, " +
            "the Universe and Everything... Is... Forty-two,' said " +
            "Deep Thought, with infinite majesty and calm.";
    Trie trie = Trie.builder().removeOverlaps().onlyWholeWords().caseInsensitive()
        .addKeyword("great question")
        .addKeyword("forty-two")
        .addKeyword("deep thought")
        .build();
    Collection<Token> tokens = trie.tokenize(speech);
    StringBuffer html = new StringBuffer();
    html.append("<html><body><p>");
    for (Token token : tokens) {
        if (token.isMatch()) {
            html.append("<i>");
        }
        html.append(token.getFragment());
        if (token.isMatch()) {
            html.append("</i>");
        }
    }
    html.append("</p></body></html>");
    System.out.println(html);
```

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
