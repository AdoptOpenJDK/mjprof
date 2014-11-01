/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.performizeit.mjprof.parser;

import org.junit.Test;
import static org.junit.Assert.*;


public class HexaLongTest {
    @Test
    public void testConstructor() {
        assertEquals(10,(new HexaLong("a")).getValue());
        assertEquals(20,(new HexaLong("14")).getValue());
        assertEquals(32,(new HexaLong("20")).getValue());
        assertEquals(32,(new HexaLong("0x20")).getValue());
    }
}
