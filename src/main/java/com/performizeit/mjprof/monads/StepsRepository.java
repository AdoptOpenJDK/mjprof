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

import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjprof.api.Plugin;

public class StepsRepository {
  static HashMap<String, StepInfo> repo = new HashMap<>();

  static {
    Reflections reflections = new Reflections("com.performizeit");
    Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);
    for (Class cla : annotatedPlugin) {
      Plugin pluginAnnotation = (Plugin) cla.getAnnotation(Plugin.class);
      StepInfo stepInit = new StepInfo(cla, pluginAnnotation.params(), pluginAnnotation.description());
      repo.put(pluginAnnotation.name(), stepInit);
    }
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
