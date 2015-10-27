package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DeviceManagerOverlay extends Activity {
    int selectedGroupId;
    String currentTime;
    String selectedGroupName;
    ConnectManager connect;
    ArrayList<Device> allRadionsInGroup;
    ArrayList<Device> allPumpsInGroup;
    String serialArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manager_overlay);
        connect = ConnectManager.getSharedInstance();
        allRadionsInGroup = new ArrayList<Device>();
        allPumpsInGroup = new ArrayList<Device>();
        if (getIntent() != null){
            selectedGroupId = getIntent().getIntExtra("selectedGroupId", 0);
            selectedGroupName = getIntent().getStringExtra("selectedGroupName");
        }
        initButtons();
        initDeviceArrays();
    }

    public void initButtons(){
        Button removeAquarium = (Button) findViewById(R.id.remove_aquarium_button);
        removeAquarium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //convert the View Position into an index that correlates with the structure of the user preset array
                AlertDialog.Builder RemoveAlert = new AlertDialog.Builder(DeviceManagerOverlay.this);
                RemoveAlert.setMessage(R.string.remove_aquarium_confirmation);
                RemoveAlert.setTitle(getResources().getString(R.string.remove) + " " + selectedGroupName);
                RemoveAlert.setNegativeButton(R.string.no, null);
                RemoveAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serialArray = "";
                        for (Device device: allRadionsInGroup){
                            serialArray = serialArray.concat(device.getSerialNo());
                            if (allRadionsInGroup.indexOf(device) < allRadionsInGroup.size() - 1){
                                serialArray = serialArray.concat("-");
                            }
                        }
                        if (!allPumpsInGroup.isEmpty() && serialArray.length() != 0){
                            serialArray = serialArray.concat("-");
                        }
                        for (Device device: allPumpsInGroup){
                            serialArray = serialArray.concat(device.getSerialNo());
                            if (allPumpsInGroup.indexOf(device) < allPumpsInGroup.size() - 1){
                                serialArray = serialArray.concat("-");
                            }
                        }

                        new RemoveAquariumTask().execute();
                    }
                });
                RemoveAlert.show();
            }
        });

        Button identifyButton = (Button) findViewById(R.id.identify_button);
        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IdentifyTask().execute();
            }
        });

        Button setTimeButton = (Button) findViewById(R.id.set_time_button);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar todayDate = Calendar.getInstance();
                SimpleDateFormat formatter;
                if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                    formatter = new SimpleDateFormat("h:mm aa");
                }else{
                    formatter = new SimpleDateFormat("k:mm");
                }

                currentTime = formatter.format(todayDate.getTime());
                new SetTimeTask().execute();
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

    //Find all pumps and lights and organize them into separate arrays to be used by the list
    public void initDeviceArrays() {
        //Handle lights
        for (Device device : connect.allDevices) {
            if (device.getParentGroupId() == connect.currentDeviceGroup.getGroupId() && device.getDeviceType().equalsIgnoreCase("L")) {
                //must do this since the thread will yet about the variable not being final
                allRadionsInGroup.add(device);
            }

            if (device.getParentGroupId() == connect.currentDeviceGroup.getGroupId() && device.getDeviceType().equalsIgnoreCase("P")) {
                //must do this since the thread will yet about the variable not being final
                //final Device lastMatchedPump = device;
                allPumpsInGroup.add(device);
            }
        }
    }

    public class RemoveAquariumTask extends AsyncTask<Void,Void,Void> {
        protected Void doInBackground(Void... arg0){
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.unassignDevice(serialArray);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceManagerOverlay.this, selectedGroupName + getResources().getString(R.string.removed), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }


    public class IdentifyTask extends AsyncTask<Void,Void,Void> {

        protected Void doInBackground(Void... arg0){
            String target = "G";
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendIdentifyForTarget(target, String.valueOf(selectedGroupId));
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class SetTimeTask extends AsyncTask<Void,Void,Void> {
        protected Void doInBackground(Void... arg0){
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.setTimeForGroup(selectedGroupId, currentTime);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceManagerOverlay.this, getResources().getString(R.string.set_time_devices) + " " + currentTime, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}
