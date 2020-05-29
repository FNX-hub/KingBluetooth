package it.tranigrillo.kingbluetooth;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SwitchClickListener implements Switch.OnCheckedChangeListener {

    private static final String DEBUG = "Debug";
    private static final int DISCOVERABILITY_TIME = 300; //tempo in secondi di discoverability

    private Context context;
    private Switch swtVisible;
    private Switch swtOnOff;
    private LinearLayout time; //timer discoverability/visibilità
    private TextView timeDynamic;
    private View info; //Messaggio informativo "attiva bluetooth"
    private BluetoothManager btManager;
    private CountDownTimer countDownDiscoverability;


    SwitchClickListener(Context context, View view, BluetoothManager btManager) {
        this.context = context;
        this.info = view.findViewById(R.id.cvInfo);
        this.time = view.findViewById(R.id.llTimer);
        this.timeDynamic = view.findViewById(R.id.timeDynamic);
        this.swtVisible = view.findViewById(R.id.swtBtVisible);
        this.swtOnOff = view.findViewById(R.id.swtBtOnOff);
        this.btManager = btManager;
    }

    @Override
    //button view è l'oggetto che viene cliccato
    //se viene cliccato isChecked diventa true
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Integer errCode;

        //BLUETOOTH NOT SUPPORTED
        if (!btManager.isSupported()) {
            Log.d(DEBUG, "BLUETOOTH NOT SUPPORTED");
            Toast.makeText(context, R.string.ToastTextNoBluetooth, Toast.LENGTH_SHORT).show();
            //disabilita lo switch OnOff
            swtOnOff.setChecked(false);
            info.setVisibility(View.VISIBLE); //fai sparire il messaggio informativo "attiva bluetooth"
            buttonView.setText(R.string.off);
            swtVisible.setChecked(false);
            }

        //CHECKED && ON/OFF
        if (isChecked && buttonView.getId() == swtOnOff.getId()) {
            Log.d(DEBUG, "Switch ON/OFF is ON");
            Log.d("debug", "BLUETOOTH SUPPORTED");
            errCode = btManager.enableBluetooth();
            Log.d(DEBUG, "enableBluetooth()=" + errCode.toString());
            switch (errCode) {
                case 0:
                    Toast.makeText(context, R.string.ToastTextBluetoothOn, Toast.LENGTH_SHORT).show();
                    buttonView.setText(R.string.on);
                    info.setVisibility(View.GONE); //fai sparire il messaggio informativo "attiva bluetooth"
                    swtVisible.setEnabled(true); //rendi cliccabile lo switch della discoverability
                    break;
                case 1:
                    Toast.makeText(context, R.string.ToastTextBluetoothOn, Toast.LENGTH_SHORT).show();
                    buttonView.setText(R.string.on);
                    info.setVisibility(View.GONE); //fai sparire il messaggio informativo "attiva bluetooth"
                    swtVisible.setEnabled(true); //rendi cliccabile lo switch della discoverability
                    Toast.makeText(context, R.string.ToastTextBluetoothAlreadyOn, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, R.string.ToastTextBluetoothErr, Toast.LENGTH_SHORT).show();
                    break;
            }
        }//BLUETOOTH ATTIVO

        //NOT CHECKED && ON/OFF
        if (!isChecked && buttonView.getId() == swtOnOff.getId() && btManager.isSupported()){
            Log.d(DEBUG, "Switch ON/OFF is OFF");
            buttonView.setText(R.string.off);
            info.setVisibility(View.VISIBLE); //fai ricomparire messaggio informativo "attiva bluetooth"
            swtVisible.setEnabled(false); //rendi NON cliccabile lo switch della discoverability
            errCode = btManager.disableBluetooth();
            Log.d(DEBUG, "disableBluetooth():"+errCode.toString());
            swtVisible.setChecked(false);
            time.setVisibility(View.GONE); //fai sparire il timer della discoverability
            if (!timeDynamic.getText().equals("00:00")) {countDownDiscoverability.cancel();} //distruggi il timer
            Toast.makeText(context, R.string.ToastTextBluetoothOff, Toast.LENGTH_SHORT).show();
        }//BLUETOOTH NON ATTIVO

        //CHECKED && VISIBILITY
        if (isChecked && buttonView.getId() == swtVisible.getId()) {
            Log.d(DEBUG, "Switch VISIBILITY is ON");
            buttonView.setText(R.string.visible);
            time.setVisibility(View.VISIBLE); //rendi visibile la parte statica della stringa del timer
            btManager.enableDiscoverability();
            countDownDiscoverability = new CountDownTimer(DISCOVERABILITY_TIME * 1000,1000) {
                Integer remaning = DISCOVERABILITY_TIME;
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d(DEBUG,"tic tac");
                    //convertitore rapido
                    int min = remaning / 60;
                    int sec = remaning % 60;
                    timeDynamic.setText(String.format("%02d:%02d", min, sec));
                    remaning--;
                }
                @Override
                public void onFinish() {
                    swtVisible.setChecked(false);
                    time.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.ToastTextDiscoverabilityOff, Toast.LENGTH_SHORT).show();
                }
            }.start();
            //RENDITI VISIBILE E AVVIA IL TIMER
            Toast.makeText(context, R.string.ToastTextDiscoverabilityOn, Toast.LENGTH_SHORT).show();
            Log.d(DEBUG,"Timer End");
            btManager.disableDiscoverability();
        }// DISPOSITIVO VISIBILE

        //NOT CHECKED && VISIBILITY && SUPPORTED
        if (!isChecked && buttonView.getId() == swtVisible.getId() && btManager.isSupported()){
            Log.d(DEBUG, " Switch VISIBILITY OFF");
            btManager.disableDiscoverability();
            countDownDiscoverability.cancel(); //distruggi il timer
            time.setVisibility(View.GONE);
        }// DISPOSITIVO NON VISIBILE
    }
}
