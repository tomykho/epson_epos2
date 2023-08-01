class DeviceInfo {
  final String? deviceName;
  final String? target;

  DeviceInfo({
    this.deviceName,
    this.target,
  });

  factory DeviceInfo.fromJson(Map<String, dynamic> json) => DeviceInfo(
        deviceName: json['deviceName'],
        target: json['target'],
      );
}
