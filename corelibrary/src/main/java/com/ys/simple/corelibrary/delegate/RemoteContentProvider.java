package com.ys.simple.corelibrary.delegate;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by yinshan on 2020/10/28.
 */
public class RemoteContentProvider extends ContentProvider {

  private static final String TAG = "RemoteContentProvider";

  public static final String KEY_WRAPPER_URI = "wrapper_uri";

  @Override
  public boolean onCreate() {
    return false;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
      @Nullable String[] selectionArgs, @Nullable String sortOrder) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }

  @Nullable
  @Override
  public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
    Log.d(TAG, "call " + method + " with extras:" + extras);
    // todo
    return super.call(method, arg, extras);
  }

  public static String getAuthority(Context context){
    return context.getPackageName() + ".provider";
  }

  public static String getUri(Context context){
    return "content://" + getAuthority(context);
  }
}
