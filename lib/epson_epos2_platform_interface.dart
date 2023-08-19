import 'package:epson_epos2/callbacks.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'epson_epos2_method_channel.dart';

abstract class EpsonEpos2Platform extends PlatformInterface {
  /// Constructs a EpsonEpos2Platform.
  EpsonEpos2Platform() : super(token: _token);

  static final Object _token = Object();

  static EpsonEpos2Platform _instance = MethodChannelEpsonEpos2();

  /// The default instance of [EpsonEpos2Platform] to use.
  ///
  /// Defaults to [MethodChannelEpsonEpos2].
  static EpsonEpos2Platform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [EpsonEpos2Platform] when
  /// they register themselves.
  static set instance(EpsonEpos2Platform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> init() {
    throw UnimplementedError('startDiscovery() has not been implemented.');
  }

  Future<void> startDiscovery() {
    throw UnimplementedError('startDiscovery() has not been implemented.');
  }

  Future<void> stopDiscovery() {
    throw UnimplementedError('stopDiscovery() has not been implemented.');
  }

  Future<Map<int, String>?> getSeries() {
    throw UnimplementedError('getSeries() has not been implemented.');
  }

  Future<Map<int, String>?> getLangModels() {
    throw UnimplementedError('getLangModels() has not been implemented.');
  }

  Future<void> setPrinter(int series, int langModel) {
    throw UnimplementedError('getStatus() has not been implemented.');
  }

  Future<Map<String, String>?> getStatus() {
    throw UnimplementedError('getStatus() has not been implemented.');
  }

  Future<void> connect(String target) {
    throw UnimplementedError('connect() has not been implemented.');
  }

  Future<void> disconnect() {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  void setDiscoveryCallback(DiscoveryCallback? callback) {
    throw UnimplementedError(
        'setDiscoveryCallback() has not been implemented.');
  }

  void setPtrStatusChangeCallback(PtrStatusChangeCallback? callback) {
    throw UnimplementedError(
        'setPtrStatusChangeCallback() has not been implemented.');
  }
}
