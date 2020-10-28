package com.ys.simple.plugindemo;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

  private static final String BROADCAST_ACTION = "com.ys.broadcast";

  private MySimpleReceiver receiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.second_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intent);
      }
    });

    findViewById(R.id.broadcast_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendBroadcast();
      }
    });

//    registerBroadcastReceiver();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(receiver != null){
      unregisterReceiver(receiver);
    }
  }

  private void registerBroadcastReceiver(){
    receiver = new MySimpleReceiver();
    IntentFilter intentFilter = new IntentFilter();
    registerReceiver(receiver, intentFilter);
  }

  private void sendBroadcast(){
    Intent intent = new Intent();
    intent.setAction(BROADCAST_ACTION);
    intent.setFlags(0x01000000);
//    intent.setComponent(new ComponentName(getPackageName(), MySimpleReceiver.class.getName()));
    sendBroadcast(intent);
  }
}
