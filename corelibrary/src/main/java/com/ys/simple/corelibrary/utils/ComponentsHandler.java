package com.ys.simple.corelibrary.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.activity.StubActivity;
import com.ys.simple.corelibrary.plugin.LoadedPlugin;
import com.ys.simple.corelibrary.plugin.StubActivityInfo;

/**
 * Created by yinshan on 2020/10/19.
 */
public class ComponentsHandler {

  private static final String TAG = "ComponentsHandler";

  // host context
  private Context mContext;
  private PluginManager mPluginManager;
  private StubActivityInfo mStubActivityInfo = new StubActivityInfo();

  public ComponentsHandler(PluginManager pluginManager) {
    mPluginManager = pluginManager;
    mContext = pluginManager.getHostContext();
  }

  public Intent transformIntentToExplicitAsNeeded(Intent intent){
    Log.d(TAG, "transform intent, context packageName: " + mContext.getPackageName());
    ComponentName component = intent.getComponent();
    // component为空，且非host
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
      intent.putExtra(Constants.EXTRA_TARGET_INTENT, intent);
      dispatchStubActivity(intent);
    }
  }

  private void dispatchStubActivity(Intent intent){
    ComponentName componentName = new ComponentName(mContext.getPackageName(), StubActivity.class.getName());
    intent.setComponent(componentName);
  }

  public String getStubActivityClass(Intent intent){
    final ComponentName componentName = intent.getComponent();
    LoadedPlugin loadedPlugin = mPluginManager.getLoadedPlugin(componentName.getPackageName());
    ActivityInfo activityInfo = loadedPlugin.getActivityInfo(componentName);
    if(activityInfo != null){
      int launchMode = activityInfo.launchMode;
      return mStubActivityInfo.getStubActivity(componentName.getClassName(), launchMode, null);
    }
    return StubActivity.class.getName();
  }
}
