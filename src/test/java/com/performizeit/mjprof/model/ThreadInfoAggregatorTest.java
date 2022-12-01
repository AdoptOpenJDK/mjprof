package com.performizeit.mjprof.model;

import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugins.mappers.singlethread.stackframe.FileNameEliminator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.performizeit.mjprof.parser.ThreadInfoProps.TID;

/**
 * Created by life on 28/5/14.
 */
public class ThreadInfoAggregatorTest {
  public static String stack1 = """
      "main" prio=5 tid=0x00007fe81d001000 nid=0x1b03 waiting on condition [0x000000010d192000]
         java.lang.Thread.State: TIMED_WAITING (sleeping)
             at java.lang.Thread.sleep
             at com.performizeit.jpo.ex.gctuning.T9.main
             at sun.reflect.NativeMethodAccessorImpl.invoke0
             at sun.reflect.NativeMethodAccessorImpl.invoke
             at sun.reflect.DelegatingMethodAccessorImpl.invoke
             at java.lang.reflect.Method.invoke
             at com.intellij.rt.execution.application.AppMain.main""";
  public static String stack2 = """
      "main" prio=5 tid=0x00007fe81d001000 nid=0x1b03 runnable [0x000000010d192000]
         java.lang.Thread.State: RUNNABLE
             at com.performizeit.jpo.ex.gctuning.GraphObject.<init>
             at com.performizeit.jpo.ex.gctuning.T9.allocateGarbage
             at com.performizeit.jpo.ex.gctuning.T9.main
             at sun.reflect.NativeMethodAccessorImpl.invoke0
             at sun.reflect.NativeMethodAccessorImpl.invoke
             at sun.reflect.DelegatingMethodAccessorImpl.invoke
             at java.lang.reflect.Method.invoke
             at com.intellij.rt.execution.application.AppMain.main""";
  public static String stck1a = """
      "main" prio=5 tid=0x00007fe81d001000 nid=0x1b03 waiting on condition [0x000000010d192000]
         java.lang.Thread.State: TIMED_WAITING (sleeping)
             at java.lang.Thread.sleep(Native Method)
             at com.performizeit.jpo.ex.gctuning.T9.main(T9.java:16)
             at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
             at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
             at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
             at java.lang.reflect.Method.invoke(Method.java:483)
             at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)
      """;
  public static String stck2a = """
      "main" prio=5 tid=0x00007fe81d001000 nid=0x1b03 runnable [0x000000010d192000]
         java.lang.Thread.State: RUNNABLE
             at com.performizeit.jpo.ex.gctuning.GraphObject.<init>(GraphObject.java:20)
             at com.performizeit.jpo.ex.gctuning.T9.allocateGarbage(T9.java:42)
             at com.performizeit.jpo.ex.gctuning.T9.main(T9.java:15)
             at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
             at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
             at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
             at java.lang.reflect.Method.invoke(Method.java:483)
             at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)""";

  @Test
  public void testTwo() {
    ThreadInfoAggregator a = new ThreadInfoAggregator(List.of(TID));
    a.accumulateThreadInfo(new ThreadInfo(stack1));
    a.accumulateThreadInfo(new ThreadInfo(stack2));
  }

  @Test
  public void testTwoA() {
    ThreadInfoAggregator a = new ThreadInfoAggregator(List.of(TID));
    a.accumulateThreadInfo(new ThreadInfo(stck1a));
    a.accumulateThreadInfo(new ThreadInfo(stck2a));
  }

  @Test
  public void testTwoB() {
    ThreadInfoAggregator a = new ThreadInfoAggregator(List.of(TID));
    FileNameEliminator fne = new FileNameEliminator();
    a.accumulateThreadInfo(fne.map(new ThreadInfo(stck1a)));
    a.accumulateThreadInfo(fne.map(new ThreadInfo(stck2a)));
    // System.out.println(a.getAggrInfos().get(0).toString());
  }
}
