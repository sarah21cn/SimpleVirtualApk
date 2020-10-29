package com.ys.simple.plugindemo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by yinshan on 2020/10/29.
 */
public class PluginContentProvider extends ContentProvider {

  // id : grade
  private volatile ConcurrentHashMap<Long, Integer> grades;

  @Override
  public boolean onCreate() {
    grades = new ConcurrentHashMap<>();
    grades.put(1l, 99);
    grades.put(2l, 80);
    return true;
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
    MatrixCursor cursor = new MatrixCursor(new String[]{"Id", "Grade"});
    for(Map.Entry<Long, Integer> entry : grades.entrySet()){
      cursor.addRow(new Object[]{entry.getKey(), entry.getValue()});
    }
    return cursor;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }
}
