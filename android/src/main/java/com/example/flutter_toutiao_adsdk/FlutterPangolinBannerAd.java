package com.example.flutter_toutiao_adsdk;

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

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

import com.example.flutter_toutiao_adsdk.FlutterPangolinBaseAd;

public class FlutterPangolinBannerAd extends FlutterPangolinBaseAd implements MethodChannel.MethodCallHandler {
  private Activity mActivity;
  private MethodChannel methodChannel;
  private TTNativeExpressAd mBannerAd;
  private int mId;

  FlutterPangolinBannerAd(Activity activity, BinaryMessenger messenger, int id) {
    super(activity);

    mId = id;
    methodChannel = new MethodChannel(messenger, "flutter_pangolin_banner_ad_" + id);
    methodChannel.setMethodCallHandler(this);
    mActivity = activity;
  }

  @Override
  public void dispose() {
    try {
      methodChannel.setMethodCallHandler(null);

      if (mBannerAd != null) {
        mBannerAd.destroy();
        mBannerAd = null;
      }
      Log.i(Consts.TAG, String.format("BannerAd released, %s", mId));

      super.dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
    if (Consts.FunctionName.LOAD_BANNER_AD.equals(methodCall.method)) {
      loadBannerAd(methodCall, result);
      return;
    }
    result.notImplemented();
  }

  private void loadBannerAd(final MethodCall call, final MethodChannel.Result result) {
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
        Log.e(Consts.TAG, "no codeId with bannerAd");
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }

      Object width = call.argument("bannerWidth");
      if (width == null) {
        Log.e(Consts.TAG, "no bannerWidth with bannerAd");
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }

      final Object height = call.argument("bannerHeight");
      if (height == null) {
        Log.e(Consts.TAG, "no bannerHeight with bannerAd");
        try {
          result.success(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }

      final int bannerWidth = (int) width;
      final int bannerHeight = (int) height;

      BannerAdManager.getInstance().getBannerView(mActivity, codeId, bannerWidth, bannerHeight,
          new BannerAdManager.BannerGetCallback() {
            @Override
            public void bannerAdGetOk(TTNativeExpressAd bannerAd) {
              if (bannerAd == null || bannerAd.getExpressAdView() == null) {
                Log.e(Consts.TAG, String.format("BannerAd empty. %s, %s, %s", codeId, bannerWidth, bannerHeight));

                mActivity.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    methodChannel.invokeMethod("adError", null);
                  }
                });
                return;
              }

              if (mBannerAd != null) {
                Log.i(Consts.TAG, String.format("BannerAd destroy, %s", mId));
                mBannerAd.destroy();
              }
              mBannerAd = bannerAd;
              bannerAd.setSlideIntervalTime(10*1000);//设置轮播间隔 ms,不调用则不进行轮播展示

              bannerAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int i) {
                  Log.i(Consts.TAG, String.format("BannerAd clicked. %s, %s, %s", codeId, bannerWidth, bannerHeight));

                  mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      methodChannel.invokeMethod("adClicked", null);
                    }
                  });
                }

                @Override
                public void onAdShow(View view, int i) {
                  Log.i(Consts.TAG, String.format("BannerAd showed. %s, %s, %s, %s, %s", codeId, bannerWidth,
                      bannerHeight, view.getWidth(), view.getHeight()));
                  mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      methodChannel.invokeMethod("adLoaded", null);
                    }
                  });
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                  Log.e(Consts.TAG, String.format("BannerAd render fail: %s, %s, %s, %s, %s", codeId, bannerWidth,
                      bannerHeight, code, msg));
                  mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      methodChannel.invokeMethod("adError", null);
                    }
                  });
                }

                @Override
                public void onRenderSuccess(View view, float width, float height) {
                  Log.i(Consts.TAG, String.format("BannerAd render success: %s, %s, %s, %s, %s", codeId, bannerWidth,
                      bannerHeight, width, height));
                  mContentView.removeAllViews();
                  mContentView.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT));
                }
              });

              bannerAd.render();
            }

            @Override
            public void bannerAdGetError(int code, String reason) {
              Log.e(Consts.TAG, String.format("bannerAd load error. %s, %s, %s, code: %d, reason: %s", codeId,
                  bannerWidth, bannerHeight, code, reason));

              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  methodChannel.invokeMethod("adError", null);
                }
              });
            }
          });

      result.success(true);

    } catch (Exception e) {
      e.printStackTrace();

      result.success(false);
    }
  }
}
