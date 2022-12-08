package com.performizeit.mjprof.plugins.mappers;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadDumpTextualParser;
import org.junit.jupiter.api.Test;

public class MergedCalleesTest {
	

	@Test
	public void testName() throws Exception {
		  MergedCallees g = new MergedCallees("org.eclipse.core.internal.jobs.WorkerPool.test");
	  //	MergedCallees g = new MergedCallees("java.lang.Object.wait");
		//    MergedCallees g = new MergedCallees("org.eclipse.core.internal.jobs.WorkerPool.startJob");

		//	GroupByProp g = new GroupByProp("org.eclipse.core.internal.jobs.WorkerPool.test");
		//	ThreadDump dump = new ThreadDump(readFromFile("src/test/res/test1.txt"));
		ThreadDump dump = ThreadDumpTextualParser.parseStringRepresentation(readFromFile("src/test/res/test2.txt"));
//		ThreadDump resDump = new ThreadDump(readFromFile("src/test/res/resTest2.txt"));

//System.out.println(g.map(dump).getStacks());

	}

	private static String readFromFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		}
	
	}
}
