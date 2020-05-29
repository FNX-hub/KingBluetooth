package it.tranigrillo.kingbluetooth;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// Classe che dovrebbe prendere in input un array di device e il contesto
// Crea il layout della scermata opzioni
// popola la recycleView con gli elemetnti dell'array
// aggiunge il clickLister per gli switch
//

public class OptionFragment extends Fragment implements View.OnClickListener {

    private final BluetoothManager bluetooth;
    private View root;
    private ArrayList<Device> deviceBoundedArrayList;
    private ArrayList<Device> deviceAvailableArrayList;
    private Context context;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private BluetoothManager.Receiver receiver;

    OptionFragment(Context context, BluetoothManager bluetooth) {

        this.context = context;
        this.bluetooth = bluetooth;
        this.deviceBoundedArrayList = new ArrayList<>();
        this.deviceAvailableArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //carica dentro root il layout da mettere nel fragment
        this.root = inflater.inflate(R.layout.layout_option, container, false);

//      crea un listner e assegnagli gli switch
        Switch onoff = root.findViewById(R.id.swtBtOnOff);
        Switch visibility = root.findViewById(R.id.swtBtVisible);
        SwitchClickListener listener = new SwitchClickListener(context, root, bluetooth);
        onoff.setOnCheckedChangeListener(listener);
        visibility.setOnCheckedChangeListener(listener);
        if(bluetooth.isEnabled()) onoff.setChecked(true);

//                                                /\
//================================================||    codice ottimizzato


        //      assegna il listner alla ricerca dispositivi
        CardView cvAdd = root.findViewById(R.id.cvAction);
        cvAdd.setOnClickListener(this);

//      procedura per l'inflate della recycle view

        recyclerView = root.findViewById(R.id.rvDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.adapter = new CustomAdapter(context, deviceBoundedArrayList, bluetooth);
        recyclerView.setAdapter(adapter);
        if (bluetooth.isEnabled()) {
            bluetooth.getBoundedDevices(deviceBoundedArrayList, adapter);
        }
        return root;
    }

    @Override
    public void onClick(View v) {
        TextView tvButton = root.findViewById(R.id.tvAdd);
        TextView scan = root.findViewById(R.id.tvScan);
        final ProgressBar progressBar = root.findViewById(R.id.progressBar);
        if (tvButton.getText() == context.getResources().getString(R.string.pairing)){
            tvButton.setText(context.getResources().getString(R.string.history_devices));
            Drawable icon = context.getResources().getDrawable(R.drawable.ic_devices_other, context.getTheme());
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            tvButton.setCompoundDrawables( icon, null, null, null);
            scan.setText(context.getResources().getString(R.string.search_device));
            progressBar.setVisibility(View.VISIBLE);
            adapter = new CustomAdapter(context, deviceAvailableArrayList, bluetooth);
            recyclerView.setAdapter(adapter);
            receiver = bluetooth.startScanAvailableDevice(deviceAvailableArrayList, adapter);
            new CountDownTimer(50 * 1000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (!bluetooth.isDiscovering()) {
                        this.onFinish();
                    }
                    Log.d("bluetooth",""+bluetooth.isDiscovering() );
                }

                @Override
                public void onFinish() {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }.start();
        }

        else{
            tvButton.setText(context.getResources().getString(R.string.pairing));
            Drawable icon = context.getResources().getDrawable(R.drawable.ic_add, context.getTheme());
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            tvButton.setCompoundDrawables( icon, null, null, null);
            scan.setText(context.getResources().getString(R.string.history_devices));
            progressBar.setVisibility(View.INVISIBLE);
            bluetooth.stopScanAvailableDevice(receiver);
            bluetooth.getBoundedDevices(deviceBoundedArrayList, adapter);
        }
    }
}