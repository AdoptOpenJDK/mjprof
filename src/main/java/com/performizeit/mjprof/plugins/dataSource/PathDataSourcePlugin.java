package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.plugin.types.DataSource;


@SuppressWarnings("unused")
@Plugin(name = "path", params = {@Param("path")}, category = PluginCategory.DATA_SOURCE, description = "Reads thread dump from a file")
public class PathDataSourcePlugin extends StreamDataSourcePluginBase {
  String fileName;

  public PathDataSourcePlugin(String fileName) {
    this.fileName = fileName;
  }

  @Override
  protected void initReader() {
    try {
      reader = new BufferedReader(new FileReader(fileName));
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
  }
}
