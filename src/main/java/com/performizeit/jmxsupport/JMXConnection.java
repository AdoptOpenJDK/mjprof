/*
 *
 * Copyright 2011 Performize-IT LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.performizeit.jmxsupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXConnection {
  String host;
  String port;
  String userName;
  String userPassword;
  JMXServiceURL serviceURL;
  private final String connectURL;
  private boolean originalThreadContentionEnabledValue;

  public boolean isOriginalThreadContentionEnabledValue() {
    return originalThreadContentionEnabledValue;
  }

  public void setOriginalThreadContentionEnabledValue(boolean originalThreadContentionEnabledValue) {
    this.originalThreadContentionEnabledValue = originalThreadContentionEnabledValue;
  }


public JMXConnection(String serverUrl, String uName, String passwd) throws MalformedURLException {
    userName = uName;
    userPassword = passwd;
    host = serverUrl;
    port = "";

    int colonIndex = serverUrl.lastIndexOf(":");
    if (colonIndex > 0) {
      port = serverUrl.substring(colonIndex + 1);
      host = serverUrl.substring(0, colonIndex);
    }
    connectURL = host + ":" + port;
    //    System.out.println("[" + host + "] [" + port + "] [" + userName + "] [" + userPassword + "]");
    serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverUrl + "/jmxrmi");
  }

  MBeanServerConnection server = null;

  public String getConnectURL() {
    return connectURL;
  }

  public MBeanServerConnection getServerConnection() throws IOException {
    if (server == null) {

      Map env = new HashMap();
      if (userName != null && userPassword != null && userName.trim().length() > 0) {
        String[] creds = {userName, userPassword};
        env.put(JMXConnector.CREDENTIALS, creds);
      }
      JMXConnector conn = JMXConnectorFactory.connect(serviceURL, env);
      server = conn.getMBeanServerConnection();
    }
    return server;
  }

  public static ObjectName RUNTIME = null;
  public static ObjectName GC = null;
  public static ObjectName THREADING = null;

  static {
    try {
      RUNTIME = new ObjectName("java.lang:type=Runtime");
      GC = new ObjectName("java.lang:type=GarbageCollector,name=*");
      THREADING = new ObjectName("java.lang:type=Threading");
    } catch (MalformedObjectNameException | NullPointerException ex) {
      Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public long getUptime() {
    long l = -1;
    try {

      l = (Long) getServerConnection().getAttribute(JMXConnection.RUNTIME, "Uptime");

    } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {
      Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
    return l;
  }

  public static float inSecsTimestamp(long ts) {
    return ((float) ts) / 1000;

  }

  boolean isUseAuthentication() {
    return !userName.isEmpty();
  }

  public CompositeData[] getThreads(long[] thIds, int stackTraceEntriesNo) throws Exception {
    String[] signature = {"[J", "int"};
    Object[] params = {thIds, stackTraceEntriesNo};
    return (CompositeData[]) server.invoke(THREADING, "getThreadInfo", params, signature);
  }

  public long[] getThreadsCPU(long[] thIds) throws Exception {

    String[] signature = {"[J"};
    Object[] params = {thIds};
    return (long[]) server.invoke(THREADING, "getThreadCpuTime", params, signature);
  }

  public long getThreadCPU(long thId) throws Exception {

    String[] signature = {"long"};
    Object[] params = {thId};
    return (long) (Long) server.invoke(THREADING, "getThreadCpuTime", params, signature);

  }

  public long[] getThreadsAllocBytes(long[] thIds) throws Exception {

    String[] signature = {"[J"};
    Object[] params = {thIds};
    long[] threadsAllocBytes = (long[]) server.invoke(THREADING, "getThreadAllocatedBytes", params, signature);
    return threadsAllocBytes;
  }

  private boolean supportAdvFeatures = true;

  public boolean isJava16_25andAbove() {
    return supportAdvFeatures;
  }

  public void unsetJava16_25andAbove() {
    supportAdvFeatures = false;
  }

  public static Class addToolsJar() {
    try {
      return com.sun.tools.attach.VirtualMachine.class;
    } catch (Throwable t) {
      System.err.println("tools.jar not in class path from" + System.getProperty("java.home"));
      File toolsJar = new File(System.getProperty("java.home") + "/lib/tools.jar"); //when jdk
      System.err.println("try:" + toolsJar);
      if (toolsJar.exists()) {
        addURL(toolsJar);
        System.err.println(toolsJar);
      } else {
        toolsJar = new File(System.getProperty("java.home") + "/../lib/tools.jar"); // when jre part of jdk
        System.out.println("try:" + toolsJar);
        if (toolsJar.exists()) {
          addURL(toolsJar);
          System.err.println("Found:" + toolsJar);
        } else {
          System.err.println("Unable to locate tools.jar pls add it to classpath");
        }
      }
    }
    return com.sun.tools.attach.VirtualMachine.class;


  }

  public long[] getThreadIds() throws Exception {

    long[] thIds = (long[]) getServerConnection().getAttribute(THREADING, "AllThreadIds");
    return thIds;

  }

  public static void addURL(File file) throws RuntimeException {
    try {
      URL url = file.toURL();
      URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Class clazz = URLClassLoader.class;

      // Use reflection
      Method method = clazz.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(classLoader, new Object[]{url});

    } catch (Exception e) {
      throw new RuntimeException(e);

    }
  }


  public long[] getThreadIds(JMXConnection server) throws Exception {

    long[] thIds = (long[]) server.getServerConnection().getAttribute(THREADING, "AllThreadIds");
    return thIds;

  }

}
