package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class ProgramSchedule extends Activity implements IESLWebSocketMessageReceiver{

    // Web socket
    SharedPreferences sp;
    int programScheduleDeviceCounter = 0;
    int programScheduleStepCounter = 0;
    int currentProgramScheduleDeviceId = 0;
    boolean calculatedProgress;
    int selectedRadionCount;
    int totalScheduleSteps;
    ProgressBar resetRadions;
    CountDownTimer timeOut;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.program_schedule);

        if(NavigationWheel.wsClient != null){
           NavigationWheel.wsClient.addWebSocketMessageReceiver(this);
        }

        calculatedProgress = false;

        sp = getSharedPreferences("USER_PREF",0);

        selectedRadionCount = sp.getInt("selectedRadionCount", 0);

        resetRadions = (ProgressBar)findViewById(R.id.reset_progress);
        resetRadions.setVisibility(View.INVISIBLE);

        int count = selectedRadionCount * 120000;

        //if there is a connection problem, make sure the view times out if it takes longer than 2 minutes per device
        timeOut = new CountDownTimer(count,1000){
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if(NavigationWheel.wsClient != null){
                    //remove listener for overlay and restore listener to navwheel
                    NavigationWheel.wsClient.deleteWebSocketMessageReceiver(ProgramSchedule.this);
                    NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
                }
                //send cancel for timeout
                setResult(RESULT_CANCELED);
                finish();
            }
        }.start();
    }

    //override method to disable the back button
    @Override
    public void onBackPressed() {
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage eslWebSocketMessage) {
        if(eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.ProgramSchedule.getValue())){

            final ProgressBar programProgress = (ProgressBar)findViewById(R.id.program_progress);
            if(!calculatedProgress){
                totalScheduleSteps = (selectedRadionCount * 3) + 1;
                calculatedProgress = true;
                programProgress.setMax(totalScheduleSteps);
                programProgress.setProgress(programScheduleStepCounter);
            }

            //Do stuff when you know you get a ProgramSchedule message back
            final TextView programmingText = (TextView) findViewById(R.id.schedule_updating);

            String [] tokens = eslWebSocketMessage.getMessage().toString().split("\\|");
            final int origin = Integer.parseInt(tokens[0]);
            System.out.println("Origin = " + origin);
            final String msg = tokens[1];
            int deviceId = 0;
            if(tokens.length > 2){
                deviceId = Integer.parseInt(tokens[2]);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    programmingText.setText(msg);
                }
            });

            if (origin == 0 || origin == 3){
                if (origin == 3){ //Complete
                    timeOut.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            programProgress.setVisibility(View.INVISIBLE);
                            programmingText.setText(getResources().getString(R.string.resetting_radions));
                            resetRadions.setVisibility(View.VISIBLE);
                            new CountDownTimer(5000,1000){
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    if(NavigationWheel.wsClient != null){
                                        //remove listener for overlay and restore listener to navwheel
                                        NavigationWheel.wsClient.deleteWebSocketMessageReceiver(ProgramSchedule.this);
                                        NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
                                    }
                                    finish();
                                }
                            }.start();
                        }
                    });
                    //send OK for proper completion
                    setResult(RESULT_OK);
                }else{ //Error
                    timeOut.cancel();
                    //send cancel for error
                    setResult(RESULT_CANCELED);
                    if(NavigationWheel.wsClient != null){
                        //remove listener for overlay and restore listener to navwheel
                        NavigationWheel.wsClient.deleteWebSocketMessageReceiver(ProgramSchedule.this);
                        NavigationWheel.wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
                    }
                    finish();
                }
            }else if (origin == 1 || origin == 2){
                if (msg != null && msg.length() > 0){
                    programScheduleStepCounter++;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            programProgress.setProgress(programScheduleStepCounter);
                        }
                    });

                    if (currentProgramScheduleDeviceId != 0 && currentProgramScheduleDeviceId != deviceId){
                        if (origin == 2){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    programmingText.setText(R.string.programming_complete_on + " " + programScheduleDeviceCounter);
                                }
                            });
                        }
                        programScheduleDeviceCounter++;
                        currentProgramScheduleDeviceId = deviceId;
                    }else if (currentProgramScheduleDeviceId == 0){
                        programScheduleDeviceCounter++;
                        currentProgramScheduleDeviceId = deviceId;
                    }
                }
                if (msg != null && msg.length() > 3){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            programmingText.setText(msg.substring(0, msg.length() - 3) + " on Radion " + programScheduleDeviceCounter);
                        }
                    });
                }
            }
        }
    }
}