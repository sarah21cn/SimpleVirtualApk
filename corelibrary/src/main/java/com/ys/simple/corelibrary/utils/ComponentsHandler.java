package com.ys.simple.corelibrary.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.activity.StubActivity;

/**
 * Created by yinshan on 2020/10/19.
 */
public class ComponentsHandler {

  private static final String TAG = "ComponentsHandler";

  // host context
  private Context mContext;
  private PluginManager mPluginManager;

  public ComponentsHandler(PluginManager pluginManager) {
    mPluginManager = pluginManager;
    mContext = pluginManager.getHostContext();
  }

  public Intent transformIntentToExplicitAsNeeded(Intent intent){
    Log.d(TAG, "transform intent, context packageName: " + mContext.getPackageName());
    ComponentName component = intent.getComponent();
    // mContext为hostContext，？？？
    if(component == null || component.getPackageName().equals(mContext.getPackageName())){
      ResolveInfo info = mPluginManager.resolveActivity(intent);
      if(info != null && info.activityInfo != null){
        component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        intent.setComponent(component);
      }
    }
    return intent;
  }

  public void markIntentIfNeeded(Intent intent){
    if(null == intent.getComponent()){
      return;
    }
    String targetPackageName = intent.getComponent().getPackageName();
    String targetClassName = intent.getComponent().getClassName();
    // host intent不需要修改 && 插件已加载
    if(!targetPackageName.equals(mContext.getPackageName())
        && mPluginManager.getLoadedPlugin(targetPackageName) != null){
      intent.putExtra(Constants.KEY_IS_PLUGIN, true);
      intent.putExtra(Constants.KEY_TARGET_PACKAGE, targetPackageName);
      intent.putExtra(Constants.KEY_TARGET_ACTIVITY, targetClassName);
      dispatchStubActivity(intent);
    }
  }

  private void dispatchStubActivity(Intent intent){
    ComponentName componentName = new ComponentName(mContext.getPackageName(), StubActivity.class.getName());
    intent.setComponent(componentName);
  }
}
