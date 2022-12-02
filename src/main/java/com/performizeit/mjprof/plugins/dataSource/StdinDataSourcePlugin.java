package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;


@SuppressWarnings("unused")
@Plugin(name = "stdin",
    params = {},
    category = PluginCategory.DATA_SOURCE,
    description = "Reads thread dumps from standard input (chosen automatically when no other data source is specified)")
public class StdinDataSourcePlugin extends StreamDataSourcePluginBase {
  public StdinDataSourcePlugin() {
    reader = new BufferedReader(new InputStreamReader(System.in));
  }
}
