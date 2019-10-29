package com.example.flutter_toutiao_adsdk;

import android.content.Context;

import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;

public class Consts {
    private static TTAdNative mAdNative;
    synchronized static TTAdNative getAdNative(Context context) {
        if (mAdNative == null) {
            assert TTAdSdk.getAdManager() != null;
            mAdNative = TTAdSdk.getAdManager().createAdNative(context);
        }
        return mAdNative;
    }

    static final String TAG = "flutter_pangolin";
    static final class FunctionName {
        static final String INIT_SDK = "initSDK";
        static final String LOAD_REWARD_VIDEO_AD = "loadRewardVideoAd";
        static final String CHECK_PERMISSIONS = "checkPermissions";
        static final String PRELOAD_BANNER_AD = "preloadBannerAd";
        static final String LOAD_BANNER_AD = "loadBannerAd";
        static final String LOAD_FEED_AD = "loadFeedAd";
        static final String LOAD_INTERACTION_AD = "loadInteractionAd";
        static final String LOAD_EXPRESS_AD = "loadExpressAd";
        static final String LOAD_SPLASH_AD = "loadSplashAd";
    }

    static final class ParamKey {
        static final String APP_ID = "appId";
        static final String PARAMS = "params";
        static final String APP_NAME = "APP_NAME";
        static final String USE_TEXTURE_VIEW = "USE_TEXTURE_VIEW";
        static final String TITLE_BAR_THEME = "TITLE_BAR_THEME";
        static final String ALLOW_SHOW_NOTIFY = "ALLOW_SHOW_NOTIFY";
        static final String ALLOW_SHOW_PAGE_WHEN_SCREEN_LOCK = "ALLOW_SHOW_PAGE_WHEN_SCREEN_LOCK";
        static final String DEBUG = "DEBUG";
        static final String DOWNLOAD_NETWORK_TYPE_LIST = "DOWNLOAD_NETWORK_TYPE_LIST";
        static final String SUPPORT_MULTI_PROCESS = "SUPPORT_MULTI_PROCESS";
        static final String USER_GENDER = "USER_GENDER";

        static final String CODE_ID = "codeId";
        static final String SUPPORT_DEEP_LINK = "SUPPORT_DEEP_LINK";
        static final String AD_COUNT = "AD_COUNT";
        static final String REWARD_NAME = "REWARD_NAME";
        static final String REWARD_AMOUNT = "REWARD_AMOUNT";
        static final String HORIZONTAL = "HORIZONTAL";
        static final String IMAGE_WIDTH = "IMAGE_WIDTH";
        static final String IMAGE_HEIGHT = "IMAGE_HEIGHT";
        static final String REQUEST_PERMISSION = "REQUEST_PERMISSION";
        static final String TIMEOUT = "timeout";

        static final String BANNER_WIDTH = "bannerWidth";
        static final String BANNER_HEIGHT = "bannerHeight";

        static final String EXPRESS_VIEW_WIDTH = "EXPRESS_VIEW_WIDTH";
        static final String EXPRESS_VIEW_HEIGHT = "EXPRESS_VIEW_HEIGHT";
    }
}
