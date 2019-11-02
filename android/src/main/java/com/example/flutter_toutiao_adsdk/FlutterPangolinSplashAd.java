package com.example.flutter_toutiao_adsdk;

import com.example.flutter_toutiao_adsdk.FlutterPangolinBaseAd;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.os.PowerManager;
import android.widget.LinearLayout;
import java.lang.reflect.Method;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterPangolinSplashAd extends FlutterPangolinBaseAd implements MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private TTAdNative mTTAdNative;
  private TTSplashAd mTTNaviteAd;
  private MethodChannel methodChannel;

  FlutterPangolinSplashAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    methodChannel = new MethodChannel(messenger, "flutter_pangolin_splash_ad_" + id);
    methodChannel.setMethodCallHandler(this);
    mActivity = activity;
    mTTAdNative = TTAdSdk.getAdManager().createAdNative(mActivity);
  }

  @Override
  public void dispose() {
    try {
      methodChannel.setMethodCallHandler(null);

      super.dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
    if (Consts.FunctionName.LOAD_SPLASH_AD.equals(methodCall.method)) {
      loadSplashAd(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void loadSplashAd(final MethodCall call, final MethodChannel.Result result) {
    try {
      if (TTAdSdk.getAdManager() == null) {
        Log.e(Consts.TAG, "You should invoke TTAdSdk.init() first !!!");
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }

      final String codeId = call.argument("codeId");
      if (TextUtils.isEmpty(codeId)) {
        Log.e(Consts.TAG, "no codeId with splashAd");
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }

      int timeout = 5000;
      Object timeoutVal = call.argument(Consts.ParamKey.TIMEOUT);
      if (timeoutVal != null) {
        timeout = (int) timeoutVal;
      }

      int imageWidth = 1080;
      Object imageWidthData = call.argument("imageWidth");
      if (imageWidthData != null) {
        imageWidth = (int) imageWidthData;
      }
      int imageHeight = 1920;
      Object imageHeightData = call.argument("imageHeight");
      if (imageHeightData != null) {
        imageHeight = (int) imageHeightData;
      }

      AdSlot adSlot = new AdSlot.Builder().setCodeId(codeId) // 开发者申请的广告位
          .setSupportDeepLink(true)
          .setImageAcceptedSize(imageWidth, imageHeight) // 符合广告场景的广告尺寸
          .setAdCount(1) // 请求广告数量为1到3条
          .build();

      mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
        @Override
        public void onError(int code, String message) {
          Log.e(Consts.TAG, String.format("SplashAd loaded error: %s, %s, %s", codeId, code, message));
          mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              methodChannel.invokeMethod("adError", null);
            }
          });
        }

        @Override
        public void onTimeout() {
          Log.e(Consts.TAG, String.format("SplashAd loaded timeout"));
          mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              methodChannel.invokeMethod("adError", null);
            }
          });
        }

        @Override
        public void onSplashAdLoad(TTSplashAd ad) {
          if (ad == null) {
            Log.e(Consts.TAG, "SplashAd loaded empty: ad is null!");
            mActivity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                methodChannel.invokeMethod("adError", null);
              }
            });
            return;
          }

          mTTNaviteAd = ad;
          mTTNaviteAd.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
              Log.i(Consts.TAG, String.format("Splash ad clicked, %s", type));

              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  methodChannel.invokeMethod("adClicked", null);
                }
              });
            }

            @Override
            public void onAdShow(View view, int type) {
              Log.i(Consts.TAG, String.format("Splash ad showed, %s", type));

              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  methodChannel.invokeMethod("adLoaded", null);
                }
              });
            }

            @Override
            public void onAdSkip() {
              Log.i(Consts.TAG, String.format("Splash ad skip"));
              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  methodChannel.invokeMethod("adDismissed", null);
                }
              });
            }

            @Override
            public void onAdTimeOver() {
              Log.i(Consts.TAG, String.format("Splash ad timeout"));
              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  methodChannel.invokeMethod("adDismissed", null);
                }
              });
            }
          });

          mContentView.removeAllViews();
          mContentView.addView(ad.getSplashView());
        }
      }, timeout);
      try {
        result.success(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
      try {
        result.success(false);
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
  }
}
