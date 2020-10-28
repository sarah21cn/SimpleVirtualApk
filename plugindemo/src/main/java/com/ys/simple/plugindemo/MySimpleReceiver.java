package com.ys.simple.plugindemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by yinshan on 2020/10/28.
 */
public class MySimpleReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Toast.makeText(context, "接收到Broadcast", Toast.LENGTH_SHORT).show();
  }
}
