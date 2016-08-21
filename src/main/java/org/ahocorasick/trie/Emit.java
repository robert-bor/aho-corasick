package org.ahocorasick.trie;

import org.ahocorasick.interval.Interval;
import org.ahocorasick.interval.Intervalable;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Emit extends Interval implements Intervalable, Serializable {

    private final String keyword;

    public Emit(final int start, final int end, final String keyword) {
        super(start, end);
        this.keyword = keyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

    @Override
    protected void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        super.writeObject(stream);
        stream.writeUTF(keyword);
    }

    @Override
    protected void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Field f = this.getClass().getDeclaredField("keyword");
        super.readObject(stream);
        f.setAccessible(true);
        f.set(this, stream.readUTF());
    }
}
