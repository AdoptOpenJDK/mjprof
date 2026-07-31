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
package com.performizeit.mjprof.core.plugins.comparators;

import com.performizeit.mjprof.core.api.Plugin;
import com.performizeit.mjprof.core.api.Param;
import com.performizeit.mjprof.core.api.PluginCategory;
import com.performizeit.mjprof.core.parser.ThreadInfo;

@Plugin(name = "sort", params = {@Param("attr") },category = PluginCategory.THREAD_INFO_COMPARTAOR,
    description="Sorts based on an attribute")
public class PropComparator extends SortMapper{
    private final String prop;

    public PropComparator(String prop) {
        this.prop = prop;
    }

    // Threads that never got this attribute set (e.g. a singleton group() entry, which never goes
    // through ThreadInfoAggregator's merge path that would populate "count") have a null value here.
    // Nulls sort last regardless of direction: ReversePropComparator reverses compareNonNull(), not
    // this method, so the null placement below is shared by both sort/ and -sort/.
    @Override
    public int compare(ThreadInfo o1, ThreadInfo o2) {
        Object v1 = o1.getVal(prop);
        Object v2 = o2.getVal(prop);
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return 1;
        if (v2 == null) return -1;
        return compareNonNull((Comparable) v1, v2);
    }

    @SuppressWarnings("unchecked")
    protected int compareNonNull(Comparable v1, Object v2) {
        return v1.compareTo(v2);
    }
}
