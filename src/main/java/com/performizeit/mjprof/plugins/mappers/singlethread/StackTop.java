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

package com.performizeit.mjprof.plugins.mappers.singlethread;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.plugins.filters.ProfileNodeFilter;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadInfo;


@Plugin(name = "top", params = {@Param(type = int.class)},
    category = PluginCategory.SINGLE_THREAD_MAPPER,
  description = "Returns at most n top stack frames of the stack")
public class StackTop extends SingleThreadMapperBase {
  private final int count;

  public StackTop(int count) {
    this.count = count;
  }

  @Override
  public ThreadInfo map(ThreadInfo stck) {
    Profile p = (Profile) stck.getVal("stack");
    p.filter(new ProfileNodeFilter() {
      @Override
      public boolean accept(SFNode node, int level, Object context) {
        int depthBelow = node.depthBelow();
        return depthBelow < count;
      }
    }, null);
    return stck;
  }
}
