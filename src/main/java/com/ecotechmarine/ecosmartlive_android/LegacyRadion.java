package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

import org.json.JSONObject;


public class LegacyRadion extends NavigationUtility implements IESLWebSocketMessageReceiver{
    public static boolean legacyRadionActive;
    public static Activity legacyRadionActivity;
    public static int LightWizardUSBWaiting = 1;
    public static int LightWizardFoundDevice = 2;
    public static int LightWizardFirmwareUpdate = 3;
    public static int LightWizardFactoryReset = 4;
    public static int FirmwareUpdateProgressTypeError = 0;
    public static int FirmwareUpdateProgressTypeGeneral = 1;
    public static int FirmwareUpdateProgressTypeProgressOS= 2;
    public static int FirmwareUpdateProgressTypeProgressRF = 3;
    public static int FirmwareUpdateProgressTypeNextDevice = 4;
    public static int FirmwareUpdateProgressTypeEstimatedTime = 5;
    public static int FirmwareUpdateProgressTypeComplete = 6;
    public static int FirmwareUpdateProgressTypeNotUpdated = 7;
    public static int FirmwareUpdateProgressTypeStateComplete = 8;
    public static int BRIDGE_USB_MODE_DEVICE = 1;
    public static int BRIDGE_USB_MODE_HOST = 2;
    public static int USB_DEVICE = 239;
    int USBModelNumber = 0;
    long USBSerialNumber = 0;
    boolean currentlyUpdating = false;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legacy_radion_view);
        initActionBar();
        setActionBarDefaults();
        legacyRadionActivity = this;
        legacyRadionActive = true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(NavigationWheel.wsClient != null){
            //remove listener for overlay and restore listener to navwheel
            NavigationWheel.wsClient.deleteWebSocketMessageReceiver(NavigationWheel.wheelActivity);
            NavigationWheel.wsClient.addWebSocketMessageReceiver(LegacyRadion.this);
        }
        new SetBridgeToHost().execute();
        //new StartLightUpdate().execute();
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(R.string.add_radion);

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
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updateUIOnFoundDevice(){
        //Handle Next button UI
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        final ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
        saveButton.setText(R.string.next);
        saveButton.setVisibility(View.VISIBLE);
        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start the update when the next button is selected
                new StartLightUpdate().execute();
                currentlyUpdating = true;
                //Fire off the update overlay
                Intent intent = new Intent(LegacyRadion.this, RadionUpdateOverlay.class);
                startActivity(intent);
            }
        });

        //handle display UI
        TextView addLightTitle = (TextView) findViewById(R.id.add_light_text);
        ImageView radionImage = (ImageView) findViewById(R.id.radion_image);
        TextView usbPlugIn = (TextView) findViewById(R.id.plug_in_USB);
        ProgressBar findingDevice = (ProgressBar) findViewById(R.id.radion_device_connection_progress);
        TextView waitForConnection = (TextView) findViewById(R.id.wait_for_connection);
        ImageView identifyIcon = (ImageView) findViewById(R.id.identify_icon);
        TextView serialTitle = (TextView) findViewById(R.id.serial_number_title);
        TextView serialData = (TextView) findViewById(R.id.serial_number_data);
        TextView modelTitle = (TextView) findViewById(R.id.model_title);
        TextView modelData = (TextView) findViewById(R.id.model_data);

        serialTitle.append(":");
        modelTitle.append(":");

        addLightTitle.setText(R.string.device_found);
        radionImage.setColorFilter(Color.argb(0, 0, 0, 0));
        usbPlugIn.setText(R.string.verify_serial_number);
        findingDevice.setVisibility(View.INVISIBLE);
        waitForConnection.setVisibility(View.INVISIBLE);
        identifyIcon.setVisibility(View.VISIBLE);

        //reveal data text views after getting device information from web sockets
        serialTitle.setVisibility(View.VISIBLE);
        serialData.setVisibility(View.VISIBLE);
        modelTitle.setVisibility(View.VISIBLE);
        modelData.setVisibility(View.VISIBLE);

        //set Model and Serial number post-download
        switch (USBModelNumber){
            //cases to handle lights
            case 30:
                modelData.setText(getResources().getString(R.string.RadionXR30w));
                break;
            case 31:
                modelData.setText(getResources().getString(R.string.RadionXR30wG2));
                break;
            case 32:
                modelData.setText(getResources().getString(R.string.RadionXR30wPro));
                break;
            case 33:
                modelData.setText(getResources().getString(R.string.RadionXR30wG3));
                break;
            case 34:
                modelData.setText(getResources().getString(R.string.RadionXR30wG3Pro));
                break;
            case 39:
                modelData.setText(getResources().getString(R.string.RadionXR30wProto));
                break;
            //XR15 Support
            case 160:
                modelData.setText(getResources().getString(R.string.RadionXR15wG3Pro));
                break;
            case 161:
                modelData.setText(R.string.RadionXR15FW);
                break;
            default:
                break;
        }
        //make sure to format the serial number as a 14 digit number. Also precede it with leading 0s
        String formatSerial = String.format("%014d", USBSerialNumber);
        serialData.setText(String.valueOf(formatSerial));

        //Identify light when icon is clicked
        identifyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IdentifyUSBLight().execute();
            }
        });
    }

    class StartLightUpdate extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.updateRadionFirmware(USB_DEVICE);
            return null;
        }
    }

    class GetUSBDeviceStatus extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager cm = ConnectManager.getSharedInstance();
            cm.getDeviceStatus(USB_DEVICE);
            return null;
        }
    }

    class DiscoverDevicesTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.discoverDevices();
            return null;
        }
    }

    class IdentifyUSBLight extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.sendIdentifyForTarget("D", String.valueOf(USB_DEVICE));
            return null;
        }
    }

    class SetBridgeToHost extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.setBridgeMode(BRIDGE_USB_MODE_HOST);
            return null;
        }
    }

    class SetBridgeToDevice extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.setBridgeMode(BRIDGE_USB_MODE_DEVICE);
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
                    AlertDialog.Builder updateFinished = new AlertDialog.Builder(LegacyRadion.this);
                    updateFinished.setMessage(R.string.radion_updated);
                    updateFinished.setTitle(getResources().getString(R.string.add_radion));
                    updateFinished.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //close current radion activity, reset, and restart it to add another device
                            finish();
                            Intent intent = getIntent();
                            startActivity(intent);
                        }
                    });
                    updateFinished.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Intent intent = new Intent(LegacyRadion.this, AquariumProgramOverlay.class);
                            startActivity(intent);
                            new DiscoverDevicesTask().execute();
                        }
                    });
                    updateFinished.show();
                    if(NavigationWheel.wsClient != null){
                        //remove listener for overlay and restore listener to navwheel
                        NavigationWheel.wsClient.deleteWebSocketMessageReceiver(LegacyRadion.this);
                    }
                }
            });
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
                    AlertDialog.Builder updateFinished = new AlertDialog.Builder(LegacyRadion.this);
                    updateFinished.setMessage(R.string.error_updating_radion);
                    updateFinished.setTitle(getResources().getString(R.string.update_error));
                    updateFinished.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Restart the update overlay
                            Intent intent = new Intent(LegacyRadion.this, RadionUpdateOverlay.class);
                            intent.putExtra("onRetry", true);
                            startActivity(intent);
                        }
                    });
                    updateFinished.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Intent intent = new Intent(LegacyRadion.this, AquariumProgramOverlay.class);
                            startActivity(intent);
                            new DiscoverDevicesTask().execute();
                        }
                    });
                    updateFinished.show();
                }
            });
            if(NavigationWheel.wsClient != null){
                //remove listener for overlay and restore listener to navwheel
                NavigationWheel.wsClient.deleteWebSocketMessageReceiver(LegacyRadion.this);
            }
        }
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage notification) {
        if (notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.USBDeviceConnected.getValue())){
            //Make sure the update only launches once
            count++;
            if (count == 1){
                System.out.println("USB Device Connected Web Socket Call!!!!!!!!!!!");
                new GetUSBDeviceStatus().execute();
            }
        }
        if (notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.GetDeviceStatus.getValue())){
            System.out.println("DEVICE STATUS IS RESPONDING!!!!!!!!!!!");
            JSONObject jsonMsg = null;
            String msg = notification.getMessage().toString();
            //try catch must be used when parsing a json formatted string
            try{
                //Condition to handle default set up of progress update
                if (count == 1){
                    //parse message and save device numbers
                    jsonMsg = new JSONObject(msg);
                    USBModelNumber = jsonMsg.getInt("model_number");
                    USBSerialNumber = jsonMsg.getLong("serial_number");
                    System.out.println("USB Device Status = " + msg);

                    //update the UI appropriately
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUIOnFoundDevice();
                        }
                    });
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.LegacyLightFirmwareUpdate.getValue())) {
            String msg = "";
            String[] tokens = notification.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);

            if (currentlyUpdating && origin == FirmwareUpdateProgressTypeComplete){
                new SetBridgeToDevice().execute();
                new AlertSuccess().execute();
            }else if (currentlyUpdating && origin == FirmwareUpdateProgressTypeError){
                new SetBridgeToDevice().execute();
                new AlertError().execute();
            }
        }
    }
}
