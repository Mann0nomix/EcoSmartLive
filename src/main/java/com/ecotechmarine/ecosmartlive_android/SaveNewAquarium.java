package com.ecotechmarine.ecosmartlive_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SaveNewAquarium extends NavigationUtility {
    public static SaveNewAquarium saveNewAquariumActivity;
    public static String aquariumName;
    boolean onLaunch;
    SharedPreferences sp;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveNewAquariumActivity = this;
        setContentView(R.layout.save_new_aquarium);
        if (getIntent() != null){
            onLaunch = getIntent().getBooleanExtra("onLaunch", false);
        }
        initActionBar();
        setActionBarDefaults();
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(R.string.new_aquarium);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        final ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        final ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);
        final EditText aquariumField = (EditText)findViewById(R.id.save_aquarium_text_field);

        //Hide action buttons
        mainMenuButton.setVisibility(View.INVISIBLE);
        groupListButton.setVisibility(View.INVISIBLE);
        saveButton.setText(R.string.next);

        //Make save buttons visible
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        //hide cancel button on launch and force them to do a new aquarium
        if (onLaunch){
            actionButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NavigationWheel.wheelActivity != null){
                        NavigationWheel.wheelActivity.finish();
                    }
                    Intent intent = new Intent(SaveNewAquarium.this, LoginView.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else{
            actionButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set Result so that the navigation wheel knows to refresh
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }

        //Fade out next button because the edittext starts out as null
        if (Build.VERSION.SDK_INT < 11){
            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            saveButton.startAnimation(alpha);
        }else{
            saveButton.setAlpha(.5f);
        }

        actionButton2.setOnClickListener(null);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //SharedPreferences sp = getSharedPreferences("USER_PREF",MODE_PRIVATE);
                //SharedPreferences.Editor edit = sp.edit();
                TextView saveButton = (TextView)findViewById(R.id.save_display_only);
                aquariumName = aquariumField.getText().toString();

                //Manually created boolean to determine if field is blank or not
                if (aquariumField == null || aquariumName.equalsIgnoreCase("")){
                    if (Build.VERSION.SDK_INT < 11){
                        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        saveButton.startAnimation(alpha);
                    }else{
                        saveButton.setAlpha(0.5f);
                    }
                    actionButton2.setOnClickListener(null);
                }else{
                    if (Build.VERSION.SDK_INT < 11){
                        AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        saveButton.startAnimation(alpha);
                    }else{
                        saveButton.setAlpha(1f);
                    }
                    actionButton2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Intent intent = new Intent(SaveNewAquarium.this, AquariumProgramOverlay.class);
                            intent.putExtra("deviceNameEntered", aquariumName);
                            //set SharedPreferences and edit to be used globally
                            sp = getSharedPreferences("USER_PREF",0);
                            edit = sp.edit();
                            edit.putString("newAquariumEntered", aquariumName);
                            edit.commit();
                            if (onLaunch){
                                intent.putExtra("onLaunch", true);
                            }
                            setResult(RESULT_OK);
                            new DiscoverDevicesTask().execute();
                            startActivity(intent);
                        }
                    });
                }
            }
        };
        aquariumField.addTextChangedListener(watcher);
    }

    class DiscoverDevicesTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.discoverDevices();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (onLaunch){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Disconnect from Websocket
                    try{
                        if(NavigationWheel.wsClient != null){
                            NavigationWheel.wsClient.disconnect();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    ConnectManager cm = ConnectManager.getSharedInstance();
                    cm.stopPCControl(0);
                }
            }).start();
            finish();
            if (NavigationWheel.wheelActivity != null){
                NavigationWheel.wheelActivity.finish();
            }
        }
    }
}