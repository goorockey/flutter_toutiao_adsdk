package com.example.flutter_toutiao_adsdk;

import com.example.flutter_toutiao_adsdk.FlutterPangolinBaseAd;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.graphics.BitmapFactory;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTDrawFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.content.res.Resources;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterPangolinRewardVideoAd extends FlutterPangolinBaseAd implements MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private TTAdNative mTTAdNative;
  private MethodChannel methodChannel;

  FlutterPangolinRewardVideoAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    methodChannel = new MethodChannel(messenger, "flutter_pangolin_reward_video_" + id);
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
  public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
    if ("loadRewardVideo".equals(methodCall.method)) {
      loadRewardVideo(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void _loadRewardVideo(final String codeId, final int width, final int height, final MethodChannel.Result result) {
    AdSlot adSlot = new AdSlot.Builder().setCodeId(codeId) // 开发者申请的广告位
        .setSupportDeepLink(true)
        .setImageAcceptedSize(width, height) // 符合广告场景的广告尺寸
        .setAdCount(1) // 请求广告数量为1到3条
        .setUserID("")
        .setOrientation(TTAdConstant.VERTICAL)
        .build();
    // 加载广告
    mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
      @Override
      public void onError(int code, String message) {
        System.out.println(String.format("Flutter_pangolin_reward_video: load error, %s, %s", code, message));
        result.success(false);
      }

      //视频广告加载后的视频文件资源缓存到本地的回调
      @Override
      public void onRewardVideoCached() {
        System.out.println("Flutter_pangolin_reward_video: video cached");
      }

      @Override
      public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
        if (ad == null) {
          System.out.println("Flutter_pangolin_reward_video: ad is null!");
          result.success(false);
          return;
        }

        System.out.println("Flutter_pangolin_reward_video: video loaded");

        initAdViewAndAction(ad, result);
        ad.showRewardVideoAd(mActivity);
      }
    });
  }

  private float dip2Px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return dipValue * scale + 0.5f;
  }

  // 绑定广告行为
  private void initAdViewAndAction(final TTRewardVideoAd ad, final MethodChannel.Result result) {
    ad.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
      @Override
      public void onAdShow() {
        System.out.println("Flutter_pangolin_reward_video: onAdShow");
        result.success(true);
      }

      @Override
      public void onAdVideoBarClick() {
        System.out.println("Flutter_pangolin_reward_video: onAdVideoBarClick");

        mActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            methodChannel.invokeMethod("adClicked", null);
          }
        });
      }

      @Override
      public void onAdClose() {
        System.out.println("Flutter_pangolin_reward_video: onAdClose");
      }

      @Override
      public void onVideoComplete() {
        System.out.println("Flutter_pangolin_reward_video: onVideoComplete");
      }

      @Override
      public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
        System.out.println("Flutter_pangolin_reward_video: onRewardVerify");
      }

      @Override
      public void onSkippedVideo() {
        System.out.println("Flutter_pangolin_reward_video: onSkippedVideo");
      }
      
      @Override
      public void onVideoError() {
        System.out.println("Flutter_pangolin_reward_video: onVideoError");
      }
    });
  }

  private void loadRewardVideo(final MethodCall call, final MethodChannel.Result result) {
    if (TTAdSdk.getAdManager() == null) {
      System.out.println("Flutter_pangolin_reward_video: You should invoke TTAdSdk.init() first !!!");
      result.success(false);
      return;
    }

    final String codeId = call.argument("codeId");
    if (TextUtils.isEmpty(codeId)) {
      System.out.println("Flutter_pangolin_reward_video: no codeId with bannerAd");
      result.success(false);
      return;
    }

    Object width = call.argument("bannerWidth");
    if (width == null) {
      System.out.println("Flutter_pangolin_reward_video: no bannerWidth with bannerAd");
      result.success(false);
      return;
    }

    Object height = call.argument("bannerHeight");
    if (height == null) {
      System.out.println("Flutter_pangolin_reward_video: no bannerHeight with bannerAd");
      result.success(false);
      return;
    }

    int bannerWidth = (int) width;
    int bannerHeight = (int) height;

    _loadRewardVideo(codeId, bannerWidth, bannerHeight, result);
  }
}