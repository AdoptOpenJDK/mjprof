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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.plugin.PluginRepoBuilder;
import com.performizeit.mjprof.plugin.PluginUtils;

public class StepsRepository {
  static HashMap<String, StepInfo> repo = new HashMap<>();

  static {
    ArrayList<Class<?>> plugins = new ArrayList<>();

    resolvePlugins(plugins);
    for (Class<?> cla : plugins) {
      addPluginToRepo(cla);
    }
  }

  private static void resolvePlugins(ArrayList<Class<?>> plugins) {
    try (var inputStream = new BufferedInputStream(StepsRepository.class.getResourceAsStream("/supported_monads.txt"))) {
     if (inputStream == null) {
        resolvePluginsDynamically(plugins);
        return;
      }

      String text = new String(inputStream.readAllBytes());
      for (var line : text.split(System.lineSeparator())) {
        try {
          plugins.add(Class.forName(line));
        } catch (Exception e) {
          System.out.println("Unable to locate plugin class " + line);
          throw new RuntimeException(e);
        }
      }
    } catch (IOException | NullPointerException e)  {
      System.out.println("Could not read /supported_monads.txt generating scanning plugins");
      resolvePluginsDynamically(plugins);
    }
  }

  private static void resolvePluginsDynamically(ArrayList<Class<?>> plugins) {
    // the prefered way is to create /supported_monads.txt during build whe it is missing create dynamically
    // dynamic creation will not work in graal native
    PluginRepoBuilder.resolvePluginListFromClassPath(plugins);
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
