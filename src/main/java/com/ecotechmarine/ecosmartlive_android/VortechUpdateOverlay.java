package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;


public class VortechUpdateOverlay extends Activity implements IESLWebSocketMessageReceiver{
    public static Activity vortechOverlayActivity;
    public static int FirmwareUpdateProgressTypeError = 0;
    public static int FirmwareUpdateProgressTypeGeneral = 1;
    public static int FirmwareUpdateProgressTypeProgressOS= 2;
    public static int FirmwareUpdateProgressTypeProgressRF = 3;
    public static int FirmwareUpdateProgressTypeNextDevice = 4;
    public static int FirmwareUpdateProgressTypeEstimatedTime = 5;
    public static int FirmwareUpdateProgressTypeComplete = 6;
    public static int FirmwareUpdateProgressTypeNotUpdated = 7;
    public static int FirmwareUpdateProgressTypeStateComplete = 8;
    boolean onRetry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vortech_update_overlay);
        if (getIntent() != null){
            onRetry = getIntent().getBooleanExtra("onRetry", false);
        }
        vortechOverlayActivity = this;
        if(NavigationWheel.wsClient != null){
            //remove listener for overlay and restore listener to navwheel
            NavigationWheel.wsClient.addWebSocketMessageReceiver(VortechUpdateOverlay.this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage notification) {
        if(notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.LegacyPumpFirmwareUpdate.getValue())){
            String msg = "";
            String [] tokens = notification.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            System.out.println("Origin = " + origin);
            if (tokens.length > 1){
                msg = tokens[1];
            }

            if (origin == FirmwareUpdateProgressTypeProgressOS){
                if (msg.equalsIgnoreCase("The firmware on the device doesn't require an update.")){
                    final String progressMsg = msg;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar loadingIcon = (ProgressBar)findViewById(R.id.vortech_preparation_progress);
                            TextView progressText = (TextView)findViewById(R.id.progress_text);
                            ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.vortech_update_progress);
                            loadingIcon.setVisibility(View.INVISIBLE);
                            progressText.setText(progressMsg);
                            firmwareProgress.setMax(1);
                            firmwareProgress.setProgress(1);
                        }
                    });
                }else {
                    System.out.println("Web Socket Message for Vortech Update: " + msg);
                    String[] splitMsg = msg.split(" ");
                    final int currentProgress = Integer.valueOf(splitMsg[1]);
                    final int totalSteps = Integer.valueOf(splitMsg[splitMsg.length - 2]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar loadingIcon = (ProgressBar)findViewById(R.id.vortech_preparation_progress);
                            TextView progressText = (TextView)findViewById(R.id.progress_text);
                            ProgressBar firmwareProgress = (ProgressBar)findViewById(R.id.vortech_update_progress);
                            loadingIcon.setVisibility(View.INVISIBLE);
                            if (onRetry){
                                progressText.setText(getResources().getString(R.string.updating_vortech_firmware) + " (Retry)");
                            }else{
                                progressText.setText(R.string.updating_vortech_firmware);
                            }
                            firmwareProgress.setMax(totalSteps);
                            firmwareProgress.setProgress(currentProgress);
                        }
                    });
                }
            }else if (origin == FirmwareUpdateProgressTypeComplete){
                if (origin == VortechUpdateOverlay.FirmwareUpdateProgressTypeComplete){
                    if(NavigationWheel.wsClient != null){
                        //remove listener for overlay and restore listener to navwheel
                        NavigationWheel.wsClient.deleteWebSocketMessageReceiver(VortechUpdateOverlay.this);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }else if (origin == FirmwareUpdateProgressTypeError){
                if(NavigationWheel.wsClient != null){
                    //remove listener for overlay and restore listener to navwheel
                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(VortechUpdateOverlay.this);
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
}
