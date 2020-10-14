package com.ys.simple.corelibrary.plugin;

import java.io.File;
import java.util.Map;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;

import com.ys.simple.corelibrary.PluginManager;

/**
 * Created by yinshan on 2020/10/14.
 */
public class LoadedPlugin {

  private static final String TAG = "LoadedPlugin";

  protected PluginManager mPluginManager;
  protected Context mHostContext;
  protected Context mPluginContext;
  //
//  protected final PackageParser.Package mPackage;
//  protected final PackageInfo mPackageInfo;
  protected Resources mResources;
  protected ClassLoader mClassLoader;
  // packagemanager

  protected Map<ComponentName, ActivityInfo> mActivityInfos;
  protected Map<ComponentName, ServiceInfo> mServiceInfos;
  protected Map<ComponentName, ActivityInfo> mReceiverInfos;
  protected Map<ComponentName, ProviderInfo> mProviderInfos;

  protected Application mApplication;

  public LoadedPlugin(PluginManager pluginManager, Context context, File apk) throws Exception{
    this.mPluginManager = pluginManager;
    this.mHostContext = context;

//    this.mPackage =
  }
}
