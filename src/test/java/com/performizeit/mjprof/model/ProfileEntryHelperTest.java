package com.performizeit.mjprof.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileEntryHelperTest {
    static String root = "100.00%     [3/3]\\ at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)" ;
      String lineWithSplit   =   "100.00%     [3/3]    X at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)" ;

      String lineWithPipe =       " 33.33%     [1/3]    | \\ at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)";

       String leafLine =      " 66.67%     [2/3]          V at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)";
    @Test
    public void testRoot() {
        ProfileEntryHelper p = new ProfileEntryHelper(root);
        assertEquals(p.count, 3);
        assertEquals(p.countAll, 3);
        assertEquals(p.indentation, 0);
        assertEquals(p.charType, ProfileEntryHelper.INDENT);
        assertEquals(p.description, "at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)");

    }
    @Test
    public void testSplit() {
        ProfileEntryHelper p = new ProfileEntryHelper(lineWithSplit);
        assertEquals(p.count, 3);
        assertEquals(p.countAll, 3);
        assertEquals(p.indentation, 4);
        assertEquals(p.charType, ProfileEntryHelper.SPLIT);
        assertEquals(p.description, "at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)");

    }
    @Test
    public void testPipe() {
        ProfileEntryHelper p = new ProfileEntryHelper(lineWithPipe);
        assertEquals(p.count, 1);
        assertEquals(p.countAll, 3);
        assertEquals(p.indentation, 6);
        assertEquals(p.charType, ProfileEntryHelper.INDENT);
        assertEquals(p.description, "at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)");
    }

    @Test
    public void testLeaf() {
        ProfileEntryHelper p = new ProfileEntryHelper(leafLine);
        assertEquals(p.count, 2);
        assertEquals(p.countAll, 3);
        assertEquals(10, p.indentation);
        assertEquals(p.charType, ProfileEntryHelper.LEAF);
        assertEquals(p.description, "at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)");
    }
}
