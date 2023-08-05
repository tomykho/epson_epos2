class DeviceInfo {
  final String deviceName;
  final String target;
  final int deviceType;

  DeviceInfo({
    required this.deviceName,
    required this.target,
    required this.deviceType,
  });

  factory DeviceInfo.fromJson(Map<String, dynamic> json) => DeviceInfo(
        deviceName: json['deviceName'],
        target: json['target'],
        deviceType: json['deviceType'],
      );

  Map<String, dynamic> toJson() => <String, dynamic>{
        'deviceName': deviceName,
        'target': target,
        'deviceType': deviceType,
      };
}
