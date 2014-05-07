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

package com.performizeit.mjstack.filters;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;

@Plugin(name="contains", paramTypes={String.class,String.class},
        description = "Returns only threads which contain the string (regexp not supported)")
public class JStackFilterFieldContains implements JStackFilter {
    private final String fieldName;
    private final String valContained;

    public JStackFilterFieldContains(String fieldName, String valContained) {
        this.fieldName = fieldName;
        this.valContained = valContained;
    }

    @Override
    public boolean filter(ThreadInfo stck) {
        Object o = stck.getVal(fieldName);
        if (o == null) return false;
        return o.toString().contains(valContained);

    }
}
