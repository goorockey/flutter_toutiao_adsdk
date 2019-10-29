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
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterPangolinFeedAd extends  FlutterPangolinBaseAd implements  MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private TTAdNative mTTAdNative;
  private MethodChannel methodChannel;

  FlutterPangolinFeedAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    methodChannel = new MethodChannel(messenger, "flutter_pangolin_feed_ad_" + id);
    methodChannel.setMethodCallHandler(this);
    mActivity = activity;
    mTTAdNative = TTAdSdk.getAdManager().createAdNative(mActivity);
  }

  @Override
  public void dispose() {
    methodChannel.setMethodCallHandler(null);
    super.dispose();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
    if (Consts.FunctionName.LOAD_FEED_AD.equals(methodCall.method)) {
      loadFeedAd(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void loadFeedAd(final MethodCall call, final MethodChannel.Result result) {
    if (TTAdSdk.getAdManager() == null) {
      Log.e(Consts.TAG, "You should invoke TTAdSdk.init() first !!!");
      result.success(false);
      return;
    }

    final String codeId = call.argument("codeId");
    if (TextUtils.isEmpty(codeId)) {
      Log.e(Consts.TAG, "no codeId with feedAd");
      result.success(false);
      return;
    }

    AdSlot adSlot = new AdSlot.Builder()
            .setCodeId(codeId) // 开发者申请的广告位
            .setSupportDeepLink(true)
            .setImageAcceptedSize(640, 320) // 符合广告场景的广告尺寸
            .setAdCount(1) // 请求广告数量为1到3条
            .build();

    mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
      @Override
      public void onError(int code, String message) {
        Log.e(Consts.TAG, String.format("FeedAd loaded error: %s, %s, %s", codeId, code, message));
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onFeedAdLoad(List<TTFeedAd> ads) {
        if (ads == null || ads.isEmpty()) {
          Log.e(Consts.TAG, "FeedAd loaded empty: ad is null!");

          try {
            result.success(false);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return;
        }
        TTFeedAd ad = ads.get(0);

        ad.setVideoAdListener(new TTFeedAd.VideoAdListener() {
          @Override
          public void onVideoLoad(TTFeedAd ad) {
            Log.e(Consts.TAG, String.format("FeedAd video load success: %s", codeId));
          }

          @Override
          public void onVideoAdComplete(TTFeedAd ad) {
            Log.e(Consts.TAG, String.format("FeedAd video complete: %s", codeId));
          }

          @Override
          public void onProgressUpdate(long current, long duration) {
            Log.e(Consts.TAG, String.format("FeedAd video progress update: %s, %s, %s", codeId, current, duration));
          }

          @Override
          public void onVideoError(int errorCode, int extraCode) {
            Log.e(Consts.TAG, String.format("FeedAd load failed: %s, %s, %s", codeId, errorCode, extraCode));
          }

          @Override
          public void onVideoAdStartPlay(TTFeedAd ad) {
            Log.e(Consts.TAG, String.format("FeedAd start play: %s", codeId));
          }

          @Override
          public void onVideoAdPaused(TTFeedAd ad) {
            Log.e(Consts.TAG, String.format("FeedAd paused: %s", codeId));
          }

          @Override
          public void onVideoAdContinuePlay(TTFeedAd ad) {
            Log.e(Consts.TAG, String.format("FeedAd continue play: %s", codeId));
          }
        });

        List<View> clickViewList = new ArrayList<View>();
        List<View> creativeViewList = new ArrayList<View>();

        ad.registerViewForInteraction(mContentView, clickViewList, creativeViewList,
                new TTNativeAd.AdInteractionListener() {
                  @Override
                  public void onAdClicked(View view, TTNativeAd ad) {
                    Log.e(Consts.TAG, String.format("FeedAd clicked: %s", codeId));

                    mActivity.runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        methodChannel.invokeMethod("adClicked", null);
                      }
                    });
                  }

                  @Override
                  public void onAdCreativeClick(View view, TTNativeAd ad) {
                    Log.e(Consts.TAG, String.format("FeedAd creative clicked: %s", codeId));
                  }

                  @Override
                  public void onAdShow(TTNativeAd ad) {
                    Log.e(Consts.TAG, String.format("FeedAd adShoww: %s", codeId));
                    try {
                      result.success(true);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                });

        mContentView.removeAllViews();
        mContentView.addView(ad.getAdView());
      }
    });
  }
}

