package com.posstation;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.os.Bundle;

import com.facebook.react.ReactActivity;

public class BluetoothActivity extends ReactActivity {

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Fragment fragment = new SettingsFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.frame_container, fragment).commit();
        }

    }


}
