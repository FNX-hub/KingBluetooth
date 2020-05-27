package it.tranigrillo.kingbluetooth;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
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

    private class Holder {
        ViewPager viewPager;
        TabLayout tabLayout;
        ArrayList<Fragment> fragments;

        Holder(Context context) {
            this.tabLayout = findViewById(R.id.tabLayout);
            this.viewPager = findViewById(R.id.viewPager);

            fragments = new ArrayList<>();
            fragments.add(new OptionFragment(context));
            fragments.add(new DeviceFragment(context));

        }

        void tabInflate() {
            final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), getApplicationContext(), fragments);
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
        Holder holder = new Holder(this);
        holder.tabInflate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
}
