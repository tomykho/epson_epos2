import 'package:epson_epos2/device_info.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:epson_epos2/epson_epos2.dart';
import 'package:permission_handler/permission_handler.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  Map<Permission, PermissionStatus> statuses = await [
    Permission.bluetoothConnect,
    Permission.bluetoothScan,
  ].request();
  print(statuses);
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final List<DeviceInfo> _deviceInfos = [];

  @override
  void initState() {
    super.initState();
    EpsonEpos2.startDiscovery();
    EpsonEpos2.setDiscoveryCallback((deviceInfo) {
      setState(() {
        _deviceInfos.add(deviceInfo);
      });
    });
  }

  @override
  void dispose() {
    EpsonEpos2.stopDiscovery();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ListView.builder(
            itemCount: _deviceInfos.length,
            itemBuilder: (context, index) {
              var deviceInfo = _deviceInfos[index];
              return ListTile(
                title: Text("${deviceInfo.deviceName}"),
                subtitle: Text("${deviceInfo.target}"),
              );
            }),
      ),
    );
  }
}
