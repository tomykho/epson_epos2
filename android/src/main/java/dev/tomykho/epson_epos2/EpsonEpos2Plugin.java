package dev.tomykho.epson_epos2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private final Map<Integer, String> events = new HashMap<>();
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
        mPrinter.setStatusChangeEventListener(null);
        try {
            mPrinter.stopMonitor();
            mPrinter.disconnect();
        } catch (Epos2Exception ignored) {
        }
        mPrinter.clearCommandBuffer();
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
            case "sendData":
                sendData(call, result);
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
        Field[] fields = Printer.class.getFields();
        List<String> excludeEvents = Arrays.asList("EVENT_RECONNECTING", "EVENT_RECONNECT", "EVENT_DISCONNECT");
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
                    if (name.startsWith("EVENT_") && !excludeEvents.contains(name)) {
                        events.put(id, name);
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
                mPrinter.setStatusChangeEventListener(mStatusChangeListener);
            } catch (Epos2Exception e) {
                result.error("" + e.getErrorStatus(), errors.get(e.getErrorStatus()), e);
                return;
            }
        }
        result.success(null);
    }

    private void connect(MethodCall call, Result result) {
        if (mPrinter != null) {
            String target = call.argument("target");
            try {
                mPrinter.connect(target, Printer.PARAM_DEFAULT);
                mPrinter.startMonitor();
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
                mPrinter.stopMonitor();
                mPrinter.disconnect();
            } catch (Epos2Exception e) {
                result.error("" + e.getErrorStatus(), errors.get(e.getErrorStatus()), e);
                return;
            }
            mPrinter.clearCommandBuffer();
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

    private void sendData(MethodCall call, Result result) {
        if (mPrinter == null) {
            return;
        }
        if (!createReceiptData()) {
            return;
        }
        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            Log.e("print", "sendData");
        }

        result.success(null);
    }

    private boolean createReceiptData() {
        String method = "";
//        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

//            method = "addImage";
//            mPrinter.addImage(logoData, 0, 0,
//                    logoData.getWidth(),
//                    logoData.getHeight(),
//                    Printer.COLOR_1,
//                    Printer.MODE_MONO,
//                    Printer.HALFTONE_DITHER,
//                    Printer.PARAM_DEFAULT,
//                    Printer.COMPRESS_AUTO);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("400 OHEIDA 3PK SPRINGF  9.99 R\n");
            textData.append("410 3 CUP BLK TEAPOT    9.99 R\n");
            textData.append("445 EMERIL GRIDDLE/PAN 17.99 R\n");
            textData.append("438 CANDYMAKER ASSORT   4.99 R\n");
            textData.append("474 TRIPOD              8.99 R\n");
            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n");
            textData.append("458 AQUA MICROTERRY SC  6.99 R\n");
            textData.append("493 30L BLK FF DRESS   16.99 R\n");
            textData.append("407 LEVITATING DESKTOP  7.99 R\n");
            textData.append("441 **Blue Overprint P  2.99 R\n");
            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n");
            textData.append("461 WESTGATE BLACK 25  59.99 R\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("Purchased item total number\n");
            textData.append("Sign Up and Save !\n");
            textData.append("With Preferred Saving Card\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            mPrinter.clearCommandBuffer();
            Log.e("createReceiptData", method);
            return false;
        }
        textData = null;
        return true;
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

    private final StatusChangeListener mStatusChangeListener = new StatusChangeListener() {
        @Override
        public void onPtrStatusChange(Printer printer, int i) {
            String event = events.get(i);
            if (event != null) {
                new Handler(Looper.getMainLooper()).post(() -> channel.invokeMethod("onPtrStatusChange", event));
            }
        }
    };
}
