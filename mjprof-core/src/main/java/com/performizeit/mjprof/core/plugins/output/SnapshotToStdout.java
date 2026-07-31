package com.performizeit.mjprof.core.plugins.output;

import com.performizeit.mjprof.core.api.Plugin;
import com.performizeit.mjprof.core.api.PluginCategory;

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
