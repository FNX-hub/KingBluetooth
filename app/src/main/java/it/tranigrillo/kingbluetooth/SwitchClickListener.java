package it.tranigrillo.kingbluetooth;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SwitchClickListener extends Activity implements Switch.OnCheckedChangeListener {

    private Context context;
    private Switch swtVisible;
    private Switch swtOnOff;
    private LinearLayout time; //timer discoverability/visibilità
    private TextView timeDynamic;
    private View info; //Messaggio informativo "attiva bluetooth"
    private BluetoothManager BTManager;
    private CountDownTimer countDownDiscoverability;

    final static int DISCOVERABILITY_TIME = 300; //tempo in secondi di discoverability


    public SwitchClickListener(Context context, View view) {
        this.context = context;
        this.info = view.findViewById(R.id.cvInfo);
        this.time = view.findViewById(R.id.llTimer);
        this.timeDynamic = view.findViewById(R.id.tvtimerDynamuc);
        this.swtVisible = view.findViewById(R.id.swtBtVisible);
        this.swtOnOff = view.findViewById(R.id.swtBtOnOff);
        this.BTManager = new BluetoothManager(context);
    }

    @Override
    //button view è l'oggetto che viene cliccato
    //se viene cliccato isChecked diventa true
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Integer errCode;

        //CHECKED = TRUE
        if (isChecked) {

            //Switch ON/OFF is ON
            if (buttonView.getId() == swtOnOff.getId()) {
                Log.d("Debug", "Switch ON/OFF is ON");
                if (BTManager.isSupported()) {
                    Log.d("debug", "BLUETOOTH SUPPORTED");
                    errCode = BTManager.enableBluetooth();
                    Log.d("Debug", "enableBluetooth():" + errCode.toString());
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
                    } //Bluetooth abilitato
                }
                else {
                    Log.d("Debug", "BLUETOOTH NOT SUPPORTED");
                    Toast.makeText(context, R.string.ToastTextNoBluetooth, Toast.LENGTH_SHORT).show();
                    //disabilita lo switch OnOff
                    swtOnOff.setChecked(false);
                    info.setVisibility(View.VISIBLE); //fai sparire il messaggio informativo "attiva bluetooth"
                    buttonView.setText(R.string.off);
                    swtVisible.setChecked(false);
                } //Bluetooth non abilitato
            }
//                                                      /\
//------------------------------------------------------||    fino a quì funziona

            if (buttonView.getId() == swtVisible.getId() ) {
                Log.d("Debug", "Switch VISIBILITY");
                //MODIFICA L'INTERFACCIA
                buttonView.setText(R.string.visible);
//                swtVisible.setEnabled(true);
                time.setVisibility(View.VISIBLE); //rendi visibile la parte statica della stringa del timer

                //RENDITI VISIBILE E AVVIA IL TIMER
                BTManager.enableDiscoverability(DISCOVERABILITY_TIME);
                Integer counter = DISCOVERABILITY_TIME;
                countDownDiscoverability = new CountDownTimer(DISCOVERABILITY_TIME * 1000,1000) {
                    Integer counter = DISCOVERABILITY_TIME;
                    Integer counterSec;
                    Integer counterMin;
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.d("Debug","tic tac");
                        //convertitore rapido
                        counterMin = counter / 60;
                        counterSec = counter % 60;
                        timeDynamic.setText(counterMin.toString() + ":" + counterSec.toString());
                        counter--;
                    }

                    @Override
                    public void onFinish() {
                        swtVisible.setChecked(false);
                        time.setVisibility(View.GONE);
                    }
                }.start();
                Toast.makeText(context, R.string.ToastTextDiscoverabilityOn, Toast.LENGTH_SHORT).show();
                Log.d("Debug","Timer End");
            } //chiusura hai premuto SWITCH VISIBLE

//                                                      /\
//------------------------------------------------------||    lo switch disoverability non funziona perchè nn funziona il metodo Discoverability
        } //CHIUDI is checked

        //CHECKED = FALSE
        else{

            //Switch ON/OFF is OFF
            if (buttonView.getId() == swtOnOff.getId()){
                Log.d("Debug", "Switch ON/OFF is OFF");
                buttonView.setText(R.string.off);
                info.setVisibility(View.VISIBLE); //fai ricomparire messaggio informativo "attiva bluetooth"
                swtVisible.setEnabled(false); //rendi NON cliccabile lo switch della discoverability
                errCode = BTManager.disableBluetooth();
                Log.d("Debug", "disableBluetooth():"+errCode.toString());

                //Da attivare a prescindere se hai attivato o meno lo switch della discoverability
                swtVisible.setChecked(false);
                time.setVisibility(View.GONE); //fai sparire il timer della discoverability
                if (!timeDynamic.getText().equals("0:00")) {countDownDiscoverability.cancel();} //distruggi il timer
                Toast.makeText(context, R.string.ToastTextBluetoothOff, Toast.LENGTH_SHORT).show();
            }

//                                                      /\
//------------------------------------------------------||    funziona tutto bisogna fare attenzione alla distruzione del timer

            //Switch Visibility is OFF
            if(buttonView.getId() == swtVisible.getId() ) {
                Log.d("Debug", " Switch VISIBILITY OFF");
                countDownDiscoverability.cancel(); //distruggi il timer
                time.setVisibility(View.GONE);
            }
        }
    }  //chiudi onCheckedChanged
} //chiudi classe
