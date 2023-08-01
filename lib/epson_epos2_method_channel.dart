import 'package:epson_epos2/device_info.dart';
import 'package:epson_epos2/discovery_callback.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'epson_epos2_platform_interface.dart';

/// An implementation of [EpsonEpos2Platform] that uses method channels.
class MethodChannelEpsonEpos2 extends EpsonEpos2Platform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('epson_epos2');

  DiscoveryCallback? _discoveryCallback;

  MethodChannelEpsonEpos2() {
    methodChannel.setMethodCallHandler(_channelHandler);
  }

  Future _channelHandler(MethodCall call) async {
    switch (call.method) {
      case "onDiscovery":
        try {
          Map<String, dynamic> map = Map<String, String>.from(call.arguments);
          var deviceInfo = DeviceInfo.fromJson(map);
          _discoveryCallback?.call(deviceInfo);
        } catch (e) {
          print(e);
        }
        break;
    }
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
  void setDiscoveryCallback(DiscoveryCallback callback) async {
    _discoveryCallback = callback;
  }
}
