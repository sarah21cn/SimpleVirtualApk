package com.ys.simple.corelibrary.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.utils.Constants;
import com.ys.simple.corelibrary.utils.PackageParserCompat;
import com.ys.simple.corelibrary.utils.Reflector;

import dalvik.system.DexClassLoader;

/**
 * Created by yinshan on 2020/10/14.
 */
public class LoadedPlugin {

  private static final String TAG = "LoadedPlugin";

  protected PluginManager mPluginManager;
  protected Context mHostContext;
  protected Context mPluginContext;
  //
  protected String mLocation;
  protected PackageParser.Package mPackage;
  protected PackageInfo mPackageInfo;
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
    this.mLocation = apk.getAbsolutePath();

    this.mPackage = PackageParserCompat.parsePackage(context, apk, PackageParser.PARSE_MUST_BE_APK);
    this.mPackage.applicationInfo.metaData = this.mPackage.mAppMetaData;

    this.mPackageInfo = new PackageInfo();
    this.mPackageInfo.applicationInfo = this.mPackage.applicationInfo;
    this.mPackageInfo.applicationInfo.sourceDir = apk.getAbsolutePath();
    this.mPackageInfo.packageName = this.mPackage.packageName;
    this.mPackageInfo.versionCode = this.mPackage.mVersionCode;
    this.mPackageInfo.versionName = this.mPackage.mVersionName;
    this.mPackageInfo.permissions = new PermissionInfo[0];

    this.mPluginContext = createPluginContext(context);
    this.mResources = createResources(context, this.mPackage.packageName, apk);
    this.mClassLoader = createClassLoader(context, apk, context.getClassLoader());

    Map<ComponentName, ActivityInfo> activityInfos = new HashMap<>();
    for(PackageParser.Activity activity : this.mPackage.activities){
      activity.info.metaData = activity.metaData;
      activityInfos.put(activity.getComponentName(), activity.info);
    }
    mActivityInfos = Collections.unmodifiableMap(activityInfos);
    this.mPackageInfo.activities = activityInfos.values().toArray(new ActivityInfo[activityInfos.size()]);

    // ...
  }

  private PluginContext createPluginContext(Context context){
    if(context == null){
      return new PluginContext(this);
    }
    return new PluginContext(this, context);
  }

  private Resources createResources(Context context, String packageName, File apk) throws Exception{
    if(Constants.COMBINE_RESOURCES){
      return ResourcesManager.createResources(context, packageName, apk);
    }else{
      Resources hostResource = context.getResources();
      AssetManager assetManager = createAssetManager(apk);
      return new Resources(assetManager, hostResource.getDisplayMetrics(), hostResource.getConfiguration());
    }
  }

  private AssetManager createAssetManager(File apk) throws Exception{
    // 创建一个AssetManager
    AssetManager assetManager = AssetManager.class.newInstance();
    Reflector.with(assetManager).method("addAssetPath", String.class).call(apk.getAbsolutePath());
    return assetManager;
  }

  private ClassLoader createClassLoader(Context context, File apk, ClassLoader parent) throws Exception{
    File dexOutputDir = getDir(context, Constants.OPTIMIZE_DIR);
    String dexOutputPath = dexOutputDir.getAbsolutePath();
    // 创建一个DexClassLoader
    // 此处也可以自定义ClassLoader，重写findClass等方法
    DexClassLoader loader = new DexClassLoader(apk.getAbsolutePath(), dexOutputPath, null, parent);
    return loader;
  }

  protected File getDir(Context context, String name) {
    return context.getDir(name, Context.MODE_PRIVATE);
  }

  public ResolveInfo resolveActivity(Intent intent){
    List<ResolveInfo> query = queryIntentActivities(intent);
    if(query == null || query.isEmpty()){
      return null;
    }
    return query.get(0);
  }

  public List<ResolveInfo> queryIntentActivities(Intent intent){
    ComponentName component = intent.getComponent();
    List<ResolveInfo> resolveInfos = new ArrayList<>();
    // 插件的ContentResolver
    ContentResolver resolver = mPluginContext.getContentResolver();

    for(PackageParser.Activity activity : mPackage.activities){
      if(match(activity, component)){
        // new一个ResolveInfo，填充ActivityInfo
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.activityInfo = activity.info;
        resolveInfos.add(resolveInfo);
      }else if(component == null){
        // 使用intentInfo去match
        for(PackageParser.ActivityIntentInfo intentInfo : activity.intents){
          if(intentInfo.match(resolver, intent, true, TAG) >= 0){
            ResolveInfo resolveInfo = new ResolveInfo();
            resolveInfo.activityInfo = activity.info;
            resolveInfos.add(resolveInfo);
          }
        }
      }
    }
    return resolveInfos;
  }

  private boolean match(PackageParser.Component component, ComponentName target){
    ComponentName source = component.getComponentName();
    if(source == target) return true;
    if(source != null && target != null
        && source.getClassName().equals(target.getClassName())
        && (source.getPackageName().equals(target.getPackageName())
        // ???
        || mHostContext.getPackageName().equals(target.getPackageName()))){
      return true;
    }
    return false;
  }

  public PluginManager getPluginManager(){
    return this.mPluginManager;
  }

  public String getPackageName(){
    return this.mPackage.packageName;
  }

  public ClassLoader getClassLoader(){
    return mClassLoader;
  }

  public Resources getResources(){
    return mResources;
  }

  public String getLocation(){
    return mLocation;
  }

  public void updateResources(Resources newResources){
    this.mResources = newResources;
  }
}
