package it.tranigrillo.kingbluetooth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Classe che dovrebbe prendere in input un array di device e il contesto
// Crea il layout della scermata opzioni
// popola la recycleView con gli elemetnti dell'array
// aggiunge il clickLister per gli switch
//


public class OptionFragment extends Fragment {

    private Context context;
//    private ArrayList<Device> device;

    OptionFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //carica dentro root il layout da mettere nel fragment
        View root = inflater.inflate(R.layout.layout_option, container, false);

//      assegna il listner alla ricerca dispositivi
        CardView cvAdd = root.findViewById(R.id.cvAction);
        cvAdd.setOnClickListener(new PairingListener(context, root));

//      crea un listner e assegnagli gli switch
        Switch onoff = root.findViewById(R.id.swtBtOnOff);
        Switch vis = root.findViewById(R.id.swtBtVisible);
        SwitchClickListener listener = new SwitchClickListener(context, root);
        onoff.setOnCheckedChangeListener(listener);
        vis.setOnCheckedChangeListener(listener);

//      procedura per l'inflate della recycle view
        RecyclerView recyclerView = root.findViewById(R.id.rvDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        RecycleViewInflater recycleInflater = new RecycleViewInflater(context);
        ArrayList<Device> device = new ArrayList<>();
//  funzione dummy da eliminare quando implementato l'array
        for (int i= 0; i < 10; i++) {
            device.add(new Device(getResources().getString(R.string.device_name), getResources().getString(R.string.statusActive)));
        }
//
        recycleInflater.inflate(recyclerView, device);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return root;
    }

}
