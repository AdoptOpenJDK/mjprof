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
package com.performizeit.mjprof.plugin;

import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.plugin.types.SingleThreadMapper;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadInfo;

@Plugin(name = "test1", params = {}, category = PluginCategory.SINGLE_THREAD_MAPPER)
public class PluginWithDefaultConstructorTest implements SingleThreadMapper {
  private final String expr = "kk";


  public ThreadInfo map(ThreadInfo stck) {
    return stck;
  }

  public ThreadInfo execute(ThreadInfo stck) {
    return map(stck);
  }

  public String getHelpLine() {
    return "Trim";
  }

}
