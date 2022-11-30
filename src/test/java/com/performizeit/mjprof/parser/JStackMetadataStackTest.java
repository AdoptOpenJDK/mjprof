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
package com.performizeit.mjprof.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JStackMetadataStackTest {
  String metaLine = "\"qtp188618231-14\" prio=10 tid=0x0007fd8d8d5b000 nid=0xd17 waiting for monitor entry [0x00007fd8ae207000]\n";
  String metaLineWithMixedOrder = "\"qtp188618231-14\"   nid=0xd17 tid=0x0007fd8d8d5b000  prio=10 waiting for monitor entry [0x00007fd8ae207000]\n";
  String metaLineMissingFields = "\"qtp188618231-14\"   nid=0xd17  waiting for monitor entry [0x00007fd8ae207000]\n";
  String stck =
      """
             java.lang.Thread.State: BLOCKED (on object monitor)
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

  @Test
  public void testMetadataLineProperties() throws Exception {
    ThreadInfo s = new ThreadInfo(metaLine + stck);

    assertEquals("10", s.metadataProperty(metaLine, "prio"));
    assertEquals("0xd17", s.metadataProperty(metaLine, "nid"));
    assertEquals("0x0007fd8d8d5b000", s.metadataProperty(metaLine, "tid"));

  }

  @Test
  public void testMetadata() {
    ThreadInfo s = new ThreadInfo(metaLine + stck);

    assertEquals("10", s.getVal("prio"));
    assertEquals(0x0007fd8d8d5b000l, ((HexaLong) s.getVal("tidLong")).getValue());
    assertEquals(0xd17, ((HexaLong) s.getVal("nidLong")).getValue());
    assertEquals("qtp188618231-14", s.getVal("name"));
    assertEquals("waiting for monitor entry [0x00007fd8ae207000]", s.getVal("status"));

  }

  @Test
  public void testMetadataMixed() {
    ThreadInfo s = new ThreadInfo(metaLineWithMixedOrder + stck);

    assertEquals("10", s.getVal("prio"));
    assertEquals(0x0007fd8d8d5b000l, ((HexaLong) s.getVal("tidLong")).getValue());
    assertEquals(0xd17, ((HexaLong) s.getVal("nidLong")).getValue());
    assertEquals("qtp188618231-14", s.getVal("name"));
    assertEquals("waiting for monitor entry [0x00007fd8ae207000]", s.getVal("status"));

  }

  @Test
  public void testMissingFields() {
    ThreadInfo s = new ThreadInfo(metaLineMissingFields + stck);

    assertNull(s.getVal("prio"));
    assertNull(s.getVal("tidLong"));
    assertEquals(0xd17, ((HexaLong) s.getVal("nidLong")).getValue());
    assertEquals("qtp188618231-14", s.getVal("name"));
    assertEquals("waiting for monitor entry [0x00007fd8ae207000]", s.getVal("status"));

  }
}
