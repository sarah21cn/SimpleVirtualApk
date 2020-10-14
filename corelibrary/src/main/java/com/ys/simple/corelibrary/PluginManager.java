package com.ys.simple.corelibrary;

import android.content.Context;

/**
 * Created by yinshan on 2020/10/13.
 */
public class PluginManager {

  private Context mContext;

  protected PluginManager(Context context){
    this.mContext = context;
  }

  public Context getHostContext(){
    return this.mContext;
  }
}
