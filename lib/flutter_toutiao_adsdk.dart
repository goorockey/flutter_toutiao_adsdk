import 'dart:async';

import 'package:flutter/services.dart';

export 'views/index.dart';

class FlutterPangolin {
  static const MethodChannel _channel = MethodChannel('flutter_pangolin');
  static bool _pangolinInited = false;
  static bool _pangolinPermissionAuthed = false;
  static Future<bool> initSDK(
    String appId, {
    bool useTextureView = false,
    String appName = '',
    int titleBarTheme = 1,
    bool allowShowNotify = false,
    bool allowShowPageWhenScreenLock = false,
    bool debug = false,
    List<int> downloadNetworkType = const [1, 2, 3, 4, 5],
    bool supportMultiProcess = false,
  }) async {
    if (!_pangolinInited) {
      final resp = await _channel.invokeMethod('initSDK', {
        "appId": appId,
        "params": {
          InitKey.userTextureView: useTextureView,
          InitKey.appName: appName,
          InitKey.titlebarTheme: titleBarTheme,
          InitKey.allowShowNotify: allowShowNotify,
          InitKey.allowShowPageWhenScreenLock: allowShowPageWhenScreenLock,
          InitKey.debug: debug,
          InitKey.downloadNetworkTypeList: downloadNetworkType,
          InitKey.supportMultiProcess: supportMultiProcess,
        }
      });
      if (resp != null && resp) {
        _pangolinInited = true;
      }
    }

    return _pangolinInited;
  }

  static Future<bool> checkPermissions() async {
    if (!_pangolinPermissionAuthed) {
      final resp = await _channel.invokeMethod('checkPermissions');
      if (resp != null && resp) {
        _pangolinPermissionAuthed = true;
      }
    }

    return _pangolinPermissionAuthed;
  }

  static Future<void> preloadBannerAd(
      List<BannerParamItem> bannerParamList) async {
    bannerParamList.forEach((_) {
      _channel.invokeMethod('preloadBannerAd', {
        'codeId': _.codeId,
        'bannerWidth': _.bannerWidth,
        'bannerHeight': _.bannerHeight,
      });
    });
  }

  static Future<int> loadRewardVideoAd(
    String codeId, {
    bool supportDeepLink = true,
    int adCount = 1,
    String rewardName = '',
    int rewardAmount = 0,
    String userId = '',
    bool horizontal = true,
    int imageWidth = 1080,
    int imageHeight = 1920,
    bool requestPermission = true,
  }) async {
    return await _channel.invokeMethod('loadRewardVideoAd', {
      "codeId": codeId,
      "params": {
        RewardedVideoKey.supportDeepLink: supportDeepLink,
        RewardedVideoKey.adCount: adCount,
        RewardedVideoKey.rewardName: rewardName,
        RewardedVideoKey.rewardAmount: rewardAmount,
        RewardedVideoKey.userId: userId,
        RewardedVideoKey.horizontal: horizontal,
        RewardedVideoKey.imageWidth: imageWidth,
        RewardedVideoKey.imageHeight: imageHeight,
        RewardedVideoKey.requestPermission: requestPermission,
      },
    });
  }

  static Future<bool> loadInteractionAd(
    String codeId, {
    bool supportDeepLink = true,
    int adCount = 1,
    String userId = '',
    int imageWidth = 640,
    int imageHeight = 320,
    int expressViewWidth = 600,
    int expressViewHeight = 900,
    bool requestPermission = true,
  }) async {
    return await _channel.invokeMethod('loadInteractionAd', {
      "codeId": codeId,
      "params": {
        RewardedVideoKey.supportDeepLink: supportDeepLink,
        RewardedVideoKey.adCount: adCount,
        RewardedVideoKey.userId: userId,
        RewardedVideoKey.imageWidth: imageWidth,
        RewardedVideoKey.imageHeight: imageHeight,
        RewardedVideoKey.requestPermission: requestPermission,
        RewardedVideoKey.expressViewWidth: expressViewWidth,
        RewardedVideoKey.expressViewHeight: expressViewHeight,
      },
    });
  }
}

class InitKey {
  static String userTextureView = 'USE_TEXTURE_VIEW';
  static String appName = 'APP_NAME';
  static String titlebarTheme = 'TITLE_BAR_THEME';
  static String allowShowNotify = 'ALLOW_SHOW_NOTIFY';
  static String allowShowPageWhenScreenLock =
      'ALLOW_SHOW_PAGE_WHEN_SCREEN_LOCK';
  static String debug = 'DEBUG';
  static String downloadNetworkTypeList = 'DOWNLOAD_NETWORK_TYPE_LIST';
  static String supportMultiProcess = 'SUPPORT_MULTI_PROCESS';
  static String userGender = 'USER_GENDER';
}

class RewardedVideoKey {
  static String supportDeepLink = 'SUPPORT_DEEP_LINK';
  static String adCount = 'AD_COUNT';
  static String rewardName = 'REWARD_NAME';
  static String rewardAmount = 'REWARD_AMOUNT';
  static String userId = 'USER_ID';
  static String horizontal = 'HORIZONTAL';
  static String imageWidth = 'IMAGE_WIDTH';
  static String imageHeight = 'IMAGE_HEIGHT';
  static String requestPermission = 'REQUEST_PERMISSION';
  static String expressViewWidth = 'EXPRESS_VIEW_WIDTH';
  static String expressViewHeight = 'EXPRESS_VIEW_HEIGHT';
}

class BannerParamItem {
  String codeId;
  int bannerWidth;
  int bannerHeight;

  BannerParamItem.fromJson(Map map) {
    codeId = map['codeId'];
    bannerWidth = map['bannerWidth'];
    bannerHeight = map['bannerHeight'];
  }
}
