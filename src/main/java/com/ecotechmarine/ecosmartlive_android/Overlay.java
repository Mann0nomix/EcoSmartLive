package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketClient;
import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class Overlay extends Activity implements IESLWebSocketMessageReceiver {
    //For saving activity to be finished in Warning View
    public static Overlay overlayActivity;
    int count = 0;

    // Web socket
    private CountDownTimer countDownTimer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlay);
        overlayActivity = this;
        if (NavigationWheel.wsClient != null){
            //stop listening once you are connected
            NavigationWheel.wsClient.deleteWebSocketMessageReceiver(this);
        }

        // Start our initial connection timer
        countDownTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // NOT CONNECTED TO ESL
                // Display warning screen using the WarningModeClientNotConnected message type
                Intent intent = new Intent(getApplicationContext(), WarningView.class);
                intent.putExtra("msg",getResources().getString(R.string.warning_not_connect));
                intent.putExtra("msg2",getResources().getString(R.string.warning_not_connect2));
                startActivity(intent);
                finish();
            }
        };
        countDownTimer.start();
        connectToEsl();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage eslWebSocketMessage) {
        if(eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.ConnectedToEsl.getValue())){
            if (count < 1){
                count++;
                // CONNECTED TO ESL - GOOD TO GO!
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NavigationWheel.wheelActivity.loadWheelData();
                    }
                });
                cancelTimer();
                finish();
            }
        }
    }

    public void cancelTimer(){
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    private void connectToEsl(){
        // Check the internet connection
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "true");
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");

        try{
            // Open a web socket connection
            SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
            String wsHost = sp.getString("wsHost", null);
            ConnectManager.webSocketHost = wsHost;
            String wsPort = sp.getString("wsPort", "8881");
            Long userId = Long.parseLong(sp.getString("userID", null));
            NavigationWheel.wsClient = ESLWebSocketClient.sharedInstance(String.format("%s:%s", wsHost, wsPort), userId);
            NavigationWheel.wsClient.addWebSocketMessageReceiver(this);
            NavigationWheel.wsClient.connect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

