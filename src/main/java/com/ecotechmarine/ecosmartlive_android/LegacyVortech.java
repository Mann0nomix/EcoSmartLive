package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

import java.util.Timer;
import java.util.TimerTask;


public class LegacyVortech extends NavigationUtility implements IESLWebSocketMessageReceiver{
    int stepCount;
    int altCount;
    Timer alternation;
    public static Activity legacyVortechActivity;
    public static boolean legacyVortechActive;
    int count;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legacy_vortech_view);
        //set SharedPreferences and edit to be used globally
        sp = getSharedPreferences("USER_PREF", 0);
        initActionBar();
        setActionBarDefaults();
        legacyVortechActivity = this;
        legacyVortechActive = true;
        if(NavigationWheel.wsClient != null){
            //remove listener for overlay and restore listener to navwheel
            NavigationWheel.wsClient.deleteWebSocketMessageReceiver(NavigationWheel.wheelActivity);
            NavigationWheel.wsClient.addWebSocketMessageReceiver(LegacyVortech.this);
        }
        new StartPumpUpdate().execute();
        count = 0;
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(R.string.add_vortech);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        final ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        final ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);

        //Hide action buttons
        mainMenuButton.setVisibility(View.INVISIBLE);
        groupListButton.setVisibility(View.INVISIBLE);

        saveButton.setText(R.string.next);

        //Make save buttons visible
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alternation != null){
                    alternation.purge();
                    alternation.cancel();
                }
                legacyVortechActive = false;
                finish();
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount++;
                nextStep();
            }
        });
    }

    public void nextStep(){
        final ImageView vortechImage = (ImageView)findViewById(R.id.pump_control_img);
        final TextView vortechInstructions = (TextView)findViewById(R.id.vortech_instructions);
        switch (stepCount){
            case 1:
                altCount = 0;
                vortechInstructions.setText(R.string.hold_mode_and_set);
                alternation = new Timer();
                alternation.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        altCount++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (altCount % 2 == 0){
                                    vortechImage.setImageResource(R.drawable.icon_pump_white);
                                }else{
                                    vortechImage.setImageResource(R.drawable.icon_pump_red);
                                }
                            }
                        });
                    }
                }, 0, 500);
                break;
            case 2:
                altCount = 0;
                vortechInstructions.setText(R.string.hold_set_until_control_dial);
                if (alternation != null){
                    alternation.purge();
                    alternation.cancel();
                }
                alternation = new Timer();
                alternation.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        altCount++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (altCount % 2 == 0){
                                    vortechImage.setImageResource(R.drawable.icon_pump_blue);
                                }else{
                                    vortechImage.setImageResource(R.drawable.icon_pump_green);
                                }
                            }
                        });
                    }
                }, 0, 350);
                break;
            default:
                if (alternation != null){
                    alternation.purge();
                    alternation.cancel();
                }
                legacyVortechActive = false;
                finish();
                break;
        }
    }

    class StartPumpUpdate extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.updatePumpFirmware(0, 1);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        legacyVortechActive = false;
        finish();
    }

    class DiscoverDevicesTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.discoverDevices();
            return null;
        }
    }

    class AlertSuccess extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            //ensure that the overlay gets dismissed before throwing alert view (This can cause crashes in the app if not done)
            if (VortechUpdateOverlay.vortechOverlayActivity != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        VortechUpdateOverlay.vortechOverlayActivity.finish();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder updateFinished = new AlertDialog.Builder(LegacyVortech.this);
                    updateFinished.setMessage(R.string.vortech_updated);
                    updateFinished.setTitle(getResources().getString(R.string.add_vortech));
                    updateFinished.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //close current vortech activity, reset, and restart it to add another device
                            finish();
                            Intent intent = getIntent();
                            startActivity(intent);
                        }
                    });
                    updateFinished.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Intent intent = new Intent(LegacyVortech.this, AquariumProgramOverlay.class);
                            startActivity(intent);
                            new DiscoverDevicesTask().execute();
                        }
                    });
                    updateFinished.show();
                }
            });
            if(NavigationWheel.wsClient != null){
                NavigationWheel.wsClient.deleteWebSocketMessageReceiver(LegacyVortech.this);
            }
        }
    }

    class AlertError extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            //ensure that the overlay gets dismissed before throwing alert view (This can cause crashes in the app if not done)
            if (VortechUpdateOverlay.vortechOverlayActivity != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        VortechUpdateOverlay.vortechOverlayActivity.finish();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder updateFinished = new AlertDialog.Builder(LegacyVortech.this);
                    updateFinished.setMessage(R.string.error_updating_vortech);
                    updateFinished.setTitle(getResources().getString(R.string.update_error));
                    updateFinished.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Restart the update overlay
                            Intent intent = new Intent(LegacyVortech.this, VortechUpdateOverlay.class);
                            intent.putExtra("onRetry", true);
                            startActivity(intent);
                        }
                    });
                    updateFinished.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Intent intent = new Intent(LegacyVortech.this, AquariumProgramOverlay.class);
                            startActivity(intent);
                            new DiscoverDevicesTask().execute();
                        }
                    });
                    updateFinished.show();
                }
            });
            if(NavigationWheel.wsClient != null){
                NavigationWheel.wsClient.deleteWebSocketMessageReceiver(LegacyVortech.this);
            }
        }
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage notification) {
        if(notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.LegacyPumpFirmwareUpdate.getValue())){
            String msg = "";
            String [] tokens = notification.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            count++;
            //Condition to handle default set up of progress update
            if (count == 1){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alternation != null){
                            alternation.purge();
                            alternation.cancel();
                        }
                        final ImageView vortechImage = (ImageView)findViewById(R.id.pump_control_img);
                        final ImageView vortechIcon = (ImageView) findViewById(R.id.pump_icon);
                        final TextView vortechTitle = (TextView)findViewById(R.id.vortech_title);
                        final TextView vortechInstructions = (TextView)findViewById(R.id.vortech_instructions);
                        //set visibility for when Vortech Pump Starts to update
                        vortechTitle.setVisibility(View.INVISIBLE);
                        vortechIcon.setVisibility(View.INVISIBLE);
                        vortechInstructions.setVisibility(View.INVISIBLE);
                        vortechImage.setImageResource(R.drawable.icon_mp40_lg);
                        //center the image while updating
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT);
                        vortechImage.setLayoutParams(params);
                        //Fire off the update overlay
                        Intent intent = new Intent(LegacyVortech.this, VortechUpdateOverlay.class);
                        startActivity(intent);
                    }
                });
            }
            //Condition to handle completion of the update process with alert view navigation
            if (origin == VortechUpdateOverlay.FirmwareUpdateProgressTypeComplete){
                new AlertSuccess().execute();
            }else if (origin == VortechUpdateOverlay.FirmwareUpdateProgressTypeError){
                new AlertError().execute();
            }
            System.out.println("Web Socket is listening to your pump udpate itself!");
        }
    }
}
