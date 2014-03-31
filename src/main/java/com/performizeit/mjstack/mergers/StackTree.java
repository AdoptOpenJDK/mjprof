package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.parser.JStackStack;


import java.util.HashMap;

/**
 * Created by life on 25/3/14.
 */
public class StackTree {
    class SFNode {
        int count;
        String sf;
         HashMap<String,SFNode> children=     new HashMap<String,SFNode>();

        @Override
        public String toString() {
            return toString(0);
        }

        public String toString(int lvl) {

            String a = "";
            for (int i=0;i<lvl;i++) {
                a += " ";
            }
            a = count+a+" "+ sf;
            a += "\n";
            for(SFNode n: children.values())  {
                a  += n.toString(lvl+1);
            }
            return a;
        }

    }
    HashMap<String,SFNode> roots =     new HashMap<String,SFNode>();
    void transformStack(JStackStack stackTrace) {
        String[] sf = stackTrace.getStackFrames();
        HashMap<String,SFNode> c = roots;
        for (int i=sf.length-1;i>=0;i--) {
            SFNode node = c.get(sf[i]);
            if (node != null) {
                node.count++;



            }  else {
                node = new SFNode();
                node.count++;
                node.sf = sf[i];
                c.put(sf[i],node);
            }
            c = node.children;
            System.out.println(sf[i]);
        }



    }

    @Override
    public String toString() {
        String a = "";
        for(SFNode n: roots.values())  {
            a  += n.toString();
        }
        return a;
    }

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

    public static void main(String[] args) {
        StackTree st = new StackTree();
        st.transformStack(new JStackStack(stck));
        st.transformStack(new JStackStack(stck));
        st.transformStack(new JStackStack(stck2));
        System.out.println(st.toString());
    }
}
