import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';

class FlutterPangolinRewardVideoView extends StatefulWidget {
  final String positionId;
  final num bannerWidth;
  final num bannerHeight;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinRewardVideoView(
    this.positionId, {
    this.bannerWidth = 1080,
    this.bannerHeight = 1920,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinRewardVideoViewState createState() =>
      _FlutterPangolinRewardVideoViewState(this.bannerWidth, this.bannerHeight);
}

class _FlutterPangolinRewardVideoViewState
    extends State<FlutterPangolinRewardVideoView> {
  bool loaded = false;
  final int bannerWidth;
  final int bannerHeight;
  _FlutterPangolinRewardVideoViewState(this.bannerWidth, this.bannerHeight);

  MethodChannel _channel;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if ((widget.positionId ?? '').isEmpty) {
      return Container(height: 0, width: 0);
    }

    if ((bannerWidth ?? 0) <= 0 || (bannerHeight ?? 0) <= 0) {
      Log.e(
          'flutter_pangolin_plugin, reward video config empty, ${widget.positionId}, ${bannerWidth}, ${bannerHeight}');
      return Container(height: 0, width: 0);
    }

    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView();
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return _iosView();
    }

    Log.i('不支持的平台, ${widget.positionId}');
    return Container(width: 0, height: 0);
  }

  Future<dynamic> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'adClicked':
        {
          widget.onClick?.call();
          break;
        }
      default:
        break;
    }
  }

  _loadView(final int id) async {
    _channel = _channel ??
        MethodChannel(
            PangolinNativeChannelPrefix.REWARD_VIDEO_PREFIX + id.toString());

    _channel.setMethodCallHandler(_onMethodCall);

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_REWARD_VIDEO, {
      "codeId": widget.positionId,
      "bannerWidth": bannerWidth,
      "bannerHeight": bannerHeight,
    });

    if (result) {
      widget.onLoaded?.call(() {
        _loadView(id);
      });
    } else {
      widget.onError?.call(() {
        _loadView(id);
      });
    }
  }

  Widget _androidView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      child: AndroidView(
        viewType: PangolinNativeKey.REWARD_VIDEO_KEY,
        onPlatformViewCreated: (final int id) async {
          Log.i("flutter_pangolin_plugin: android reward video view created.");
          _loadView(id);
        },
      ),
    );
  }

  Widget _iosView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      child: UiKitView(
        viewType: PangolinNativeKey.REWARD_VIDEO_KEY,
        creationParams: <String, dynamic>{
          "codeId": widget.positionId,
          "params": {
            "bannerWidth": bannerWidth,
            "bannerHeight": bannerHeight,
          },
        },
        creationParamsCodec: new StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          Log.i("flutter_pangolin_plugin: ios reward video view created.");

          widget.onLoaded?.call();
        },
      ),
    );
  }
}
