/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.performizeit.mjstack.comparators;

import com.performizeit.mjstack.api.JStackComparator;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadInfo;

@Plugin(name = "sort", paramTypes = {String.class },description="Sorts based on an attribute") 
public class PropComparator implements JStackComparator {
    private final String prop;

    public PropComparator(String prop) {
        this.prop = prop;
    }
    @Override
    public int compare(ThreadInfo o1, ThreadInfo o2) {
        return ((Comparable)o1.getVal(prop)) .compareTo(o2.getVal(prop));

    }
}
