#import "FlutterPangolinBannerAd.h"
#import "FlutterPangolinSplashAd.h"
#import "FlutterToutiaoAdsdkPlugin.h"
#import <BUAdSDK/BUAdSDK.h>

//typedef void (^FlutterResult)(id _Nullable rewardedVideoResult);
#define ErrorBlock void (^)(id error)
//#define RewardedCallback void (^)(id error)

@interface FlutterToutiaoAdsdkPlugin () <BURewardedVideoAdDelegate>
@property(nonatomic, strong) BURewardedVideoAd *rewardedVideoAd;
@property(nonatomic, assign) BOOL videoFinished;
@property(nonatomic, strong) void (^rewardedVideoCallback)(NSNumber *ok);
@end

static const int VIDEO_FAILED = 1;
static const int VIDEO_ERROR = 2;
static const int VIDEO_CLOSE = 3;
static const int VIDEO_COMPLETE = 4;
static const int VIDEO_REWARD_VERIFIED = 5;

@implementation FlutterToutiaoAdsdkPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_pangolin"
            binaryMessenger:[registrar messenger]];
  FlutterToutiaoAdsdkPlugin* instance = [[FlutterToutiaoAdsdkPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
  
  [registrar registerViewFactory:[[FlutterPangolinBannerAd alloc] initWithMessenger:registrar.messenger] withId:@"flutter_pangolin_banner_ad"];
  [registrar registerViewFactory:[[FlutterPangolinSplashAd alloc] initWithMessenger:registrar.messenger] withId:@"flutter_pangolin_splash_ad"];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"initSDK" isEqualToString:call.method]) {
    [self initSDK:call result:result];
  } else if ([@"loadRewardVideoAd" isEqualToString:call.method]) {
    [self loadRewardVideoAd:call result:result];
  } else if ([@"checkPermissions" isEqualToString:call.method]) {
    result(@(YES));
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)initSDK:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSString *appId = call.arguments[@"appId"];
  [BUAdSDKManager setAppID:appId];
  [BUAdSDKManager setIsPaidApp:NO];
  result(@(YES));
}

- (void)loadRewardVideoAd:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSString *codeId = call.arguments[@"codeId"];
  
  BURewardedVideoModel *model = [[BURewardedVideoModel alloc] init];
  model.userId = @"";

  self.videoFinished = NO;
  
  self.rewardedVideoAd = [[BURewardedVideoAd alloc] initWithSlotID:codeId rewardedVideoModel:model];
  self.rewardedVideoAd.delegate = self;
  self.rewardedVideoCallback = ^(NSNumber *ok) {
    result(ok);
  };  

  [self.rewardedVideoAd loadAdData];
}

/**
 This method is called when video ad material loaded successfully.
 */
- (void)rewardedVideoAdDidLoad:(BURewardedVideoAd *)rewardedVideoAd {
  UIWindow *window = [UIApplication sharedApplication].keyWindow;
  UIViewController *viewController = window.rootViewController;
  [rewardedVideoAd showAdFromRootViewController:viewController];
}

/**
     This method is called when video ad is closed.
 */
- (void)rewardedVideoAdDidClose:(BURewardedVideoAd *)rewardedVideoAd {
  if (self.videoFinished) {
    self.rewardedVideoCallback([NSNumber numberWithInt:VIDEO_COMPLETE]);
  } else {
    self.rewardedVideoCallback([NSNumber numberWithInt:VIDEO_CLOSE]);
  }
}

/**
 This method is called when video ad materia failed to load.
 @param error : the reason of error
 */
- (void)rewardedVideoAd:(BURewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
  self.rewardedVideoCallback([NSNumber numberWithInt:VIDEO_FAILED]);
}

/**
 This method is called when video ad play completed or an error occurred.
 @param error : the reason of error
 */
- (void)rewardedVideoAdDidPlayFinish:(BURewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
  if (error != nil) {
    self.rewardedVideoCallback([NSNumber numberWithInt:VIDEO_ERROR]);
  } else {
    self.videoFinished = YES;
  }
}

@end
