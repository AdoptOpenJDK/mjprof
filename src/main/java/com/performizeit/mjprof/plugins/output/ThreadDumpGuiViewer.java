package com.performizeit.mjprof.plugins.output;

import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ProfileVisitor;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.parser.ThreadInfoProps;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by life on 27/10/14.
 */
public class ThreadDumpGuiViewer extends JPanel implements TreeSelectionListener {
  ThreadDump toDisplay;
  private final JTree tree;

  //Optionally play with line styles.  Possible values are
  //"Angled" (the default), "Horizontal", and "None".
  private static boolean playWithLineStyle = false;
  private static String lineStyle = "Horizontal";

  //Optionally set the look and feel.
  private static boolean useSystemLookAndFeel = false;

  public ThreadDumpGuiViewer(ThreadDump toDisplay) {
    super(new GridLayout(1, 0));
    this.toDisplay = toDisplay;
    //Create the nodes.
    DefaultMutableTreeNode top =
      new DefaultMutableTreeNode("Thread Dump");
    createNodes(top);

    //Create a tree that allows one selection at a time.
    tree = new JTree(top);
    tree.getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);

    //Listen for when the selection changes.
    tree.addTreeSelectionListener(this);
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
    if (playWithLineStyle) {
      System.out.println("line style = " + lineStyle);
      tree.putClientProperty("JTree.lineStyle", lineStyle);
    }

    //Create the scroll pane and add the tree to it.
    JScrollPane treeView = new JScrollPane(tree);


    Dimension minimumSize = new Dimension(1000, 800);
    this.setMinimumSize(minimumSize);
    this.setSize(minimumSize);
    treeView.setMaximumSize(minimumSize);
    treeView.setSize(minimumSize);


    //Add the split pane to this panel.
    add(treeView);
  }

  /**
   * Required by TreeSelectionListener interface.
   */
  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
      tree.getLastSelectedPathComponent();

    if (node == null) return;

    node.getUserObject();

  }


  private void createNodes(DefaultMutableTreeNode top) {
    for (ThreadInfo ti : toDisplay.getThreadInfos()) {
      DefaultMutableTreeNode tgui = createThreadProfile(ti);

      top.add(tgui);
    }

  }

  static class Vis implements ProfileVisitor {
    HashMap<Integer, DefaultMutableTreeNode> parents = new HashMap<>();

    public Vis(DefaultMutableTreeNode thread) {
      parents.put(0, thread);
    }

    @Override
    public void visit(SFNode stackframe, int level) {
      if (level == 0) return;
      DefaultMutableTreeNode parent = parents.get(level - 1);
      DefaultMutableTreeNode me = new DefaultMutableTreeNode("[" + stackframe.getCount() + "] " + stackframe.getStackFrame());
      parent.add(me);
      parents.put(level, me);

    }
  }

  private DefaultMutableTreeNode createThreadProfile(ThreadInfo ti) {
    Profile p = (Profile) ti.getVal(ThreadInfoProps.STACK);
    DefaultMutableTreeNode thread = new DefaultMutableTreeNode(p.getCount() + " " + ti.getVal(ThreadInfoProps.NAME) + " " + ti.getVal(ThreadInfoProps.STATE));


    p.visit(new Vis(thread));
    return thread;
  }

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event dispatch thread.
   */
  public static void createAndShowGUI(ThreadDump td, String title,boolean show) {
    if (useSystemLookAndFeel) {
      try {
        UIManager.setLookAndFeel(
          UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        System.err.println("Couldn't use system look and feel.");
      }
    }

    //Create and set up the window.
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Add content to the window.
    frame.add(new ThreadDumpGuiViewer(td));

    //Display the window.
    frame.pack();
    frame.setVisible(show);
  }
}

