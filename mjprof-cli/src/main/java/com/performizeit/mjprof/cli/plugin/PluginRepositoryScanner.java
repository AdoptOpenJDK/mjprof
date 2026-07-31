package com.performizeit.mjprof.cli.plugin;

import com.performizeit.mjprof.cli.monads.Macros;
import com.performizeit.mjprof.cli.monads.StepsRepository;

public class PluginRepositoryScanner {
  public static void main(String[] args) {
    StepsRepository.getRepository();
    Macros.getInstance();
    /*ThreadDump t = new ThreadDump();
    ThreadDumpGuiViewer.createAndShowGUI(t,"sdfsd",false);
    System.exit(0);*/
  }
}
