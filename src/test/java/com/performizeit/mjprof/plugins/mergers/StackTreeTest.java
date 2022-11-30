/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.performizeit.mjprof.plugins.mergers;

import com.performizeit.mjprof.model.Profile;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StackTreeTest {
  static char sep = getSeparator();
  static String stck =

      """
                 at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)
                 at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)
                 at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)
                 at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)
                 at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)
                 at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:86)
                 at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)
                 at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)
                 at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)
                 at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)
                 at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)
          """;
  static String stck2 =

      """
                 at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)
                 at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)
                 at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)
                 at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)
                 at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)
                 at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:87)
                 at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)
                 at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)
                 at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)
                 at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)
                 at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)
          """;
  static String tree = "100" + sep + "00%     [3/3]\\ at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)\n"
                       + "100" + sep + "00%     [3/3] \\ at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)\n"
                       + "100" + sep + "00%     [3/3]  \\ at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)\n"
                       + "100" + sep + "00%     [3/3]   \\ at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)\n"
                       + "100" + sep + "00%     [3/3]    X at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)\n"
                       + " 33" + sep + "33%     [1/3]    |\\ at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:87)\n"
                       + " 33" + sep + "33%     [1/3]    | \\ at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n"
                       + " 33" + sep + "33%     [1/3]    |  \\ at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n"
                       + " 33" + sep + "33%     [1/3]    |   \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n"
                       + " 33" + sep + "33%     [1/3]    |    \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n"
                       + " 33" + sep + "33%     [1/3]    |     V at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n"
                       + " 66" + sep + "67%     [2/3]     \\ at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:86)\n"
                       + " 66" + sep + "67%     [2/3]      \\ at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n"
                       + " 66" + sep + "67%     [2/3]       \\ at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n"
                       + " 66" + sep + "67%     [2/3]        \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n"
                       + " 66" + sep + "67%     [2/3]         \\ at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n"
                       + " 66" + sep + "67%     [2/3]          V at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n";

  @Test
  public void testConstructor() {
    Profile st = new Profile(stck);
    st.addMulti(new Profile(stck));
    st.addMulti(new Profile(stck2));
    // System.out.println(st.toString());
    Profile st2 = new Profile(st.toString());
    // System.out.println(st2.toString());
    assertEquals(st.toString(), st2.toString());
    // System.out.println(st2.toString());
    assertEquals(tree, st2.toString());
  }

  private static char getSeparator() {
    DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
    DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
    return symbols.getDecimalSeparator();
  }
}
