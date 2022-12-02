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

import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.parser.ThreadInfo;

@SuppressWarnings("unused")
@Plugin(name = "trimbelow", params = {@Param()},
    category = PluginCategory.SINGLE_THREAD_MAPPER,
  description = "Trim all stack frames below the first occurrence of string")
public class TrimBelow extends SingleThreadMapperBase {
  protected final String expr;

  public TrimBelow(String expr) {
    this.expr = expr;

  }

  private static class TrimBelowContext {
    int flowLevel = Integer.MAX_VALUE;
  }

  @Override
  public ThreadInfo map(ThreadInfo stack) {
    Profile p = (Profile) stack.getVal("stack");
    p.filter((node, level, context) -> {
      TrimBelowContext ctx = (TrimBelowContext) context;

      String stackFrame = node.getStackFrame();
      if (level > ctx.flowLevel) {
        return true;
      }    // we are in an interesting subtree
      if (level < ctx.flowLevel)
        ctx.flowLevel = Integer.MAX_VALUE;        // this signifies exiting an interesting subtree
      if (stackFrame.contains(expr)) {
        ctx.flowLevel = level;
        return true;
      }
      return false;
    }, new TrimBelowContext());
    return stack;
  }
}
