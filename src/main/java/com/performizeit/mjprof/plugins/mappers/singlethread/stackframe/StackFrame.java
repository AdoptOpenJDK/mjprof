package com.performizeit.mjprof.plugins.mappers.singlethread.stackframe;

/**
 * Created by life on 27/10/14.
 */
public class StackFrame {
  public static String SEVEN_SPACES = "       ";
  String lockFrame = "";
  String at = "";
  String packageName = "";
  String className = "";
  String methodName = "";
  String fileName = "";
  String lineNum = "";

  public StackFrame(String stringRep) {
    stringRep = stringRep.trim();
    if (stringRep.startsWith("-")) {
      lockFrame = stringRep;
      return;
    }
    int atStart = stringRep.indexOf("at ");

    if (atStart == 0) {
      at = stringRep.substring(0, atStart + 3);
      stringRep = stringRep.substring(atStart + 3);
    }


    int fnStart = stringRep.indexOf("(");

    String pkgClsMthd;

    if (fnStart < 0) {
      fileName = "";
      lineNum = "";
      pkgClsMthd = stringRep;
    } else {
      String fnln = stringRep.substring(fnStart + 1, stringRep.length() - 1);
      String[] ss = fnln.split(":");

      if (ss.length == 2) {
        fileName = ss[0];
        lineNum = ss[1];
      } else {
        fileName = fnln;
      }
      pkgClsMthd = stringRep.substring(0, fnStart);
    }


    methodName = pkgClsMthd.substring(pkgClsMthd.lastIndexOf(".") + 1);
    pkgClsMthd = pkgClsMthd.substring(0, pkgClsMthd.lastIndexOf("."));
    if (pkgClsMthd.contains(".")) {   // class name contains package
      className = pkgClsMthd.substring(pkgClsMthd.lastIndexOf(".") + 1);
      packageName = pkgClsMthd.substring(0, pkgClsMthd.lastIndexOf("."));
    } else {
      className = pkgClsMthd;
    }
  }

  @Override
  public String toString() {
    // TODO to string builder
    String build = "";
    build += SEVEN_SPACES;
    if (!lockFrame.isEmpty()) return build + lockFrame;
    build += at;
    if (packageName.length() > 0) build += packageName;
    if (className.length() > 0) {
      if (packageName.length() > 0) build += ".";
      build += className;
    }
    if (methodName.length() > 0) {
      if (packageName.length() > 0 || className.length() > 0) build += ".";
      build += methodName;
    }
    if (fileName.length() > 0 || lineNum.length() > 0) {
      build += "(";
      if (fileName.length() > 0) build += fileName;
      if (lineNum.length() > 0 && fileName.length() > 0) build += ":";
      if (lineNum.length() > 0) build += lineNum;
      build += ")";
    }
    return build;
  }

  public String getAt() {
    return at;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getFileName() {
    return fileName;
  }

  public String getLineNum() {
    return lineNum;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setLineNum(String lineNum) {
    this.lineNum = lineNum;
  }

  public void setAt(String at) {
    this.at = at;
  }
}
