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

package com.performizeit.mjprof.plugins.filters;

import com.performizeit.mjprof.parser.ThreadInfo;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JStackFilterFieldRegexMatchesTest {
    @Test
    public void should_match_optional_attribute_part() throws Exception {
        ThreadInfo fooThreadInfo = new ThreadInfo("" +
                "\"foo\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        ThreadInfo foobarThreadInfo = new ThreadInfo("" +
                "\"foobar\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        ThreadInfo barThreadInfo = new ThreadInfo("" +
                "\"bar\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        JStackFilterFieldRegexMatches filter = new JStackFilterFieldRegexMatches("name", "fo+(bar)?");

        boolean matchedFoo = filter.filter(fooThreadInfo);
        boolean matchedFoobar = filter.filter(foobarThreadInfo);
        boolean matchedBar = filter.filter(barThreadInfo);

        assertTrue(matchedFoo);
        assertTrue(matchedFoobar);
        assertFalse(matchedBar);
    }

    @Test
    public void should_implement_or_operator() throws Exception {
        ThreadInfo fooThreadInfo = new ThreadInfo("" +
                "\"foo\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        ThreadInfo barThreadInfo = new ThreadInfo("" +
                "\"bar\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        ThreadInfo bazThreadInfo = new ThreadInfo("" +
                "\"baz\" prio=7 tid=0x0000000000000007 nid=0x0007 runnable [0x0000000000000007]\n" +
                "   java.lang.Thread.State: RUNNABLE\n" +
                "       at fr.pingtimeout.mjprof.regex.someMethod(Regex.java:42)\n" +
                "       at java.lang.Thread.run(Thread.java:745)");
        JStackFilterFieldRegexMatches filter = new JStackFilterFieldRegexMatches("name", "^foo|bar$");

        boolean matchedFoo = filter.filter(fooThreadInfo);
        boolean matchedBar = filter.filter(barThreadInfo);
        boolean matchedBaz = filter.filter(bazThreadInfo);

        assertTrue(matchedFoo);
        assertTrue(matchedBar);
        assertFalse(matchedBaz);
    }
}
