package com.performizeit.mjprof.parser;

import java.util.ArrayList;

public class ThreadContainer {
  String name;
  String parent;
  String owner;
  ArrayList<ThreadInfo> threads = new ArrayList<>();
}
