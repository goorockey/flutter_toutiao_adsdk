package com.example.flutter_toutiao_adsdk;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;

public class FlutterPangolinFactory extends PlatformViewFactory {
    private Activity mActivity;
    private BinaryMessenger mMessager;
    private String type;

    public FlutterPangolinFactory(String type, Registrar registrar) {
        super(StandardMessageCodec.INSTANCE);
        this.mActivity = registrar.activity();
        this.mMessager = registrar.messenger();
        this.type = type;
    }

    @Override
    public PlatformView create(Context context, int i, Object o) {
      if (type == "flutter_pangolin_banner_ad") {
        return new FlutterPangolinBannerAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_draw_video") {
        return new FlutterPangolinDrawVideoAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_feed_ad") {
        return new FlutterPangolinFeedAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_reward_video") {
        return new FlutterPangolinRewardVideoAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_interaction_ad") {
        return new FlutterPangolinInteractionAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_express_ad") {
        return new FlutterPangolinExpressAd(mActivity, mMessager, i);
      }

      if (type == "flutter_pangolin_splash_ad") {
        return new FlutterPangolinSplashAd(mActivity, mMessager, i);
      }

      throw new RuntimeException("Not implmented pangolin view type");
    }
}
