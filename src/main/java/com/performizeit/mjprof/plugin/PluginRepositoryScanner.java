package com.performizeit.mjprof.plugin;

import com.performizeit.mjprof.monads.Macros;
import com.performizeit.mjprof.monads.StepsRepository;

public class PluginRepositoryScanner {
  public static void main(String[] args) {
    StepsRepository.getRepository();
    Macros.getInstance();
  }
}
