package com.ys.simple.corelibrary.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.plugin.LoadedPlugin;
import com.ys.simple.corelibrary.utils.Reflector;

/**
 * Created by yinshan on 2020/10/14.
 */
public class VAInstrumentation extends Instrumentation {

  private static final String TAG = "VAInstrumentation";

  private PluginManager mPluginManager;
  private Instrumentation mBase;

  public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }

  @Override
  public Activity newActivity(ClassLoader cl, String className, Intent intent)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    try {
      cl.loadClass(className);
      Log.i(TAG, String.format("newActivity[%s]", className));
    } catch (ClassNotFoundException e) {
      ComponentName component = intent.getComponent();

      if (component != null) {
        String targetClassName = component.getClassName();
        LoadedPlugin loadedPlugin = mPluginManager.getLoadedPlugin(component.getPackageName());
        if (loadedPlugin != null) {
          Activity activity =
              mBase.newActivity(loadedPlugin.getClassLoader(), targetClassName, intent);
          activity.setIntent(intent);
          Reflector.QuietReflector.with(activity).field("mResources")
              .set(loadedPlugin.getResources());
          return activity;
        }
      }
    }
    return super.newActivity(cl, className, intent);
  }

//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
//  Activity target, Intent intent, int requestCode) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
//  Activity target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode,
//    options);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
//  Fragment target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode,
//    options);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
//  String target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode,
//    options);
//  }

  protected void injectIntent(Intent intent) {
    mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
    if (intent.getComponent() != null) {
      Log.i(TAG, String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
          intent.getComponent().getClassName()));
      this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
    }
  }
}
