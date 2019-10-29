//
//  FlutterPangolinBannerAd.m
//  Runner
//
//  Created by luopetter on 2019/6/24.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import "FlutterPangolinBannerAd.h"
#import <BUAdSDK/BUAdSDK.h>


@implementation FlutterPangolinBannerAd
{
    NSObject<FlutterBinaryMessenger>*_messenger;
}
- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger> *)messager
{
    if ([super init]) {
        _messenger = messager;
    }
    return self;
}
- (NSObject<FlutterMessageCodec> *)createArgsCodec
{
    return [FlutterStandardMessageCodec sharedInstance];
}
- (NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args
{
    return [[FlutterPangolinBannerAdController alloc] initWithWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger];
}
@end

@interface FlutterPangolinBannerAdController()<BUBannerAdViewDelegate>
@end

@implementation FlutterPangolinBannerAdController
{
    int64_t _viewId;
    UIView* _adView;
}

- (instancetype) initWithWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterBinaryMessenger> *)messenger
{
    if ([super init]) {
        NSDictionary *dic = args;
        NSString *codeId = dic[@"codeId"];
        if (codeId == nil) {
            NSLog(@"Flutter_pangolin_plugin: no codeId with bannerAd");
            return self;
        }

        NSDictionary *params = dic[@"params"];
        if (params == nil || params[@"bannerWidth"] == nil || params[@"bannerHeight"] == nil) {
            NSLog(@"Flutter_pangolin_plugin: no bannerWidth or bannerHeight with bannerAd");
            return self;
        }
        
        _adView = [[UIView alloc] init];
        _viewId = viewId;
        
        NSString* channelName = [NSString stringWithFormat:@"flutter_pangolin_banner_ad_%lld", viewId];
        FlutterMethodChannel* channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger:messenger];
        __weak __typeof__(self) weakSelf = self;
        [channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result];
        }];
        
        UIWindow *window = [UIApplication sharedApplication].keyWindow;
        UIViewController *viewController = window.rootViewController;
        BUSize* bannerSize = [BUSize sizeBy:BUProposalSize_Banner600_260];
        BUBannerAdView* bannerView = [[BUBannerAdView alloc] initWithSlotID:codeId size:bannerSize rootViewController:viewController];
        [bannerView loadAdData];
        bannerView.frame = CGRectMake(0, 0, [params[@"bannerWidth"] floatValue], [params[@"bannerHeight"] floatValue]);
        bannerView.delegate = self;
        [_adView addSubview:bannerView];
    }
    return self;
}

- (void) onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
}

- (UIView *)view
{
    return _adView;
}

/**
 This method is called when bannerAdView ad slot loaded successfully.
 @param bannerAdView : view for bannerAdView
 @param nativeAd : nativeAd for bannerAdView
 */
- (void)bannerAdViewDidLoad:(BUBannerAdView *)bannerAdView WithAdmodel:(BUNativeAd *_Nullable)nativeAd
{
    NSLog(@"Flutter_pangolin_plugin: bannerAd did load.");
}

/**
 This method is called when bannerAdView ad slot failed to load.
 @param error : the reason of error
 */
- (void)bannerAdView:(BUBannerAdView *)bannerAdView didLoadFailWithError:(NSError *_Nullable)error
{
    NSLog(@"Flutter_pangolin_plugin: bannerAd load failed. %@", error);
}

/**
 This method is called when bannerAdView ad slot showed new ad.
 */
- (void)bannerAdViewDidBecomVisible:(BUBannerAdView *)bannerAdView WithAdmodel:(BUNativeAd *_Nullable)nativeAd
{
    NSLog(@"Flutter_pangolin_plugin：bannerAd become visible");
}

/**
 This method is called when bannerAdView is clicked.
 */
- (void)bannerAdViewDidClick:(BUBannerAdView *)bannerAdView WithAdmodel:(BUNativeAd *_Nullable)nativeAd
{
    NSLog(@"Flutter_pangolin_plugin: bannerAd did click.");
}

/**
 This method is called when the user clicked dislike button and chose dislike reasons.
 @param filterwords : the array of reasons for dislike.
 */
- (void)bannerAdView:(BUBannerAdView *)bannerAdView dislikeWithReason:(NSArray<BUDislikeWords *> *_Nullable)filterwords
{
    
}
@end
