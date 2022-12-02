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

package com.performizeit.mjprof.monads;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.HashMap;


import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.plugin.PluginUtils;

public class StepsRepository {
  static HashMap<String, StepInfo> repo = new HashMap<>();

  static {
    ArrayList<Class> plugins = new ArrayList<>();

    try {
      String inputStream = new String(new BufferedInputStream(StepsRepository.class.getResourceAsStream("/supported_monads.txt")).readAllBytes());
      for (var line : inputStream.split("\n")) {
        try {
          plugins.add(Class.forName(line));
        } catch (Exception e) {
          System.out.println("Unable to find class "+line);
          throw new RuntimeException(e);
        }
      }
    } catch (Exception e) {

      throw new RuntimeException(e);
    }
    for (Class cla : plugins) {
      addPluginToRepo(cla);
    }
  }

  private static void addPluginToRepo(Class cla) {
    Plugin pluginAnnotation = (Plugin) cla.getAnnotation(Plugin.class);
    StepInfo stepInit = new StepInfo(cla, pluginAnnotation.params(), pluginAnnotation.description());
    repo.put(pluginAnnotation.name(), stepInit);
    PluginUtils.initObj(stepInit.getClazz(), stepInit.getParamTypes(), stepInit.getParamDummyArgs()); // needed for reflection in graal
  }

  public static boolean stepValid(MJStep a) {
    StepInfo pr = repo.get(a.getStepName());
    return pr != null && (a.stepArgs.size() <= pr.getArgNum()) && (a.stepArgs.size() >= pr.getMinArgNum());
  }

  public static StepInfo getStep(String stepName) {
    return repo.get(stepName);
  }

  public static HashMap<String, StepInfo> getRepository() {
    return repo;
  }
}
