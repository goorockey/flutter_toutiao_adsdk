import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';

class FlutterPangolinDrawVideoView extends StatefulWidget {
  final String positionId;
  final int bannerWidth;
  final int bannerHeight;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinDrawVideoView(
    this.positionId, {
    this.bannerWidth = 1920,
    this.bannerHeight = 1080,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinDrawVideoViewState createState() =>
      _FlutterPangolinDrawVideoViewState();
}

class _FlutterPangolinDrawVideoViewState
    extends State<FlutterPangolinDrawVideoView> {
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
    if ((widget.positionId ?? '').isEmpty ||
        (widget.bannerWidth ?? 0) <= 0 ||
        (widget.bannerHeight ?? 0) <= 0) {
      Log.e(
          'flutter_pangolin_plugin, draw video config empty, ${widget.positionId}, ${widget.bannerWidth}, ${widget.bannerHeight}');
      return Container(height: 0, width: 0);
    }

    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView();
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      // TODO
      return Container();
//      return _iosView();
    }

    Log.i('不支持的平台');
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
    if (_channel == null) {
      _channel = MethodChannel(
          PangolinNativeChannelPrefix.DRAW_VIDEO_PREFIX + id.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_DRAW_VIDEO, {
      "codeId": widget.positionId,
      "bannerWidth": widget.bannerWidth,
      "bannerHeight": widget.bannerHeight,
    });
    Log.i('flutter_pangolin_plugin draw video load result: $result');

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
    return AndroidView(
      viewType: PangolinNativeKey.DRAW_VIDEO_KEY,
      onPlatformViewCreated: (final int id) async {
        Log.i("flutter_pangolin_plugin: android draw video view created.");
        _loadView(id);
      },
    );
  }

  Widget _iosView() {
    return UiKitView(
      viewType: PangolinNativeKey.DRAW_VIDEO_KEY,
      creationParams: <String, dynamic>{
        "codeId": widget.positionId,
        "params": {
          "bannerWidth": widget.bannerWidth,
          "bannerHeight": widget.bannerHeight,
        },
      },
      creationParamsCodec: StandardMessageCodec(),
      onPlatformViewCreated: (int id) {
        Log.i("flutter_pangolin_plugin: ios draw video view created.");

        widget.onLoaded?.call();
      },
    );
  }
}
