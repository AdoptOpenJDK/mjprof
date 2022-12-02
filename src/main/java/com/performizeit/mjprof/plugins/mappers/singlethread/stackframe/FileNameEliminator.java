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

package com.performizeit.mjprof.plugins.mappers.singlethread.stackframe;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugins.mappers.singlethread.SingleThreadMapperBase;

import java.util.HashMap;

@SuppressWarnings("unused")
@Plugin(name = "-fn", params = {},
    category = PluginCategory.SINGLE_THREAD_MAPPER,
  description = "Eliminates file name and line from stack frames")
public class FileNameEliminator extends SingleThreadMapperBase {


  public FileNameEliminator() {

  }

  @Override
  public ThreadInfo map(ThreadInfo threadInfo) {
    HashMap<String, Object> mtd = threadInfo.cloneMetaData();
    Profile jss = (Profile) threadInfo.getVal("stack");
    jss.visit((sf, level) -> {
      if (sf.getStackFrame() == null) return;
      sf.setStackFrame(eliminatePackage(sf.getStackFrame()));
    });
    return threadInfo;
  }

  public static String eliminatePackage(String stackFrame) {
    StackFrame sf = new StackFrame(stackFrame);
    sf.setLineNum("");
    sf.setFileName("");
    return sf.toString();

  }
}
