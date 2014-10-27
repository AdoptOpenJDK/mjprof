package com.performizeit.mjprof.plugins.mappers.singlethread.stackframe;

import com.performizeit.mjprof.plugins.mappers.singlethread.stackframe.StackFrame;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackFrameTest {
    @Test
    public void testStackFrame() {
        String frame = "       at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:142)";
        StackFrame sf = new StackFrame(frame);
        assertEquals("java.lang.ref",sf.getPackageName());
        assertEquals("ReferenceQueue",sf.getClassName());
        assertEquals("remove",sf.getMethodName());
        assertEquals("ReferenceQueue.java",sf.getFileName());
        assertEquals("142",sf.getLineNum());
        assertEquals(frame.trim(),sf.toString().trim());
    }

    public void testStackFrameNoPackage() {
        String frame = "       at ReferenceQueue.remove(ReferenceQueue.java:142)";
        StackFrame sf = new StackFrame(frame);
        assertEquals("",sf.getPackageName());
        assertEquals("ReferenceQueue",sf.getClassName());
        assertEquals("remove",sf.getMethodName());
        assertEquals("ReferenceQueue.java",sf.getFileName());
        assertEquals("142",sf.getLineNum());
        assertEquals(frame.trim(),sf.toString().trim());
    }

    public void testStackFrameFileNameLine() {
        String frame = "       at java.lang.ref.ReferenceQueue.remove";
        StackFrame sf = new StackFrame(frame);
        assertEquals("java.lang.ref",sf.getPackageName());
        assertEquals("ReferenceQueue",sf.getClassName());
        assertEquals("remove",sf.getMethodName());
        assertEquals("",sf.getFileName());
        assertEquals("",sf.getLineNum());
        assertEquals(frame.trim(),sf.toString().trim());
    }

    public void testLockFrame() {
        String frame = "       - locked <0x00000007801cbf58> (a java.lang.ref.ReferenceQueue$Lock)";
        StackFrame sf = new StackFrame(frame);
        assertEquals("",sf.getPackageName());
        assertEquals("",sf.getClassName());
        assertEquals("",sf.getMethodName());
        assertEquals("",sf.getFileName());
        assertEquals("",sf.getLineNum());
        assertEquals(frame.trim(),sf.toString().trim());
    }

}