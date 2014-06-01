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

package com.performizeit.mjprof.filters;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadInfo;

@Plugin(name="ncontains", params ={@Param("attr"),@Param("str")},
        description = "Returns only threads which do not contain the string (regexp not supported)")
public class JStackFilterFieldNotContains extends JStackFilterFieldContains {
    public JStackFilterFieldNotContains(String attr, String valNotContained) {
       super(attr,valNotContained);
    }

    @Override
    public boolean filter(ThreadInfo stck) {
        return !super.filter(stck);
    }
}
