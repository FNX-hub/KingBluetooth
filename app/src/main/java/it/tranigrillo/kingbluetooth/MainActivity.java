package it.tranigrillo.kingbluetooth;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

/*
classe MainActivity
l'older pensa a generare il layout con il metodo tabInflate
i singolo comportamenti sono rimandati ai singoli fragment
*/
public class MainActivity extends AppCompatActivity {
//    public static final String BLUETOOTH = "Bluetooth";
//    public static final String DEBUG = "debug";
//    public static final String ERROR = "error";
//    private static final String BLUETOOTH_NOT_ENABLED = ">>>>> BLUETOOTH NOT ENABLED";
//    private static final String BLUETOOTH_ENABLED = ">>>>> BLUETOOTH ENABLED ";

//    private Bluetooth bluetooth;
    private Holder holder;

    private class Holder {

        ArrayList<Fragment> fragments;
        BluetoothManager bluetooth;
        ViewPager viewPager;
        TabLayout tabLayout;

        Holder(Context context, boolean value) {
            this.bluetooth = new BluetoothManager(context);
            this.tabLayout = findViewById(R.id.tabLayout);
            this.viewPager = findViewById(R.id.viewPager);

            fragments = new ArrayList<>();
            fragments.add(new OptionFragment(context, bluetooth));
            fragments.add(new DeviceFragment(context));
        }
            void tabInflater(){
                final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
                Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getResources().getString(R.string.option));
                Objects.requireNonNull(tabLayout.getTabAt(1)).setText(getResources().getString(R.string.device));
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        else {
            this.holder = new Holder(this, true);
            holder.tabInflater();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //action if granted
                    this.holder = new Holder(this, true);
                    holder.tabInflater();
                }
                else {
                    //action if not granted
                    this.holder = new Holder(this, false);
                    holder.tabInflater();
                }
    }


    //    public class Bluetooth {
//        Context context;
//        BluetoothAdapter adapter;
//        Method setDiscoverability;
//
//        Bluetooth(Context context){
//         this.context = context;
//         this.adapter = BluetoothAdapter.getDefaultAdapter();
//         try {
//             this.setDiscoverability = BluetoothAdapter.class.getMethod("setScanMode", int.class);
//         } catch (NoSuchMethodException | IllegalArgumentException e) {
//             Log.e(ERROR, ">>>> FAILED TO GET METHOD");
//         }
//        }
//
//        Boolean isEnabled(){
//            Boolean value = adapter.isEnabled();
//            Log.d(BLUETOOTH, BLUETOOTH_ENABLED + value );
//            return value;
//        }
//
//        Boolean isSupported(){
//            if (this.adapter == null) {
//                Log.d(BLUETOOTH, ">>>>> BLUETOOTH NOT SUPPORTED");
//                return false;
//            }
//            Log.d(BLUETOOTH, ">>>>> BLUETOOTH SUPPORTED");
//            return true;
//        }
//
//        Integer enableBluetooth() {
//            if (!adapter.isEnabled()) {
//                adapter.enable();
//                try {
//                    setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
//                    Log.d(BLUETOOTH, ">>>>> BLUETOOTH ENABLED, VISIBILITY OFF");
//                    return 0;
//                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
//                    Log.e(ERROR, ">>>>> Failed to turn off bluetooth device discoverability", e);
//                    return -1;
//                }
//            }
//            Log.d(BLUETOOTH, BLUETOOTH_ENABLED);
//            return 1;
//        }
//
//        Integer disableBluetooth() {
//            if (adapter.isEnabled()) {
//                adapter.disable();
//                Log.d(BLUETOOTH, BLUETOOTH_NOT_ENABLED);
//                return 0;
//            }
//            Log.d(BLUETOOTH, BLUETOOTH_NOT_ENABLED);
//            return 1;
//        }
//
//        void enableDiscoverability() {
//            try {
//                setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
//                Log.d(BLUETOOTH, ">>>>> DISCOVERABILITY ENABLED");
//            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
//                Log.e(ERROR, "Failed to turn on bluetooth device discoverability.", e);
//            }
//        }
//
//        void disableDiscoverability() {
//            try {
//                setDiscoverability.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
//                Log.d(DEBUG, ">>>>> DISCOVERABILITY ENABLED");
//            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
//                Log.e(ERROR, "Failed to turn off bluetooth device discoverability.", e);
//            }
//        }
//
//        ArrayList<Object[]> getBoundedDevices( ArrayList<Object[]> deviceArrayList) {
//            Set<BluetoothDevice> boundedDevicesSet = adapter.getBondedDevices();
//            if (boundedDevicesSet.size() > 0) {
//                for (BluetoothDevice element : boundedDevicesSet) {
//                    String name = element.getName();
//                    String address = element.getAddress();
//                    int state = element.getBondState();
//                    int type = element.getType();
//                    Object[] device = new Object[]{name, address, state, type};
//                    deviceArrayList.add(device);
//                    Log.d(BLUETOOTH, "Device name: " + name);
//                    Log.d(BLUETOOTH, "Device address: " + address);
//                    Log.d(BLUETOOTH, "Device state: " + state);
//                    Log.d(BLUETOOTH, "Device type: " + type);
//                }
//            }
//            return deviceArrayList;
//        }
//
//        ArrayList<Object[]> startScan(final ArrayList<Object[]> deviceArrayList) {
//            if (adapter.isDiscovering()) adapter.cancelDiscovery();
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//            BroadcastReceiver receiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    String action = intent.getAction();
//                    Log.d(BLUETOOTH, ">>>> Broadcast reciver action: " + action);
//                    if (action.equals(BluetoothDevice.ACTION_FOUND)) {
//                        BluetoothDevice element = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        assert element != null;
//                        String name = element.getName();
//                        String address = element.getAddress();
//                        int state = element.getBondState();
//                        int type = element.getType();
//                        Object[] device = new Object[]{name, address, state, type};
//                        deviceArrayList.add(device);
//                        Log.d(BLUETOOTH, "Device name: " + name);
//                        Log.d(BLUETOOTH, "Device address: " + address);
//                        Log.d(BLUETOOTH, "Device state: " + state);
//                        Log.d(BLUETOOTH, "Device type: " + type);
//                    }
//                }
//            };
//            registerReceiver(receiver, filter);
//            adapter.startDiscovery();
//            return deviceArrayList;
//        }
//
//    }



}
