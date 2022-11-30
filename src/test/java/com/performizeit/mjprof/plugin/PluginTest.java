package com.performizeit.mjprof.plugin;



import java.util.HashMap;
import java.util.Set;

import com.performizeit.mjprof.MJProf;
import com.performizeit.mjprof.plugins.mappers.GroupByProp;
import com.performizeit.mjprof.plugins.mappers.singlethread.StackTop;
import com.performizeit.mjprof.plugins.terminals.ListProps;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.plugins.mappers.singlethread.StackFrameContains;
import com.performizeit.mjprof.monads.StepInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginTest {
	String stck = """
			"qtp188618231-14" prio=10 tid=0x0007fd8d8d5b000 nid=0xd17 waiting for monitor entry [0x00007fd8ae207000]
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
	String stck2 =
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
	public void testPlugin() throws Exception {
        assertNotNull(PluginUtils.getAllPlugins().get(GroupByProp.class));
	}

	@Test
	public void testInitObject() {
		Object[] intObjArray = new Object[]{1};
		Object obj = PluginUtils.initObj(StackTop.class, new Class[]{int.class},intObjArray);
		assertEquals(StackTop.class ,obj.getClass());

		Object[] strObjArray = new Object[]{"cc"};
		obj = PluginUtils.initObj(StackFrameContains.class, new Class[]{String.class},strObjArray);
		assertEquals(StackFrameContains.class ,obj.getClass());
		
	}

	@Test
	public void testSynopsis() {
		assertTrue(MJProf.getSynopsisString().contains("Synopsis"));
		assertTrue(MJProf.getSynopsisString().contains(ListProps.class.getAnnotation(Plugin.class).description()));

	}
	
	@Test
	public void testGetDtaSourcePlugins() {
		HashMap<String, StepInfo> repo = new HashMap<String, StepInfo>();
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);
		for(Class cla: annotatedPlugin){
			Plugin pluginAnnotation = (Plugin) cla.getAnnotation(Plugin.class);
			StepInfo stepInit = new StepInfo(cla, pluginAnnotation.params(),pluginAnnotation.description());
			repo.put(pluginAnnotation.name(), stepInit);
		}
	}
}
