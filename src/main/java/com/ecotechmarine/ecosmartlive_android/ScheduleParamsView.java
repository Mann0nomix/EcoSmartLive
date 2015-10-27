package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class ScheduleParamsView extends NavigationUtility{

    Bundle schedData;
    int schedIndex;
    Calendar schedTime;
    Calendar schedTime2;
    String selectedGroup;
    TextView startDay;
    TextView startNight;
    TimePicker timePicker;
    SeekBar intenseBar;
    TextView schedPercent;
    RelativeLayout timeLayout;
    boolean onDay;
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    //Link list view object to the ArrayAdapter to create table/list view!
    String[] schedList;
    String[] schedTitle;
    //set array to contain image resources (all resources can be stored as integers)
    //Integer[] schedIcons = {R.drawable.sun,R.drawable.sun,R.drawable.icon_signout,R.drawable.icon_signout};

    //globals that will be passed to the server
    float serverBrightness;
    String serverStartDay;
    String serverStartNight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Handle intent passed data and initialize selectedGroup & index
        schedData = getIntent().getExtras();
        if (schedData != null){
            selectedGroup = schedData.getString("groupName");
            schedIndex = schedData.getInt("Index");
        }

        //get list from string resources for list views
        schedList = getResources().getStringArray(R.array.schedList);
        schedTitle = getResources().getStringArray(R.array.schedTitle);

        //initialize global variables for starting state
        sp = getSharedPreferences("USER_PREF",0);
        edit = sp.edit();
        edit.putInt("origin",1);
        edit.commit();

        //Intensity is from 0 to 1 NOT 0 to 2!
        serverBrightness = 1;

        setContentView(R.layout.schedule_params_view);
        initActionBar();
        //handles save button
        setActionBarDefaults();
        initScheduleList();
        initTimeDefaults();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        intenseBar = (SeekBar) findViewById(R.id.intensity_slider);
        schedPercent = (TextView)findViewById(R.id.sched_slider_percent);
        intenseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                schedPercent.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                serverBrightness = (float) intenseBar.getProgress() / 100;
            }
        });
    }

    public void initTimeDefaults(){
        timePicker = (TimePicker) findViewById(R.id.timepicker);
        timeLayout = (RelativeLayout) findViewById(R.id.time_layout);
        timeLayout.setVisibility(View.INVISIBLE);

        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
            timePicker.setIs24HourView(false);
        }else{
            timePicker.setIs24HourView(true);
        }

        //grab instance to initialize calendar objects for the schedules
        schedTime = Calendar.getInstance();
        schedTime2 = Calendar.getInstance();

        //set the default times based on the index of the schedule flipper when the view is created
        switch(schedIndex){
            case 0:
                schedTime.set(Calendar.HOUR_OF_DAY,9);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,21);
                schedTime2.set(Calendar.MINUTE,0);
                break;
            case 1:
                schedTime.set(Calendar.HOUR_OF_DAY,9);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,21);
                schedTime2.set(Calendar.MINUTE,0);
                break;
            case 2:
                schedTime.set(Calendar.HOUR_OF_DAY,9);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,21);
                schedTime2.set(Calendar.MINUTE,0);
                break;
            case 3:
                schedTime.set(Calendar.HOUR_OF_DAY,7);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,18);
                schedTime2.set(Calendar.MINUTE,0);
                break;
            case 4:
                schedTime.set(Calendar.HOUR_OF_DAY,6);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,19);
                schedTime2.set(Calendar.MINUTE,0);
                break;
            case 5:
                schedTime.set(Calendar.HOUR_OF_DAY,6);
                schedTime.set(Calendar.MINUTE,0);
                schedTime2.set(Calendar.HOUR_OF_DAY,19);
                schedTime2.set(Calendar.MINUTE,0);
                break;
        }
    }

    public class CustomAdapter extends ArrayAdapter<String>
    {
        public CustomAdapter(Context context, int textViewResourceId, String[] objects)
        {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater;
            TextView item;

            switch (position){
                case 0:
                    // Inflate the layout in each row.
                    inflater = ScheduleParamsView.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.schedule_list_item, parent, false);

                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    item = (TextView)convertView.findViewById(R.id.sched_list_text);

                    item.setText(schedTitle[schedIndex]);

                    // Declare and define the TextView, "icon." This is where
                    // the icon in each row will appear.

                    //icon=(ImageView)row.findViewById(R.id.sched_list_img);
                    //icon.setImageResource(android.R.drawable.checkbox_on_background);
                    break;
                case 1:
                    inflater = ScheduleParamsView.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.schedule_list_datepicker, parent, false);

                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    item = (TextView)convertView.findViewById(R.id.sched_list_text);
                    item.setText(schedList[position]);
                    startDay = (TextView)convertView.findViewById(R.id.sched_list_picker);
                    SimpleDateFormat formatter;
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    String dayTime = formatter.format(schedTime.getTime());
                    startDay.setText(dayTime);
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("HH:mm");
                    }else{
                        formatter = new SimpleDateFormat("HH:mm");
                    }
                    dayTime = formatter.format(schedTime.getTime());
                    serverStartDay = dayTime;
                    break;
                case 2:
                    // Inflate the layout in each row.
                    inflater = ScheduleParamsView.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.schedule_list_datepicker, parent, false);

                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    item = (TextView)convertView.findViewById(R.id.sched_list_text);
                    item.setText(schedList[position]);
                    startNight = (TextView)convertView.findViewById(R.id.sched_list_picker);
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    String nightTime = formatter.format(schedTime2.getTime());
                    startNight.setText(nightTime);
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("HH:mm");
                    }else{
                        formatter = new SimpleDateFormat("HH:mm");
                    }
                    nightTime = formatter.format(schedTime2.getTime());
                    serverStartNight = nightTime;
                    break;
                default:
                    // Inflate the layout in each row.
                    inflater = ScheduleParamsView.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.schedule_list_slider, parent, false);


                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    item = (TextView)convertView.findViewById(R.id.sched_list_text);
                    item.setText(schedList[position]);
                    break;
            }
            // Inflate the layout in each row.
            /*LayoutInflater inflater = ScheduleParamsView.this.getLayoutInflater();
            View row = inflater.inflate(R.layout.schedule_list_item, parent, false);

            // Declare and define the TextView, "item." This is where
            // the name of each item will appear.
            TextView item = (TextView)row.findViewById(R.id.sched_list_text);
            item.setText(schedList[position]);

            // Declare and define the TextView, "icon." This is where
            // the icon in each row will appear.
            ImageView icon=(ImageView)row.findViewById(R.id.sched_list_img);
            icon.setImageResource(schedIcons[position]);*/

            return convertView;
        }
    }

    public void initScheduleList(){
        //Link list view object to the ArrayAdapter to create table/list view!
        final ListView schedView = (ListView) findViewById(R.id.schedule_list);
        schedView.setDivider(new ColorDrawable(getResources().getColor(R.color.DarkGray)));
        schedView.setDividerHeight(1);
        schedView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ArrayAdapter<String> schedAdapter = new CustomAdapter(this,R.layout.schedule_list_slider,schedList);
        schedView.setAdapter(schedAdapter);
        schedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        onDay = true;
                        timeLayout.setVisibility(View.VISIBLE);
                        timePicker.setCurrentHour(schedTime.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(schedTime.get(Calendar.MINUTE));
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("HH:mm");
                        }else{
                            formatter = new SimpleDateFormat("HH:mm");
                        }
                        serverStartDay = formatter.format(schedTime.getTime());
                        break;
                    case 2:
                        onDay = false;
                        timeLayout.setVisibility(View.VISIBLE);
                        timePicker.setCurrentHour(schedTime2.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(schedTime2.get(Calendar.MINUTE));
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("HH:mm");
                        }else{
                            formatter = new SimpleDateFormat("HH:mm");
                        }
                        serverStartNight = formatter.format(schedTime2.getTime());
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
            }

        });

        TextView timeCancel = (TextView) findViewById(R.id.time_cancel);
        TextView timeDone = (TextView) findViewById(R.id.time_done);

        timeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeLayout.setVisibility(View.INVISIBLE);
            }
        });

        timeDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the time to the values selected
                if (onDay){
                    schedTime.set(Calendar.HOUR_OF_DAY,timePicker.getCurrentHour());
                    schedTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                }else{
                    schedTime2.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    schedTime2.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                }
                  
                if (Math.abs(schedTime.get(Calendar.HOUR_OF_DAY) - schedTime2.get(Calendar.HOUR_OF_DAY)) >= 1){
                    if (onDay){
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        String dayTime = formatter.format(schedTime.getTime());
                        startDay.setText(dayTime);
                        formatter = new SimpleDateFormat("KK:mm");
                        serverStartDay = formatter.format(schedTime.getTime());
                    }else{
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        String nightTime = formatter.format(schedTime2.getTime());
                        startNight.setText(nightTime);
                        formatter = new SimpleDateFormat("KK:mm");
                        serverStartNight = formatter.format(schedTime2.getTime());
                    }
                    timeLayout.setVisibility(View.INVISIBLE);
                    schedAdapter.notifyDataSetChanged();
                }else{
                    AlertDialog.Builder myAlert = new AlertDialog.Builder(ScheduleParamsView.this)
                            .setMessage(R.string.wrong_start_time_msg)
                            .setTitle(R.string.wrong_start_time_title)
                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    myAlert.show();
                    //set the time back to the default on failure of following the rules!
                    switch(schedIndex){
                        case 0:
                            schedTime.set(Calendar.HOUR_OF_DAY,9);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,21);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                        case 1:
                            schedTime.set(Calendar.HOUR_OF_DAY,9);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,21);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                        case 2:
                            schedTime.set(Calendar.HOUR_OF_DAY,9);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,21);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                        case 3:
                            schedTime.set(Calendar.HOUR_OF_DAY,7);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,18);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                        case 4:
                            schedTime.set(Calendar.HOUR_OF_DAY,6);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,19);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                        case 5:
                            schedTime.set(Calendar.HOUR_OF_DAY,6);
                            schedTime.set(Calendar.MINUTE,0);
                            schedTime2.set(Calendar.HOUR_OF_DAY,19);
                            schedTime2.set(Calendar.MINUTE,0);
                            break;
                    }
                    //set text of views back to default
                    SimpleDateFormat formatter;
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    String dayTime = formatter.format(schedTime.getTime());
                    startDay.setText(dayTime);
                    String nightTime = formatter.format(schedTime2.getTime());
                    startNight.setText(nightTime);
                    schedAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(selectedGroup);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
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

                SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
                final int groupID = sp.getInt("groupID", 0);

                if (!serverStartDay.equalsIgnoreCase("") || serverStartDay != null || !serverStartNight.equalsIgnoreCase("") || serverStartNight != null){
                    Intent intent = new Intent(getApplicationContext(),ProgramSchedule.class);
                    startActivityForResult(intent,0);
                    try{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ConnectManager connect = ConnectManager.getSharedInstance();
                                String scheduleType = "A";
                                if (schedIndex + 1 >= 5){
                                    scheduleType = "N";
                                }
                                //convert saved time to be sent to back end
                                System.out.println("Server Start Day = " + serverStartDay);
                                System.out.println("Server Start Night = " + serverStartNight);
                                connect.resetToDefaultSchedule(schedIndex + 1, groupID, scheduleType, serverBrightness, serverStartDay, serverStartNight);
                                connect.sendProgramGroup(groupID, scheduleType);
                            }
                        }).start();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }else{
                    AlertDialog.Builder otherAlert = new AlertDialog.Builder(context)
                            .setMessage("This should never ever be seen")
                            .setTitle("Null Times!")
                            .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    otherAlert.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(RESULT_OK);
        if (resultCode == RESULT_OK){
            AlertDialog.Builder completionAlert = new AlertDialog.Builder(this)
                    .setMessage(R.string.radions_programmed)
                    .setTitle(R.string.scheduling_successful_title)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });
            completionAlert.show();
        }else{
            setResult(RESULT_CANCELED);
            AlertDialog.Builder completionAlert = new AlertDialog.Builder(this)
                    .setMessage(R.string.error_programming)
                    .setTitle(R.string.error_programming_title)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            completionAlert.show();
        }
    }
}
