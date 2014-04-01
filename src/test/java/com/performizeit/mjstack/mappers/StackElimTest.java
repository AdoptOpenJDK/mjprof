package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.parser.JStackMetadataStack;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by life on 3/3/14.
 */
public class StackElimTest {
    String strt = "\"qtp188618231-14\" prio=10 tid=0x0007fd8d8d5b000 nid=0xd17 waiting for monitor entry [0x00007fd8ae207000]\n" +
            "   java.lang.Thread.State: BLOCKED (on object monitor)\n";
    String stck =
            "       at org.apache.hadoop.hdfs.DFSUtil.<clinit>(DFSUtil.java:128)\n" +
            "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:437)\n" +
            "       at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:410)\n" +
            "       at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:127)\n" +
            "       at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2273)\n" +
            "       at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:86)\n" +
            "       at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2307)\n" +
            "       at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2289)\n" +
            "       at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:316)\n" ;

    String akkka=
            "       at com.akkka.aaa.bbbb.rest.FileSystemFactory.provide(FileSystemFactory.java:32)\n" +
            "       at com.akkka.aaa.bbb.rest.FileSystemFactory.provide(FlsFactory.java:44)\n";
    String stck2 =
            "       at org.jvnet.hk2.internal.FactoryCreator.create(FactoryCreator.java:56)\n" +
            "       at org.jvnet.hk2.internal.SystemDescriptor.create(SystemDescriptor.java:456)\n" +
            "       at org.jvnet.hk2.internal.PerLookupContext.findOrCreate(PerLookupContext.java:69)\n" +
            "       at org.jvnet.hk2.internal.Utilities.createService(Utilities.java:2350)\n" +
            "       at org.jvnet.hk2.internal.ServiceLocatorImpl.getService(ServiceLocatorImpl.java:580)\n" +
            "       at org.jvnet.hk2.internal.ThreeThirtyResolver.resolve(ThreeThirtyResolver.java:77)\n" +
            "       at org.jvnet.hk2.internal.ClazzCreator.resolve(ClazzCreator.java:208)\n" +
            "       at org.jvnet.hk2.internal.ClazzCreator.resolveAllDependencies(ClazzCreator.java:225)\n" +
            "       at org.jvnet.hk2.internal.ClazzCreator.create(ClazzCreator.java:329)\n" +
            "       at org.jvnet.hk2.internal.SystemDescriptor.create(SystemDescriptor.java:456)\n" +
            "       at org.glassfish.jersey.process.internal.RequestScope.findOrCreate(RequestScope.java:158)\n" +
            "       at org.jvnet.hk2.internal.Utilities.createService(Utilities.java:2350)\n" +
            "       at org.jvnet.hk2.internal.ServiceLocatorImpl.getService(ServiceLocatorImpl.java:612)\n\n";
    @Test
    public void testStack() throws Exception {
        JStackMetadataStack js = new JStackMetadataStack(strt+stck+akkka+stck2);

        assertEquals(strt+stck+akkka+stck2,js.toString() );

    }
    @Test
    public void testKeep() throws Exception {
        JStackMetadataStack js = new JStackMetadataStack(strt + stck+akkka+stck2);
        StackFrameContains tb = new StackFrameContains("com.akkka");
        JStackMetadataStack js2 = tb.map(js);
        assertEquals(strt+akkka,js2.toString() );

    }
    @Test
    public void testElim() throws Exception {
        JStackMetadataStack js = new JStackMetadataStack(strt + stck+akkka+stck2);
        StackFrameNotContains tb = new StackFrameNotContains("com.akkka");
        JStackMetadataStack js2 = tb.map(js);
        assertEquals(strt+stck+stck2,js2.toString() +"\n");

    }
}
