import 'package:epson_epos2/discovery_callback.dart';

import 'epson_epos2_platform_interface.dart';

class EpsonEpos2 {
  static Future<void> startDiscovery() {
    return EpsonEpos2Platform.instance.startDiscovery();
  }

  static Future<void> stopDiscovery() {
    return EpsonEpos2Platform.instance.stopDiscovery();
  }

  static void setDiscoveryCallback(DiscoveryCallback callback) {
    return EpsonEpos2Platform.instance.setDiscoveryCallback(callback);
  }
}
