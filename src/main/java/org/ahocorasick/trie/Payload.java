package org.ahocorasick.trie;

public class Payload<T> implements Comparable<Payload<T>> {

    private final String keyword;
    private final T data;

    public Payload(final String keyword, final T data) {
        super();
        this.keyword = keyword;
        this.data = data;
    }

    public String getKeyword() {
        return keyword;
    }

    public T getData() {
        return data;
    }

    @Override
    public int compareTo(Payload<T> other) {
        return keyword.compareTo(other.getKeyword());
    }
}
