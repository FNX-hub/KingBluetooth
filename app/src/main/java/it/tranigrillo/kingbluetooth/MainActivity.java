package it.tranigrillo.kingbluetooth;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

/*
classe MainActivity
l'older pensa a generare il layout con il metodo tabInflate
i singolo comportamenti sono rimandati ai singoli fragment
*/
public class MainActivity extends AppCompatActivity {
//    public static final String BLUETOOTH = "Bluetooth";
//    public static final String DEBUG = "debug";
//    public static final String ERROR = "error";
//    private static final String BLUETOOTH_NOT_ENABLED = ">>>>> BLUETOOTH NOT ENABLED";
//    private static final String BLUETOOTH_ENABLED = ">>>>> BLUETOOTH ENABLED ";

//    private Bluetooth bluetooth;
    private Holder holder;

    private class Holder {

        ArrayList<Fragment> fragments;
        BluetoothManager bluetooth;
        ViewPager viewPager;
        TabLayout tabLayout;

        Holder(Context context) {
            this.bluetooth = new BluetoothManager(context);
            this.tabLayout = findViewById(R.id.tabLayout);
            this.viewPager = findViewById(R.id.viewPager);

            fragments = new ArrayList<>();
            fragments.add(new OptionFragment(context, bluetooth));
            fragments.add(new DeviceFragment(context));
        }
            void tabInflater(){
                final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
                Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getResources().getString(R.string.option));
                Objects.requireNonNull(tabLayout.getTabAt(1)).setText(getResources().getString(R.string.device));
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        else {
            this.holder = new Holder(this);
            holder.tabInflater();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //action if granted
                    this.holder = new Holder(this);
                    holder.tabInflater();
                }
                else {
                    //action if not granted
                    this.holder = new Holder(this);
                    holder.tabInflater();
                }
    }
}
