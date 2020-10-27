package com.ys.simple.corelibrary.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
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
      Log.i(TAG, className + " not found");
      ComponentName component = intent.getComponent();

      if (component != null) {
        String targetClassName = component.getClassName();
        LoadedPlugin loadedPlugin = mPluginManager.getLoadedPlugin(component.getPackageName());
        if (loadedPlugin != null) {
          Activity activity =
              mBase.newActivity(loadedPlugin.getClassLoader(), targetClassName, intent);
          activity.setIntent(intent);
          // 替换插件Activity的Resources，如果不替换，插件会找不到资源
          Reflector.QuietReflector.with(activity).field("mResources").set(loadedPlugin.getResources());
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

  @Override
  public Application newApplication(ClassLoader cl, String className, Context context)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    return mBase.newApplication(cl, className, context);
  }


}
