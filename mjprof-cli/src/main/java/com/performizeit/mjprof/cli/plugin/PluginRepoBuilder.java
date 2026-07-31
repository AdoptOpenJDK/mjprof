package com.performizeit.mjprof.cli.plugin;

import com.performizeit.mjprof.core.api.Plugin;
import org.reflections.Reflections;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PluginRepoBuilder {

  /** Classpath resources the CLI loads by name at runtime, so native-image must embed them. */
  private static final List<String> RUNTIME_RESOURCES =
      List.of("supported_monads\\.txt", "internal\\.macros\\.properties");

  public static void resolvePluginListFromClassPath(ArrayList<Class<?>> discoveredClasses) {

    Reflections reflections = new Reflections("com.performizeit");
    Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);
    discoveredClasses.addAll(annotatedPlugin);
  }

  /**
   * Writes the plugin index, and optionally the GraalVM native-image metadata.
   *
   * @param args {@code [0]} path of the supported_monads.txt to write; {@code [1]} optional
   *             directory to write reflect-config.json and resource-config.json into.
   */
  public static void main(String[] args) {
    System.out.println("I got this file to write to :" + args[0]);
    ArrayList<Class<?>> plugins = new ArrayList<>();
    resolvePluginListFromClassPath(plugins);
    try (var f = new BufferedWriter(new FileWriter(args[0]))) {
      for (Class<?> cla : plugins) {
        f.write(cla.getName());
        f.newLine();
        System.out.println("**discovered: '" + cla.getName() + "'");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (args.length > 1) {
      writeNativeImageConfig(Path.of(args[1]), plugins);
    }
  }

  /**
   * Emits the native-image metadata the CLI needs at runtime: every plugin is instantiated
   * reflectively through {@link PluginUtils#initObj}, and the plugin index plus the macro file are
   * loaded as classpath resources.
   *
   * <p>Generated deterministically from the discovered plugin list rather than by running the
   * {@code native-image-agent}. The agent library ships only with GraalVM, which used to make
   * {@code mvn -Pnative package} fail on a plain JDK before it ever reached native-image.
   */
  static void writeNativeImageConfig(Path outputDir, List<Class<?>> plugins) {
    try {
      Files.createDirectories(outputDir);

      StringBuilder reflect = new StringBuilder("[\n");
      for (int i = 0; i < plugins.size(); i++) {
        reflect.append("  {\n    \"name\": \"").append(plugins.get(i).getName()).append("\",\n")
            .append("    \"allDeclaredConstructors\": true\n  }")
            .append(i < plugins.size() - 1 ? ",\n" : "\n");
      }
      reflect.append("]\n");
      Files.writeString(outputDir.resolve("reflect-config.json"), reflect.toString());

      StringBuilder resources = new StringBuilder("{\n  \"resources\": {\n    \"includes\": [\n");
      for (int i = 0; i < RUNTIME_RESOURCES.size(); i++) {
        // the patterns are regexes, so their backslashes must survive as JSON string escapes
        String pattern = RUNTIME_RESOURCES.get(i).replace("\\", "\\\\");
        resources.append("      { \"pattern\": \"").append(pattern).append("\" }")
            .append(i < RUNTIME_RESOURCES.size() - 1 ? ",\n" : "\n");
      }
      resources.append("    ]\n  }\n}\n");
      Files.writeString(outputDir.resolve("resource-config.json"), resources.toString());

      System.out.println("**native-image config written to " + outputDir + " for "
          + plugins.size() + " plugins");
    } catch (IOException e) {
      throw new RuntimeException("unable to write native-image config to " + outputDir, e);
    }
  }
}
