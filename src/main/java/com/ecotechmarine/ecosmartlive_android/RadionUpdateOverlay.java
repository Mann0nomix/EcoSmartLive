package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;


public class RadionUpdateOverlay extends Activity implements IESLWebSocketMessageReceiver{
    public static Activity radionOverlayActivity;
    public static int FirmwareUpdateProgressTypeError = 0;
    public static int FirmwareUpdateProgressTypeGeneral = 1;
    public static int FirmwareUpdateProgressTypeProgressOS= 2;
    public static int FirmwareUpdateProgressTypeProgressRF = 3;
    public static int FirmwareUpdateProgressTypeNextDevice = 4;
    public static int FirmwareUpdateProgressTypeEstimatedTime = 5;
    public static int FirmwareUpdateProgressTypeComplete = 6;
    public static int FirmwareUpdateProgressTypeNotUpdated = 7;
    public static int FirmwareUpdateProgressTypeStateComplete = 8;
    int retryCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radion_update_overlay);
        radionOverlayActivity = this;
        if(NavigationWheel.wsClient != null){
            //remove listener for overlay and restore listener to navwheel
            NavigationWheel.wsClient.addWebSocketMessageReceiver(RadionUpdateOverlay.this);
        }
        ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.radion_update_progress);
        firmwareProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage notification) {
        if(notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.LegacyLightFirmwareUpdate.getValue())){
            String msg = "";
            String [] tokens = notification.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            System.out.println("Origin = " + origin);
            if (tokens.length > 1){
                msg = tokens[1];
            }

            if (origin >= FirmwareUpdateProgressTypeGeneral && origin <= FirmwareUpdateProgressTypeProgressRF){
                if(msg.equalsIgnoreCase("The firmware on the device doesn't require an update.")){
                    final String progressMsg = msg;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar loadingIcon = (ProgressBar)findViewById(R.id.radion_preparation_progress);
                            TextView progressText = (TextView)findViewById(R.id.progress_text);
                            ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.radion_update_progress);
                            loadingIcon.setVisibility(View.INVISIBLE);
                            progressText.setText(progressMsg);
                            firmwareProgress.setMax(2);
                            firmwareProgress.setProgress(2);
                        }
                    });
                }else{
                    System.out.println("Web Socket Message for Radion Update: " + msg);
                    final String progressMsg = msg;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar loadingIcon = (ProgressBar)findViewById(R.id.radion_preparation_progress);
                            TextView progressText = (TextView)findViewById(R.id.progress_text);
                            ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.radion_update_progress);
                            loadingIcon.setVisibility(View.INVISIBLE);
                            progressText.setText(progressMsg);
                        }
                    });
                }
            }else if (origin == FirmwareUpdateProgressTypeComplete){
                if(NavigationWheel.wsClient != null){
                    //remove listener for overlay and restore listener to navwheel
                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(RadionUpdateOverlay.this);
                }
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
            }else if (origin == FirmwareUpdateProgressTypeError){
                if(NavigationWheel.wsClient != null){
                    //remove listener for overlay and restore listener to navwheel
                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(RadionUpdateOverlay.this);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        }
    }

    public void resetFirmwareUpdate(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar loadingIcon = (ProgressBar)findViewById(R.id.radion_preparation_progress);
                TextView progressText = (TextView)findViewById(R.id.progress_text);
                ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.radion_update_progress);
                progressText.setText(getResources().getString(R.string.updating_radion_firmware) + " (" + getResources().getString(R.string.retry) + " " +  retryCounter + ")");
                firmwareProgress.setProgress(0);
            }
        });
    }
}
