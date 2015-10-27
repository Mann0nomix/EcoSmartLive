package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;


public class AquariumProgramOverlay extends Activity implements IESLWebSocketMessageReceiver{
    String msg;
    String deviceNameEntered;
    boolean onAssign;
    boolean onLaunch;
    CountDownTimer timeOut;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_overlay);

        //set SharedPreferences and edit to be used globally
        sp = getSharedPreferences("USER_PREF", 0);

        if (getIntent() != null){
            deviceNameEntered = sp.getString("newAquariumEntered", "");
            onAssign = getIntent().getBooleanExtra("onAssign", false);
            onLaunch = getIntent().getBooleanExtra("onLaunch", false);
        }

        if(NavigationWheel.wsClient != null){
            NavigationWheel.wsClient.addWebSocketMessageReceiver(this);
        }

        //set initial default text for the overlay
        final TextView refreshText = (TextView) findViewById(R.id.refresh_text);
        //handle whether you are doing assign and group or discovering devices
        if (!onAssign){
            refreshText.setText(R.string.searching_for_devices);
        }else{
            refreshText.setText(R.string.addressing_devices);
        }

        //if there is a connection problem, make sure the view times out if it takes longer than 2 minutes per device
        timeOut = new CountDownTimer(120000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //send cancel for timeout
                if(NavigationWheel.wsClient != null){
                    //remove listener for overlay and restore listener to navwheel
                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(AquariumProgramOverlay.this);
                    NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
                }
                if (SaveNewAquarium.saveNewAquariumActivity != null){
                    SaveNewAquarium.saveNewAquariumActivity.finish();
                }
                if (NewAquariumList.wizardActivity != null){
                    NewAquariumList.wizardActivity.finish();
                }
                finish();
            }
        }.start();
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage eslWebSocketMessage) {
        if(eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.DiscoverDevices.getValue())){
            final TextView refreshText = (TextView) findViewById(R.id.refresh_text);
            String [] tokens = eslWebSocketMessage.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            System.out.println("Origin = " + origin);
            if (tokens.length > 1){
                msg = tokens[1];
            }

            if (origin == 0 || origin == 3){
                timeOut.cancel();

                //timeOut.cancel();
                if (origin == 3){ //Complete
                    //timeOut.cancel();
                    if (!msg.equalsIgnoreCase("0")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = msg.replace("[", "");
                                message = message.replace("]", "");
                                message = message.replace("\\", "");
                                message = message.replace("}\",\"{", "},{");
                                message = message.replace("\"\"", "");
                                message = message.replace("[","[sssssss");
                                message = message.replace("\"{\"","{\"");
                                message = message.replace("\"}\"","\"}");
                                message = String.format("{\"devices\":[%s]}", message);
                                System.out.println(message);

                                //If the NewAquariumList already exists, make sure to close it before opening a new copy
                                if (NewAquariumList.wizardActivity != null){
                                    NewAquariumList.wizardActivity.finish();
                                }

                                Intent intent = new Intent(AquariumProgramOverlay.this, NewAquariumList.class);
                                intent.putExtra("deviceListString", message);
                                intent.putExtra("deviceNameEntered", deviceNameEntered);
                                if (onLaunch){
                                    intent.putExtra("onLaunch", true);
                                }
                                startActivity(intent);

                                if (SaveNewAquarium.saveNewAquariumActivity != null){
                                    SaveNewAquarium.saveNewAquariumActivity.finish();
                                }
                                if(NavigationWheel.wsClient != null){
                                    //remove listener for overlay and restore listener to navwheel
                                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(AquariumProgramOverlay.this);
                                    NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
                                }
                                finish();
                            }
                        });

                    }
                }
            }else if (origin == 2){
                if (msg != null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshText.setText(msg);
                        }
                    });
                }
            }
        }

        if(eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.AssignAndGroup.getValue())){
            final TextView refreshText = (TextView) findViewById(R.id.refresh_text);
            String [] tokens = eslWebSocketMessage.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            System.out.println("Origin = " + origin);
            if (tokens.length > 1){
                msg = tokens[1];
            }

            if (origin == 0 || origin == 3){
                timeOut.cancel();
                if (origin == 3) { //Complete
                    new ProcessAndSetRTTask().execute();
                }
            }else if (origin == 2){
                if (msg != null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshText.setText(msg);
                        }
                    });
                }
            }
        }
    }

    public class ProcessAndSetRTTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.processAndSetRT();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setResult(RESULT_CANCELED);
            finish();
            if(NavigationWheel.wsClient != null){
                //remove listener for overlay and restore listener to navwheel
                NavigationWheel.wsClient.deleteWebSocketMessageReceiver(AquariumProgramOverlay.this);
                NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
            }
        }
    }

    @Override
    public void onBackPressed() {}
}