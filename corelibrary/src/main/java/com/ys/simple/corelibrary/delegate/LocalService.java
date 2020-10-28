package com.ys.simple.corelibrary.delegate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * Created by yinshan on 2020/10/27.
 */
public class LocalService extends Service {

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // 在这里做分发
    return super.onStartCommand(intent, flags, startId);
  }
}
