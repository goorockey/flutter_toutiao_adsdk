package com.example.flutter_toutiao_adsdk;

import com.example.flutter_toutiao_adsdk.FlutterPangolinBaseAd;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Button;
import android.graphics.BitmapFactory;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTDrawFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.content.res.Resources;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterPangolinDrawVideoAd extends FlutterPangolinBaseAd implements MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private TTAdNative mTTAdNative;
  private MethodChannel methodChannel;

  FlutterPangolinDrawVideoAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    methodChannel = new MethodChannel(messenger, "flutter_pangolin_draw_video_" + id);
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
  public void onMethodCall(final MethodCall methodCall, final MethodChannel.Result result) {
    if ("loadDrawVideo".equals(methodCall.method)) {
      loadDrawVideo(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void _loadDrawVideo(final String codeId, final int width, final int height, final MethodChannel.Result result) {
    AdSlot adSlot = new AdSlot.Builder().setCodeId(codeId) // 开发者申请的广告位
            .setSupportDeepLink(true).setImageAcceptedSize(width, height) // 符合广告场景的广告尺寸
            .setAdCount(1) // 请求广告数量为1到3条
            .build();
    // 加载广告
    mTTAdNative.loadDrawFeedAd(adSlot, new TTAdNative.DrawFeedAdListener() {
      @Override
      public void onError(int code, String message) {
        System.out.println(String.format("Flutter_pangolin_draw_video: load error, %s, %s", code, message));
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onDrawFeedAdLoad(List<TTDrawFeedAd> ads) {
        if (ads == null || ads.isEmpty()) {
          System.out.println("Flutter_pangolin_draw_video: ad is null!");
          try {
            result.success(false);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return;
        }
        // 为广告设置activity对象，下载相关行为需要该context对象
        for (TTDrawFeedAd ad : ads) {
          ad.setActivityForDownloadApp(mActivity);
        }
        // 设置广告视频区域是否响应点击行为，控制视频暂停、继续播放，默认不响应；
        ads.get(0).setCanInterruptVideoPlay(true);
        // 设置视频暂停的Icon和大小
        // ads.get(0).setPauseIcon(BitmapFactory.decodeResource(mActivity.getResources(),
        // R.drawable.dislike_icon), 60);
        // 获取广告视频播放的view并放入广告容器中
        mContentView.removeAllViews();
        mContentView.addView(ads.get(0).getAdView());
        // 初始化并绑定广告行为
        initAdViewAndAction(ads.get(0), result);
      }
    });
  }

  private float dip2Px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return dipValue * scale + 0.5f;
  }

  // 绑定广告行为
  private void initAdViewAndAction(final TTDrawFeedAd ad, final MethodChannel.Result result) {
    Button action = new Button(mActivity);
    action.setText(ad.getButtonText());
    Button btTitle = new Button(mActivity);
    btTitle.setText(ad.getTitle());

    int height = (int) dip2Px(mActivity, 50);
    int margin = (int) dip2Px(mActivity, 10);
    // noinspection SuspiciousNameCombination
    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(height * 3, height);
    lp.gravity = Gravity.END | Gravity.BOTTOM;
    lp.rightMargin = margin;
    lp.bottomMargin = margin;
    mContentView.addView(action, lp);

    FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(height * 3, height);
    lp1.gravity = Gravity.START | Gravity.BOTTOM;
    lp1.rightMargin = margin;
    lp1.bottomMargin = margin;
    mContentView.addView(btTitle, lp1);

    // 其他代码略

    // 响应点击区域的设置，分为普通的区域clickViews和创意区域creativeViews
    // clickViews中的view被点击会尝试打开广告落地页；creativeViews中的view被点击会根据广告类型
    // 响应对应行为，如下载类广告直接下载，打开落地页类广告直接打开落地页。
    // 注意：ad.getAdView()获取的view请勿放入这两个区域中。
    List<View> clickViews = new ArrayList<>();
    clickViews.add(btTitle);
    List<View> creativeViews = new ArrayList<>();
    creativeViews.add(action);
    ad.registerViewForInteraction(mContentView, clickViews, creativeViews, new TTNativeAd.AdInteractionListener() {
      @Override
      public void onAdClicked(View view, TTNativeAd ad) {
        System.out.println("Flutter_pangolin_draw_video: onAdClicked");

        mActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            methodChannel.invokeMethod("adClicked", null);
          }
        });
      }

      @Override
      public void onAdCreativeClick(View view, TTNativeAd ad) {
        System.out.println("Flutter_pangolin_draw_video: onAdCreativeClick");
      }

      @Override
      public void onAdShow(TTNativeAd ad) {
        System.out.println("Flutter_pangolin_draw_video: onAdShow");
        try {
          result.success(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void loadDrawVideo(final MethodCall call, final MethodChannel.Result result) {
    if (TTAdSdk.getAdManager() == null) {
      System.out.println("Flutter_pangolin_draw_video: You should invoke TTAdSdk.init() first !!!");
      try {
        result.success(false);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }

    final String codeId = call.argument("codeId");
    if (TextUtils.isEmpty(codeId)) {
      System.out.println("Flutter_pangolin_draw_video: no codeId with bannerAd");
      try {
        result.success(false);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }

    Object width = call.argument("bannerWidth");
    if (width == null) {
      System.out.println("Flutter_pangolin_draw_video: no bannerWidth with bannerAd");
      try {
        result.success(false);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }

    Object height = call.argument("bannerHeight");
    if (height == null) {
      System.out.println("Flutter_pangolin_draw_video: no bannerHeight with bannerAd");
      try {
        result.success(false);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }

    int bannerWidth = (int) width;
    int bannerHeight = (int) height;

    _loadDrawVideo(codeId, bannerWidth, bannerHeight, result);
  }
}