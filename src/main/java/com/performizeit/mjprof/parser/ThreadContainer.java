package com.performizeit.mjprof.parser;

import java.util.ArrayList;

public class ThreadContainer {
  public ThreadContainer(String name, String parent, String owner) {
    this.name = name;
    this.parent = parent;
    this.owner = owner;
  }


  String name;
  String parent;
  String owner;
  ArrayList<ThreadInfo> threads = new ArrayList<>();
}
