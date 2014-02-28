package com.performizeit.mjstack.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by life on 28/2/14.
 */
public class HexaLongTest {
    @Test
    public void testConstructor() {
        assertEquals(10,(new HexaLong("a")).getValue());
        assertEquals(20,(new HexaLong("14")).getValue());
        assertEquals(32,(new HexaLong("20")).getValue());
        assertEquals(32,(new HexaLong("0x20")).getValue());
    }
}
