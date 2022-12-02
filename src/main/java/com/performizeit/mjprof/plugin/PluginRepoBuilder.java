package com.performizeit.mjprof.plugin;

import com.performizeit.mjprof.api.Plugin;
import org.reflections.Reflections;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class PluginRepoBuilder {


   //native-image   -H:IncludeResources=".*/*.txt" -jar target/mjprof-1.0-jar-with-dependencies.jar target/mjprof
  public static ArrayList<Class> resolvePluginListFromClassPath(ArrayList<Class> discoveredClasses) {

    Reflections reflections = new Reflections("com.performizeit");
    Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);
    for (Class cla : annotatedPlugin) {
      discoveredClasses.add(cla);
    }
    return discoveredClasses;
  }

  public static void main(String[] args) {
    System.out.println("I got this file to write to :" + args[0]);
    ArrayList<Class> plugins = new ArrayList<>();
    resolvePluginListFromClassPath(plugins);
    try (var f = new BufferedWriter(new FileWriter(args[0]))) {
      for (Class cla : plugins) {
        f.write(cla.getName());
        f.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
