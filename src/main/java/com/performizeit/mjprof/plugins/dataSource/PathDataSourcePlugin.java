package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;


@SuppressWarnings("unused")
@Plugin(name = "path", params = {@Param("path")}, description = "Reads thread dump from a file")
public class PathDataSourcePlugin extends StreamDataSourcePluginBase {
  String fileName;

  public PathDataSourcePlugin(String fileName) {
    this.fileName = fileName;

    try {
      reader = new BufferedReader(new FileReader(fileName));
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
  }
}
