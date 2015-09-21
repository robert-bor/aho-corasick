package org.ahocorasick.trie.configuration;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class ParseConfigurationTest {

    @Test
    public void reader() throws IOException {
        StringReader reader = new StringReader("hällö");
        ParseConfiguration parseConfiguration = new ParseConfiguration().setText(reader);
        assertIterator(parseConfiguration);
        reader.close();
    }

    @Test
    public void string() throws IOException {
        ParseConfiguration parseConfiguration = new ParseConfiguration().setText("hällö");
        assertIterator(parseConfiguration);
    }

    private void assertIterator(ParseConfiguration parseConfiguration) {
        StringBuffer text = new StringBuffer();
        for (Character character : parseConfiguration) {
            text.append(character);
        }
        assertEquals("hällö", text.toString());
    }
}
