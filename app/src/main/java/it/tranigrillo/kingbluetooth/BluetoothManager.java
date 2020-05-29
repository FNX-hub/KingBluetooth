package it.tranigrillo.kingbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

class BluetoothManager {

    private static final String BLUETOOTH = "Bluetooth";
    private static final String DEBUG = "debug";
    private static final String ERROR = "error";
    private static final String BLUETOOTH_NOT_ENABLED = ">>>>> BLUETOOTH NOT ENABLED";
    private static final String BLUETOOTH_ENABLED = ">>>>> BLUETOOTH ENABLED ";

    private Context context;
    BluetoothAdapter adapter;
    private boolean discovering = false;

    BluetoothManager(Context context){
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    Boolean isEnabled(){
        Boolean value = adapter.isEnabled();
        Log.d(BLUETOOTH, BLUETOOTH_ENABLED + value );
        return value;
    }

    Boolean isSupported(){
        if (this.adapter == null) {
            Log.d(BLUETOOTH, ">>>>> BLUETOOTH NOT SUPPORTED");
            return false;
        }
        Log.d(BLUETOOTH, ">>>>> BLUETOOTH SUPPORTED");
        return true;
    }

    void enableDiscoverability() {
        try {
            Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            method.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            Log.d(BLUETOOTH, ">>>>> DISCOVERABILITY ENABLED");
        } catch ( NoSuchMethodException |IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(ERROR, "Failed to turn on bluetooth device discoverability.", e);
        }
    }

    void disableDiscoverability() {
        try {
            Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            method.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            Log.d(DEBUG, ">>>>> DISCOVERABILITY ENABLED");
        } catch ( NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(ERROR, "Failed to turn off bluetooth device discoverability.", e);
        }
    }

    Integer enableBluetooth() {
        if (!adapter.isEnabled()) {
            adapter.enable();
            disableDiscoverability();
            Log.d(BLUETOOTH, ">>>>> BLUETOOTH ENABLED, VISIBILITY OFF");
            return 0;
        }
        Log.d(BLUETOOTH, BLUETOOTH_ENABLED);
        return 1;
    }

    Integer disableBluetooth() {
        if (adapter.isEnabled()) {
            adapter.disable();
            Log.d(BLUETOOTH, BLUETOOTH_NOT_ENABLED);
            return 0;
        }
        Log.d(BLUETOOTH, BLUETOOTH_NOT_ENABLED);
        return 1;
    }

    boolean isDiscovering(){
        return discovering;
    }

    void getBoundedDevices(List<Device> deviceArrayList, CustomAdapter viewAdapter) {
        deviceArrayList.clear();
        viewAdapter.removeAll();
        Set<BluetoothDevice> boundedDevicesSet = adapter.getBondedDevices();
        if (boundedDevicesSet.size() > 0) {
            for (BluetoothDevice element : boundedDevicesSet) {
                String name = element.getName();
                String address = element.getAddress();
                int state = element.getBondState();
                int type = element.getType();
                Object[] data = new Object[]{name, address, state, type};
                Device device = new Device(data);
                viewAdapter.addItem(device);
                Log.d(BLUETOOTH, "Device name: " + name);
                Log.d(BLUETOOTH, "Device address: " + address);
                Log.d(BLUETOOTH, "Device state: " + state);
                Log.d(BLUETOOTH, "Device type: " + type);
            }
        }
    }

    Receiver startScanAvailableDevice(List<Device> deviceArrayList, CustomAdapter viewAdapter) {
        deviceArrayList.clear();
        viewAdapter.removeAll();
        if (discovering) {
            adapter.cancelDiscovery();
            discovering = false;
            deviceArrayList.clear();
            viewAdapter.removeAll();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        Receiver receiver = new Receiver(deviceArrayList, viewAdapter);
        context.registerReceiver(receiver, filter);
        adapter.startDiscovery();
        discovering = true;
        Log.d(BLUETOOTH, "scan: " + deviceArrayList.size());
        return receiver;
    }

    void stopScanAvailableDevice(Receiver receiver) {
        context.unregisterReceiver(receiver);
    }

    void boundDevice(String mac){
        BluetoothDevice device = adapter.getRemoteDevice(mac);
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch ( NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(ERROR, String.format(">>>>> Failed to pair bluetooth device %s", mac), e);
        }
    }

    void unBoundDevice(String mac){
        Set<BluetoothDevice> setBoundedDevice = adapter.getBondedDevices();
        for (BluetoothDevice element : setBoundedDevice) {
            if (element.getAddress().equals(mac)) {
                try {
                    Method method = element.getClass().getMethod("removeBond", (Class[]) null);
                    method.invoke(element, (Object[]) null);
                } catch ( NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    Log.e(ERROR, String.format(">>>>> Failed to unPair bluetooth device %s", mac), e);
                }
            }
        }
    }

    public class Receiver extends BroadcastReceiver {

        private final CustomAdapter viewAdapter;
        private final List<Device> deviceArrayList;


        public Receiver(List<Device> deviceArrayList, CustomAdapter viewAdapter) {
            this.viewAdapter = viewAdapter;
            this.deviceArrayList = deviceArrayList;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(BLUETOOTH, ">>>> Broadcast reciver action: " + action);
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                discovering = false;
                Log.d(BLUETOOTH, ">>>> discovering: " + discovering);
            }
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice element = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name;
                if (element.getName() == null) {
                    name = "No name";
                } else {
                    name = element.getName();
                }
                String address = element.getAddress();
                int state = element.getBondState();
                int type = element.getType();
                Object[] data = new Object[]{name, address, state, type};
                Device device = new Device(data);
                if (!deviceArrayList.contains(device)) {
                    deviceArrayList.add(device);
                    viewAdapter.notifyItemInserted(deviceArrayList.indexOf(device));
                }
                Log.d(BLUETOOTH, "Device name: " + name);
                Log.d(BLUETOOTH, "Device address: " + address);
                Log.d(BLUETOOTH, "Device state: " + state);
                Log.d(BLUETOOTH, "Device type: " + type);
                Log.d(BLUETOOTH, "scan: " + deviceArrayList.size());
            }
        }
    }
}
