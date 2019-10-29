import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';

class FlutterPangolinFeedView extends StatefulWidget {
  final String positionId;
  final int bannerWidth;
  final int bannerHeight;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinFeedView(
    this.positionId, {
    this.bannerWidth = 1920,
    this.bannerHeight = 1080,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinFeedViewState createState() =>
      _FlutterPangolinFeedViewState(this.bannerWidth, this.bannerHeight);
}

class _FlutterPangolinFeedViewState extends State<FlutterPangolinFeedView> {
  bool loaded = false;
  final int bannerWidth;
  final int bannerHeight;
  _FlutterPangolinFeedViewState(this.bannerWidth, this.bannerHeight);

  MethodChannel _channel;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  _loadView(final int id) async {
    if (_channel == null) {
      _channel = MethodChannel(
          PangolinNativeChannelPrefix.FEED_AD_PREFIX + id.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_FEED_AD, {
      "codeId": widget.positionId,
      "bannerWidth": bannerWidth,
      "bannerHeight": bannerHeight,
    });
    Log.i('flutter_pangolin_plugin feed load result: $result');

    if (mounted && loaded != result) {
      setState(() {
        loaded = result;
      });
    }

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

  @override
  Widget build(BuildContext context) {
    if ((widget.positionId ?? '').isEmpty) {
      return Container(height: 0, width: 0);
    }

    if ((widget.bannerWidth ?? 0) <= 0 || (widget.bannerHeight ?? 0) <= 0) {
      Log.e(
          'flutter_pangolin_plugin, feed ad config empty, ${widget.positionId}, ${widget.bannerWidth}, ${widget.bannerHeight}');
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

  Widget _androidView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      width: loaded ? widget.bannerWidth.toDouble() : 1,
      child: AndroidView(
        viewType: PangolinNativeKey.FEED_AD_KEY,
        onPlatformViewCreated: (final int id) async {
          Log.i("flutter_pangolin_plugin: android feed view created, $id");
          _loadView(id);
        },
      ),
    );
  }

  Widget _iosView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      width: loaded ? widget.bannerWidth.toDouble() : 1,
      child: UiKitView(
        viewType: PangolinNativeKey.FEED_AD_KEY,
        creationParams: <String, dynamic>{
          "codeId": widget.positionId,
          "params": {
            "bannerWidth": bannerWidth,
            "bannerHeight": bannerHeight,
          },
        },
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          Log.i("flutter_pangolin_plugin: ios feed view created, $id");
          widget.onLoaded?.call();
        },
      ),
    );
  }
}
