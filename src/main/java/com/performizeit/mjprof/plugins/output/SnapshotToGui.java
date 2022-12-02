package com.performizeit.mjprof.plugins.output;


/**
 * This application that requires the following additional files:
 * TreeDemoHelp.html
 * arnold.html
 * bloch.html
 * chan.html
 * jls.html
 * swingtutorial.html
 * tutorial.html
 * tutorialcont.html
 * vm.html
 */

import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.plugin.types.Outputer;
import com.performizeit.plumbing.PipeHandler;


@SuppressWarnings("unused")
@Plugin(name = "gui", params = {@Param(type = String.class, value = "title", optional = true, defaultValue = ""),
    @Param(type = int.class, value = "maxInvocations", optional = true, defaultValue = "10")},
    category = PluginCategory.OUTPUTER,
    description = "Displays current thread dump in a GUI window")
public class SnapshotToGui implements Outputer, PipeHandler {
  private int maxInvocations;
  private int curInvocation;
  private String title;
  private int instanceNumber;
  private static int snapshotToGuiInstanceNumber = 1;

  public SnapshotToGui(String title, int maxInvocations) {

    this.maxInvocations = maxInvocations;
    curInvocation = 0;
    this.title = title;
    this.instanceNumber = snapshotToGuiInstanceNumber;
    snapshotToGuiInstanceNumber++;
  }

  @Override
  public Object handleMsg(final Object msg) {
    curInvocation++;
    String title1 = title;
    if (title1.isEmpty()) title1 = "MJProf " + Thread.currentThread().getName();

    if (curInvocation <= maxInvocations) {
      final int cinv = curInvocation;
      final String tit = title1;
      final ThreadDump msg2 = (ThreadDump) msg;
      ThreadDumpGuiViewer.createAndShowGUI(msg2, tit + ":" + cinv);
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {


        }
      });
    }
    return msg;
  }

  @Override
  public Object handleDone() {
    return null;
  }
}
