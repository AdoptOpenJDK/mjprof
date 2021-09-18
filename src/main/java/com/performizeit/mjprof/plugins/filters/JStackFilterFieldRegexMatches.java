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

import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.regex.Pattern;

@Plugin(name = "regexMatches",
        params = {@Param("attr"), @Param("regex")},
        description = "Returns only threads which contain the string in certain attribute (regexp not supported)")
public class JStackFilterFieldRegexMatches extends SingleThreadFilter {
    private final String attrName;
    private final Pattern regex;

    public JStackFilterFieldRegexMatches(String attrName, String regex) {
        this.attrName = attrName;
        this.regex = Pattern.compile(regex);
    }

    @Override
    public boolean filter(ThreadInfo stck) {
        Object o = stck.getVal(attrName);
        return o != null && regex.matcher(o.toString()).matches();
    }
}
