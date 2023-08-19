import 'package:epson_epos2/callbacks.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:epson_epos2/epson_epos2.dart';
import 'package:epson_epos2/epson_epos2_platform_interface.dart';
import 'package:epson_epos2/epson_epos2_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockEpsonEpos2Platform
    with MockPlatformInterfaceMixin
    implements EpsonEpos2Platform {
  @override
  Future<void> init() => Future.value();
  @override
  Future<void> startDiscovery() => Future.value();
  @override
  Future<void> stopDiscovery() => Future.value();
  @override
  Future<Map<int, String>?> getSeries() => Future.value();
  @override
  Future<Map<int, String>?> getLangModels() => Future.value();
  @override
  Future<void> setPrinter(int series, int langModel) => Future.value();
  @override
  Future<Map<String, String>?> getStatus() => Future.value();
  @override
  Future<void> connect(String target) => Future.value();
  @override
  Future<void> disconnect() => Future.value();
  @override
  void setDiscoveryCallback(DiscoveryCallback callback) {}
  @override
  void setPtrStatusChangeCallback(PtrStatusChangeCallback callback) {}
}

void main() {
  final EpsonEpos2Platform initialPlatform = EpsonEpos2Platform.instance;

  test('$MethodChannelEpsonEpos2 is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelEpsonEpos2>());
  });

  test('getPlatformVersion', () async {
    EpsonEpos2 epsonEpos2Plugin = EpsonEpos2();
    MockEpsonEpos2Platform fakePlatform = MockEpsonEpos2Platform();
    EpsonEpos2Platform.instance = fakePlatform;

    // expect(await epsonEpos2Plugin.getPlatformVersion(), '42');
  });
}
