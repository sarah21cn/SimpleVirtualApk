package com.ys.simple.corelibrary.hook;

import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;

/**
 * Created by yinshan on 2020/10/14.
 */
public class ActivityThreadHandlerCallback implements Handler.Callback {

  @Override
  public boolean handleMessage(@NonNull Message msg) {
    return false;
  }
}
