package com.ys.simple.corelibrary.hook;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.plugin.LoadedPlugin;
import com.ys.simple.corelibrary.utils.Constants;
import com.ys.simple.corelibrary.utils.Reflector;

/**
 * Created by yinshan on 2020/10/14.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback{

  private static final String TAG = "VAInstrumentation";

  // 缓存
  protected final ArrayList<WeakReference<Activity>> mActivities = new ArrayList<>();

  private PluginManager mPluginManager;
  private Instrumentation mBase;

  private final int EXECUTE_TRANSACTION = 159;
  private int what;

  public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }

  @Override
  public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    try{
      cl.loadClass(className);
      Log.i(TAG, String.format("newActivity[%s]", className));
    }catch (ClassNotFoundException e){
      ComponentName component = intent.getComponent();

      if(component != null){
        String targetClassName = component.getClassName();
        LoadedPlugin loadedPlugin = mPluginManager.getLoadedPlugin(component.getPackageName());
        if(loadedPlugin != null){
          Activity activity = mBase.newActivity(loadedPlugin.getClassLoader(), targetClassName, intent);
          activity.setIntent(intent);
          Reflector.QuietReflector.with(activity).field("mResources").set(loadedPlugin.getResources());
          return activity;
        }
      }
    }
    // Resource替换回来
    Activity activity = super.newActivity(cl, className, intent);
    Reflector.QuietReflector.with(activity).field("mResources").set(mPluginManager.getHostContext().getResources());
    return activity;
  }

//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
//  }
//
//  @Override
//  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
//    injectIntent(intent);
//    return mBase.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
//  }

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
    Log.d(TAG, "handle Message " + msg.what);
    if(what == 0){
      try{
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        Handler handler = Reflector.with(activityThread).field("mH").get();
        what = Reflector.with(handler).field("EXECUTE_TRANSACTION").get();
      }catch (Reflector.ReflectedException e){
        e.printStackTrace();
        what = EXECUTE_TRANSACTION;
      }
    }
    if(msg.what == what){
      handleLaunchActivity(msg);
    }
    return false;
  }

  private void handleLaunchActivity(Message msg){
    try{
      List list = Reflector.with(msg.obj).field("mActivityCallbacks").get();
      if(list == null || list.isEmpty()) return;
      Class<?> launchActivityItemClz = Class.forName("android.app.servertransaction.LaunchActivityItem");
      if(launchActivityItemClz.isInstance(list.get(0))) {
        Intent intent = Reflector.with(list.get(0)).field("mIntent").get();
        Intent target = intent.getParcelableExtra(Constants.EXTRA_TARGET_INTENT);
        intent.setComponent(target.getComponent());
      }
    }catch (Reflector.ReflectedException e){
      e.printStackTrace();
    }catch (ClassNotFoundException e){
      e.printStackTrace();
    }
  }
}
