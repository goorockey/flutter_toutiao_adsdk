import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_toutiao_adsdk/constant.dart';
import 'package:flutter_toutiao_adsdk/logger.dart';

class FlutterPangolinSplashView extends StatefulWidget {
  final String positionId;
  final int timeout;
  final Function onLoaded;
  final Function onError;
  final Function onClick;
  final Function onFinish;

  FlutterPangolinSplashView(
    this.positionId, {
    this.timeout = 5000,
    this.onLoaded,
    this.onError,
    this.onClick,
    this.onFinish,
  });

  @override
  _FlutterPangolinSplashViewState createState() =>
      _FlutterPangolinSplashViewState();
}

class _FlutterPangolinSplashViewState extends State<FlutterPangolinSplashView> {
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
      _channel =
          MethodChannel('${PangolinNativeKey.SPLASH_AD_KEY}_$_channelId');
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result =
        await _channel.invokeMethod(PangolinNativeMethod.LOAD_SPLASH_AD, {
      "codeId": widget.positionId,
      "timeout": widget.timeout,
    });
    Log.i('flutter_pangolin_plugin splash load result: $result');

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

    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView();
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return _iosView();
    }

    Log.e('Splash不支持的平台, ${widget.positionId}');
    return Container(width: 0, height: 0);
  }

  Future<dynamic> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'adLoaded': {
        widget.onLoaded?.call(() {
          _loadView();
        });
        break;

      }
      case 'adClicked': {
        widget.onClick?.call(() {
          _loadView();
        });
        break;
      }
      case 'adDismissed': {
          widget.onFinish?.call();
          break;
        }
      default:
        break;
    }
  }

  Widget _androidView() {
    return AndroidView(
        viewType: PangolinNativeKey.SPLASH_AD_KEY,
        onPlatformViewCreated: (final int id) async {
          _channelId = id;
          Log.i("flutter_pangolin_plugin: android splash view created, $id");
          _loadView();
        });
  }

  Widget _iosView() {
    return UiKitView(
      viewType: PangolinNativeKey.SPLASH_AD_KEY,
      creationParams: <String, dynamic>{
        "codeId": widget.positionId,
      },
      creationParamsCodec: StandardMessageCodec(),
      onPlatformViewCreated: (int id) {
        _channelId = id;
        Log.i("flutter_pangolin_plugin: ios splash view created.");
        _loadView();
      },
    );
  }
}
