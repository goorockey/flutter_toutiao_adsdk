package com.example.flutter_toutiao_adsdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.view.View;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTInteractionAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;

public class FlutterToutiaoAdsdkPlugin implements MethodCallHandler {
  private final Registrar mRegistrar;

  public FlutterToutiaoAdsdkPlugin(Registrar registrar) {
    this.mRegistrar = registrar;
  }

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_pangolin");
    channel.setMethodCallHandler(new FlutterToutiaoAdsdkPlugin(registrar));

    registerNativeView(registrar);
  }

  private static void registerNativeView(Registrar registrar) {
    String[] viewTypeList = {
      "flutter_pangolin_banner_ad",
      "flutter_pangolin_draw_video",
      "flutter_pangolin_reward_video",
      "flutter_pangolin_feed_ad",
      "flutter_pangolin_interaction_ad",
      "flutter_pangolin_express_ad",
      "flutter_pangolin_splash_ad",
    };
    for (String viewType : viewTypeList) {
      registrar.platformViewRegistry().registerViewFactory(viewType,
              new FlutterPangolinFactory(viewType, registrar));
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (Consts.FunctionName.INIT_SDK.equals(call.method)) {
      this.initSdk(call, result);
    } else if (Consts.FunctionName.LOAD_REWARD_VIDEO_AD.equals(call.method)) {
      this.loadRewardVideoAd(call, result);
    } else if (Consts.FunctionName.LOAD_INTERACTION_AD.equals(call.method)) {
      this.loadInteractionAd(call, result);
    } else if (Consts.FunctionName.CHECK_PERMISSIONS.equals(call.method)) {
      this.checkPermissions(call, result);
    } else if (Consts.FunctionName.PRELOAD_BANNER_AD.equals(call.method)) {
      this.preloadBannerAd(call);
    } else {
      result.notImplemented();
    }
  }

  private void initSdk(final MethodCall call, final Result result) {
    try {
      TTAdConfig.Builder builder = new TTAdConfig.Builder();
      builder.appId((String)call.argument(Consts.ParamKey.APP_ID));
      HashMap<String, Object> params = call.argument(Consts.ParamKey.PARAMS);

      if (params == null) {
        params = new HashMap<>(16);
      }

      // lazy code. best practice is try catch every cast.
      try {
        if (params.containsKey(Consts.ParamKey.USE_TEXTURE_VIEW)) {
          boolean useTextureView = (boolean) params.get(Consts.ParamKey.USE_TEXTURE_VIEW);
          builder.useTextureView(useTextureView);
        }

        if (params.containsKey(Consts.ParamKey.APP_NAME)) {
          String appName = (String) params.get(Consts.ParamKey.APP_NAME);
          builder.appName(appName);
        }

        if (params.containsKey(Consts.ParamKey.TITLE_BAR_THEME)) {
          builder.titleBarTheme((int) params.get(Consts.ParamKey.TITLE_BAR_THEME));
        }

        if (params.containsKey(Consts.ParamKey.ALLOW_SHOW_NOTIFY)) {
          builder.allowShowNotify((boolean) params.get(Consts.ParamKey.ALLOW_SHOW_NOTIFY));
        }

        if (params.containsKey(Consts.ParamKey.ALLOW_SHOW_PAGE_WHEN_SCREEN_LOCK)) {
          builder.allowShowPageWhenScreenLock((boolean) params.get(Consts.ParamKey.ALLOW_SHOW_PAGE_WHEN_SCREEN_LOCK));
        }

        if (params.containsKey(Consts.ParamKey.DEBUG)) {
          builder.debug((boolean) params.get(Consts.ParamKey.DEBUG));
        }

        if (params.containsKey(Consts.ParamKey.DOWNLOAD_NETWORK_TYPE_LIST)) {
          int[] netWorkTypeList = (int[]) params.get(Consts.ParamKey.DOWNLOAD_NETWORK_TYPE_LIST);
          builder.directDownloadNetworkType(netWorkTypeList);
        }

        if (params.containsKey(Consts.ParamKey.SUPPORT_MULTI_PROCESS)) {
          builder.supportMultiProcess((boolean) params.get(Consts.ParamKey.SUPPORT_MULTI_PROCESS));
        }
      } catch (Exception e) {
        // do nothing
      }

      TTAdSdk.init(this.mRegistrar.activity(), builder.build());
      result.success(true);
    } catch (Exception e) {
      e.printStackTrace();
      result.success(false);
    }
  }

  private AdSlot getAdSlot(MethodCall call) {
    AdSlot.Builder builder = new AdSlot.Builder();
    String codeId = call.argument(Consts.ParamKey.CODE_ID);
    builder.setCodeId(codeId);

    HashMap<String, Object> params = call.argument(Consts.ParamKey.PARAMS);
    if (params == null) {
      params = new HashMap<>(16);
    }

    try {
      if (params.containsKey(Consts.ParamKey.SUPPORT_DEEP_LINK)) {
        builder.setSupportDeepLink((boolean) params.get(Consts.ParamKey.SUPPORT_DEEP_LINK));
      }

      if (params.containsKey(Consts.ParamKey.AD_COUNT)) {
        builder.setAdCount((int) params.get(Consts.ParamKey.AD_COUNT));
      }

      if (params.containsKey(Consts.ParamKey.REWARD_NAME)) {
        builder.setRewardName((String) params.get(Consts.ParamKey.REWARD_NAME));
      }

      if (params.containsKey(Consts.ParamKey.REWARD_AMOUNT)) {
        builder.setRewardAmount((int) params.get(Consts.ParamKey.REWARD_AMOUNT));
      }

      if (params.containsKey(Consts.ParamKey.HORIZONTAL)) {
        boolean horizontal = (boolean) params.get(Consts.ParamKey.HORIZONTAL);
        builder.setOrientation(horizontal ? TTAdConstant.HORIZONTAL : TTAdConstant.VERTICAL);
      }

      if (params.containsKey(Consts.ParamKey.EXPRESS_VIEW_WIDTH) && params.containsKey(Consts.ParamKey.EXPRESS_VIEW_HEIGHT)) {
        int width = (int) params.get(Consts.ParamKey.EXPRESS_VIEW_WIDTH);
        int height = (int) params.get(Consts.ParamKey.EXPRESS_VIEW_HEIGHT);
        builder.setExpressViewAcceptedSize(width, height);
      }

      if (params.containsKey(Consts.ParamKey.IMAGE_WIDTH) && params.containsKey(Consts.ParamKey.IMAGE_HEIGHT)) {
        int width = (int) params.get(Consts.ParamKey.IMAGE_WIDTH);
        int height = (int) params.get(Consts.ParamKey.IMAGE_HEIGHT);
        builder.setImageAcceptedSize(width, height);
      }

      if (params.containsKey(Consts.ParamKey.REQUEST_PERMISSION)) {
        if ((boolean) params.get(Consts.ParamKey.REQUEST_PERMISSION)) {
          TTAdSdk.getAdManager().requestPermissionIfNecessary(mRegistrar.activity());
        }
      }

      return builder.build();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void loadInteractionAd(MethodCall call, final Result result) {
    try {
      AdSlot adSlot = getAdSlot(call);
      Context context = mRegistrar.activity() == null ? mRegistrar.activeContext() : mRegistrar.activity();

      Consts.getAdNative(context).loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
        @Override
        public void onError(int code, String message) {
          Log.e(Consts.TAG, String.format("Interaction ad load error, %s, %s", code, message));
          try {
            result.success(false);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
          if (ads == null || ads.size() == 0){
            Log.e(Consts.TAG, String.format("Interaction ad load empty"));
            try {
              result.success(false);
            } catch (Exception e) {
              e.printStackTrace();
            }
            return;
          }

          final TTNativeExpressAd ttInteractionAd = ads.get(0);

          ttInteractionAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
              Log.e(Consts.TAG, String.format("Interaction ad clicked, %s", type));
            }

            @Override
            public void onAdShow(View view, int type) {
              Log.e(Consts.TAG, String.format("Interaction ad showed, %s", type));

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
              Log.e(Consts.TAG, String.format("Interaction ad render success, %s, %s", width, height));

              ttInteractionAd.showInteractionExpressAd(mRegistrar.activity());
            }
          });

          //如果是下载类型的广告，可以注册下载状态回调监听
          if (ttInteractionAd.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            ttInteractionAd.setDownloadListener(new TTAppDownloadListener() {
              @Override
              public void onIdle() {
                Log.e(Consts.TAG, String.format("Interaction ad download idle."));
              }

              @Override
              public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(Consts.TAG, String.format("Interaction ad download active, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
              }

              @Override
              public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(Consts.TAG, String.format("Interaction ad download paused, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
              }

              @Override
              public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(Consts.TAG, String.format("Interaction ad download failed, %s, %s, %s, %s", totalBytes, currBytes, fileName, appName));
              }

              @Override
              public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.e(Consts.TAG, String.format("Interaction ad download finished. %s, %s, %s", totalBytes, fileName, appName));
              }

              @Override
              public void onInstalled(String fileName, String appName) {
                Log.e(Consts.TAG, String.format("Interaction ad download installed. %s, %s", fileName, appName));
              }
            });
          }

          ttInteractionAd.render();
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadRewardVideoAd(MethodCall call, final Result result) {
    try {
      AdSlot adSlot = getAdSlot(call);

      Context context = mRegistrar.activity() == null ? mRegistrar.activeContext() : mRegistrar.activity();

      Consts.getAdNative(context).loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
        @Override
        public void onError(int i, String s) {
          Log.e(Consts.TAG, String.format("Reward video ad error. code: %d, reason: %s.", i, s));
          try {
            result.success(VIDEO_RESULT_TYPE.VIDEO_ERROR);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {
          ttRewardVideoAd.setRewardAdInteractionListener(new RewardAdListener(result));
          ttRewardVideoAd.showRewardVideoAd(mRegistrar.activity());
        }

        @Override
        public void onRewardVideoCached() {
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      try {
        result.success(VIDEO_RESULT_TYPE.VIDEO_FAILED);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  private void preloadBannerAd(MethodCall call) {
    if (TTAdSdk.getAdManager() == null) {
      Log.e(Consts.TAG, "You should invoke TTAdSdk.init() first !!!");
      return;
    }
    String codeId = call.argument(Consts.ParamKey.CODE_ID);
    if (TextUtils.isEmpty(codeId)) {
      Log.e(Consts.TAG, "no codeId with bannerAd");
      return;
    }

    Object width = call.argument(Consts.ParamKey.BANNER_WIDTH);
    if (width == null) {
      return;
    }
    int bannerWidth = (int) width;
    Object height = call.argument(Consts.ParamKey.BANNER_HEIGHT);
    if (height == null) {
      return;
    }
    int bannerHeight = (int) height;

    Context context = mRegistrar.activity();
    if (context == null) {
      context = mRegistrar.activeContext();
    }
    BannerAdManager.getInstance().preloadBannerAd(context, codeId, bannerWidth, bannerHeight);
  }

  private class RewardAdListener implements TTRewardVideoAd.RewardAdInteractionListener {
    private Result mResult;
    private boolean mVideoComplete;

    RewardAdListener(Result result) {
      mResult = result;
      mVideoComplete = false;
    }

    @Override
    public void onAdShow() {

    }

    @Override
    public void onAdVideoBarClick() {

    }

    @Override
    public void onAdClose() {
      try {
        mResult.success(mVideoComplete ? VIDEO_RESULT_TYPE.VIDEO_COMPLETE : VIDEO_RESULT_TYPE.VIDEO_CLOSE);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onVideoComplete() {
      mVideoComplete = true;
    }

    @Override
    public void onVideoError() {
      try {
        mResult.success(VIDEO_RESULT_TYPE.VIDEO_ERROR);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {

    }

    @Override
    public void onSkippedVideo() {

    }
  }

  private void checkPermissions(MethodCall call, Result result) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      this.checkAndRequestPermission(result);
    } else {
      result.success(true);
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private void checkAndRequestPermission(Result result) {
    List<String> lackedPermission = new ArrayList<String>();
    if ((mRegistrar.activity()
            .checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
      lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
    }

    if ((mRegistrar.activity()
            .checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
      lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    if ((mRegistrar.activity()
            .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
      lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // 权限都已经有了，那么直接调用SDK
    if (lackedPermission.size() == 0) {
      result.success(true);
    } else {
      // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
      result.success(false);
      String[] requestPermissions = new String[lackedPermission.size()];
      lackedPermission.toArray(requestPermissions);
      mRegistrar.activity().requestPermissions(requestPermissions, 1024);
    }
  }

  private static final class VIDEO_RESULT_TYPE {
    static final int VIDEO_FAILED = 1;
    static final int VIDEO_ERROR = 2;
    static final int VIDEO_CLOSE = 3;
    static final int VIDEO_COMPLETE = 4;
    static final int VIDEO_REWARD_VERIFIED = 5;
  }
}
