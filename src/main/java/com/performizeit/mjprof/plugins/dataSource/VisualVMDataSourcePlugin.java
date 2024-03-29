package com.performizeit.mjprof.plugins.dataSource;

import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.performizeit.plumbing.GeneratorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.File;

@SuppressWarnings("unused")
@Plugin(name = "visualvm", params = {@Param("path")},
    category = PluginCategory.DATA_SOURCE,
    description = "Read profiling session from xml export of VisualVM(not implemented yet)")
public class VisualVMDataSourcePlugin implements DataSource, GeneratorHandler<ThreadDump> {
  String fileName;

  public VisualVMDataSourcePlugin(String fileName) {
    this.fileName = fileName;
  }

  public ArrayList<ThreadDump> getThreadDumps() {
    File fXmlFile = new File(fileName);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(fXmlFile);
      //ExportedView tree node
      Element e = doc.getDocumentElement();
      NodeList nl = e.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        System.out.println("aa[" + nl.item(i).getNodeName() + "]");
      }

      //doc.getDocumentElement()
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new ArrayList<>();
  }

  @Override
  public ThreadDump generate() {
    return null;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public void sleepBetweenIteration() {
  }
}
