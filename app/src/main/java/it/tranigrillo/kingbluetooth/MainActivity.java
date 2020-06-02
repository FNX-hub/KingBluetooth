package it.tranigrillo.kingbluetooth;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int DISCOVERABILITY_TIME = 300;

    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList;
    BluetoothAdapter bluetoothAdapter;
    Switch swtOnOff;
    Switch swtVisibility;
    TextView tvTimer;
    CardView cvAction;
    TextView tvScan;
    TextView tvInfo;
    ProgressBar progressBar;
    TextView tvAction;
    RecyclerView recyclerView;
    Adapter recycleAdapter;
    CountDownTimer timer = null;
    Map<String, String> connection;
    BluetoothService bluetoothService;
    StringBuilder builder = new StringBuilder();

    //    reciever for ACTION_FOUND
    private final BroadcastReceiver actionFound = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//        when deiscovery find a device
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "actionFoundReceiver: STATE OFF");
                        swtOnOff.setText(getText(R.string.off));
                        swtOnOff.setChecked(false);
                        swtVisibility.setEnabled(false);
                        cvAction.setEnabled(false);
                        tvTimer.setVisibility(View.GONE);
                        recycleAdapter.removeAll();
                        connection = new HashMap<>();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "actionFoundReceiver: STATE TURNING OFF");
                        bluetoothService.stop();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "actionFoundReceiver: STATE ON");
                        swtOnOff.setChecked(true);
                        swtOnOff.setText(getText(R.string.on));
                        swtVisibility.setEnabled(true);
                        cvAction.setEnabled(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        bluetoothService = new BluetoothService(MainActivity.this, bluetoothAdapter);
                        Log.d(TAG, "actionFoundReceiver: STATE TURNING ON");
                        break;
                    default:
                }
            }
        }
    };


    //    reciever for Scan Mode
    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//        when deiscovery scan mode
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "scanModeReceiver: Discoverability Disabled. Able to receive connections.");
                        swtVisibility.setText(getText(R.string.not_visible));
                        swtVisibility.setChecked(false);
                        tvTimer.setVisibility(View.GONE);
                        if (timer != null) timer.cancel();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "scanModeReceiver: Discoverability Enabled.");
                        swtVisibility.setText(getText(R.string.visible));
                        swtVisibility.setChecked(true);
                        tvTimer.setVisibility(View.VISIBLE);
                        timer = new CountDownTimer(DISCOVERABILITY_TIME * 1000, 1000) {
                            Integer remaning = DISCOVERABILITY_TIME;

                            @Override
                            public void onTick(long millisUntilFinished) {
                                int min = remaning / 60;
                                int sec = remaning % 60;
                                tvTimer.setText(String.format("%s: %02d:%02d", getResources().getString(R.string.visibility_time_t), min, sec));
                                remaning--;
                            }

                            @Override
                            public void onFinish() {
                                tvTimer.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), R.string.ToastTextDiscoverabilityOff, Toast.LENGTH_SHORT).show();
                                enableDisableVisibility(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
                            }
                        };
                        timer.start();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "scanModeReceiver: Discoverability Disabled. Not able to receive connections.");
                        swtVisibility.setText(getText(R.string.not_visible));
                        swtVisibility.setChecked(false);
                        if (timer != null) timer.cancel();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "scanModeReceiver: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "scanModeReceiver: Connected.");
                        break;
                    default:
                }
            }
        }
    };

    //    // reciver for discovery
    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == BluetoothAdapter.ACTION_DISCOVERY_STARTED) {
                Log.d(TAG, "discoveryReceiver: ACTION_DISCOVERY_STARTED");
//                bluetoothService.start();
                recycleAdapter.removeAll();
                tvScan.setText(getText(R.string.scanning));
                tvAction.setText(getText(R.string.stop_scan));
                progressBar.setVisibility(View.VISIBLE);
            }
            if (action == BluetoothDevice.ACTION_FOUND) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                recycleAdapter.add(device);
                assert device.getName() != null;
                Log.d(TAG, "discoveryReceiver: " + device.getName() + ": " + device.getAddress());
            }
            if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Log.d(TAG, "discoveryReceiver: ACTION_DISCOVERY_FINISHED");
                tvScan.setText(getText(R.string.available));
                tvAction.setText(getText(R.string.scan));
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    };

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, ""+action);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                //3 cases:
                //case1: bonded already
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    bluetoothService = new BluetoothService(MainActivity.this, bluetoothAdapter);
                    recycleAdapter.add(device);
                    Log.d(TAG, String.format("pairingReceiver: %s BOND_BONDED.", device.getName()));
                }
                //case2: creating a bone
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, String.format("pairingReceiver: %s BOND_BONDING.", device.getName()));
                }
                //case3: breaking a bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    recycleAdapter.notifyDataSetChanged();
                    Log.d(TAG, String.format("pairingReceiver: %s BOND_NONE.", device.getName()));
                }
            }
            if (action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                connection.remove(device.getAddress());
                recycleAdapter.notifyDataSetChanged();
            }
            if (action == "connectionData") {
                try {
                    connection.put(intent.getStringExtra("device"), intent.getStringExtra("connection"));
                } catch (NullPointerException e) {
                    connection  = new HashMap<>();
                }
                recycleAdapter.notifyDataSetChanged();
            }
            if (action == "messagesData"){
                String message = intent.getStringExtra("message");
                builder.append(message);
                Toast.makeText(context, builder.toString(), Toast.LENGTH_LONG).show();
                builder.delete(0, builder.length());
            }
        }
    };

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        ArrayList<BluetoothDevice> bluetoothDeviceArrayList;

        public Adapter(ArrayList<BluetoothDevice> bluetoothDeviceArrayList) {
            this.bluetoothDeviceArrayList = bluetoothDeviceArrayList;

        }

        @NonNull
        @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final Adapter.ViewHolder holder, int position) {
            BluetoothDevice device = bluetoothDeviceArrayList.get(position);
            if (device.getName() != null) {
                holder.deviceName.setText(device.getName());
            } else holder.deviceName.setText(device.getAddress());

            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    holder.deviceStatus.setText(R.string.bounded);
                    holder.ivConnect.setImageResource(R.drawable.ic_bluetooth_disabled);
                    holder.ivSend.setImageResource(R.drawable.ic_delete);
                    if (connection.get(device.getAddress()) == (String) getText(R.string.connected)) {
                        holder.deviceStatus.setText(R.string.connected);
                        holder.ivConnect.setImageResource(R.drawable.ic_bluetooth_connected);
                        holder.ivSend.setImageResource(R.drawable.ic_send);
                    }
                    break;

                case BluetoothDevice.BOND_BONDING:
                    holder.deviceStatus.setText(R.string.bonding);

                case BluetoothDevice.BOND_NONE:
                    holder.deviceStatus.setText(R.string.not_bounded);
                    holder.ivConnect.setImageResource(R.drawable.ic_bluetooth_disabled);
                    holder.ivSend.setImageResource(R.drawable.ic_add);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return bluetoothDeviceArrayList.size();
        }

        public void add(BluetoothDevice device) {
            if (!bluetoothDeviceArrayList.contains(device)){
                bluetoothDeviceArrayList.add(device);
                notifyItemInserted(getItemCount());
            }
            else notifyDataSetChanged();
        }

        public void removeAll() {
            int end = getItemCount();
            bluetoothDeviceArrayList.clear();
            notifyItemRangeRemoved(0, end);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView deviceName;
            TextView deviceStatus;
            ImageView ivConnect;
            ImageView ivSend;
            CardView cvConnect;
            CardView cvSend;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                deviceName = itemView.findViewById(R.id.tvDevice);
                deviceStatus = itemView.findViewById(R.id.tvStatus);
                ivConnect = itemView.findViewById(R.id.ivConnect);
                cvConnect = itemView.findViewById(R.id.cvConnect);
                cvConnect.setOnClickListener(this);
                ivSend = itemView.findViewById(R.id.ivSend);
                cvSend = itemView.findViewById(R.id.cvSend);
                cvSend.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                BluetoothDevice device = bluetoothDeviceArrayList.get(getAdapterPosition());
                if (deviceStatus.getText() == getText(R.string.not_bounded) & (v.getId() == R.id.cvSend)) {
                    device.createBond();
                }
                if (deviceStatus.getText() == getText(R.string.bounded) & (v.getId() == R.id.cvSend)) {
                    unBoundDevice(device);
                }
                if (deviceStatus.getText() == getText(R.string.bounded) & v.getId() == R.id.cvConnect) {
                    bluetoothService.startClient(device);
                    notifyDataSetChanged();

                }
                if (deviceStatus.getText() == getText(R.string.connected) & v.getId() == R.id.cvConnect) {
                    bluetoothService.stop();
                    bluetoothService = new BluetoothService(MainActivity.this, bluetoothAdapter);
                    if (connection.get(device.getAddress()) != null) connection.remove(device.getAddress());
                    connection.put(device.getAddress(), (String) getText(R.string.not_connected));
                    notifyDataSetChanged();
                }

                if (deviceStatus.getText() == getText(R.string.not_bounded) & v.getId() == R.id.cvConnect) {
                    Toast.makeText(MainActivity.this, "You must pair first", Toast.LENGTH_SHORT).show();
                }
                if (deviceStatus.getText() == getText(R.string.connected) & v.getId() == R.id.cvSend) {
                    byte[] messaggio = String.format("Ricevuto messaggio da %s", Build.MODEL).getBytes(Charset.defaultCharset());
                    bluetoothService.write(messaggio);
                }
            }
        }
    }

    private void enableDisableVisibility(int scanMode){
        try {
            Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
            method.invoke(bluetoothAdapter, scanMode);
        }
        catch ( NoSuchMethodException |IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "Failed to turn on bluetooth device discoverability.", e);
        }
    }

    void unBoundDevice(BluetoothDevice device){
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        }
        catch ( NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, String.format(">>>>> Failed to unPair bluetooth device %s", device.getAddress()), e);
        }
    }

    /**
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     */
    private void checkPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }


    class Holder implements View.OnClickListener {
        Context context;
        public Holder(Context context) {
            this.context = context;
            tvInfo = findViewById(R.id.tvInfo);
            tvTimer = findViewById(R.id.tvRemainigTime);
            tvTimer.setVisibility(View.GONE);
            swtOnOff = findViewById(R.id.swtBtOnOff);
            swtOnOff.setChecked(bluetoothAdapter.isEnabled());
            if (bluetoothAdapter.isEnabled()) swtOnOff.setText(R.string.on);
            swtVisibility = findViewById(R.id.swtBtVisible);
            swtVisibility.setChecked(bluetoothAdapter.getScanMode()==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            cvAction = findViewById(R.id.cvAction);
            tvAction = findViewById(R.id.tvDevice);
            tvScan = findViewById(R.id.tvScan);
            progressBar = findViewById(R.id.progressBar);
            cvAction.setOnClickListener(this);
            swtOnOff.setOnClickListener(this);
            swtVisibility.setOnClickListener(this);

            bluetoothDeviceArrayList = new ArrayList<>();
            recyclerView = findViewById(R.id.rvDevices);
            recycleAdapter = new Adapter(bluetoothDeviceArrayList);
            recyclerView.setAdapter(recycleAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.swtBtOnOff:
                    if (bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.disable();
                    } else {
                        bluetoothAdapter.enable();
                    }
                    break;
                case R.id.swtBtVisible: {
                    switch (bluetoothAdapter.getScanMode()){
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            enableDisableVisibility(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            enableDisableVisibility(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
                            break;
                        default: break;
                    }
                    break;
                }
                case R.id.cvAction:
                    checkPermissions();
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    } else {
                        bluetoothAdapter.startDiscovery();
                    }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connection = new HashMap<>();
        IntentFilter onOffFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter visibilityFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter connectionFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        connectionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        connectionFilter.addAction("connectionData");
        connectionFilter.addAction("messagesData");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())bluetoothService = new BluetoothService(this, bluetoothAdapter);

        registerReceiver(actionFound, onOffFilter);
        registerReceiver(scanModeReceiver, visibilityFilter);
        registerReceiver(discoveryReceiver, discoveryFilter);
        registerReceiver(connectionReceiver, connectionFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionReceiver, connectionFilter);
        if (bluetoothAdapter == null) onDestroy();
        new Holder(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(actionFound);
        unregisterReceiver(scanModeReceiver);
        unregisterReceiver(discoveryReceiver);
        unregisterReceiver(connectionReceiver);

    }
}

