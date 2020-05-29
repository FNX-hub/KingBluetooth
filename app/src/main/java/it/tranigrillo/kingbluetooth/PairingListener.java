package it.tranigrillo.kingbluetooth;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PairingListener implements View.OnClickListener{
    private final Context context;
    private final TextView scan;
    private final TextView add;
    private final ProgressBar progressBar;
    private BluetoothManager bluetooth;

    PairingListener(Context context, View root, BluetoothManager bluetooth){
        this.context = context;
        this.add = root.findViewById(R.id.tvAdd);
        this.scan = root.findViewById(R.id.tvScan);
        this.progressBar = root.findViewById(R.id.progressBar);
        this.bluetooth = bluetooth;
    }
    @Override
    public void onClick(View v) {

        if (add.getText() == context.getResources().getString(R.string.pairing)){
            add.setText(context.getResources().getString(R.string.history_devices));
            Drawable icon = context.getResources().getDrawable(R.drawable.ic_devices_other, context.getTheme());
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            add.setCompoundDrawables( icon, null, null, null);
            scan.setText(context.getResources().getString(R.string.search_device));
            progressBar.setVisibility(View.VISIBLE);


        }
        else{
            add.setText(context.getResources().getString(R.string.pairing));
            Drawable icon = context.getResources().getDrawable(R.drawable.ic_add, context.getTheme());
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            add.setCompoundDrawables( icon, null, null, null);
            scan.setText(context.getResources().getString(R.string.history_devices));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
