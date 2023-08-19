package dev.tomykho.epson_epos2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.epson.epos2.ConnectionListener;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.epos2.printer.StatusChangeListener;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
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
    private final HashMap<Integer, String> series = new HashMap<>();
    private final HashMap<Integer, String> langModels = new HashMap<>();
    private final HashMap<String, Map<Integer, String>> statuses = new HashMap<>();
    private final Map<Integer, String> errors = new HashMap<>();
    private final Map<Integer, String> connections = new HashMap<>();
    private Printer  mPrinter = null;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "epson_epos2");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        mPrinter.setReceiveEventListener(null);
        mPrinter.setConnectionEventListener(null);
        mPrinter = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "init":
                init(call, result);
                break;
            case "startDiscovery":
                startDiscovery(call, result);
                break;
            case "stopDiscovery":
                stopDiscovery(call, result);
                break;
            case "getSeries":
                getSeries(call, result);
                break;
            case "getLangModels":
                getLangModels(call, result);
                break;
            case "setPrinter":
                setPrinter(call, result);
                break;
            case "connect":
                connect(call, result);
                break;
            case "disconnect":
                disconnect(call, result);
                break;
            case "getStatus":
                getStatus(call, result);
                break;
            case "print":
                print(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void init(MethodCall call, Result result) {
        Field[] statusFields = PrinterStatusInfo.class.getDeclaredFields();
        List<String> boolStatusNames = Arrays.asList("online", "connection", "coverOpen", "paperFeed");
        for (Field field : statusFields) {
            String name = field.getName();
            Map<Integer, String> map = new HashMap<>();
            if (boolStatusNames.contains(name)) {
                map.put(Printer.FALSE, "FALSE");
                map.put(Printer.TRUE, "TRUE");
            }
            if (name.equals("autoRecoverError")) {
                map.put(Printer.WRONG_PAPER, "WRONG_PAPER");
            }
            map.put(Printer.UNKNOWN, "UNKNOWN");
            statuses.put(name, map);
        }
        Field[] errorFields = Epos2Exception.class.getFields();
        for (Field field : errorFields) {
            String name = field.getName();
            try {
                Integer id = (Integer) field.get(null);
                errors.put(id, name);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Field[] connectionFields = ConnectionListener.class.getFields();
        for (Field field : connectionFields) {
            String name = field.getName();
            try {
                Integer id = (Integer) field.get(null);
                connections.put(id, name);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Field[] fields = Printer.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (field.getType().equals(Integer.TYPE)) {
                try {
                    Integer id = (Integer) field.get(null);
                    if (name.startsWith("TM_")) {
                        series.put(id, name);
                    }
                    if (name.startsWith("MODEL_")) {
                        langModels.put(id, name);
                    }
                    // Printer statuses
                    if (name.startsWith("PAPER_")) {
                        Objects.requireNonNull(statuses.get("paper")).put(id, name);
                    }
                    if (name.startsWith("SWITCH_")) {
                        Objects.requireNonNull(statuses.get("panelSwitch")).put(id, name);
                    }
                    if (name.endsWith("_ERR")) {
                        if (name.endsWith("_VOLTAGE_ERR")) {
                            Objects.requireNonNull(statuses.get("unrecoverError")).put(id, name);
                        } else {
                            Objects.requireNonNull(statuses.get("errorStatus")).put(id, name);
                        }
                    }
                    if (name.endsWith("_OVERHEAT")) {
                        Objects.requireNonNull(statuses.get("autoRecoverError")).put(id, name);
                    }
                    if (name.startsWith("BATTERY_LEVEL_")) {
                        Objects.requireNonNull(statuses.get("batteryLevel")).put(id, name);
                    }
                    if (name.startsWith("REMOVAL_WAIT_")) {
                        Objects.requireNonNull(statuses.get("removalWaiting")).put(id, name);
                    }
                    if (name.startsWith("REMOVAL_WAIT_")) {
                        Objects.requireNonNull(statuses.get("removalWaiting")).put(id, name);
                    }

                } catch (Exception e) {
                    Log.e("name", name);
                    e.printStackTrace();
                }
            }
        }
        result.success(null);
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

    private void getSeries(MethodCall call, Result result) {
        result.success(series);
    }

    private void getLangModels(MethodCall call, Result result) {
        result.success(langModels);
    }

    private void setPrinter(MethodCall call, Result result) {
        Integer series = call.argument("series");
        Integer langModel = call.argument("langModel");
        if (series != null && langModel != null) {
            try {
                mPrinter = new Printer(series, langModel, context);
                mPrinter.setReceiveEventListener(mReceiveListener);
                mPrinter.setConnectionEventListener(mConnectionListener);
                mPrinter.startMonitor();
            } catch (Epos2Exception e) {
                throw new RuntimeException(e);
            }
        }
        result.success(null);
    }

    private void connect(MethodCall call, Result result) {
        if (mPrinter != null) {
            String target = call.argument("target");
            try {
                mPrinter.connect(target, Printer.PARAM_DEFAULT);
            } catch (Epos2Exception e) {
                result.error("" + e.getErrorStatus(), errors.get(e.getErrorStatus()), e);
                return;
            }
        }
        result.success(null);
    }

    private void disconnect(MethodCall call, Result result) {
        if (mPrinter != null) {
            try {
                mPrinter.disconnect();
            } catch (Epos2Exception e) {
                result.error("" + e.getErrorStatus(), errors.get(e.getErrorStatus()), e);
                return;
            }
        }
        result.success(null);
    }

    private void getStatus(MethodCall call, Result result) {
        if (mPrinter == null) {
            result.success(null);
            return;
        }
        Map<String, String> status = new HashMap<>();
        PrinterStatusInfo info = mPrinter.getStatus();
        Field[] fields = PrinterStatusInfo.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String name = field.getName();
                Integer id = (Integer) field.get(info);
                if (id != null) {
                    Map<Integer, String> map = statuses.get(name);
                    if (map != null) {
                        String value = map.get(id);
                        if (value != null) {
                            status.put(name, value);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        result.success(status);
    }

    private void print(MethodCall call, Result result) {
        result.success(null);
    }

    private final ReceiveListener mReceiveListener = new ReceiveListener() {
        @Override
        public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {

        }
    };

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
    private final ConnectionListener mConnectionListener = new ConnectionListener() {
        @Override
        public void onConnection(Object o, int id) {
            String event = connections.get(id);
            new Handler(Looper.getMainLooper()).post(() -> channel.invokeMethod("onConnection", event));
        }
    };
}
