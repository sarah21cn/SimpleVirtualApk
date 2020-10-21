package com.ys.simple.corelibrary.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.activity.StubActivity;
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
      Intent intent;
      int index = 0;
      for(int i = 0; i < args.length; i++){
        if(args[i] instanceof Intent){
          index = i;
          break;
        }
      }
      intent = (Intent) args[index];

      // 将隐式intent转化为显式intent
      mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);

      if(intent.getComponent() != null
          && !intent.getComponent().getPackageName().equals(mPluginManager.getHostContext().getPackageName())){
        Intent newIntent = new Intent();
        String stubPackage = mPluginManager.getHostContext().getPackageName();
        ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
        newIntent.setComponent(componentName);

        // 将之前的intent存起来
        newIntent.putExtra(Constants.EXTRA_TARGET_INTENT, intent);
        args[index] = newIntent;
        Log.d(TAG, "hook succeed");
      }

      // todo
//      mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
//      if(intent.getComponent() != null){
//        Log.i(TAG, String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
//            intent.getComponent().getClassName()));
//        this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
//      }
      return method.invoke(mBase, args);
    }

    return method.invoke(mBase, args);
  }
}
