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

import com.performizeit.mjstack.api.Filter;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.api.Param;
import com.performizeit.mjstack.parser.ThreadInfo;

@Plugin(name="contains", params ={@Param("attr"),@Param("value")},
        description = "Returns only threads which contain the string in certain attribute (regexp not supported)")
public class JStackFilterFieldContains implements Filter {
    private final String attrName;
    private final String valContained;

    public JStackFilterFieldContains(String attrName, String valContained) {
        this.attrName = attrName;
        this.valContained = valContained;
    }

    @Override
    public boolean filter(ThreadInfo stck) {
        Object o = stck.getVal(attrName);
        if (o == null) return false;
        return o.toString().contains(valContained);

    }
}
