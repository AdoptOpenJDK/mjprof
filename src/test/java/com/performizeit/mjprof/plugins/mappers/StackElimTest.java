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

package com.performizeit.mjprof.plugins.mappers;

import com.performizeit.mjprof.plugins.mappers.singlethread.StackFrameContains;
import com.performizeit.mjprof.plugins.mappers.singlethread.StackFrameNotContains;
import com.performizeit.mjprof.parser.ThreadInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class StackElimTest {
  String threadHeader = """
      "qtp188618231-14" prio=10 tid=0x0007fd8d8d5b000 nid=0xd17 waiting for monitor entry [0x00007fd8ae207000]
         java.lang.Thread.State: BLOCKED (on object monitor)
      """;
  String stackTrace =
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
          """;

  String stackTraceWithPhrase =
      """
                 at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)
                 at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)
          """;
  String stackTracePart2 =
      """
                 at org.jvnet.hk2.internal.FactoryCreator.create(FactoryCreator.java:56)
                 at org.jvnet.hk2.internal.SystemDescriptor.create(SystemDescriptor.java:456)
                 at org.jvnet.hk2.internal.PerLookupContext.findOrCreate(PerLookupContext.java:69)
                 at org.jvnet.hk2.internal.Utilities.createService(Utilities.java:2350)
                 at org.jvnet.hk2.internal.ServiceLocatorImpl.getService(ServiceLocatorImpl.java:580)
                 at org.jvnet.hk2.internal.ThreeThirtyResolver.resolve(ThreeThirtyResolver.java:77)
                 at org.jvnet.hk2.internal.ClazzCreator.resolve(ClazzCreator.java:208)
                 at org.jvnet.hk2.internal.ClazzCreator.resolveAllDependencies(ClazzCreator.java:225)
                 at org.jvnet.hk2.internal.ClazzCreator.create(ClazzCreator.java:329)
                 at org.jvnet.hk2.internal.SystemDescriptor.create(SystemDescriptor.java:456)
                 at org.glassfish.jersey.process.internal.RequestScope.findOrCreate(RequestScope.java:158)
                 at org.jvnet.hk2.internal.Utilities.createService(Utilities.java:2350)
                 at org.jvnet.hk2.internal.ServiceLocatorImpl.getService(ServiceLocatorImpl.java:612)
          """;

  @Test
  public void testStack() {
    ThreadInfo js = new ThreadInfo(threadHeader + stackTrace + stackTraceWithPhrase + stackTracePart2);

    assertEquals(threadHeader + stackTrace + stackTraceWithPhrase + stackTracePart2, js.toString());
  }

  @Test
  public void testKeep() {
    ThreadInfo js = new ThreadInfo(threadHeader + stackTrace + stackTraceWithPhrase + stackTracePart2);
    StackFrameContains tb = new StackFrameContains("com.akkka");
    ThreadInfo js2 = tb.map(js);
    assertEquals(threadHeader + stackTraceWithPhrase, js2.toString());

  }

  @Test
  public void testElim() {
    ThreadInfo js = new ThreadInfo(threadHeader + stackTrace + stackTraceWithPhrase + stackTracePart2);
    StackFrameNotContains tb = new StackFrameNotContains("com.akkka");
    ThreadInfo js2 = tb.map(js);
    assertEquals(threadHeader + stackTrace + stackTracePart2, js2.toString());
  }
}
