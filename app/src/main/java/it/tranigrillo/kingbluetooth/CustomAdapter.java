package it.tranigrillo.kingbluetooth;

import android.content.Context;
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
    private List<Device> deviceList;

    CustomAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View deviceView = inflater.inflate(R.layout.cardview_device, parent, false);
        return new CustomViewHolder(deviceView, this, deviceList);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.tvDeviceName.setText(device.getDeviceName());
        holder.tvStatus.setText(device.getStatus());
        if (device.getStatus().equals(holder.tvStatus.getResources().getString(R.string.statusConnected))) {
            holder.ivOption.setImageResource(R.drawable.ic_bluetooth_disabled);
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void removeItem(int position) {
        deviceList.remove(position);
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvDeviceName;
        private TextView tvStatus;
        private ImageView ivOption;
        private List<Device> deviceList;
        private CardView cvName;
        private CardView cvOption;
        CustomAdapter adapter;

        CustomViewHolder(@NonNull View itemView, CustomAdapter adapter, List<Device> deviceList) {
            super(itemView);
            this.adapter = adapter;
            this.tvDeviceName = itemView.findViewById(R.id.tvAdd);
            this.tvStatus = itemView.findViewById(R.id.tvStatus);
            this.ivOption = itemView.findViewById(R.id.ivOption);
            this.deviceList = deviceList;
            this.cvName = itemView.findViewById(R.id.cvDevice);
            this.cvOption = itemView.findViewById(R.id.cvOption);
            cvName.setOnClickListener(this);
            cvOption.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == cvName.getId()) {
                if (deviceList.get(getAdapterPosition()).getStatus().equals(v.getResources().getString(R.string.statusActive))) {
                    tvStatus.setText(R.string.statusConnecting);
                }
                if (deviceList.get(getAdapterPosition()).getStatus().equals(v.getResources().getString(R.string.statusConnected))) {
                    tvStatus.setText("Invio file...");
                }
            }
            else {
                adapter.removeItem(getAdapterPosition());
                adapter.notifyItemRemoved(getAdapterPosition());
            }
        }
    }
}
