package com.performizeit.mjprof.model;

import com.performizeit.mjprof.plugins.mappers.singlethread.FileNameEliminator;
import com.performizeit.mjprof.parser.ThreadInfo;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

/**
 * Created by life on 28/5/14.
 */
public class ThreadInfoAggregatorTest extends TestCase {
    public static String stack1 = "\"main\" prio=5 tid=0x00007fe81d001000 nid=0x1b03 waiting on condition [0x000000010d192000]\n" +
            "   java.lang.Thread.State: TIMED_WAITING (sleeping)\n" +
            "       at java.lang.Thread.sleep\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.main\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke0\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke\n" +
            "       at sun.reflect.DelegatingMethodAccessorImpl.invoke\n" +
            "       at java.lang.reflect.Method.invoke\n" +
            "       at com.intellij.rt.execution.application.AppMain.main";
    public static String stack2 = "\"main\" prio=5 tid=0x00007fe81d001000 nid=0x1b03 runnable [0x000000010d192000]\n" +
            "   java.lang.Thread.State: RUNNABLE\n" +
            "       at com.performizeit.jpo.ex.gctuning.GraphObject.<init>\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.allocateGarbage\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.main\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke0\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke\n" +
            "       at sun.reflect.DelegatingMethodAccessorImpl.invoke\n" +
            "       at java.lang.reflect.Method.invoke\n" +
            "       at com.intellij.rt.execution.application.AppMain.main";
    public static String stck1a = "\"main\" prio=5 tid=0x00007fe81d001000 nid=0x1b03 waiting on condition [0x000000010d192000]\n" +
            "   java.lang.Thread.State: TIMED_WAITING (sleeping)\n" +
            "       at java.lang.Thread.sleep(Native Method)\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.main(T9.java:16)\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "       at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "       at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "       at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)\n";
    public static String stck2a = "\"main\" prio=5 tid=0x00007fe81d001000 nid=0x1b03 runnable [0x000000010d192000]\n" +
            "   java.lang.Thread.State: RUNNABLE\n" +
            "       at com.performizeit.jpo.ex.gctuning.GraphObject.<init>(GraphObject.java:20)\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.allocateGarbage(T9.java:42)\n" +
            "       at com.performizeit.jpo.ex.gctuning.T9.main(T9.java:15)\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
            "       at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
            "       at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
            "       at java.lang.reflect.Method.invoke(Method.java:483)\n" +
            "       at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)";
    @Test
    public void testTwo() {
        ThreadInfoAggregator a = new ThreadInfoAggregator(Arrays.asList(TID));
        a.accumulateThreadInfo(new ThreadInfo(stack1));
        a.accumulateThreadInfo(new ThreadInfo(stack2));
        System.out.println(a.getAggrInfos().get(0).toString());

    }

    @Test
    public void testTwoA() {
        ThreadInfoAggregator a = new ThreadInfoAggregator(Arrays.asList(TID));
        a.accumulateThreadInfo(new ThreadInfo(stck1a));
        a.accumulateThreadInfo(new ThreadInfo(stck2a));
        System.out.println(a.getAggrInfos().get(0).toString());

    }

    @Test
    public void testTwoB() {
        ThreadInfoAggregator a = new ThreadInfoAggregator(Arrays.asList(TID));
        FileNameEliminator fne = new FileNameEliminator();
        a.accumulateThreadInfo(fne.map(new ThreadInfo(stck1a)));
        a.accumulateThreadInfo(fne.map(new ThreadInfo(stck2a)));
        System.out.println(a.getAggrInfos().get(0).toString());

    }
}
