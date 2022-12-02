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
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.HashMap;

@SuppressWarnings("unused")
@Plugin(name = "-prop", params = {@Param("attr")},
    category = PluginCategory.THREAD_INFO_COMPARTAOR,
  description = "Removes a certain attribute e.g. eliminate/stack/")
public class PropEliminator extends SingleThreadMapperBase {
  private final String prop;

  public PropEliminator(String prop) {
    this.prop = prop;
  }

  @Override
  public ThreadInfo map(ThreadInfo threadInfo) {

    HashMap<String, Object> mtd = threadInfo.cloneMetaData();
    mtd.remove(prop);
    return new ThreadInfo(mtd);

  }
}
