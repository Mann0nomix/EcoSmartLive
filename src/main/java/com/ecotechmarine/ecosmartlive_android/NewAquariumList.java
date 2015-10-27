package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class NewAquariumList extends NavigationUtility{
    String deviceNameEntered;
    ExpandableListView deviceList;
    BaseExpandableListAdapter deviceAdapter;
    String deviceListString;
    JSONArray jsonDevices;
    ArrayList<Device> deviceArray;
    HashMap<Integer, ArrayList<Device>> allGroups;
    ArrayList<Integer> keyArray;
    Button[] selectAllButtons;
    boolean[] selectAllChosen;
    public static NewAquariumList wizardActivity;
    int selectedGroupPosition = 256; //you will never have a group this high
    String currentSerial;
    boolean onLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_aquarium_list);
        wizardActivity = this;
        allGroups = new HashMap<Integer, ArrayList<Device>>();
        deviceArray = new ArrayList<Device>();
        keyArray = new ArrayList<Integer>();
        if (getIntent() != null){
            deviceListString = getIntent().getStringExtra("deviceListString");
            deviceNameEntered = getIntent().getStringExtra("deviceNameEntered");
            onLaunch = getIntent().getBooleanExtra("onLaunch",false);
        }
        initActionBar();
        initLegacyMenu();
        setActionBarDefaults();
        new LoadDeviceListTask().execute();
    }

    public void initLegacyMenu(){
        TextView missingDevices = (TextView)findViewById(R.id.devices_missing_text);
        missingDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewAquariumList.this, LegacyMenuOverlay.class);
                startActivity(intent);
            }
        });
    }

    public void initDeviceList(){
        deviceList = (ExpandableListView) findViewById(R.id.device_list);
        deviceAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return keyArray.size();
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                //use the key array as your index (handles entry order of groups)
                if (groupPosition == 0){
                    if (keyArray.contains(255)){
                        return allGroups.get(keyArray.get(keyArray.size() - 1)).size();
                    }else{
                        return allGroups.get(keyArray.get(groupPosition)).size();
                    }
                }else{
                    if (keyArray.contains(255)){
                        return allGroups.get(keyArray.get(groupPosition-1)).size();
                    }else{
                        return allGroups.get(keyArray.get(groupPosition)).size();
                    }
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
            public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.new_aquarium_list_header, null);
                final TextView deviceHeaderItem = (TextView) convertView.findViewById(R.id.header_item);
                selectAllButtons[groupPosition] = ((Button) convertView.findViewById(R.id.select_all_button));
                //react off if the select button is still selected or not and set the text appropriately
                if (selectAllChosen[groupPosition]){
                    selectAllButtons[groupPosition].setBackgroundResource(R.drawable.select_all_button_selected);
                    selectAllButtons[groupPosition].setText(R.string.deselect_all);
                }
                //use the key array as your index (handles entry order of groups)
                if (groupPosition == 0){
                    if (keyArray.contains(255)){
                        deviceHeaderItem.setText(allGroups.get(keyArray.get(keyArray.size() - 1)).get(0).getParentGroupName());
                    }else{
                        deviceHeaderItem.setText(allGroups.get(keyArray.get(groupPosition)).get(0).getParentGroupName());
                    }
                }else{
                    if (keyArray.contains(255)){
                        deviceHeaderItem.setText(allGroups.get(keyArray.get(groupPosition - 1)).get(0).getParentGroupName());
                    }else{
                        deviceHeaderItem.setText(allGroups.get(keyArray.get(groupPosition)).get(0).getParentGroupName());
                    }
                }
                selectAllButtons[groupPosition].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //handle switching of state on click of select all button
                        selectedGroupPosition = groupPosition;
                        if (selectAllChosen[groupPosition]){
                            selectAllChosen[groupPosition] = false;
                            selectAllButtons[groupPosition].setBackgroundResource(R.drawable.select_all_button_deselected);
                            selectAllButtons[groupPosition].setText(R.string.select_all);
                        }else{
                            deviceList.expandGroup(groupPosition);
                            selectAllChosen[groupPosition] = true;
                            selectAllButtons[groupPosition].setBackgroundResource(R.drawable.select_all_button_selected);
                            selectAllButtons[groupPosition].setText(R.string.deselect_all);
                        }
                        deviceAdapter.notifyDataSetChanged();
                    }
                });
                //manually handle the closing and opening of group based on if the user clicks on the group name or not
                /*deviceHeaderItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExpanded){
                            deviceList.collapseGroup(groupPosition);
                        }else{
                            deviceList.expandGroup(groupPosition);
                        }
                    }
                });*/
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                convertView = (RelativeLayout) getLayoutInflater().inflate(R.layout.new_aquarium_list_item, null);
                TextView deviceCount = (TextView) convertView.findViewById(R.id.device_count);
                TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
                TextView deviceSerial = (TextView) convertView.findViewById(R.id.device_serial);
                ImageView deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
                final float scale = getResources().getDisplayMetrics().density;
                int padding_10dp = (int) (10 * scale + 0.5f);
                //handle background coloring if the button is selected or not
                int groupIndex;
                if (groupPosition == 0){
                    if (keyArray.contains(255)){
                        groupIndex = keyArray.get(keyArray.size() - 1);
                    }else{
                        groupIndex = keyArray.get(groupPosition);
                    }
                }else{
                    if (keyArray.contains(255)){
                        groupIndex = keyArray.get(groupPosition - 1);
                    }else{
                        groupIndex = keyArray.get(groupPosition);
                    }
                }
                //handle selection for all devices based off of press of select all button
                if (groupPosition == selectedGroupPosition  && selectAllChosen[groupPosition]){
                    convertView.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_selected);
                    convertView.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                    allGroups.get(groupIndex).get(childPosition).isSelected = true;
                }

                if (groupPosition == selectedGroupPosition && !selectAllChosen[groupPosition]){
                    allGroups.get(groupIndex).get(childPosition).isSelected = false;
                    convertView.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_border);
                    convertView.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                }

                //Default Cases when the childviews are loaded
                if (allGroups.get(groupIndex).get(childPosition).isSelected){
                    allGroups.get(groupIndex).get(childPosition).isSelected = true;
                    convertView.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_selected);
                    convertView.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                }else{
                    allGroups.get(groupIndex).get(childPosition).isSelected = false;
                    convertView.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_border);
                    convertView.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                }

                //Handle layout for device details
                deviceCount.setText(allGroups.get(groupIndex).get(childPosition).getName());
                //need switch here for Model Number conversion
                switch (allGroups.get(groupIndex).get(childPosition).getModelNumber()){
                    //cases to handle lights
                    case 30:
                        deviceName.setText(getResources().getString(R.string.RadionXR30w));
                        break;
                    case 31:
                        deviceName.setText(getResources().getString(R.string.RadionXR30wG2));
                        break;
                    case 32:
                        deviceName.setText(getResources().getString(R.string.RadionXR30wPro));
                        break;
                    case 33:
                        deviceName.setText(getResources().getString(R.string.RadionXR30wG3));
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr30_g3));
                        break;
                    case 34:
                        deviceName.setText(getResources().getString(R.string.RadionXR30wG3Pro));
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr30_g3));
                        break;
                    case 39:
                        deviceName.setText(getResources().getString(R.string.RadionXR30wProto));
                        break;
                    //XR15 Support
                    case 160:
                        deviceName.setText(R.string.RadionXR15wG3Pro);
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr15));
                        break;
                    case 161:
                        deviceName.setText(R.string.RadionXR15FW);
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr15));
                        break;
                    //Vortech Pump Support
                    case 10:
                        deviceName.setText(R.string.MP10wES);
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp10));
                        break;
                    case 40:
                        deviceName.setText(R.string.MP40wES);
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp40));
                        break;
                    case 60:
                        deviceName.setText(R.string.MP60wES);
                        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp60));
                        break;
                    default:
                        break;
                }

                deviceSerial.setText(getResources().getString(R.string.serial) + " " + allGroups.get(groupIndex).get(childPosition).getSerialNo());
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };
        //wire up the list to the adapter and set settings
        deviceList.setAdapter(deviceAdapter);
        deviceList.setDividerHeight(0);
        deviceList.setGroupIndicator(null);
        deviceList.setChildIndicator(null);
        deviceList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        deviceList.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        //expand all groups in the list
        for (int i=0; i < deviceAdapter.getGroupCount(); i++){
            deviceList.expandGroup(i);
        }
        deviceList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final float scale = getResources().getDisplayMetrics().density;
                int padding_10dp = (int) (10 * scale + 0.5f);
                int groupIndex;
                if (groupPosition == 0){
                    if (keyArray.contains(255)){
                        groupIndex = keyArray.get(keyArray.size() - 1);
                    }else{
                        groupIndex = keyArray.get(groupPosition);
                    }
                }else{
                    if (keyArray.contains(255)){
                        groupIndex = keyArray.get(groupPosition - 1);
                    }else{
                        groupIndex = keyArray.get(groupPosition);
                    }
                }
                //handle selection and deselection of devices
                if (allGroups.get(groupIndex).get(childPosition).isSelected){
                    allGroups.get(groupIndex).get(childPosition).isSelected = false;
                    v.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_border);
                    v.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                }else{
                    allGroups.get(groupIndex).get(childPosition).isSelected = true;
                    v.findViewById(R.id.device_layout).setBackgroundResource(R.drawable.aquarium_list_selected);
                    v.findViewById(R.id.device_layout).setPadding(padding_10dp,padding_10dp,padding_10dp,padding_10dp);
                    //handle sending of message to server only when the view BECOMES selected
                    currentSerial = allGroups.get(groupIndex).get(childPosition).getSerialNo();
                    new IdentifyLightsTask().execute();
                }
                return true;
            }
        });
        //Always make no device text view invisible on load of list to reset it
        TextView noDeviceFound = (TextView) findViewById(R.id.no_devices_found);
        noDeviceFound.setVisibility(View.INVISIBLE);
        //If no groups exist in the list, then it is empty. Display the no devices found text view
        if (deviceAdapter.getGroupCount() == 0){
            noDeviceFound.setVisibility(View.VISIBLE);
            noDeviceFound.bringToFront();
        }
        //Display Tooltip for new Aquarium Wizard
        Toast.makeText(NewAquariumList.this, getResources().getString(R.string.aquarium_wizard_tip), Toast.LENGTH_LONG).show();
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(deviceNameEntered);

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

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLaunch){
                    if (NavigationWheel.wheelActivity != null){
                        NavigationWheel.wheelActivity.finish();
                    }
                    Intent intent = new Intent(NewAquariumList.this, LoginView.class);
                    startActivity(intent);
                    finish();
                }else{
                    finish();
                }
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int groupIndex;
                int selectedDeviceCount = 0;
                final ArrayList<String> selectedSerials = new ArrayList<String>();
                //Use nested loop to count all the devices that are selected
                for (int i = 0; i < allGroups.keySet().size(); i++) {
                    if (i == 0) {
                        if (keyArray.contains(255)){
                            groupIndex = keyArray.get(keyArray.size() - 1);
                        }else{
                            groupIndex = keyArray.get(i);
                        }

                    } else {
                        if (keyArray.contains(255)){
                            groupIndex = keyArray.get(i-1);
                        }else{
                            groupIndex = keyArray.get(i);
                        }
                    }
                    for (int j = 0; j < allGroups.get(groupIndex).size(); j++){
                        if (allGroups.get(groupIndex).get(j).isSelected){
                            selectedDeviceCount++;
                            String token = String.valueOf(allGroups.get(groupIndex).get(j).getSerialNo());
                            if (allGroups.get(groupIndex).get(j).getDeviceId() > 0){
                                token = token.concat(":");
                                token = token.concat(String.valueOf(allGroups.get(groupIndex).get(j).getDeviceId()));
                            }
                            selectedSerials.add(token);
                        }
                    }
                }
                if (selectedSerials.isEmpty()){
                    AlertDialog.Builder noSelectAlert = new AlertDialog.Builder(NewAquariumList.this);
                    noSelectAlert.setMessage(R.string.must_select_device);
                    noSelectAlert.setTitle(getResources().getString(R.string.no_device_selected));
                    noSelectAlert.setNegativeButton(getResources().getString(R.string.ok),null);
                    noSelectAlert.show();
                }else{
                    final String[] serialArray = selectedSerials.toArray(new String[selectedSerials.size()]);
                    if (selectedSerials.size() > 0){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int maxId = 0;
                                int groupId = 0;
                                //set the group id if the name entered exists, if not, set the group to the max id of all groups
                                for (int i = 0; i < NavigationWheel.wheelActivity.deviceGroups.size(); i++){
                                    //handle calculation for max id
                                    if (i > 0 && NavigationWheel.wheelActivity.deviceGroups.get(i).getGroupId() > maxId){
                                        maxId = NavigationWheel.wheelActivity.deviceGroups.get(i).getGroupId();
                                    }else{
                                        maxId = NavigationWheel.wheelActivity.deviceGroups.get(i).getGroupId();
                                    }
                                    //handle calculation for group Id
                                    if (deviceNameEntered.equalsIgnoreCase(NavigationWheel.wheelActivity.deviceGroups.get(i).getName())){
                                        groupId = NavigationWheel.wheelActivity.deviceGroups.get(i).getGroupId();
                                    }else{
                                        groupId = maxId + 1;
                                    }
                                }
                                //if groupId is still 0, that means we have a group of unassigned devices. Set the groupId to 1 since it will be the first group in the list
                                if (groupId == 0){
                                    groupId = 1;
                                }
                                ConnectManager connect = ConnectManager.getSharedInstance();
                                connect.assignAndGroupDevicesForGroupName(deviceNameEntered, groupId, serialArray);
                                //System.out.println("Max ID is: " + maxId);
                                //System.out.println("Group ID is: " + groupId);
                            }
                        }).start();
                    }
                    //System.out.println("The total of all selected devices = " + selectedDeviceCount);
                    Intent intent = new Intent(NewAquariumList.this, AquariumProgramOverlay.class);
                    intent.putExtra("onAssign", true);
                    startActivityForResult(intent, 0);
                }
            }
        });
    }

    class IdentifyLightsTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.sendIdentifyForTarget("D", "sn" + currentSerial);
            return null;
        }
    }

    class LoadDeviceListTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                JSONObject json = new JSONObject(deviceListString);
                jsonDevices = json.getJSONArray("devices");
                for (int i = 0; i < jsonDevices.length(); i++){
                    Device device = new Device();
                    JSONObject deviceUnparsed = jsonDevices.getJSONObject(i);
                    device.setDeviceId(Integer.valueOf(deviceUnparsed.getString("id")));
                    device.setName(String.valueOf(deviceUnparsed.getString("name")));
                    device.setParentGroupId(Integer.valueOf(deviceUnparsed.getString("parent_group_id")));
                    if (device.getParentGroupId() > 14 || device.getParentGroupId() == 0){
                        device.setParentGroupId(255);
                        device.setParentGroupName(getResources().getString(R.string.new_devices));
                    }else{
                        device.setParentGroupName(deviceUnparsed.getString("parent_group_name"));
                    }
                    device.setSerialNo(deviceUnparsed.getString("serial_number"));
                    String deviceType = deviceUnparsed.getString("device_type");
                    device.setDeviceType(deviceType);
                    device.setModelNumber(Integer.valueOf(deviceUnparsed.getString("model")));
                    device.isSelected = false;
                    deviceArray.add(device);

                    //if the parent group id of current device does not exist as a key in the map, create a new arraylist and add the device to it. Then add the arraylist to the map
                    if (!allGroups.containsKey(device.getParentGroupId())){
                        ArrayList<Device> groupDevices = new ArrayList<Device>();
                        groupDevices.add(device);
                        allGroups.put(device.getParentGroupId(), groupDevices);
                        //make sure 0 is not included in the index
                        if (device.getParentGroupId() != 0){
                            keyArray.add(device.getParentGroupId());
                        }
                    }else{
                        allGroups.get(device.getParentGroupId()).add(device);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Collections.sort(keyArray);
                    selectAllButtons = new Button[allGroups.keySet().size()];
                    selectAllChosen = new boolean[allGroups.keySet().size()];
                    for (int i = 0; i < selectAllChosen.length; i++){
                        selectAllChosen[i] = false;
                    }
                    initDeviceList();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            finish();
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