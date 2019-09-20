Aho-Corasick
============

[![Build Status](https://travis-ci.org/robert-bor/aho-corasick.svg?branch=master)](https://travis-ci.org/robert-bor/aho-corasick)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0f65bfb641f745a4b301b85d028a4a8d)](https://www.codacy.com/app/bor-robert/aho-corasick)
[![Codecov](https://codecov.io/gh/robert-bor/aho-corasick/branch/master/graph/badge.svg)](https://codecov.io/gh/robert-bor/aho-corasick)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.ahocorasick/ahocorasick/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.ahocorasick/ahocorasick)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.ahocorasick/ahocorasick/badge.svg)](http://www.javadoc.io/doc/org.ahocorasick/ahocorasick)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Dependency
----------

Include this dependency in your POM. Be sure to check for the latest version in Maven Central.

```xml
<dependency>
  <groupId>org.ahocorasick</groupId>
  <artifactId>ahocorasick</artifactId>
  <version>0.5.0</version>
</dependency>
```

Introduction
------------

Most free-text searching is based on Lucene-like approaches, where the
search text is parsed into its various components. For every keyword a
lookup is done to see where it occurs. When looking for a couple of keywords
this approach is great, but when searching for 100,000 words, the approach
is quite slow (for example, checking against a dictionary).

The Aho-Corasick algorithm shines when looking for multiple words.
Rather than chop up the search text, it uses all the keywords to build
a [Trie](http://en.wikipedia.org/wiki/Trie) construct. The crucial
Aho-Corasick components include:

* goto
* fail
* output

Every character encountered is presented to a state object within the
*goto* structure. If there is a matching state, that will be elevated to
the new current state.

However, if there is no matching state, the algorithm will signal a
*fail* and fall back to states with less depth (i.e., a match less long)
and proceed from there, until it found a matching state, or it has reached
the root state.

Whenever a state is reached that matches an entire keyword, it is
emitted to an *output* set which can be read after the entire scan
has completed.

The algorithm is O(n). No matter how many keywords are given, or how large
the search text is, the performance will decline linearly.

The Aho-Corasick algorithm can help:

* find words in texts to link or emphasize them;
* add semantics to plain text; or
* check against a dictionary to see if syntactic errors were made.

See the [white paper](http://cr.yp.to/bib/1975/aho.pdf) by Aho and
Corasick for algorithmic details.

Usage
-----
Set up the Trie using a builder as follows:

```java
Trie trie = Trie.builder()
    .addKeyword("hers")
    .addKeyword("his")
    .addKeyword("she")
    .addKeyword("he")
    .build();
Collection<Emit> emits = trie.parseText("ushers");
```

The collection will contain `Emit` objects that match:

* "she" starting at position 1, ending at position 3
* "he" starting at position 2, ending at position 3
* "hers" starting at position 2, ending at position 5

In situations where overlapping instances are not desired, retain
the longest and left-most matches by calling `ignoreOverlaps()`:

```java
Trie trie = Trie.builder()
    .ignoreOverlaps()
    .addKeyword("hot")
    .addKeyword("hot chocolate")
    .build();
Collection<Emit> emits = trie.parseText("hot chocolate");
```

The `ignoreOverlaps()` method tells the Trie to remove all overlapping
matches. For this it relies on the following conflict resolution rules:

1. longer matches prevail over shorter matches; and
1. left-most prevails over right-most.

Only one result is returned:

* "hot chocolate" starting at position 0, ending at position 12

To check for whole words exclusively, call `onlyWholeWords()` as follows:

```java
Trie trie = Trie.builder()
    .onlyWholeWords()
    .addKeyword("sugar")
    .build();
Collection<Emit> emits = trie.parseText("sugarcane sugar canesugar");
```

Only one match is found; whereas, without calling `onlyWholeWords()` four
matches are found. The sugarcane/canesugar words are discarded because
they are partial matches.

Some text is `WrItTeN` in mixed case, which makes it hard to identify.
Instruct the Trie to convert the searchtext to lowercase to ease the
matching process. The lower-casing applies to keywords as well.

```java
Trie trie = Trie.builder()
    .ignoreCase()
    .addKeyword("casing")
    .build();
Collection<Emit> emits = trie.parseText("CaSiNg");
```

Normally, this match would not be found. By calling `ignoreCase()`,
the entire search text is made lowercase before matching begins.
Therefore it will find exactly one match.

It is also possible to just ask whether the text matches any of
the keywords, or just to return the first match it finds.

```java
Trie trie = Trie.builder().ignoreOverlaps()
        .addKeyword("ab")
        .addKeyword("cba")
        .addKeyword("ababc")
        .build();
Emit firstMatch = trie.firstMatch("ababcbab");
```

The value for `firstMatch` will be "ababc" from position 0. The
`containsMatch()` method checks whether `firstMatch` found a match and
returns `true` if that is the case.

For a barebones Aho-Corasick algorithm with a custom emit handler use:
 
```java
Trie trie = Trie.builder()
        .addKeyword("hers")
        .addKeyword("his")
        .addKeyword("she")
        .addKeyword("he")
        .build();

final List<Emit> emits = new ArrayList<>();
EmitHandler emitHandler = new EmitHandler() {

    @Override
    public void emit(Emit emit) {
        emits.add(emit);
    }
};
```

In many cases you may want to do perform tasks with both the non-matching
and the matching text. Such implementations may be better served by using
`Trie.tokenize()`. The `tokenize()` method allows looping over the
corpus to deal with matches as soon as they are encountered. Here's an
example that outputs key words as italicized HTML elements:

```java
String speech = "The Answer to the Great Question... Of Life, " +
        "the Universe and Everything... Is... Forty-two,' said " +
        "Deep Thought, with infinite majesty and calm.";

Trie trie = Trie.builder().ignoreOverlaps().onlyWholeWords().ignoreCase()
    .addKeyword("great question")
    .addKeyword("forty-two")
    .addKeyword("deep thought")
    .build();

Collection<Token> tokens = trie.tokenize(speech);
StringBuilder html = new StringBuilder();
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

You can also emit custom outputs. This might for example be useful to
implement a trivial named entity recognizer. In this case use a
`PayloadTrie` instead of a `Trie` as follows:

```java
class Word {
    private final String gender;
    public Word(String gender) {
        this.gender = gender;
    }
}

PayloadTrie<Word> trie = PayloadTrie.<Word>builder()
    .addKeyword("hers", new Word("f")
    .addKeyword("his", new Word("m"))
    .addKeyword("she", new Word("f"))
    .addKeyword("he", new Word("m"))
    .addKeyword("nonbinary", new Word("nb"))
    .addKeyword("transgender", new Word("tg"))
    .build();
Collection<PayloadEmit<Word>> emits = trie.parseText("ushers");
```

Releases
--------

See [releases](https://github.com/robert-bor/aho-corasick/releases) for details.

