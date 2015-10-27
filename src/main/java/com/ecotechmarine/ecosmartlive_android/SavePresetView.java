package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class SavePresetView extends NavigationUtility {

    public static Context context;
    boolean onPumpSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_preset_view);

        context = SavePresetView.this;
        if (getIntent() != null){
            onPumpSave = getIntent().getBooleanExtra("onPumpSave", false);
        }
        //Set Dat Actionbar!
        initActionBar();
        setActionBarDefaults();
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(R.string.save_preset);

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

        //Make save buttons visible
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder myAlert = new AlertDialog.Builder(context)
                        .setMessage(getResources().getString(R.string.forgot_preset_name))
                        .setTitle(R.string.save_preset)
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                myAlert.show();
            }
        });

        final EditText presetTitle = (EditText) findViewById(R.id.save_preset_text_field);
        //ensure that text field is focused on for key listener
        presetTitle.setFocusableInTouchMode(true);
        presetTitle.requestFocus();

        //On Key listener causes issues in lower APIs
        if (Build.VERSION.SDK_INT >= 11){
            presetTitle.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //Dismiss keyboard when enter is pressed
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(presetTitle.getWindowToken(), 0);
                    return true;
                }
            });
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final EditText presetTitle = (EditText) findViewById(R.id.save_preset_text_field);
                String presetText = presetTitle.getText().toString();

                //Manually created boolean to determine if field is blank or not
                if (presetText == null || presetText.equalsIgnoreCase("")){
                    actionButton2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder myAlert = new AlertDialog.Builder(context)
                                .setMessage(getResources().getString(R.string.forgot_preset_name))
                                .setTitle(R.string.save_preset)
                                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        myAlert.show();
                        }
                    });
                }else{
                    actionButton2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onPumpSave){
                                final String presetText = presetTitle.getText().toString();
                                final ConnectManager connect = ConnectManager.getSharedInstance();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //name must be set or else it remains null and backend function will not work
                                        connect.currentPulse.name = presetText;
                                        connect.savePumpPreset(connect.currentPulse);
                                    }
                                }).start();
                                System.out.println("Current Pulse Stats: " + connect.currentPulse.displayName);
                                Toast.makeText(context, presetText + " " + getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                String presetText = presetTitle.getText().toString();
                                final ConnectManager connect = ConnectManager.getSharedInstance();
                                final Preset preset = connect.getNewPreset();
                                preset.setName(presetText);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect.savePreset(preset);
                                    }
                                }).start();
                                Toast.makeText(context, presetText + " " + getResources().getString(R.string.saved), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
                Log.i("Saved Preset: ", presetText);
            }
        };
        presetTitle.addTextChangedListener(watcher);
    }
}
