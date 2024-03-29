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

package com.performizeit.mjprof.plugins.mergers;

import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.plugin.types.Terminal;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfoProps;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

@Plugin(name = "tree", params = {},
    category = PluginCategory.TERMINAL,
    description = "combine all stack traces ")
public class StackTreeMerger implements Terminal, PipeHandler<ThreadDump, String> {
  protected Profile st = new Profile();

  public void addStackDump(ThreadDump jsd) {
    for (ThreadInfo mss : jsd.getThreadInfos()) {
      st.addMulti((Profile) mss.getVal(ThreadInfoProps.STACK));      // System.out.println();
    }
  }

  @Override
  public String toString() {
    return st.toString();
  }

  @Override
  public String handleMsg(ThreadDump msg) {
    addStackDump(msg);
    return null;
  }

  @Override
  public String handleDone() {
    return toString();
  }
}
