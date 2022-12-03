package com.performizeit.mjprof.plugin;

import com.performizeit.mjprof.monads.Macros;
import com.performizeit.mjprof.monads.StepsRepository;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.plugins.output.ThreadDumpGuiViewer;

public class PluginRepositoryScanner {
  public static void main(String[] args) {
    StepsRepository.getRepository();
    Macros.getInstance();
    /*ThreadDump t = new ThreadDump();
    ThreadDumpGuiViewer.createAndShowGUI(t,"sdfsd",false);
    System.exit(0);*/
  }
}
