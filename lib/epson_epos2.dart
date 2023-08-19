import 'package:epson_epos2/discovery_callback.dart';

import 'epson_epos2_platform_interface.dart';

class EpsonEpos2 {
  static Future<void> init() {
    return EpsonEpos2Platform.instance.init();
  }

  static Future<void> startDiscovery() {
    return EpsonEpos2Platform.instance.startDiscovery();
  }

  static Future<void> stopDiscovery() {
    return EpsonEpos2Platform.instance.stopDiscovery();
  }

  static Future<Map<int, String>?> getSeries() {
    return EpsonEpos2Platform.instance.getSeries();
  }

  static Future<Map<int, String>?> getLangModels() {
    return EpsonEpos2Platform.instance.getLangModels();
  }

  static Future<void> setPrinter(int series, int langModel) {
    return EpsonEpos2Platform.instance.setPrinter(series, langModel);
  }

  static Future<Map<String, String>?> getStatus() {
    return EpsonEpos2Platform.instance.getStatus();
  }

  static Future<void> connect(String target) {
    return EpsonEpos2Platform.instance.connect(target);
  }

  static Future<void> disconnect() {
    return EpsonEpos2Platform.instance.disconnect();
  }

  static void setDiscoveryCallback(DiscoveryCallback callback) {
    return EpsonEpos2Platform.instance.setDiscoveryCallback(callback);
  }
}
