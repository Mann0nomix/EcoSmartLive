package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SchedulePointView extends NavigationUtility {

    String[] groupHeaders = new String[3];
    String currentTime;
    RelativeLayout pickerOverlay;
    Calendar now;
    Calendar newTime;
    TextView pointTime;
    String chosenTime;
    Boolean timeChanged = false;
    int cloudProgress = 0;
    int stormProgress = 0;
    int brightProgress = 100;
    String presetName;
    int presetId;
    int customUV;
    int customRB;
    int customBlue;
    int customWhite;
    int customGreen;
    int customRed;
    int customBright;
    boolean customSelected;
    boolean isEditPoint;
    String editPointTime;
    int editPointHours;
    int editPointMins;
    Calendar editTimeObject;
    TextView customViewLabel;
    ExpandableListAdapter pointAdapter;
    ScheduleDataPoint saveDataPoint;
    String userScheduleType;
    int userScheduleId;
    int editIndex;
    SharedPreferences sp;
    Preset preset;
    SeekBar brightnessBar;
    int[] schedulePointTimes;
    int pickerTotalSeconds;
    int startDayTime;
    int startNightTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_point_view);
        //get values passed from Data Point Scroll View
        saveDataPoint = new ScheduleDataPoint();
        sp = getSharedPreferences("USER_PREF",0);
        //set saveDataPoint = the current time
        now = Calendar.getInstance();
        saveDataPoint.startTimeHour = now.get(Calendar.HOUR_OF_DAY);
        saveDataPoint.startTimeMinute = now.get(Calendar.MINUTE);
        saveDataPoint.startTimeSeconds = now.get(Calendar.SECOND);
        //set initial brightness if not edit point to 100
        saveDataPoint.brightness = 2;
        pickerOverlay = (RelativeLayout)findViewById(R.id.time_layout);

        //always default sliders to 50%
        customUV = 50;
        customRB = 50;
        customBlue = 50;
        customWhite = 50;
        customGreen = 50;
        customRed = 50;
        customBright = 50;

        //default save data point to store the values above (only when the view is created)
        float convertUV = customUV * 2 / 100;
        float convertRB = customRB * 2 / 100;
        float convertBlue = customBlue * 2 / 100;
        float convertWhite = customWhite * 2 / 100;
        float convertGreen = customGreen * 2 / 100;
        float convertRed = customRed * 2 / 100;
        float convertBright = customBright * 2 / 100;

        //Store converted values into the saveDataPoint object for a default save setting
        saveDataPoint.uv = convertUV;
        saveDataPoint.royalBlue1 = convertRB;
        saveDataPoint.royalBlue2 = convertRB;
        saveDataPoint.blue = convertBlue;
        saveDataPoint.white = convertWhite;
        saveDataPoint.green = convertGreen;
        saveDataPoint.red = convertRed;
        saveDataPoint.brightness = convertBright;
        saveDataPoint.presetName = getResources().getString(R.string.custom);

        if (getIntent() != null) {
            userScheduleType = getIntent().getStringExtra("scheduleType");
            userScheduleId = getIntent().getIntExtra("scheduleId",0);
            isEditPoint = getIntent().getBooleanExtra("isEditPoint", false);
            schedulePointTimes = getIntent().getIntArrayExtra("schedulePointTimes");
            startDayTime = getIntent().getIntExtra("startDayTime", 0);
            startNightTime = getIntent().getIntExtra("startNightTime", 0);

            if (isEditPoint){
                editPointTime = getIntent().getStringExtra("editPointTime");
                editPointHours = getIntent().getIntExtra("editPointHours", 0);
                editPointMins = getIntent().getIntExtra("editPointMins", 0);
                editIndex = getIntent().getIntExtra("editIndex", 0);

                //schedule data
                saveDataPoint.scheduleDataPointId = getIntent().getLongExtra("editDataPointId",0);
                saveDataPoint.uv = getIntent().getFloatExtra("editUV", 0);
                saveDataPoint.royalBlue1 = getIntent().getFloatExtra("editRB", 0);
                saveDataPoint.royalBlue2 = getIntent().getFloatExtra("editRB2", 0);
                saveDataPoint.blue = getIntent().getFloatExtra("editBlue", 0);
                saveDataPoint.white = getIntent().getFloatExtra("editWhite", 0);
                saveDataPoint.red = getIntent().getFloatExtra("editRed", 0);
                saveDataPoint.green = getIntent().getFloatExtra("editGreen", 0);
                saveDataPoint.brightness = getIntent().getFloatExtra("editBright", 0);
                saveDataPoint.stormFrequency = getIntent().getIntExtra("editStorm", 0);
                saveDataPoint.cloudFrequency = getIntent().getIntExtra("editCloud", 0);
                saveDataPoint.startTimeHour = getIntent().getIntExtra("editTimeHours",0);
                saveDataPoint.startTimeMinute = getIntent().getIntExtra("editTimeMins",0);
                saveDataPoint.startTimeSeconds = getIntent().getIntExtra("editTimeSeconds",0);
                saveDataPoint.presetId = getIntent().getIntExtra("editPresetId",0);
                saveDataPoint.presetName = getIntent().getStringExtra("editPresetName");
                saveDataPoint.isNightMode = getIntent().getBooleanExtra("editIsNightMode",false);
                saveDataPoint.isStartDay = getIntent().getBooleanExtra("editIsStartDay",false);
                saveDataPoint.isStartNight = getIntent().getBooleanExtra("editIsStartNight",false);

                if (saveDataPoint.presetId != 0  && !customSelected){
                    preset = new Preset();
                    preset.setPresetId(saveDataPoint.presetId);
                    preset.setName(saveDataPoint.presetName);
                    preset.setRoyalBlue((saveDataPoint.royalBlue1));
                    preset.setBlue((saveDataPoint.blue));
                    preset.setWhite(saveDataPoint.white);
                    preset.setRed(saveDataPoint.red);
                    preset.setGreen(saveDataPoint.green);
                    preset.setUv(saveDataPoint.uv);

                    customUV = (int)(saveDataPoint.uv * 100.0f / 2.0f);
                    customRB = (int)(saveDataPoint.royalBlue1 * 100.0f / 2.0f);
                    customBlue = (int)(saveDataPoint.blue * 100.0f / 2.0f);
                    customWhite = (int)(saveDataPoint.white * 100.0f / 2.0f);
                    customGreen = (int)(saveDataPoint.green * 100.0f / 2.0f);
                    customRed = (int)(saveDataPoint.red * 100.0f / 2.0f);
                    customBright = (int)(saveDataPoint.brightness * 100.0f / 2.0f);

                    new PresetLoad().execute();
                }

                presetName = saveDataPoint.presetName;
                if (saveDataPoint.presetName.equalsIgnoreCase(getResources().getString(R.string.custom))){
                    customSelected = true;
                    presetName = null;

                    customUV = (int)(saveDataPoint.uv * 100.0f / 2.0f);
                    customRB = (int)(saveDataPoint.royalBlue1 * 100.0f / 2.0f);
                    customBlue = (int)(saveDataPoint.blue * 100.0f / 2.0f);
                    customWhite = (int)(saveDataPoint.white * 100.0f / 2.0f);
                    customGreen = (int)(saveDataPoint.green * 100.0f / 2.0f);
                    customRed = (int)(saveDataPoint.red * 100.0f / 2.0f);
                    customBright = (int)(saveDataPoint.brightness * 100.0f / 2.0f);

                    new CustomColorTask().execute();
                }

                cloudProgress = (saveDataPoint.cloudFrequency * 10);
                System.out.println(cloudProgress);
                stormProgress = (saveDataPoint.stormFrequency * 10);
                System.out.println(stormProgress);
                brightProgress = (int)(saveDataPoint.brightness * 100.0f / 2.0f);
                System.out.println(brightProgress);
            }
        }
        initActionBar();
        setActionBarDefaults(); //used to changed buttons and title on ActionBar
        initPointList();
        groupHeaders = getResources().getStringArray(R.array.point_view_headers);
        editTimeObject = Calendar.getInstance();
        editTimeObject.set(Calendar.HOUR_OF_DAY,editPointHours);
        editTimeObject.set(Calendar.MINUTE,editPointMins);
        pickerOverlay.setVisibility(View.INVISIBLE);
    }

    public void initPointList(){
        pointAdapter = new ExpandableListAdapter() {
            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                observer.onChanged();
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override

            public int getGroupCount() {
                if (saveDataPoint.isStartDay || saveDataPoint.isStartNight){
                    return groupHeaders.length - 1;
                }
                return groupHeaders.length;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                switch (groupPosition){
                    case 0:
                        return 1;
                    case 1:
                        return 3;
                    case 2:
                        return 2;
                    default:
                        return 1;
                }
            }

            @Override
            public Object getGroup(int groupPosition) {
                return null;
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return null;
            }

            @Override
            public long getGroupId(int groupPosition) {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                TextView groupTitleView = (TextView) getLayoutInflater().inflate(R.layout.point_header_view,null);
                //TextView header = (TextView)newView.findViewById(R.id.tv);

                if (groupTitleView != null){
                    groupTitleView.setText(groupHeaders[groupPosition]);
                }

                //generate margins (to do this, I had to typecast the view to a relative layout, configure the settings, then recast it to textview
                if (groupPosition == 0 && groupTitleView != null){
                    groupTitleView.setPadding(30,30,0,20);
                }

                ((ExpandableListView) parent).expandGroup(groupPosition);
                //keep all groups expanded at all times
                return groupTitleView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                switch (groupPosition){
                    case 0:
                        convertView = getLayoutInflater().inflate(R.layout.point_list_time_view, null);
                        pointTime = (TextView)convertView.findViewById(R.id.point_time_selection);
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        currentTime = formatter.format(now.getTime());
                        if (timeChanged){
                            pointTime.setText(chosenTime);
                        }else{
                            if (isEditPoint){
                                if (saveDataPoint.isStartDay){
                                    TextView pointHeader = (TextView)convertView.findViewById(R.id.point_time_header);
                                    pointHeader.setText(R.string.start_day);
                                }
                                if (saveDataPoint.isStartNight){
                                    TextView pointHeader = (TextView)convertView.findViewById(R.id.point_time_header);
                                    pointHeader.setText(R.string.start_night);
                                }
                                pointTime.setText(editPointTime);
                                chosenTime = editPointTime;
                            }else{
                                pointTime.setText(currentTime);
                                chosenTime = currentTime;
                            }
                        }
                        break;
                    case 1:
                        if (childPosition < 2){
                            convertView = getLayoutInflater().inflate(R.layout.point_list_default_view, null);
                            if (childPosition == 0){
                                if (customSelected){
                                    customViewLabel = (TextView) convertView.findViewById(R.id.point_selection_label);
                                    customViewLabel.setTypeface(null,Typeface.BOLD);
                                }else{
                                    customViewLabel = (TextView) convertView.findViewById(R.id.point_selection_label);
                                    customViewLabel.setTypeface(null,Typeface.NORMAL);
                                }
                            }else{
                                if (presetName != null){
                                    TextView presetSelectionText = (TextView)convertView.findViewById(R.id.point_selection_text);
                                    TextView presetSelectionLabel = (TextView)convertView.findViewById(R.id.point_selection_label);
                                    presetSelectionText.setText(presetName);
                                    presetSelectionLabel.setTypeface(null,Typeface.BOLD);
                                }else{
                                    TextView presetSelectionText = (TextView)convertView.findViewById(R.id.point_selection_text);
                                    TextView presetSelectionLabel = (TextView)convertView.findViewById(R.id.point_selection_label);
                                    presetSelectionText.setText("");
                                    presetSelectionLabel.setTypeface(null,Typeface.NORMAL);
                                }
                            }
                        }else{
                            convertView = getLayoutInflater().inflate(R.layout.point_list_bright_view, null);
                            brightnessBar = (SeekBar)convertView.findViewById(R.id.point_brightness_slider);
                            final TextView brightnessValue = (TextView)convertView.findViewById(R.id.point_brightness_value);
                            brightnessBar.setProgress(brightProgress);
                            brightnessValue.setText(brightProgress + "%");
                            brightnessBar.incrementProgressBy(5);
                            brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    int remainder = progress % 5;
                                    progress = progress - remainder;
                                    if (remainder == 0){
                                        brightnessValue.setText(progress + "%");
                                    }else{
                                        seekBar.setProgress(progress);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    brightProgress = seekBar.getProgress();
                                    saveDataPoint.brightness = (float)(brightProgress / 100.0f * 2.0f);
                                    new CustomColorTask().execute();
                                }
                            });
                        }
                        if (childPosition == 1){
                            TextView presetLabel = (TextView)convertView.findViewById(R.id.point_selection_label);
                            presetLabel.setText(R.string.preset);
                        }
                        break;
                    case 2:
                        convertView = getLayoutInflater().inflate(R.layout.point_list_weather_view, null);
                        final SeekBar probabilityBar = (SeekBar)convertView.findViewById(R.id.probability_slider);
                        final TextView probabilityValue = (TextView)convertView.findViewById(R.id.probability_value);
                        //this value is used to pass in the child position to the seek bar listener
                        final int childPos = childPosition;
                        if (childPosition == 1){
                            TextView probabilityLabel = (TextView)convertView.findViewById(R.id.probability_header);
                            probabilityLabel.setText(R.string.cloud_probability);
                            ImageView weatherImage = (ImageView)convertView.findViewById(R.id.weather_image);
                            weatherImage.setImageResource(R.drawable.button_cloud_cover);
                            probabilityValue.setText(cloudProgress + "%");
                            probabilityBar.setProgress(cloudProgress);
                        }else{
                            probabilityValue.setText(stormProgress + "%");
                            probabilityBar.setProgress(stormProgress);
                        }

                        probabilityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                int remainder = progress % 10;
                                progress = progress - remainder;
                                if (remainder == 0){
                                    probabilityValue.setText(progress + "%");
                                }else{
                                    seekBar.setProgress(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                if (childPos == 1){
                                    cloudProgress = seekBar.getProgress();
                                    saveDataPoint.cloudFrequency = cloudProgress / 10;
                                }else{
                                    stormProgress = seekBar.getProgress();
                                    saveDataPoint.stormFrequency = stormProgress / 10;
                                }
                            }
                        });
                        break;
                }

                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public void onGroupExpanded(int groupPosition) {

            }

            @Override
            public void onGroupCollapsed(int groupPosition) {

            }

            @Override
            public long getCombinedChildId(long groupId, long childId) {
                return 0;
            }

            @Override
            public long getCombinedGroupId(long groupId) {
                return 0;
            }
        };
        ExpandableListView pointList = (ExpandableListView)findViewById(R.id.edit_point_list);
        pointList.setGroupIndicator(null);
        pointList.setOnGroupClickListener(null);
        pointList.setChildIndicator(null);
        pointList.setDividerHeight(1);
        pointList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        pointList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        pointList.setAdapter(pointAdapter);
        pointList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                switch(groupPosition){
                    case 0:
                        //reset the now time on the click of the time
                        //now = Calendar.getInstance();
                        //saveDataPoint.startTimeHour =
                        //
                        // .get(Calendar.HOUR_OF_DAY);
                        //saveDataPoint.startTimeMinute = now.get(Calendar.MINUTE);
                        //saveDataPoint.startTimeSeconds = now.get(Calendar.SECOND);
                        pickerOverlay.setVisibility(View.VISIBLE);
                        TimePicker timePicker = (TimePicker)pickerOverlay.findViewById(R.id.timepicker);
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            timePicker.setIs24HourView(false);
                        }else{
                            timePicker.setIs24HourView(true);
                        }
                        if (!timeChanged){
                            if (isEditPoint){
                                timePicker.setCurrentHour(editTimeObject.get(Calendar.HOUR_OF_DAY));
                                timePicker.setCurrentMinute(editTimeObject.get(Calendar.MINUTE));
                            }else{
                                timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
                                timePicker.setCurrentMinute(now.get(Calendar.MINUTE));
                            }
                        }
                        pickerOverlay.findViewById(R.id.time_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pickerOverlay.setVisibility(View.GONE);
                            }
                        });
                        pickerOverlay.findViewById(R.id.time_done).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TimePicker timePicker = (TimePicker)pickerOverlay.findViewById(R.id.timepicker);
                                if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                    timePicker.setIs24HourView(false);
                                }else{
                                    timePicker.setIs24HourView(true);
                                }
                                newTime = Calendar.getInstance();
                                //grab total seconds from the start of the picker
                                pickerTotalSeconds = (timePicker.getCurrentHour() * 3600) + (timePicker.getCurrentMinute() * 60);
                                //loop thru all data point times and make sure the user is not within 5 minutes of another data point time
                                boolean badtime = false;
                                for (int i = 0; i < schedulePointTimes.length; i++){
                                    //If the time chosen is ever less than 5 minutes between a data point, display an alert saying the entry is invalid
                                    if (userScheduleType.equalsIgnoreCase("A")){
                                        if (Math.abs(pickerTotalSeconds - schedulePointTimes[i]) >= 300){
                                            badtime = false;
                                        }else{
                                            AlertDialog.Builder myAlert = new AlertDialog.Builder(SchedulePointView.this)
                                                    .setMessage(R.string.enter_valid_start_time)
                                                    .setTitle(R.string.time)
                                                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            myAlert.show();
                                            pickerOverlay.setVisibility(View.GONE);
                                            timeChanged = false;
                                            //stop looping the second you know the user enter a wrong time
                                            badtime = true;
                                            break;
                                        }
                                    }else{
                                        if (Math.abs(pickerTotalSeconds - schedulePointTimes[i]) >= 300 && pickerTotalSeconds < startDayTime && pickerTotalSeconds > startNightTime){
                                            badtime = false;
                                        }else{
                                            AlertDialog.Builder myAlert = new AlertDialog.Builder(SchedulePointView.this)
                                                    .setMessage(R.string.enter_valid_start_time)
                                                    .setTitle(R.string.time)
                                                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            myAlert.show();
                                            pickerOverlay.setVisibility(View.GONE);
                                            timeChanged = false;
                                            //stop looping the second you know the user enter a wrong time
                                            badtime = true;
                                            break;
                                        }
                                    }

                                }
                                if (!badtime){
                                    newTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                                    newTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                                    saveDataPoint.startTimeHour = newTime.get(Calendar.HOUR_OF_DAY);
                                    saveDataPoint.startTimeMinute = newTime.get(Calendar.MINUTE);
                                    saveDataPoint.startTimeSeconds = newTime.get(Calendar.SECOND);
                                    SimpleDateFormat formatter;
                                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                        formatter = new SimpleDateFormat("h:mm aa");
                                    }else{
                                        formatter = new SimpleDateFormat("k:mm");
                                    }

                                    pickerOverlay.setVisibility(View.GONE);
                                    chosenTime = formatter.format(newTime.getTime());
                                    pointTime.setText(chosenTime);
                                    timeChanged = true;
                                }

                            }
                        });
                        break;
                    case 1:
                        if (childPosition == 0){
                            Intent intent = new Intent(getApplicationContext(),ColorSelection.class);
                            if (isEditPoint){
                                intent.putExtra("customUV", customUV);
                                intent.putExtra("customRB", customRB);
                                intent.putExtra("customBlue", customBlue);
                                intent.putExtra("customWhite", customWhite);
                                intent.putExtra("customGreen", customGreen);
                                intent.putExtra("customRed", customRed);
                                intent.putExtra("customBright", customBright);
                            }else{
                                intent.putExtra("customUV", 50);
                                intent.putExtra("customRB", 50);
                                intent.putExtra("customBlue", 50);
                                intent.putExtra("customWhite", 50);
                                intent.putExtra("customGreen", 50);
                                intent.putExtra("customRed", 50);
                                intent.putExtra("customBright", 50);
                            }
                            intent.putExtra("isEditPoint", isEditPoint);
                            intent.putExtra("chosenTime",chosenTime);
                            startActivityForResult(intent, 0);
                        }
                        if (childPosition == 1){
                            Intent intent = new Intent(getApplicationContext(),PresetSelection.class);
                            intent.putExtra("isEditPoint", isEditPoint);
                            intent.putExtra("chosenTime",chosenTime);
                            startActivityForResult(intent,1);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == 0){
                presetName = null;
                presetId = 0;
                customUV = data.getIntExtra("customUV",50);
                customRB = data.getIntExtra("customRB",50);
                customBlue = data.getIntExtra("customBlue",50);
                customWhite = data.getIntExtra("customWhite",50);
                customGreen = data.getIntExtra("customGreen",50);
                customRed = data.getIntExtra("customRed",50);
                customBright = data.getIntExtra("customBright",50);

                brightProgress = customBright;

                //save temporarily the converted values
                float convertUV = customUV * 2 / 100;
                float convertRB = customRB * 2 / 100;
                float convertBlue = customBlue * 2 / 100;
                float convertWhite = customWhite * 2 / 100;
                float convertGreen = customGreen * 2 / 100;
                float convertRed = customRed * 2 / 100;
                float convertBright = customBright * 2 / 100;

                //print outs
                /*System.out.println(convertUV);
                System.out.println(convertRB);
                System.out.println(convertBlue);
                System.out.println(convertWhite);
                System.out.println(convertGreen);
                System.out.println(convertRed);
                System.out.println(convertBright);*/

                //set the values
                saveDataPoint.uv = convertUV;
                saveDataPoint.royalBlue1 = convertRB;
                saveDataPoint.royalBlue2 = convertRB;
                saveDataPoint.blue = convertBlue;
                saveDataPoint.white = convertWhite;
                saveDataPoint.green = convertGreen;
                saveDataPoint.red = convertRed;
                saveDataPoint.brightness = convertBright;
                saveDataPoint.presetId = presetId;
                saveDataPoint.presetName = getResources().getString(R.string.custom);

                customSelected = true;
                initPointList();
            }else{
                //get the values that are passed from the Preset Selection Class
                presetName = data.getStringExtra("presetName");
                presetId = data.getIntExtra("presetId",0);

                //set the values to be sent to back end
                saveDataPoint.presetName = presetName;
                saveDataPoint.presetId = presetId; //This has to be plus one due to the logic on the schedule processing (works by subtracting one from the id)

                //Pass the values in the preset to be saved
                saveDataPoint.uv = data.getFloatExtra("presetUV",0);
                saveDataPoint.royalBlue1 = data.getFloatExtra("presetRB",0);
                saveDataPoint.royalBlue2 = data.getFloatExtra("presetRB",0);
                saveDataPoint.blue = data.getFloatExtra("presetBlue",0);
                saveDataPoint.white = data.getFloatExtra("presetWhite",0);
                saveDataPoint.green = data.getFloatExtra("presetGreen",0);
                saveDataPoint.red = data.getFloatExtra("presetRed",0);
                saveDataPoint.brightness = data.getFloatExtra("presetBrightness",0);

                brightProgress = 50;

                //set customSelected  = false;
                customSelected = false;
                initPointList();
            }
        }
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        final TextView viewTitle = (TextView)findViewById(R.id.action_title);
        if (isEditPoint){
            viewTitle.setText(R.string.edit_point);
        }else{
            viewTitle.setText(R.string.new_point);
        }

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
                setResult(RESULT_CANCELED);
                new StopPCControlTask().execute();
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                new AdjustPointTask().execute();
                if (isEditPoint){
                    String pointToast = getResources().getString(R.string.edit_point) + " " + chosenTime + " " + getResources().getString(R.string.saved);
                    Toast.makeText(SchedulePointView.this, pointToast, Toast.LENGTH_SHORT).show();
                }else{
                    String pointToast = getResources().getString(R.string.new_point) + " " + chosenTime + " " + getResources().getString(R.string.saved);
                    Toast.makeText(SchedulePointView.this, pointToast, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class AdjustPointTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            if (isEditPoint){
                connect.updateScheduleDataPoint(saveDataPoint,userScheduleId);
            }else{
                connect.insertScheduleDataPoint(saveDataPoint,userScheduleId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    }

    public class PresetLoad extends AsyncTask<Void,Void,Void>{

        protected Void doInBackground(Void... arg0){
            try{
                //create ConnectManager Object to send color change POST
                ConnectManager connectESL = ConnectManager.getSharedInstance();
                connectESL.sendColorChangeForGroupWithPreset(sp.getInt("groupID",0), preset, saveDataPoint.brightness);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

    }

    public class CustomColorTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {

            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendColorChangeForSliders(sp.getInt("groupID",0), saveDataPoint.uv,
                        saveDataPoint.royalBlue1,
                        saveDataPoint.blue,
                        saveDataPoint.white,
                        saveDataPoint.green,
                        saveDataPoint.red,
                        saveDataPoint.brightness);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class StopPCControlTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.stopPCControl(0);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}
