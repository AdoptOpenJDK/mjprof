package com.performizeit.mjprof.plugins.mappers.singlethread.stackframe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @Test
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
    @Test
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
    @Test
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
