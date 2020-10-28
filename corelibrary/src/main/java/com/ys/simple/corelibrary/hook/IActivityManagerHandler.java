package com.ys.simple.corelibrary.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.delegate.LocalService;
import com.ys.simple.corelibrary.utils.Constants;

/**
 * Created by yinshan on 2020/8/11.
 */
public class IActivityManagerHandler implements InvocationHandler {

  private static final String TAG = "IActivityManagerHandler";

  PluginManager mPluginManager;
  Object mBase;

  public IActivityManagerHandler(PluginManager pluginManager, Object base) {
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Log.d(TAG, "invoke " + method.getName());
    // 如果是启动Activity，替换Intent
    if("startActivity".equals(method.getName())){
      hookStartActivity(args);
      return method.invoke(mBase, args);
    }else if("startService".equals(method.getName())){
      // 将所有的操作进行拦截，都改为startService，然后统一在onStartCommond中分发
    }

    return method.invoke(mBase, args);
  }

  // 替换为占位Activity
  private void hookStartActivity(Object[] args){
    int index = getIntentIndex(args);
    Intent intent = (Intent) args[index];

    // 将插件的隐式intent转化为显式intent，host的intent不变
    // mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
    ComponentName component = intent.getComponent();
    // component为空，且非host
    if(component == null){
      // host resolveinfo 为null
      ResolveInfo info = mPluginManager.resolveActivity(intent);
      if(info != null && info.activityInfo != null){
        component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        intent.setComponent(component);
      }
    }

    // Component不为空，且非host
    if(intent.getComponent() != null
        && !intent.getComponent().getPackageName().equals(mPluginManager.getHostContext().getPackageName())){
      Intent newIntent = new Intent();
      String stubPackage = mPluginManager.getHostContext().getPackageName();
      // 占位Activity的名称
      ComponentName componentName = new ComponentName(stubPackage,
          mPluginManager.getComponentsHandler().getStubActivityClass(intent));
      newIntent.setComponent(componentName);

      // 将之前的intent存起来
      newIntent.putExtra(Constants.KEY_IS_PLUGIN, true);
      newIntent.putExtra(Constants.EXTRA_TARGET_INTENT, intent);
      args[index] = newIntent;
      Log.d(TAG, "hook succeed");
    }
  }

  private void hookStartService(Method method, Object[] args){
    int index = getIntentIndex(args);
    Intent intent = (Intent) args[index];
    ResolveInfo resolveInfo = this.mPluginManager.resolveService(intent);
    if(resolveInfo == null || resolveInfo.serviceInfo == null){
      return;
    }

    // 替换intent
    Intent newIntent = new Intent();
    newIntent.setClass(mPluginManager.getHostContext(), LocalService.class);
    newIntent.putExtra(Constants.KEY_IS_PLUGIN, true);
    newIntent.putExtra(Constants.KEY_TARGET_SERVICE, intent);
    args[index] = newIntent;
  }

  private int getIntentIndex(Object[] args){
    int index = 0;
    for(int i = 0; i < args.length; i++){
      if(args[i] instanceof Intent){
        index = i;
        break;
      }
    }
    return index;
  }
}
