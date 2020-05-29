package it.tranigrillo.kingbluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// adapter per le RecycleView
// richiede una lista di device

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private final BluetoothManager bluetooth;
    private final Context context;
    private List<Device> deviceList;

    CustomAdapter(Context context, List<Device> deviceList, BluetoothManager bluetooth) {
        this.context = context;
        this.deviceList = deviceList;
        this.bluetooth = bluetooth;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View deviceView = inflater.inflate(R.layout.cardview_device, parent, false);
        Log.d("Debug", "inflate previous");
        return new CustomViewHolder(deviceView, this/*, bluetooth, deviceList*/);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        Device device = deviceList.get(position);
        holder.tvDeviceName.setText(device.getName());
        holder.tvStatus.setText(String.format("MAC: %s\nStatus: %s", device.getAddress(),context.getResources().getString(R.string.statusActive)));
        switch (device.getState()) {
            case BluetoothDevice.BOND_NONE: {
                holder.tvStatus.setText(String.format("MAC: %s\nStatus: %s", device.getAddress(),context.getResources().getString(R.string.statusActive)));
                break;
            }
            case BluetoothDevice.BOND_BONDING: {
                holder.tvStatus.setText(String.format("MAC: %s\nStatus: %s", device.getAddress(),context.getResources().getString(R.string.statusConnecting)));
                break;
            }
            case BluetoothDevice.BOND_BONDED: {
                holder.tvStatus.setText(String.format("MAC: %s\nStatus: %s", device.getAddress(),context.getResources().getString(R.string.statusConnected)));
                break;
            }
        }
        if (device.getAddress().equals(holder.tvStatus.getResources().getString(R.string.statusConnected))) {
            holder.ivOption.setImageResource(R.drawable.ic_bluetooth_disabled);
        }
        Log.d("Debug", "onBind previous");
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void removeItem(int position) {
        deviceList.remove(position);
        notifyItemRemoved(position);
    }

    public Device getItem(int position) {
        return deviceList.get(position);
    }

    public void addItem(Device device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            notifyDataSetChanged();
        }
    }

    public void removeAll(){
        for (int i = getItemCount(); i > 0; i--){
            removeItem(i-1);
            notifyItemRemoved(i-1);
        }
    }

    public void addAll(List<Device> deviceList){
        for (int i = 0; i <deviceList.size(); i++){
            addItem(deviceList.get(i));
            notifyDataSetChanged();
        }
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvDeviceName;
        private TextView tvNoDevice;
        private TextView tvStatus;
        private ImageView ivOption;
        private CardView cvDeviceName;
        private CardView cvDeviceOption;
        private CustomAdapter adapter;

        CustomViewHolder(@NonNull View itemView, CustomAdapter adapter/*, BluetoothManager bluetooth, List<Device> deviceList*/) {
            super(itemView);
            this.adapter = adapter;

            this.tvDeviceName = itemView.findViewById(R.id.tvAdd);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.ivOption = itemView.findViewById(R.id.ivOption);
            this.cvDeviceName = itemView.findViewById(R.id.cvDevice);
            this.cvDeviceOption = itemView.findViewById(R.id.cvOption);
            cvDeviceName.setOnClickListener(this);
            cvDeviceOption.setOnClickListener(this);
            Log.d("Debug", "CustomViewHolder previous");

        }

        @Override
        public void onClick(View v) {
            Device device = adapter.getItem(getAdapterPosition());
            if (v.getId() == cvDeviceName.getId() && device.getState().equals(BluetoothDevice.BOND_NONE)) {
                adapter.bluetooth.boundDevice(device.getAddress());
                while (device.getState().equals(BluetoothDevice.BOND_BONDING)) {
                    tvStatus.setText("Pairing...");
                }
                adapter.removeItem(getAdapterPosition());
            }
            if (v.getId() == cvDeviceOption.getId() && device.getState().equals(BluetoothDevice.BOND_BONDED)) {
                adapter.bluetooth.unBoundDevice(device.getAddress());
                adapter.removeItem(getAdapterPosition());
            }
        }
    }
}
