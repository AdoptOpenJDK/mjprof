package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.performizeit.mjprof.api.Plugin;



@SuppressWarnings("unused")
@Plugin(name="stdin", params ={},description = "Read thread dumps from standard input")
public class StdinDataSourcePlugin extends StreamDataSourcePluginBase {
    public StdinDataSourcePlugin() {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }
}
