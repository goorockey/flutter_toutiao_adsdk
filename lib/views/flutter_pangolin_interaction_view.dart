import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';

class FlutterPangolinInteractionView extends StatefulWidget {
  final String positionId;
  final int width;
  final int height;
  final Function onLoaded;
  final Function onError;
  final Function onClick;

  FlutterPangolinInteractionView(
    this.positionId, {
    this.width,
    this.height,
    this.onLoaded,
    this.onError,
    this.onClick,
  });

  @override
  _FlutterPangolinInteractionViewState createState() =>
      _FlutterPangolinInteractionViewState(this.width, this.height);
}

class _FlutterPangolinInteractionViewState
    extends State<FlutterPangolinInteractionView> {
  bool loaded = false;
  int bannerWidth;
  int bannerHeight;
  _FlutterPangolinInteractionViewState(this.bannerWidth, this.bannerHeight);

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
        (bannerWidth ?? 0) <= 0 ||
        (bannerHeight ?? 0) <= 0) {
      return Container(height: 0, width: 0);
    }

    Log.i('interaction size, $bannerWidth, $bannerHeight');

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
    if (_channel == null) {
      _channel = MethodChannel(
          PangolinNativeChannelPrefix.INTERACTION_AD_PREFIX + id.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_INTERACTION_AD, {
      "codeId": widget.positionId,
      "expressViewWidth": bannerWidth,
      "expressViewHeight": bannerHeight,
    });
    Log.i('flutter_pangolin_plugin interaction load result: $result');

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

  Widget _androidView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      child: AndroidView(
        viewType: PangolinNativeKey.INTERACTION_AD_KEY,
        onPlatformViewCreated: (final int id) async {
          Log.i(
              "flutter_pangolin_plugin: android interaction ad view created, $id");
          _loadView(id);
        },
      ),
    );
  }

  Widget _iosView() {
    return Container(
      height: loaded ? bannerHeight.toDouble() : 1,
      child: UiKitView(
        viewType: PangolinNativeKey.INTERACTION_AD_KEY,
        creationParams: <String, dynamic>{
          "codeId": widget.positionId,
          "params": {
            "expressViewWidth": bannerWidth,
            "expressViewHeight": bannerHeight,
          },
        },
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          Log.i("flutter_pangolin_plugin: ios interaction view created.");
          widget.onLoaded?.call();
        },
      ),
    );
  }
}
