import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';

class FlutterPangolinExpressView extends StatefulWidget {
  final String positionId;
  final int bannerWidth;
  final int bannerHeight;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinExpressView(
    this.positionId, {
    this.bannerWidth,
    this.bannerHeight,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinExpressViewState createState() =>
      _FlutterPangolinExpressViewState(this.bannerWidth, this.bannerHeight);
}

class _FlutterPangolinExpressViewState
    extends State<FlutterPangolinExpressView> {
  int bannerWidth;
  int bannerHeight;
  _FlutterPangolinExpressViewState(this.bannerWidth, this.bannerHeight);

  bool loaded = false;
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
          PangolinNativeChannelPrefix.EXPRESS_AD_PREFIX + id.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_EXPRESS_AD, {
      "codeId": widget.positionId,
      "expressViewWidth": widget.bannerWidth,
      "expressViewHeight": widget.bannerHeight,
    });
    Log.i('flutter_pangolin_plugin express load result: $result');

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

    if ((bannerWidth ?? 0) <= 0 || (bannerHeight ?? 0) <= 0) {
      Log.e(
          'flutter_pangolin_plugin, banner ad config empty, ${widget.positionId}, ${widget.bannerWidth}, ${widget.bannerHeight}');
      return Container(height: 0, width: 0);
    }

    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView();
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return _iosView();
    }

    Log.e('Express不支持的平台, ${widget.positionId}');
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
      height: loaded ? widget.bannerHeight.toDouble() : 1,
      width: loaded ? widget.bannerWidth.toDouble() : 1,
      child: AndroidView(
          viewType: PangolinNativeKey.EXPRESS_AD_KEY,
          onPlatformViewCreated: (final int id) async {
            Log.i(
                "flutter_pangolin_plugin: android banner ad view created, $id");
            _loadView(id);
          }),
    );
  }

  Widget _iosView() {
    return Container(
      height: loaded ? widget.bannerHeight.toDouble() : 1,
      width: loaded ? widget.bannerWidth.toDouble() : 1,
      child: UiKitView(
        viewType: PangolinNativeKey.EXPRESS_AD_KEY,
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          Log.i("flutter_pangolin_plugin: ios express view created.");
          _loadView(id);
        },
      ),
    );
  }
}
