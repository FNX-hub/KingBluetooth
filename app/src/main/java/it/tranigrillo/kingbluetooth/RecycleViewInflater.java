package it.tranigrillo.kingbluetooth;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// funzione per aggiungere elementi alle RecycleView
// richiede il contesto pre creare l'adapter

public class RecycleViewInflater {

    private Context context;

    RecycleViewInflater(Context context) {
        this.context = context;
    }
    void inflate(RecyclerView recyclerView, List<Device> device) {
        CustomAdapter adapter = new CustomAdapter(device);
        recyclerView.setAdapter(adapter);
    }

    public Context getContext() {
        return context;
    }
}
