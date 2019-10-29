import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';

class FlutterPangolinBannerView extends StatefulWidget {
  final String positionId;
  final int width;
  final int height;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinBannerView(
    this.positionId, {
    this.width,
    this.height,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinBannerViewState createState() =>
      _FlutterPangolinBannerViewState(this.width, this.height);
}

class _FlutterPangolinBannerViewState extends State<FlutterPangolinBannerView> {
  int bannerWidth;
  int bannerHeight;
  _FlutterPangolinBannerViewState(this.bannerWidth, this.bannerHeight);

  bool loaded = false;
  MethodChannel _channel;
  int _channelId;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  _loadView() async {
    if (_channel == null) {
      _channel = MethodChannel(
          PangolinNativeChannelPrefix.BANNER_AD_PREFIX + _channelId.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_BANNER_AD, {
      "codeId": widget.positionId,
      "bannerWidth": bannerWidth,
      "bannerHeight": bannerHeight,
    });
    Log.i('flutter_pangolin_plugin banner load result: $result');

    if (mounted && loaded != result) {
      setState(() {
        loaded = result;
      });
    }

    if (result != true) {
      widget.onError?.call(() {
        _loadView();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if ((widget.positionId ?? '').isEmpty) {
      return Container(height: 0, width: 0);
    }

    if ((bannerWidth ?? 0) <= 0 || (bannerHeight ?? 0) <= 0) {
      Log.e(
          'flutter_pangolin_plugin, banner ad config empty, ${widget.positionId}, ${bannerWidth}, ${bannerHeight}');
      return Container(height: 0, width: 0);
    }

    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView(bannerWidth, bannerHeight);
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return _iosView(bannerWidth, bannerHeight);
    }

    Log.e('Banner不支持的平台, ${widget.positionId}');
    return Container(width: 0, height: 0);
  }

  Future<dynamic> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'adClicked':
        {
          widget.onClick?.call(() {
            _loadView();
          });
          break;
        }
      case 'adLoaded':
        {
          widget.onLoaded?.call(() {
            _loadView();
          });
          break;
        }
      case 'adError':
        {
          widget.onError?.call(() {
            _loadView();
          });
          break;
        }
      default:
        break;
    }
  }

  Widget _androidView(int bannerWidth, int bannerHeight) {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      width: loaded ? bannerWidth.toDouble() : 1,
      child: AndroidView(
          viewType: PangolinNativeKey.BANNER_AD_KEY,
          onPlatformViewCreated: (final int id) async {
            _channelId = id;
            Log.i(
                "flutter_pangolin_plugin: android banner ad view created, $id");
            _loadView();
          }),
    );
  }

  Widget _iosView(int bannerWidth, int bannerHeight) {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      width: loaded ? bannerWidth.toDouble() : 1,
      child: UiKitView(
        viewType: PangolinNativeKey.BANNER_AD_KEY,
        creationParams: <String, dynamic>{
          "codeId": widget.positionId,
          "params": {
            "bannerWidth": bannerWidth,
            "bannerHeight": bannerHeight,
          },
        },
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          _channelId = id;
          Log.i("flutter_pangolin_plugin: ios banner view created.");
          _loadView();
        },
      ),
    );
  }
}
