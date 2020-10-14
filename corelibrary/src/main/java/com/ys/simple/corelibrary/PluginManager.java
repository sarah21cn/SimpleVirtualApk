package com.ys.simple.corelibrary;

import java.lang.reflect.Proxy;

import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.ys.simple.corelibrary.hook.ActivityThreadHandlerCallback;
import com.ys.simple.corelibrary.hook.IActivityManagerHandler;
import com.ys.simple.corelibrary.hook.VAInstrumentation;
import com.ys.simple.corelibrary.utils.Reflector;

/**
 * Created by yinshan on 2020/10/13.
 * Plugin的管理类
 */
public class PluginManager {

  private static PluginManager sInstance;

  private Context mContext;

  private Instrumentation mInstrumentation;

  public static PluginManager getInstance(Context context){
    if(sInstance == null){
      synchronized (PluginManager.class){
        if(sInstance == null){
          sInstance = new PluginManager(context);
        }
      }
    }
    return sInstance;
  }

  private PluginManager(Context context){
    this.mContext = context;
    try{
      hookCurrentProcess();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  private void hookCurrentProcess() throws Exception{
    hookInstrumentationAndHandler();
    hookActivityManagerService();
  }

  private void hookInstrumentationAndHandler() throws Exception{
    ActivityThread activityThread = ActivityThread.currentActivityThread();
    Instrumentation baseInstrumentation = activityThread.getInstrumentation();
    VAInstrumentation vaInstrumentation = new VAInstrumentation(baseInstrumentation);
    Reflector.with(activityThread).field("mInstrumentation").set(vaInstrumentation);

    Handler mainHandler = Reflector.with(activityThread).method("getHandler").call();
    Reflector.with(mainHandler).field("mCallback").set(new ActivityThreadHandlerCallback());
    this.mInstrumentation = vaInstrumentation;
  }

  public static void hookActivityManagerService() throws Exception{
    Object gDefaultObj = null;
    // API 29 及以后hook android.app.ActivityTaskManager.IActivityTaskManagerSingleton
    // API 26 及以后hook android.app.ActivityManager.IActivityManagerSingleton
    // API 25 以前hook android.app.ActivityManagerNative.gDefault
    if(Build.VERSION.SDK_INT >= 29){
      gDefaultObj = Reflector.on("android.app.ActivityTaskManager").field("IActivityTaskManagerSingleton").get();
    }else if(Build.VERSION.SDK_INT >= 26){
      gDefaultObj = Reflector.on("android.app.ActivityManager").field("IActivityManagerSingleton").get();
    }else{
      gDefaultObj = Reflector.on("android.app.ActivityManagerNative").field("gDefault").get();
    }
    Object amsObj = Reflector.with(gDefaultObj).field("mInstance").get();
    Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        amsObj.getClass().getInterfaces(), new IActivityManagerHandler(amsObj));
    Reflector.with(gDefaultObj).field("mInstance").set(proxy);
  }

  public Context getHostContext(){
    return this.mContext;
  }
}
