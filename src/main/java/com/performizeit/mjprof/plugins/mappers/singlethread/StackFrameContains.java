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
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ProfileNodeFilter;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadInfo;

import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

@Plugin(name = "frame", params = {@Param()},
  description = "Eliminates stack frames from all stacks which do not contain string.")
public class StackFrameContains extends SingleThreadMapperBase {
  protected final String expr;

  public StackFrameContains(String expr) {
    this.expr = expr;
  }

  @Override
  public ThreadInfo map(ThreadInfo stck) {
    Profile p = (Profile) stck.getVal(STACK);
    p.filter((node, level, context) -> node.getStackFrame().contains(expr), null);
    return stck;
  }

}
