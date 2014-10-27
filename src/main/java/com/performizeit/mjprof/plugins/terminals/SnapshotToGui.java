package com.performizeit.mjprof.plugins.terminals;


/**
 * This application that requires the following additional files:
 *   TreeDemoHelp.html
 *    arnold.html
 *    bloch.html
 *    chan.html
 *    jls.html
 *    swingtutorial.html
 *    tutorial.html
 *    tutorialcont.html
 *    vm.html
 */
        import com.performizeit.mjprof.api.Param;
        import com.performizeit.mjprof.api.Plugin;
        import com.performizeit.mjprof.parser.ThreadDump;
        import com.performizeit.mjprof.plugin.types.Outputer;
        import com.performizeit.plumbing.PipeHandler;

        import javax.swing.JEditorPane;
        import javax.swing.JFrame;
        import javax.swing.JPanel;
        import javax.swing.JScrollPane;
        import javax.swing.JSplitPane;
        import javax.swing.UIManager;

        import javax.swing.JTree;
        import javax.swing.tree.DefaultMutableTreeNode;
        import javax.swing.tree.TreeSelectionModel;
        import javax.swing.event.TreeSelectionEvent;
        import javax.swing.event.TreeSelectionListener;

        import java.io.*;
        import java.net.URL;
        import java.awt.Dimension;
        import java.awt.GridLayout;


@SuppressWarnings("unused")
@Plugin(name="gui", params ={}, description="Display current state in a window")
public class SnapshotToGui implements Outputer,PipeHandler {

    public SnapshotToGui() {
    }

    @Override public Object handleMsg(final Object msg) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ThreadDumpGuiViewer.createAndShowGUI((ThreadDump) msg);
            }
        });
        return msg;
    }
    @Override public Object handleDone() {
        return null;
    }
}