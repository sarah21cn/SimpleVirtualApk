package com.ys.simple.corelibrary.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.delegate.RemoteContentProvider;

/**
 * Created by yinshan on 2020/10/28.
 */
public class IContentProviderProxy implements InvocationHandler {

  private static final String TAG = "IContentProviderProxy";

  private PluginManager mPluginManager;
  private IContentProvider mBase;

  private IContentProviderProxy(PluginManager pluginManager, IContentProvider base){
    this.mPluginManager = pluginManager;
    this.mBase = base;
  }

  public static IContentProvider newInstance(PluginManager pluginManager, IContentProvider base){
    return (IContentProvider) Proxy.newProxyInstance(base.getClass().getClassLoader(),
        new Class[] {IContentProvider.class}, new IContentProviderProxy(pluginManager, base));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Log.d(TAG, method.toGenericString() + " : " + Arrays.toString(args));
    wrapperUri(method, args);


    return method.invoke(mBase, args);
  }

  private void wrapperUri(Method method, Object[] args){
    Uri uri = null;
    int index = getIntentIndex(args);

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
