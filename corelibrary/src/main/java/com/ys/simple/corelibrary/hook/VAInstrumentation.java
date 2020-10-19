package com.ys.simple.corelibrary.hook;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ys.simple.corelibrary.PluginManager;

/**
 * Created by yinshan on 2020/10/14.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback{

  private static final String TAG = "VAInstrumentation";

  private PluginManager mPluginManager;
  private Instrumentation mBase;

  public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }

  @Override
  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode) {
    injectIntent(intent);
    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode);
  }

  @Override
  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
    injectIntent(intent);
    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  @Override
  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment target, Intent intent, int requestCode, Bundle options) {
    injectIntent(intent);
    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  @Override
  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
    injectIntent(intent);
    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  protected void injectIntent(Intent intent){
    mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
    if(intent.getComponent() != null){
      Log.i(TAG, String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
          intent.getComponent().getClassName()));
      this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
    }
  }


  @Override
  public boolean handleMessage(@NonNull Message msg) {
    return false;
  }
}
