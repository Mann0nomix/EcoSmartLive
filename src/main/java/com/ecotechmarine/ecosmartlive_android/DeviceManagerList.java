package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceManagerList extends NavigationUtility{
    ExpandableListView deviceManagerList;
    BaseExpandableListAdapter deviceManagerListAdapter;
    ConnectManager connect;
    ArrayList<Device> allRadionsInGroup;
    ArrayList<Device> allPumpsInGroup;
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manager_list);
        connect = ConnectManager.getSharedInstance();
        allRadionsInGroup = new ArrayList<Device>();
        allPumpsInGroup = new ArrayList<Device>();
        //grab shared preferences stuff to be used for new aquarium wizard
        sp = getSharedPreferences("USER_PREF",0);
        edit = sp.edit();
        initDeviceArrays();
        initActionBar();
        setActionBarDefaults();
        initDeviceManagerList();
        initBottomButtons();

        //Tooltip for users
        Toast.makeText(DeviceManagerList.this, getResources().getString(R.string.tap_device_icon), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refreshes the list every time the page is resumed (Used so that there is no need for a refresh button on this page)
        deviceManagerListAdapter.notifyDataSetChanged();
    }

    public void initDeviceManagerList(){
        deviceManagerList = (ExpandableListView) findViewById(R.id.device_manager_list);
        deviceManagerListAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                int count = 2;
                if (allRadionsInGroup.size() == 0){
                    count--;
                }
                if (allPumpsInGroup.size() == 0){
                    count--;
                }
                return count;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                switch (groupPosition){
                    case 0:
                        return allRadionsInGroup.size();
                    default:
                        return allPumpsInGroup.size();
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
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.device_manager_list_header, null);
                final TextView deviceManagerHeader = (TextView) convertView.findViewById(R.id.header_text);
                switch(groupPosition){
                    case 0:
                        //Handle situation where the group could be all vortechs
                        if (allRadionsInGroup.isEmpty()){
                            deviceManagerHeader.setText(R.string.vortech);
                        }else{
                            deviceManagerHeader.setText(R.string.radions);
                        }
                        break;
                    default:
                        deviceManagerHeader.setText(R.string.vortech);
                        break;
                }
                return convertView;
            }

            @Override
            public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                convertView = getLayoutInflater().inflate(R.layout.device_manager_list_item, null);
                final TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
                final TextView deviceModel = (TextView) convertView.findViewById(R.id.device_model);
                final ImageView deviceIcon = (ImageView) convertView.findViewById(R.id.device_icon);

                deviceIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (groupPosition){
                            case 0:
                                if (allRadionsInGroup.isEmpty()){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connect.sendIdentifyForTarget("D", "sn" + allPumpsInGroup.get(childPosition).getSerialNo());
                                        }
                                    }).start();
                                }else{
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            connect.sendIdentifyForTarget("D", "sn" + allRadionsInGroup.get(childPosition).getSerialNo());
                                        }
                                    }).start();
                                }
                                break;
                            default:
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        connect.sendIdentifyForTarget("D", "sn" + allPumpsInGroup.get(childPosition).getSerialNo());
                                    }
                                }).start();
                                break;
                        }

                    }
                });

                switch (groupPosition){
                    case 0:
                        //Handle scenario where there are only vortechs in the group
                        if (allRadionsInGroup.isEmpty()){
                            deviceName.setText(allPumpsInGroup.get(childPosition).getName());
                            switch (allPumpsInGroup.get(childPosition).getModelNumber()){
                                //Vortech Pump Support
                                case 10:
                                    deviceModel.setText(R.string.MP10wES);
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp10));
                                    break;
                                case 40:
                                    deviceModel.setText(R.string.MP40wES);
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp40));
                                    break;
                                case 60:
                                    deviceModel.setText(R.string.MP60wES);
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp60));
                                    break;
                                default:
                                    break;
                            }
                        }else{
                            deviceName.setText(allRadionsInGroup.get(childPosition).getName());
                            switch (allRadionsInGroup.get(childPosition).getModelNumber()){
                                //cases to handle lights
                                case 30:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30w));
                                    break;
                                case 31:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30wG2));
                                    break;
                                case 32:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30wPro));
                                    break;
                                case 33:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30wG3));
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr30_g3));
                                    break;
                                case 34:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30wG3Pro));
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr30_g3));
                                    break;
                                case 39:
                                    deviceModel.setText(getResources().getString(R.string.RadionXR30wProto));
                                    break;
                                //XR15 Support
                                case 160:
                                    deviceModel.setText(R.string.RadionXR15wG3Pro);
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr15));
                                    break;
                                case 161:
                                    deviceModel.setText(R.string.RadionXR15FW);
                                    deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_radion_xr15));
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    default:
                        deviceName.setText(allPumpsInGroup.get(childPosition).getName());
                        switch (allPumpsInGroup.get(childPosition).getModelNumber()){
                            //Vortech Pump Support
                            case 10:
                                deviceModel.setText(R.string.MP10wES);
                                deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp10));
                                break;
                            case 40:
                                deviceModel.setText(R.string.MP40wES);
                                deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp40));
                                break;
                            case 60:
                                deviceModel.setText(R.string.MP60wES);
                                deviceIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_mp60));
                                break;
                            default:
                                break;
                        }
                        break;
                }
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };
        deviceManagerList.setAdapter(deviceManagerListAdapter);
        deviceManagerList.setDividerHeight(1);
        deviceManagerList.setGroupIndicator(null);
        deviceManagerList.setChildIndicator(null);
        deviceManagerList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        //expand all groups in the list
        for (int i=0; i < deviceManagerListAdapter.getGroupCount(); i++){
            deviceManagerList.expandGroup(i);
        }
        deviceManagerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        deviceManagerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(DeviceManagerList.this, DeviceDetailsList.class);
                switch (groupPosition){
                    case 0:
                        //handle situation where there are only vortechs in the group and save the current device to the singleton
                        if (allRadionsInGroup.isEmpty()){
                            connect.saveCurrentDevice(allPumpsInGroup.get(childPosition));
                            intent.putExtra("isPump",true);
                            startActivity(intent);
                        }else{
                            connect.saveCurrentDevice(allRadionsInGroup.get(childPosition));
                            startActivity(intent);
                        }
                        break;
                    default:
                        connect.saveCurrentDevice(allPumpsInGroup.get(childPosition));
                        intent.putExtra("isPump",true);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        deviceManagerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get the position of the view out of all groups and rows
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    final int childPosition = ExpandableListView.getPackedPositionChild(id);

                    switch (groupPosition){
                        case 0:
                            //Handle siutation where there are only vortechs in the group
                            if (allRadionsInGroup.isEmpty()){
                                //convert the View Position into an index that correlates with the structure of the user preset array
                                AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(DeviceManagerList.this);
                                DeleteAlert.setMessage(R.string.delete_pump_msg);
                                DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + allPumpsInGroup.get(childPosition).getName() + "\"");
                                DeleteAlert.setNegativeButton(R.string.no, null);
                                DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (allPumpsInGroup.get(childPosition) != null) {
                                            Toast.makeText(DeviceManagerList.this, allPumpsInGroup.get(childPosition).getName() + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                            allPumpsInGroup.remove(childPosition);
                                            //backend call to delete from group
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    connect.unassignDevice(allRadionsInGroup.get(childPosition).getSerialNo());
                                                }
                                            }).start();
                                            deviceManagerListAdapter.notifyDataSetChanged();
                                            if (Build.VERSION.SDK_INT < 11) {
                                                //expand all groups in the list
                                                for (int i=0; i < deviceManagerListAdapter.getGroupCount(); i++){
                                                    deviceManagerList.expandGroup(i);
                                                }
                                            }
                                        }
                                    }
                                });
                                DeleteAlert.show();
                            }else{
                                //convert the View Position into an index that correlates with the structure of the user preset array
                                AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(DeviceManagerList.this);
                                DeleteAlert.setMessage(R.string.delete_radion_msg);
                                DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + allRadionsInGroup.get(childPosition).getName() + "\"");
                                DeleteAlert.setNegativeButton(R.string.no, null);
                                DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (allRadionsInGroup.get(childPosition) != null) {
                                            Toast.makeText(DeviceManagerList.this, allRadionsInGroup.get(childPosition).getName() + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                            allRadionsInGroup.remove(childPosition);
                                            //backend call to delete from group
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    connect.unassignDevice(allRadionsInGroup.get(childPosition).getSerialNo());
                                                }
                                            }).start();
                                            if (Build.VERSION.SDK_INT < 11) {
                                                //expand all groups in the list
                                                for (int i=0; i < deviceManagerListAdapter.getGroupCount(); i++){
                                                    deviceManagerList.expandGroup(i);
                                                }
                                            }
                                            deviceManagerListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                                DeleteAlert.show();
                            }
                            break;
                        default:
                            //convert the View Position into an index that correlates with the structure of the user preset array
                            AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(DeviceManagerList.this);
                            DeleteAlert.setMessage(R.string.delete_pump_msg);
                            DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + allPumpsInGroup.get(childPosition).getName() + "\"");
                            DeleteAlert.setNegativeButton(R.string.no, null);
                            DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (allPumpsInGroup.get(childPosition) != null) {
                                        Toast.makeText(DeviceManagerList.this, allPumpsInGroup.get(childPosition).getName() + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                        allPumpsInGroup.remove(childPosition);
                                        //Backend call to delete from group
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                connect.unassignDevice(allRadionsInGroup.get(childPosition).getSerialNo());
                                            }
                                        }).start();
                                        deviceManagerListAdapter.notifyDataSetChanged();
                                        if (Build.VERSION.SDK_INT < 11) {
                                            //expand all groups in the list
                                            for (int i=0; i < deviceManagerListAdapter.getGroupCount(); i++){
                                                deviceManagerList.expandGroup(i);
                                            }
                                        }
                                    }
                                }
                            });
                            DeleteAlert.show();
                            break;
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(connect.currentDeviceGroup.getName());

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        final ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        final ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);

        cancelButton.setText(R.string.back);

        //Hide action buttons
        mainMenuButton.setVisibility(View.INVISIBLE);
        groupListButton.setVisibility(View.INVISIBLE);

        //Make save buttons visible
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Find all pumps and lights and organize them into separate arrays to be used by the list
    public void initDeviceArrays() {
        //Handle lights
        for (Device device : connect.allDevices) {
            if (device.getParentGroupId() == connect.currentDeviceGroup.getGroupId() && device.getDeviceType().equalsIgnoreCase("L")) {
                //must do this since the thread will yet about the variable not being final
                allRadionsInGroup.add(device);
            }

            if (device.getParentGroupId() == connect.currentDeviceGroup.getGroupId() && device.getDeviceType().equalsIgnoreCase("P")) {
                //must do this since the thread will yet about the variable not being final
                //final Device lastMatchedPump = device;
                allPumpsInGroup.add(device);
            }
        }
    }

    public void initBottomButtons(){
        ImageButton groupNavButton = (ImageButton) findViewById(R.id.device_manager_menu_icon);
        groupNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceManagerList.this, DeviceManagerOverlay.class);
                intent.putExtra("selectedGroupId", (connect.currentDeviceGroup.getGroupId()));
                intent.putExtra("selectedGroupName", connect.currentDeviceGroup.getName());
                startActivity(intent);
            }
        });

        ImageButton refreshIcon = (ImageButton) findViewById(R.id.device_manager_refresh_icon);
        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DeviceManagerList.this, getResources().getString(R.string.device_refreshed), Toast.LENGTH_SHORT).show();
                deviceManagerListAdapter.notifyDataSetChanged();
            }
        });

        ImageButton addDeviceButton = (ImageButton) findViewById(R.id.device_manager_add_device);
        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //You need to send out a discover devices message to the web socket prior to calling the overlay
                new DiscoverDevicesTask().execute();
                Intent intent = new Intent(DeviceManagerList.this, AquariumProgramOverlay.class);
                edit.putString("newAquariumEntered", connect.currentDeviceGroup.getName());
                edit.commit();
                startActivity(intent);
            }
        });
    }

    class DiscoverDevicesTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.discoverDevices();
            return null;
        }
    }
}
