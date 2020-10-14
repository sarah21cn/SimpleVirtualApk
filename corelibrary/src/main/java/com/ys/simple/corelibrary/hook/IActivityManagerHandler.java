package com.ys.simple.corelibrary.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by yinshan on 2020/10/14.
 */
public class IActivityManagerHandler implements InvocationHandler {

  Object mBase;

  public IActivityManagerHandler(Object base) {
    this.mBase = base;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return null;
  }
}
