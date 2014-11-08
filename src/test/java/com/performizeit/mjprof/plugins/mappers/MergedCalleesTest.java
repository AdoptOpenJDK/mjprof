package com.performizeit.mjprof.plugins.mappers;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.performizeit.mjprof.parser.ThreadDump;

public class MergedCalleesTest {
	

	@Test
	public void testName() throws Exception {
		  MergedCallees g = new MergedCallees("org.eclipse.core.internal.jobs.WorkerPool.test");
	  //	MergedCallees g = new MergedCallees("java.lang.Object.wait");
		//    MergedCallees g = new MergedCallees("org.eclipse.core.internal.jobs.WorkerPool.startJob");

		//	GroupByProp g = new GroupByProp("org.eclipse.core.internal.jobs.WorkerPool.test");
		//	ThreadDump dump = new ThreadDump(readFromFile("src/test/res/test1.txt"));
		ThreadDump dump = new ThreadDump(readFromFile("src/test/res/test2.txt"));
//		ThreadDump resDump = new ThreadDump(readFromFile("src/test/res/resTest2.txt"));

System.out.println(g.map(dump).getStacks());

	}

	private static String readFromFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			String everything = sb.toString();
			return everything;
		} finally {
			br.close();
		}
	
	}
}
