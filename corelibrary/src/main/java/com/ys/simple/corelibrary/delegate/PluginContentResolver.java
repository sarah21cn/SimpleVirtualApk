package com.ys.simple.corelibrary.delegate;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.os.Build;
import androidx.annotation.Nullable;

import com.ys.simple.corelibrary.PluginManager;

/**
 * Created by yinshan on 2020/10/29.
 */
public class PluginContentResolver extends ContentResolver {

  private PluginManager mPluginManager;

  public PluginContentResolver(@Nullable Context context) {
    super(context);
    mPluginManager = PluginManager.getInstance(context);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  protected IContentProvider acquireUnstableProvider(Context context, String auth) {
//    if (mPluginManager.resolveContentProvider(auth, 0) != null) {
      return mPluginManager.getIContentProvider();
//    }
//    return super.acquireUnstableProvider(context, auth);
  }

}
