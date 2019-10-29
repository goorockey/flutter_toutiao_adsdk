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
import com.bytedance.sdk.openadsdk.TTInteractionAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterPangolinInteractionAd extends FlutterPangolinBaseAd implements  MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private TTAdNative mTTAdNative;
  private TTNativeExpressAd mTTNaviteExpreddAd;
  private MethodChannel methodChannel;

  FlutterPangolinInteractionAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    methodChannel = new MethodChannel(messenger, "flutter_pangolin_interaction_ad_" + id);
    methodChannel.setMethodCallHandler(this);
    mActivity = activity;
    mTTAdNative = TTAdSdk.getAdManager().createAdNative(mActivity);
  }

  @Override
  public void dispose() {
    try {
      methodChannel.setMethodCallHandler(null);

      if (mTTNaviteExpreddAd != null) {
        mTTNaviteExpreddAd.destroy();
        mTTNaviteExpreddAd = null;
      }

      super.dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
    if (Consts.FunctionName.LOAD_INTERACTION_AD.equals(methodCall.method)) {
      loadInteractionAd(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void loadInteractionAd(final MethodCall call, final MethodChannel.Result result) {
    if (TTAdSdk.getAdManager() == null) {
      Log.e(Consts.TAG, "You should invoke TTAdSdk.init() first !!!");
      result.success(false);
      return;
    }

    final String codeId = call.argument("codeId");
    if (TextUtils.isEmpty(codeId)) {
      Log.e(Consts.TAG, "no codeId with interactionAd");
      result.success(false);
      return;
    }

    Object width = call.argument("expressViewWidth");
    if (width == null) {
      result.success(false);
      return;
    }
    int bannerWidth = (int) width;
    Object height = call.argument("expressViewHeight");
    if (height == null) {
      result.success(false);
      return;
    }
    int bannerHeight = (int) height;

    int imageWidth = 640;
    Object imageWidthData = call.argument("imageWidth");
    if (imageWidthData != null) {
      imageWidth = (int) imageWidthData;
    }
    int imageHeight = 320;
    Object imageHeightData = call.argument("imageHeight");
    if (imageHeightData != null) {
      imageHeight = (int) imageHeightData;
    }

    AdSlot adSlot = new AdSlot.Builder()
            .setCodeId(codeId) // 开发者申请的广告位
            .setSupportDeepLink(true)
            .setExpressViewAcceptedSize(bannerWidth, bannerHeight) //期望模板广告view的size,单位dp
            .setImageAcceptedSize(imageWidth, imageHeight) // 符合广告场景的广告尺寸
            .setAdCount(1) // 请求广告数量为1到3条
            .build();

    mTTAdNative.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
      @Override
      public void onError(int code, String message) {
        Log.e(Consts.TAG, String.format("InteractionAd loaded error: %s, %s, %s", codeId, code, message));
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
        if (ads == null || ads.isEmpty()) {
          Log.e(Consts.TAG, "InteractionAd loaded empty: ad is null!");
          try {
            result.success(false);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return;
        }

        if (mTTNaviteExpreddAd != null) {
          mTTNaviteExpreddAd.destroy();
          mTTNaviteExpreddAd = null;
        }

        mTTNaviteExpreddAd = ads.get(0);

        mTTNaviteExpreddAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
          @Override
          public void onAdClicked(View view, int type) {
            Log.i(Consts.TAG, String.format("Interaction ad clicked, %s", type));

            mActivity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                methodChannel.invokeMethod("adClicked", null);
              }
            });
          }

          @Override
          public void onAdShow(View view, int type) {
            Log.i(Consts.TAG, String.format("Interaction ad showed, %s", type));

            try {
              result.success(true);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onRenderFail(View view, String msg, int code) {
            Log.e(Consts.TAG, String.format("Interaction ad render faild, %s, %s", msg, code));

            try {
              result.success(false);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onRenderSuccess(View view, float width, float height) {
            Log.i(Consts.TAG, String.format("Interaction ad render success, %s, %s", width, height));

            mContentView.removeAllViews();
            mContentView.addView(view);
          }
        });

        if (mTTNaviteExpreddAd.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
          mTTNaviteExpreddAd.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
              Log.i(Consts.TAG, String.format("Interaction ad download idle."));
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
              Log.i(Consts.TAG, String.format("Interaction ad download active, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
              Log.i(Consts.TAG, String.format("Interaction ad download paused, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
              Log.e(Consts.TAG, String.format("Interaction ad download failed, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
              Log.i(Consts.TAG, String.format("Interaction ad download finished. %s, %s, %s", totalBytes, fileName, appName));
            }

            @Override
            public void onInstalled(String fileName, String appName) {
              Log.e(Consts.TAG, String.format("Interaction ad download installed. %s, %s", fileName, appName));
            }
          });
        }

        mTTNaviteExpreddAd.render();
      }
    });
  }
}

