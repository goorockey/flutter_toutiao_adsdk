package com.example.flutter_toutiao_adsdk;

import android.content.Context;
import android.util.Log;
import java.util.List;
import android.view.View;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BannerAdManager {
  public interface BannerGetCallback {
    /**
     * 获取TTBannerAd成功
     *
     * @param bannerAd 穿山甲回传的banner封装类
     */
    void bannerAdGetOk(TTNativeExpressAd bannerAd);

    /**
     * 获取TTBannerAd失败
     *
     * @param code   失败code
     * @param reason 失败理由
     */
    void bannerAdGetError(int code, String reason);
  }

  private volatile static BannerAdManager mInstance;

  private BannerAdManager() {
    bannerAdViewCache = new HashMap<>();
  }

  public static BannerAdManager getInstance() {
    if (mInstance == null) {
      synchronized (BannerAdManager.class) {
        if (mInstance == null) {
          mInstance = new BannerAdManager();
        }
      }
    }
    return mInstance;
  }

  @NonNull
  private ConcurrentLinkedQueue<TTNativeExpressAd> getBannerAdQueueByPosId(String positionId) {
    ConcurrentLinkedQueue<TTNativeExpressAd> queue = bannerAdViewCache.get(positionId);
    if (queue == null) {
      queue = new ConcurrentLinkedQueue<TTNativeExpressAd>();
      bannerAdViewCache.put(positionId, queue);
    }
    return queue;
  }

  private AdSlot getAdSlot(String positionId, int bannerWidth, int bannerHeight) {
    return new AdSlot.Builder().setCodeId(positionId).setAdCount(1)
        .setExpressViewAcceptedSize(bannerWidth, bannerHeight).setImageAcceptedSize(640, 320).setSupportDeepLink(true)
        .build();
  }

  private HashMap<String, ConcurrentLinkedQueue<TTNativeExpressAd>> bannerAdViewCache;

  public void preloadBannerAd(final Context context, final String positionId, final int bannerWidth,
      final int bannerHeight) {
    Consts.getAdNative(context).loadBannerExpressAd(getAdSlot(positionId, bannerWidth, bannerHeight),
        new TTAdNative.NativeExpressAdListener() {
          @Override
          public void onError(int i, String s) {
            Log.e(Consts.TAG, String.format("preload banner error. positioned: %s, %s, %s, code: %d, reason: %s",
                positionId, bannerWidth, bannerHeight, i, s));
          }

          @Override
          public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
            if (ads == null || ads.size() == 0) {
              Log.e(Consts.TAG, String.format("preload banner error. positioned: %s, %s, %s, 获取广告失败", positionId,
                  bannerWidth, bannerHeight));
              return;
            }
            ConcurrentLinkedQueue<TTNativeExpressAd> queue = getBannerAdQueueByPosId(positionId);
            for (TTNativeExpressAd ad : ads) {
              queue.add(ad);
            }
            Log.i(Consts.TAG, String.format("preload banner success. positioned: %s, %s, %s, %s", positionId,
                bannerWidth, bannerHeight, ads.size()));
          }
        });
  }

  public void getBannerView(final Context context, final String positionId, final int bannerWidth,
      final int bannerHeight, final BannerGetCallback bannerGetCallback) {
    ConcurrentLinkedQueue<TTNativeExpressAd> queue = getBannerAdQueueByPosId(positionId);
    if (!queue.isEmpty()) {
      if (bannerGetCallback != null) {
        bannerGetCallback.bannerAdGetOk(queue.poll());

        preloadBannerAd(context, positionId, bannerWidth, bannerHeight);
      }
      return;
    }

    Consts.getAdNative(context).loadBannerExpressAd(getAdSlot(positionId, bannerWidth, bannerHeight),
        new TTAdNative.NativeExpressAdListener() {
          @Override
          public void onError(int i, String s) {
            if (bannerGetCallback != null) {
              bannerGetCallback.bannerAdGetError(i, s);
            }
          }

          @Override
          public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
            if (ads == null || ads.size() == 0) {
              if (bannerGetCallback != null) {
                bannerGetCallback.bannerAdGetError(500, "获取广告失败");
              }
              return;
            }
            if (bannerGetCallback != null) {
              bannerGetCallback.bannerAdGetOk(ads.get(0));
            }
          }
        });

    preloadBannerAd(context, positionId, bannerWidth, bannerHeight);
  }
}
