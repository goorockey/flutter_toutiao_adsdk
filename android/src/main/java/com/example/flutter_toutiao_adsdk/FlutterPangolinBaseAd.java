package com.example.flutter_toutiao_adsdk;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

import io.flutter.plugin.platform.PlatformView;


public class FlutterPangolinBaseAd implements PlatformView {
  protected LinearLayout mContentView;

  public FlutterPangolinBaseAd(Context context) {
    initContentView(context);
  }

  private void initContentView(Context context) {
    if (mContentView != null) {
      return;
    }
    mContentView = new LinearLayout(context);
    mContentView.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    mContentView.setGravity(Gravity.CENTER);
  }

  @Override
  public View getView() {
    modifyViewBgAndFocusable();
    return mContentView;
  }

  @Override
  public void onInputConnectionLocked() {
    // Log.i(Consts.TAG, "onInputConnectionLocked.");
  }

  @Override
  public void onInputConnectionUnlocked() {
    // Log.i(Consts.TAG, "onInputConnectionUnlocked.");
  }

  /**
   * 通过反射将mContentView背景改为透明，以及移除所在windowManager的FLAG_NOT_FOCUSABLE标记，该标记会导致穿山甲banner类型广告无法曝光
   */
  protected void modifyViewBgAndFocusable() {
    if (mContentView == null) {
      return;
    }

    mContentView.post(new Runnable() {
      @Override
      public void run() {
        try {
          ViewParent parent = mContentView.getParent();
          if (parent == null) {
            return;
          }

          while (parent.getParent() != null) {
            parent = parent.getParent();
          }

          Object decorView = parent.getClass().getDeclaredMethod("getView").invoke(parent);
          final Field windowField = decorView.getClass().getDeclaredField("mWindow");
          windowField.setAccessible(true);
          final Window window = (Window) windowField.get(decorView);
          windowField.setAccessible(false);
          window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
          window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
            window.setLocalFocus(true, true);
          }
        } catch (Exception e) {
          // log the exception
        }
      }
    });
  }

  @Override
  public void dispose() {
//    if (mContentView != null) {
//      mContentView.removeAllViews();
//    }
  }
}