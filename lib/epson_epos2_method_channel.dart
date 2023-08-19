import 'package:epson_epos2/device_info.dart';
import 'package:epson_epos2/callbacks.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'epson_epos2_platform_interface.dart';

/// An implementation of [EpsonEpos2Platform] that uses method channels.
class MethodChannelEpsonEpos2 extends EpsonEpos2Platform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('epson_epos2');

  DiscoveryCallback? _discoveryCallback;
  PtrStatusChangeCallback? _ptrStatusChangeCallback;

  MethodChannelEpsonEpos2() {
    methodChannel.setMethodCallHandler(_channelHandler);
  }

  Future _channelHandler(MethodCall call) async {
    switch (call.method) {
      case "onDiscovery":
        try {
          Map<String, dynamic> map = Map.from(call.arguments);
          var deviceInfo = DeviceInfo.fromJson(map);
          _discoveryCallback?.call(deviceInfo);
        } catch (e) {
          if (kDebugMode) {
            print(e);
          }
        }
        break;
      case "onPtrStatusChange":
        try {
          _ptrStatusChangeCallback?.call(call.arguments);
        } catch (e) {
          if (kDebugMode) {
            print(e);
          }
        }
        break;
    }
  }

  @override
  Future<void> init() async {
    return methodChannel.invokeMethod('init');
  }

  @override
  Future<void> startDiscovery() async {
    return methodChannel.invokeMethod('startDiscovery');
  }

  @override
  Future<void> stopDiscovery() async {
    return methodChannel.invokeMethod('stopDiscovery');
  }

  @override
  Future<Map<int, String>?> getSeries() async {
    return methodChannel.invokeMapMethod('getSeries');
  }

  @override
  Future<Map<int, String>?> getLangModels() async {
    return methodChannel.invokeMapMethod('getLangModels');
  }

  @override
  Future<void> setPrinter(int series, int langModel) async {
    return methodChannel.invokeMethod('setPrinter', {
      "series": series,
      "langModel": langModel,
    });
  }

  @override
  Future<Map<String, String>?> getStatus() async {
    return methodChannel.invokeMapMethod('getStatus');
  }

  @override
  Future<void> connect(String target) {
    return methodChannel.invokeMethod('connect', {
      "target": target,
    });
  }

  @override
  Future<void> disconnect() async {
    return methodChannel.invokeMethod('disconnect');
  }

  @override
  void setDiscoveryCallback(DiscoveryCallback? callback) async {
    _discoveryCallback = callback;
  }

  @override
  void setPtrStatusChangeCallback(PtrStatusChangeCallback? callback) async {
    _ptrStatusChangeCallback = callback;
  }
}
