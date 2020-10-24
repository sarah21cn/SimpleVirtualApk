package com.ys.simple.corelibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;

import com.ys.simple.corelibrary.hook.ActivityThreadHandlerCallback;
import com.ys.simple.corelibrary.hook.IActivityManagerHandler;
import com.ys.simple.corelibrary.hook.VAInstrumentation;
import com.ys.simple.corelibrary.plugin.LoadedPlugin;
import com.ys.simple.corelibrary.utils.ComponentsHandler;
import com.ys.simple.corelibrary.utils.Reflector;

/**
 * Created by yinshan on 2020/10/13.
 * Plugin的管理类
 */
public class PluginManager {

  private static PluginManager sInstance;

  private Context mContext;
  private ComponentsHandler mComponentsHandler;

  private Instrumentation mInstrumentation;

  // 包名 : LoadedPlugin
  // 所以插件的包名不能有重复
  private final Map<String, LoadedPlugin> mPlugins = new ConcurrentHashMap<>();
  private List<Callback> mCallbacks = new ArrayList<>();

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

    mComponentsHandler = new ComponentsHandler(this);
  }

  private void hookCurrentProcess() throws Exception{
    hookInstrumentationAndHandler();
    hookActivityManagerService();
    hookActivityThreadCallback();
  }

  private void hookInstrumentationAndHandler() throws Exception{
    ActivityThread activityThread = ActivityThread.currentActivityThread();
    Instrumentation baseInstrumentation = activityThread.getInstrumentation();
    VAInstrumentation vaInstrumentation = new VAInstrumentation(this, baseInstrumentation);
    Reflector.with(activityThread).field("mInstrumentation").set(vaInstrumentation);

//    Handler mainHandler = Reflector.with(activityThread).method("getHandler").call();
//    Reflector.with(mainHandler).field("mCallback").set(vaInstrumentation);
    this.mInstrumentation = vaInstrumentation;
  }


  public void hookActivityManagerService() throws Exception{
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
        amsObj.getClass().getInterfaces(), new IActivityManagerHandler(this, amsObj));
    Reflector.with(gDefaultObj).field("mInstance").set(proxy);
  }

  public void hookActivityThreadCallback() throws Exception {
    ActivityThread activityThread = ActivityThread.currentActivityThread();
    Handler handler = Reflector.with(activityThread).field("mH").get();
    Reflector.with(handler).field("mCallback").set(new ActivityThreadHandlerCallback(handler));
  }

  public void loadPlugin(File apk) throws Exception{
    if(apk == null){
      throw new IllegalArgumentException("error: apk is null");
    }
    if(!apk.exists()){
      // throw the FileNotFoundException by opening a stream.
      InputStream in = new FileInputStream(apk);
      in.close();
    }

    LoadedPlugin loadedPlugin = new LoadedPlugin(this, this.mContext, apk);
    if(loadedPlugin == null){
      throw new IllegalArgumentException("error: loaded plugin is null");
    }

    this.mPlugins.put(loadedPlugin.getPackageName(), loadedPlugin);
    synchronized (mCallbacks){
      for(int i = 0; i < mCallbacks.size(); i++){
        mCallbacks.get(i).onAddedLoadedPlugin(loadedPlugin);
      }
    }
  }

  public LoadedPlugin getLoadedPlugin(String packageName){
    return this.mPlugins.get(packageName);
  }

  public List<LoadedPlugin> getAllLoadedPlugins(){
    List<LoadedPlugin> list = new ArrayList<>();
    list.addAll(mPlugins.values());
    return list;
  }

  public void addCallback(Callback callback){
    if(callback == null){
      return;
    }
    synchronized (mCallbacks){
      if(mCallbacks.contains(callback)){
        throw new RuntimeException("Already added " + callback + "!");
      }
      mCallbacks.add(callback);
    }
  }

  public void removeCallback(Callback callback){
    synchronized (mCallbacks){
      mCallbacks.remove(callback);
    }
  }

  public Context getHostContext(){
    return this.mContext;
  }

  public ComponentsHandler getComponentsHandler(){
    return mComponentsHandler;
  }

  public ResolveInfo resolveActivity(Intent intent){
    for(LoadedPlugin loadedPlugin : mPlugins.values()){
      ResolveInfo resolveInfo = loadedPlugin.resolveActivity(intent);
      if(null != resolveInfo){
        return resolveInfo;
      }
    }
    return null;
  }

  public interface Callback{
    void onAddedLoadedPlugin(LoadedPlugin plugin);
  }
}
