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
import java.net.URL;
import java.util.HashMap;

/**
 * Created by life on 27/10/14.
 */
public class ThreadDumpGuiViewer  extends JPanel implements TreeSelectionListener{
    ThreadDump toDisplay;


    private JEditorPane htmlPane;
    private JTree tree;
    private URL helpURL;
    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";

    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;

    public ThreadDumpGuiViewer(ThreadDump toDisplay)  {
        super(new GridLayout(1,0));
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

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);



        Dimension minimumSize = new Dimension(1000, 800);
        this.setMinimumSize(minimumSize);
        this.setSize(new Dimension(1000, 800));


        //Add the split pane to this panel.
        add(treeView);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
  /*      if (node.isLeaf()) {
            BookInfo book = (BookInfo)nodeInfo;
            displayURL(book.bookURL);
            if (DEBUG) {
                System.out.print(book.bookURL + ":  \n    ");
            }
        } else {
            displayURL(helpURL);
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }  */
    }

    private class BookInfo {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String filename) {
            bookName = book;
            bookURL = getClass().getResource(filename);
            if (bookURL == null) {
                System.err.println("Couldn't find file: "
                        + filename);
            }
        }

        public String toString() {
            return bookName;
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        for (ThreadInfo ti : toDisplay.getStacks())  {
            DefaultMutableTreeNode tgui = createThreadProfile(ti);
            top.add(tgui) ;
        }

    }
    class Vis implements ProfileVisitor {
        HashMap<Integer,DefaultMutableTreeNode> parents = new HashMap<Integer, DefaultMutableTreeNode>();

        public Vis(DefaultMutableTreeNode thread) {
             parents.put(0,thread);
        }

        @Override
        public void visit(SFNode stackframe, int level) {
            if (level ==0) return;
            DefaultMutableTreeNode parent = parents.get(level-1);
            DefaultMutableTreeNode me = new DefaultMutableTreeNode(stackframe.getCount()+" "+stackframe.getStackFrame()) ;
            parent.add(me);
            parents.put(level,me);

        }
    }
    private DefaultMutableTreeNode createThreadProfile( ThreadInfo ti) {
        Profile p = (Profile) ti.getVal(ThreadInfoProps.STACK);
        DefaultMutableTreeNode thread = new DefaultMutableTreeNode(p.getCount()+" "+ti.getVal(ThreadInfoProps.NAME));


        p.visit(new Vis(thread));
        return thread;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI(ThreadDump td, String title) {
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
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(null,"mjprof test");
            }
        });
    }
}

