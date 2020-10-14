package com.ys.simple.corelibrary.hook;

import android.app.Instrumentation;

/**
 * Created by yinshan on 2020/10/14.
 */
public class VAInstrumentation extends Instrumentation {

  private Instrumentation mBase;

  public VAInstrumentation(Instrumentation base) {
    mBase = base;
  }
}
