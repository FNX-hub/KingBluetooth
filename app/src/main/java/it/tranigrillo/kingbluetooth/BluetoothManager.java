package it.tranigrillo.kingbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static it.tranigrillo.kingbluetooth.MainActivity.DEBUG;

class BluetoothManager {
    private static final String BLUETOOTH_NOT_ENABLED = ">>>>> BLUETOOTH NOT ENABLED";
    private static final String BLUETOOTH_ENABLED = ">>>>> BLUETOOTH ENABLED";

    private BluetoothAdapter adapter;
    private Method setDiscoverability;

    BluetoothManager() {
        this.adapter = BluetoothAdapter.getDefaultAdapter(); //ottieni un bluetooth adapter
        try {
            this.setDiscoverability = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            }
        catch (NoSuchMethodException | IllegalArgumentException e) {
            Log.e(DEBUG, "Failed to get metod", e);
        }
    }

    boolean isEnabled() {
        return adapter.isEnabled();
    }

    boolean isSupported(){
        if(this.adapter == null){
            Log.d(DEBUG, ">>>>> BLUETOOTH NOT SUPPORTED");
            return false;
        }
        Log.d(DEBUG, ">>>>> BLUETOOTH SUPPORTED");
        return true;
    }

    Integer enableBluetooth() {
        if (!adapter.isEnabled()) {
            adapter.enable();
            try {
                setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
                Log.d(DEBUG, ">>>>> BLUETOOTH ENABLED, VISIBILITY OFF");
                return 0;
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(DEBUG, ">>>>> Failed to turn off bluetooth device discoverability", e);
                return -1;
            }
        }
        Log.d(DEBUG, BLUETOOTH_ENABLED);
        return 1;
    }

    Integer disableBluetooth() {
        if (adapter.isEnabled()) {
            adapter.disable();
            Log.d(DEBUG, BLUETOOTH_NOT_ENABLED);
            return 0;
        }
        Log.d(DEBUG, BLUETOOTH_NOT_ENABLED);
        return 1;
    }

    void enableDiscoverability() {
        try {
            setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            Log.d(DEBUG, ">>>>> DISCOVERABILITY ENABLED");
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(DEBUG, "Failed to turn on bluetooth device discoverability.", e);
        }
    }

    void disableDiscoverability() {
        try {
            setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(DEBUG, "Failed to turn off bluetooth device discoverability.", e);
        }
        Log.d(DEBUG, ">>>>> DISCOVERABILITY ENABLED");
    }
}
