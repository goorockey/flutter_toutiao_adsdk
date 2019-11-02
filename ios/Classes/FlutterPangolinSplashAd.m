//
//  FlutterPangolinSplashAd.m
//  Runner
//
//

#import "FlutterPangolinSplashAd.h"
#import <BUAdSDK/BUAdSDK.h>


@implementation FlutterPangolinSplashAd
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
    return [[FlutterPangolinSplashAdController alloc] initWithWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger];
}
@end

@interface FlutterPangolinSplashAdController()<BUSplashAdDelegate>
@end

@implementation FlutterPangolinSplashAdController
{
    int64_t _viewId;
    UIView* _adView;
    FlutterMethodChannel* _channel;
    BUSplashAdView* _splashView;
}

- (instancetype) initWithWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterBinaryMessenger> *)messenger
{
    if ([super init]) {
        _adView = [[UIView alloc] init];
        _viewId = viewId;
        
        NSString* channelName = [NSString stringWithFormat:@"flutter_pangolin_splash_ad_%lld", viewId];
        _channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger:messenger];
        __weak __typeof__(self) weakSelf = self;
        [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
            [weakSelf onMethodCall:call result:result];
        }];
        
    }
    return self;
}

- (void) onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if([@"loadSplashAd" isEqualToString:call.method]){
        [self loadSplashAd:call result: result];
        return;
    }

    result(FlutterMethodNotImplemented);
}

- (UIView *)view
{
    return _adView;
}

- (void) loadSplashAd:(FlutterMethodCall*)call result:(FlutterResult)result {
    @try {
        if (_splashView == nil) {
            NSDictionary *dic = call.arguments;
            NSString *codeId = dic[@"codeId"];
            if (codeId == nil) {
                NSLog(@"Flutter_pangolin_plugin: no codeId with splashAd");
                result(@(NO));
                return;
            }

            UIWindow *window = [UIApplication sharedApplication].keyWindow;
            UIViewController *viewController = window.rootViewController;
            CGRect frame = [UIScreen mainScreen].bounds;
            _splashView = [[BUSplashAdView alloc] initWithSlotID:codeId frame:frame];
            _splashView.delegate = self;
            [_adView addSubview:_splashView];
            _splashView.rootViewController = viewController;
        }

        [_splashView loadAdData];

        result(@(YES));
    } @catch (NSException *exception) {
        NSLog(@"%@", exception);

        result(@(NO));
    }
}

/**
 This method is called when splashAdView ad slot failed to load.
 @param error : the reason of error
 */
- (void)splashAd:(BUSplashAdView *)splashAd didFailWithError:(NSError *)error;
{
    NSLog(@"Flutter_pangolin_plugin: splashAd load failed. %@", error);
    [_channel invokeMethod:@"adError" arguments:nil];
}

/**
This method is called when splash ad slot will be showing.
*/
- (void)splashAdWillVisible:(BUSplashAdView *)splashAd {
    NSLog(@"Flutter_pangolin_plugin：splashAd become visible");
    [_channel invokeMethod:@"adLoaded" arguments:nil];
}

/**
This method is called when splash ad is clicked.
*/
- (void)splashAdDidClick:(BUSplashAdView *)splashAd {
    NSLog(@"Flutter_pangolin_plugin: splashAd did click.");
    [_channel invokeMethod:@"adClicked" arguments:nil];
}

/**
This method is called when splash ad is closed.
*/
- (void)splashAdDidClose:(BUSplashAdView *)splashAd {
    NSLog(@"Flutter_pangolin_plugin: splashAd did close.");
    [_channel invokeMethod:@"adDismissed" arguments:nil];
    [_splashView removeFromSuperview];
}

/**
This method is called when splash ad is about to close.
*/
- (void)splashAdWillClose:(BUSplashAdView *)splashAd {
    NSLog(@"Flutter_pangolin_plugin: splashAd will close.");

}
@end
