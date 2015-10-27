package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DeviceDetailsOverlay extends Activity {
    ConnectManager connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details_overlay);
        connect = ConnectManager.getSharedInstance();
        initButtons();
    }

    public void initButtons(){
        Button factoryReset = (Button) findViewById(R.id.factory_reset_button);
        factoryReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //convert the View Position into an index that correlates with the structure of the user preset array
                AlertDialog.Builder confirmationAlert = new AlertDialog.Builder(DeviceDetailsOverlay.this);
                confirmationAlert.setMessage(R.string.factory_reset_device);
                confirmationAlert.setTitle(getResources().getString(R.string.reset) + " " + connect.currentDevice.getName());
                confirmationAlert.setNegativeButton(R.string.no, null);
                confirmationAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new FactoryResetDeviceTask().execute();
                        Toast.makeText(DeviceDetailsOverlay.this, getResources().getString(R.string.factory_reset), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                confirmationAlert.show();
            }
        });

        Button rebootButton = (Button) findViewById(R.id.reboot_button);
        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SoftResetDeviceTask().execute();
                Toast.makeText(DeviceDetailsOverlay.this, getResources().getString(R.string.rebooting_device), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_nav_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class SoftResetDeviceTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.softResetDevice(connect.currentDevice.getDeviceId());
            return null;
        }
    }

    class FactoryResetDeviceTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.factoryResetDevice(connect.currentDevice.getDeviceId());
            return null;
        }
    }
}
