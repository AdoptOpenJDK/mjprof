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
import com.performizeit.mjprof.parser.ThreadInfo;

@SuppressWarnings("unused")
@Plugin(name = "noop", params = {},
    category = PluginCategory.SINGLE_THREAD_MAPPER,
  description = "Does nothing")
public class NoOp extends SingleThreadMapperBase {
  @Override
  public ThreadInfo map(ThreadInfo threadInfo) {

    return threadInfo;
  }

}
