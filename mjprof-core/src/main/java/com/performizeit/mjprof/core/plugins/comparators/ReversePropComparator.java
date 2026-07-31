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

@SuppressWarnings("unused")
@Plugin(name = "-sort", params = {@Param()},
    category = PluginCategory.THREAD_INFO_COMPARTAOR,
    description = "Sorts based on an attribute (descending order)")
public class ReversePropComparator extends PropComparator {
  public ReversePropComparator(String prop) {
    super(prop);
  }

  // Reversing compare() directly would also flip null-vs-non-null to null-first, which is wrong:
  // absent attributes must sort last in both directions. compare() (inherited from PropComparator)
  // keeps nulls last and only delegates the non-null comparison to compareNonNull(), so reversing
  // just that leaves null placement untouched.
  @Override
  protected int compareNonNull(Comparable v1, Object v2) {
    return -super.compareNonNull(v1, v2);
  }
}
