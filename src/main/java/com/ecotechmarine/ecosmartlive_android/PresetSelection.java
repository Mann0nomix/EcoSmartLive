package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PresetSelection extends NavigationUtility {
    //Set up Global for getting Shared Preferences
    SharedPreferences sp;

    //Arrays used for storing everything specific to groups and presets
    ArrayList<Preset> systemPresets;
    ArrayList<Preset> userPresets;
    ArrayList<String> headerItem;
    ArrayList<ArrayList<Preset>> presetTable;
    BaseExpandableListAdapter exAdapter;
    ExpandableListView presetList;
    Boolean isDirty = false;
    Boolean isEditPoint;
    String chosenTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preset_selection_view);
        presetList = (ExpandableListView) findViewById(R.id.preset_list);
        sp = getSharedPreferences("USER_PREF", 0);
        isEditPoint = getIntent().getBooleanExtra("isEditPoint",false);
        chosenTime = getIntent().getStringExtra("chosenTime");
        new PresetsTask().execute();
        initActionBar();
        setActionBarDefaults();
    }

    //attempt at using AsyncTask
    public class PresetsTask extends AsyncTask<Void,Void,Void> {

        //Async Task to do operation in background queue
        protected Void doInBackground(Void... arg0){

            try {
                Log.i("mydebug", "Load Presets");
                SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
                String wsHost = sp.getString("wsHost", null);
                JSONParser jParser = new JSONParser();
                String json = jParser.getJSONFromUrl(String.format("http://%s/livedemo/loadPreset", wsHost));
                JSONObject jsonData = jParser.getJSONObject(json);

                boolean presetsReceived = false;

                if(jsonData == null){
                    try{
                        String email = sp.getString("userEmail", null);
                        String password = sp.getString("userPass", null);
                        String secureLoginUrl = String.format("http://%s/j_spring_security_check", ConnectManager.webSocketHost);
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("j_username", email));
                        nameValuePairs.add(new BasicNameValuePair("j_password", password));

                        String response = jParser.postJSONToUrl(secureLoginUrl, nameValuePairs, false);
                        if(response != null){
                            json = jParser.getJSONFromUrl(String.format("http://%s/livedemo/loadPreset", wsHost));
                            jsonData = jParser.getJSONObject(json);
                            if(jsonData != null && jsonData.has("result")){
                                presetsReceived = true;
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else if(jsonData != null && jsonData.has("result")){
                    presetsReceived = true;
                }

                if (presetsReceived){
                    systemPresets = new ArrayList<Preset>();
                    userPresets = new ArrayList<Preset>();

                    JSONArray jsonPresets = jsonData.getJSONArray("result");

                    for(int i = 0; i < jsonPresets.length(); i++){
                        JSONObject p = (JSONObject)jsonPresets.get(i);

                        Preset preset = new Preset();
                        preset.setPresetId(p.getInt("presetId"));
                        preset.setName(p.getString("name"));
                        preset.setRoyalBlue((float)p.getDouble("royalBlue1"));
                        preset.setBlue((float)p.getDouble("blue"));
                        preset.setWhite((float)p.getDouble("coolWhite"));
                        preset.setRed((float)p.getDouble("hyperRed"));
                        preset.setGreen((float)p.getDouble("green"));
                        preset.setUv((float)p.getDouble("uv"));

                        Boolean systemPreset = p.getBoolean("systemPreset");
                        if (systemPreset){
                            systemPresets.add(preset);
                        }else{
                            userPresets.add(preset);
                        }
                    }
                }

                if(systemPresets == null || (systemPresets != null && (systemPresets.size() == 0 || systemPresets.isEmpty()))){
                    Intent intent = new Intent(getApplicationContext(), WarningView.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //runs the check login function that alerts user if wrong login info was entered
                            initSystemPresetsView();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (exAdapter != null){
                exAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT < 11){
                    presetList.expandGroup(0);
                    presetList.expandGroup(1);
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar presetLoading = (ProgressBar)findViewById(R.id.preset_loading);
                    presetLoading.setVisibility(View.GONE);
                }
            });

            if(isDirty){
                isDirty = false;
            }
        }
    }

    public void setHeaderData(){
        //initialize ArrayList object
        headerItem = new ArrayList<String>();

        //Add the categories to the header list
        headerItem.add(getResources().getString(R.string.default_presets));
        headerItem.add(getResources().getString(R.string.my_presets));
    }

    public void setRowData(){
        //initialize row of string data and Arraylist holding to hold the other list of strings
        presetTable = new ArrayList<ArrayList<Preset>>();
        presetTable.add(systemPresets);
        presetTable.add(userPresets);
    }

    public class PresetLoad extends AsyncTask<Void,Void,Void>{
        int i;
        int i2;
        boolean b;

        public PresetLoad(int i, int i2,boolean b){
            this.i = i;
            this.i2 = i2;
            this.b = b;
        }

        protected Void doInBackground(Void... arg0){
            try{
                //create ConnectManager Object to send color change POST
                ConnectManager connectESL = ConnectManager.getSharedInstance();
                connectESL.sendColorChangeForGroupWithPreset(sp.getInt("groupID", 0), presetTable.get(i).get(i2), 1.0f);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void initSystemPresetsView(){
        //Link list view object to the ArrayAdapter to create table/list view!
        setHeaderData();
        setRowData();

        presetList.setGroupIndicator(null);
        presetList.setChildIndicator(null);
        presetList.setDividerHeight(1);
        presetList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        presetList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

        exAdapter = new BaseExpandableListAdapter() {

            @Override
            public int getGroupCount() {
                if (userPresets.size() < 1){
                    return 1;
                }
                return headerItem.size();
            }

            @Override
            public int getChildrenCount(int i) {
                return (presetTable.get(i)).size();
            }

            @Override
            public Object getGroup(int i) {
                return headerItem.get(i);
            }

            @Override
            public Object getChild(int i, int i2) {
                return (presetTable.get(i)).get(i2).getName();
            }

            @Override
            public long getGroupId(int i) {
                return i;
            }

            @Override
            public long getChildId(int i, int i2) {
                if (userPresets.size() < 1){
                    return -1L;
                }
                return (presetTable.get(i)).get(i2).getPresetId();
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

                final TextView newView = (TextView)getLayoutInflater().inflate(R.layout.header_item,null);
                //TextView header = (TextView)newView.findViewById(R.id.tv);
                if (newView != null){
                    newView.setText(headerItem.get(i));
                }

                //generate margins (to do this, I had to typecast the view to a relative layout, configure the settings, then recast it to textview

                if (i > 0){
                    if (newView != null){
                        newView.setPadding(0,30,0,25);
                    }
                }

                //keep all groups expanded at all times
                presetList.expandGroup(i);
                return newView;
            }

            @Override
            public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
                //TextView newView = (TextView)getLayoutInflater().inflate(R.layout.child_item,null);
                TextView result;
                result = (TextView) getLayoutInflater().inflate(R.layout.child_item,viewGroup, false);
                //return newView;
                if (result != null){
                    result.setText((presetTable.get(i)).get(i2).getName());
                }
                return result;
            }

            @Override
            public boolean isChildSelectable(int i, int i2) {
                return true;
            }
        };

        //final ArrayAdapter<String> presetAdapter = new SystemAdapter(this,R.layout.list_item, systemPresetsArray);
        presetList.setAdapter(exAdapter);
        presetList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                final int index = expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(i, i2));

                //final boolean itemChecked = expandableListView.isItemChecked(index);
                if (!expandableListView.isItemChecked(index)) {
                    expandableListView.setItemChecked(index, true);
                    expandableListView.setFocusable(false);
                }
                new PresetLoad(i, i2, true).execute();
                //Exit the View at this point
                Intent intent = new Intent();
                intent.putExtra("presetName", presetTable.get(i).get(i2).getName());
                intent.putExtra("presetId", presetTable.get(i).get(i2).getPresetId());
                intent.putExtra("presetUV", presetTable.get(i).get(i2).getUv());
                intent.putExtra("presetRB", presetTable.get(i).get(i2).getRoyalBlue());
                intent.putExtra("presetBlue", presetTable.get(i).get(i2).getBlue());
                intent.putExtra("presetWhite", presetTable.get(i).get(i2).getWhite());
                intent.putExtra("presetGreen",presetTable.get(i).get(i2).getGreen());
                intent.putExtra("presetRed",presetTable.get(i).get(i2).getRed());
                intent.putExtra("presetBrightness",1.0f);
                setResult(RESULT_OK,intent);
                finish();
                exAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT < 11){
                    presetList.expandGroup(0);
                    presetList.expandGroup(1);
                }
                return true;
            }
        });
        presetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get the position of the view out of all groups and rows
                final int pos = parent.getPositionForView(view);
                if (pos > 15) {
                    //convert the View Position into an index that correlates with the structure of the user preset array
                    final int upIndex = pos - 16;
                    AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(PresetSelection.this);
                    DeleteAlert.setMessage(R.string.delete_preset_msg);
                    DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + presetTable.get(1).get(upIndex).getName() + "\"");
                    DeleteAlert.setNegativeButton(R.string.no, null);
                    DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (presetTable.get(1) != null && userPresets != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ConnectManager connect = ConnectManager.getSharedInstance();
                                            connect.deletePreset(presetTable.get(1).get(upIndex));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                Toast.makeText(PresetSelection.this, presetTable.get(1).get(upIndex).getName() + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                presetTable.get(1).remove(upIndex);
                                exAdapter.notifyDataSetChanged();
                                if (Build.VERSION.SDK_INT < 11){
                                    presetList.expandGroup(0);
                                    presetList.expandGroup(1);
                                }
                                isDirty = true;
                            }
                        }
                    });
                    DeleteAlert.show();
                }
                return true;
            }
        });
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        if (isEditPoint){
            viewTitle.setText(R.string.edit_point);
        }else{
            viewTitle.setText(R.string.new_point);
        }
        viewTitle.append(" " + chosenTime);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);

        //Hide action buttons
        mainMenuButton.setVisibility(View.INVISIBLE);
        groupListButton.setVisibility(View.INVISIBLE);

        //Make save buttons visible
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
