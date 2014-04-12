package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.parser.StackTrace;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by life on 1/4/14.
 */
public class StackTreeTest {
    static String stck =

            "       at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n" +
                    "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n" +
                    "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n" +
                    "       at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:86)\n" +
                    "       at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)\n" +
                    "       at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)\n" +
                    "       at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)\n" +
                    "       at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)\n";
    static String stck2 =

            "       at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n" +
                    "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n" +
                    "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n" +
                    "       at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:87)\n" +
                    "       at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)\n" +
                    "       at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)\n" +
                    "       at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)\n" +
                    "       at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)\n" +
                    "       at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)\n";
    static String tree = "100.00 \\ at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)\n" +
            "100.00  \\ at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)\n" +
            "100.00   \\ at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)\n" +
            "100.00    \\ at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)\n" +
            "100.00     X at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)\n" +
            " 33.33     |\\ at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:87)\n" +
            " 33.33     | \\ at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n" +
            " 33.33     |  \\ at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n" +
            " 33.33     |   \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n" +
            " 33.33     |    \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n" +
            " 33.33     |     V at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n" +
            " 66.67      \\ at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:86)\n" +
            " 66.67       \\ at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n" +
            " 66.67        \\ at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n" +
            " 66.67         \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n" +
            " 66.67          \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n" +
            " 66.67           V at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)";
    @Test
    public void testConstructor() {
        StackTree st = new StackTree();
        st.addStacktrace(new StackTrace(stck));
        st.addStacktrace(new StackTrace(stck));
        st.addStacktrace(new StackTrace(stck2));
       // System.out.println(st.toString());
        StackTree st2 = new StackTree();
        st2.addStackTree(st.toString());
        //System.out.println(st2.toString());
        assertEquals(st.toString(), st2.toString());
        st2.addStackTree(st.toString());
        System.out.println(st2.toString());
    }
}
