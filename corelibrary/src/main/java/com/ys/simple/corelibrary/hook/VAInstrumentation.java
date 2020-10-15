package com.ys.simple.corelibrary.hook;

import android.app.Instrumentation;

import com.ys.simple.corelibrary.PluginManager;

/**
 * Created by yinshan on 2020/10/14.
 */
public class VAInstrumentation extends Instrumentation {

  private PluginManager mPluginManager;
  private Instrumentation mBase;

  public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }


}
