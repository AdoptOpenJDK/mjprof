package com.performizeit.mjprof.plugins.output;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.PluginCategory;

@SuppressWarnings("unused")
@Plugin(name = "stdout",
    params = {},
    category = PluginCategory.OUTPUTER,
    description = "Writes current stream of thread dumps to stdout")
public class SnapshotToStdout extends SnapshotToPrintStream {

  public SnapshotToStdout() {
    super(null);
  }
}
