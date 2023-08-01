package dev.tomykho.epson_epos2;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;

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
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "startDiscovery":
                startDiscovery(call, result);
                break;
            case "stopDiscovery":
                stopDiscovery(call, result);
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
            if(e.getErrorStatus() == Epos2Exception.ERR_CONNECT)
            {
                Log.e("testing", "error connect");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_ALREADY_OPENED)
            {
                Log.e("testing", "already open");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_ALREADY_USED)
            {
                Log.e("testing", "already used");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_BOX_CLIENT_OVER)
            {
                Log.e("testing", "box client over");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_BOX_COUNT_OVER)
            {
                Log.e("testing", "count over");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_DISCONNECT)
            {
                Log.e("testing", "disconnect");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_FAILURE)
            {
                Log.e("testing", "failure");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_ILLEGAL)
            {
                Log.e("testing", "illegal");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_IN_USE)
            {
                Log.e("testing", "in use");
            }
            if(e.getErrorStatus() == Epos2Exception.ERR_MEMORY)
            {
                Log.e("testing", "memory");
            }
            e.printStackTrace();
        }
        result.success(null);
    }

    private void stopDiscovery(MethodCall call, Result result) {
        try {
            Discovery.stop();
        } catch (Epos2Exception e) {
            throw new RuntimeException(e);
        }
        result.success(null);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            HashMap<String, String> map = new HashMap<>();
            map.put("deviceName", deviceInfo.getDeviceName());
            map.put("target", deviceInfo.getTarget());
            channel.invokeMethod("onDiscovery", map);
        }
    };
}
