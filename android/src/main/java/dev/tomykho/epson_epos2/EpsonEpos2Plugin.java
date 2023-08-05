package dev.tomykho.epson_epos2;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;

import java.util.HashMap;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * EpsonEpos2Plugin
 */
public class EpsonEpos2Plugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "epson_epos2");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "startDiscovery":
                startDiscovery(call, result);
                break;
            case "stopDiscovery":
                stopDiscovery(call, result);
                break;
            case "print":
                print(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void startDiscovery(MethodCall call, Result result) {
        FilterOption filterOption = new FilterOption();
        filterOption.setDeviceType(Discovery.TYPE_PRINTER);
        filterOption.setEpsonFilter(Discovery.FILTER_NAME);
        filterOption.setUsbDeviceName(Discovery.TRUE);
        try {
            Discovery.start(context, filterOption, mDiscoveryListener);
        } catch (Epos2Exception e) {
            e.printStackTrace();
        }
        result.success(null);
    }

    private void stopDiscovery(MethodCall call, Result result) {
        try {
            Discovery.stop();
        } catch (Epos2Exception e) {
            e.printStackTrace();
        }
        result.success(null);
    }

    private void print(MethodCall call, Result result) {
        result.success(null);
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("deviceName", deviceInfo.getDeviceName());
            map.put("target", deviceInfo.getTarget());
            map.put("deviceType", deviceInfo.getDeviceType());
            new Handler(Looper.getMainLooper()).post(() -> channel.invokeMethod("onDiscovery", map));
        }
    };
}
