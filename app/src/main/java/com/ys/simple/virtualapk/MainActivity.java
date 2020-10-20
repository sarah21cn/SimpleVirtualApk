package com.ys.simple.virtualapk;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;

import com.ys.simple.corelibrary.PluginManager;
import com.ys.simple.corelibrary.utils.PackageParserCompat;
import com.ys.simple.corelibrary.utils.Reflector;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onClick(View view){
    switch (view.getId()){
      case R.id.load_btn:
        PluginManager pluginManager = PluginManager.getInstance(this);
        final String path = getFilesDir().getPath() + "/plugindemo-debug.apk";
        File file = new File(path);
        if(file.exists()){
          try{
            pluginManager.loadPlugin(file);
            if(pluginManager.getLoadedPlugin("com.ys.simple.plugindemo") != null){
              Log.d(TAG, "load plugin succeed");
            }
          }catch (Exception e){
            e.printStackTrace();
          }
        }else{
          Log.d(TAG, "file not exists");
        }
        break;
      case R.id.start_btn:
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.ys.simple.plugindemo", "com.ys.simple.plugindemo.MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);
        break;
      case R.id.implicit_btn:
        break;
    }
  }
}
