package it.tranigrillo.kingbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothManager extends Activity {
    Context context;
    BluetoothAdapter adapter;

    public BluetoothManager(Context context){
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter(); //ottieni un bluetooth adapter
    }

    public Boolean isSupported(){
        if(this.adapter == null){
            Log.d("Debug", ">>>>> BLUETOOTH NOT SUPPORTED");
            return false;
        }
        Log.d("Debug", ">>>>> BLUETOOTH SUPPORTED");
        return true;
    }

    public Integer enableBluetooth(){
        if (!adapter.isEnabled()) {
            adapter.enable();
            Log.d("Debug", ">>>>> BLUETOOTH ENABLED");
            return 0; //attivazione effettuata con successo
        }
        Log.d("Debug", ">>>>> BLUETOOTH NOT ENABLED");
        return 1; //era gia' attivo
    }

    public Integer disableBluetooth(){
        if (adapter.isEnabled()) {
            adapter.disable();
            Log.d("Debug", ">>>>> BLUETOOTH NOT ENABLED");
            return 0; //attivazione effettuata con successo
        }
        Log.d("Debug", ">>>>> BLUETOOTH ALREADY  NOT ENABLED");
        return 1; //era gia' disattivo
    }

    public void enableDiscoverability(int secDiscoverability){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, secDiscoverability);
        startActivity(discoverableIntent);
        Log.d("Debug", ">>>>> DISCOVERABILITY ENABLED");

//        Log.d("debugTEMP", ">>>>> SEI VISIBILE CORRETTAMENTE (non è vero, sono dummy)");
//        return 0; //sei visibile correttamente


    }
}
