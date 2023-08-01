import 'package:epson_epos2/discovery_callback.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:epson_epos2/epson_epos2.dart';
import 'package:epson_epos2/epson_epos2_platform_interface.dart';
import 'package:epson_epos2/epson_epos2_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockEpsonEpos2Platform
    with MockPlatformInterfaceMixin
    implements EpsonEpos2Platform {
  @override
  Future<void> startDiscovery() => Future.value();
  @override
  Future<void> stopDiscovery() => Future.value();
  @override
  void setDiscoveryCallback(DiscoveryCallback callback) {}
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
