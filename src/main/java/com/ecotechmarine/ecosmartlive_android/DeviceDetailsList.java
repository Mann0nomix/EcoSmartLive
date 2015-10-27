package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DeviceDetailsList extends NavigationUtility implements IESLWebSocketMessageReceiver {
    ExpandableListView deviceDetailList;
    BaseExpandableListAdapter deviceDetailAdapter;
    ConnectManager connect;
    ArrayList<Device> allRadionsInGroup;
    ArrayList<Device> allPumpsInGroup;
    boolean isPump;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details_list);
        connect = ConnectManager.getSharedInstance();
        allRadionsInGroup = new ArrayList<Device>();
        allPumpsInGroup = new ArrayList<Device>();
        if (getIntent() != null){
            isPump = getIntent().getBooleanExtra("isPump", false);
        }
        initActionBar();
        setActionBarDefaults();
        initDeviceArrays();
        initDeviceDetailList();
        initBottomButtons();

        //remove listener for overlay and restore listener to navwheel
        NavigationWheel.wsClient.deleteWebSocketMessageReceiver(NavigationWheel.wheelActivity);
        NavigationWheel.wsClient.addWebSocketMessageReceiver(DeviceDetailsList.this);

        //Tooltip for users
        Toast.makeText(DeviceDetailsList.this, getResources().getString(R.string.tap_hold_rename), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (NavigationWheel.wsClient != null){
            NavigationWheel.wsClient.deleteWebSocketMessageReceiver(DeviceDetailsList.this);
        }
    }

    public void initDeviceDetailList(){
        deviceDetailList = (ExpandableListView) findViewById(R.id.device_details_list);
        deviceDetailAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return 1;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return 15;
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
                deviceManagerHeader.setText(R.string.device_details);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                if (childPosition == 12){
                    convertView = getLayoutInflater().inflate(R.layout.device_details_local_control, null);

                }else{
                    convertView = getLayoutInflater().inflate(R.layout.device_details_list_item, null);
                }
                final TextView deviceCategory = (TextView) convertView.findViewById(R.id.device_category);
                final TextView deviceData = (TextView) convertView.findViewById(R.id.device_data);
                final ToggleButton localControlToggle = (ToggleButton)convertView.findViewById(R.id.local_control_toggle);
                if (childPosition == 12){
                    localControlToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ConnectManager connect = ConnectManager.getSharedInstance();
                                    connect.toggleLocalControl(isChecked, connect.currentDevice.getDeviceId());
                                }
                            }).start();
                        }
                    });
                }
                switch(childPosition){
                    case 0:
                        deviceCategory.setText(R.string.name);
                        deviceData.setText(connect.currentDevice.getName());
                        break;
                    case 1:
                        deviceCategory.setText(R.string.model);
                        deviceData.setText(connect.currentDevice.getModel());
                        break;
                    case 2:
                        deviceCategory.setText(R.string.serial_number);
                        deviceData.setText(connect.currentDevice.getSerialNo());
                        break;
                    case 3:
                        deviceCategory.setText(R.string.last_time);
                        deviceData.setText(connect.currentDevice.getFormattedTime());
                        break;
                    case 4:
                        deviceCategory.setText(R.string.os_rev);
                        deviceData.setText(connect.currentDevice.getOsRev());
                        break;
                    case 5:
                        deviceCategory.setText(R.string.bootloader_rev);
                        deviceData.setText(connect.currentDevice.getBootRev());
                        break;
                    case 6:
                        deviceCategory.setText(R.string.rf_rev);
                        deviceData.setText(connect.currentDevice.getRfRev());
                        break;
                    case 7:
                        deviceCategory.setText(R.string.rf_frequency);
                        deviceData.setText(String.valueOf(connect.currentDevice.getRfFrequency()));
                        break;
                    case 8:
                        deviceCategory.setText(R.string.rf_status);
                        deviceData.setText(connect.currentDevice.getRfStatus());
                        break;
                    case 9:
                        deviceCategory.setText(R.string.power_state);
                        if (connect.currentDevice.getPowerState() != null && !connect.currentDevice.getOperatingMode().equalsIgnoreCase("null")){
                            deviceData.setText(connect.currentDevice.getPowerState());
                        }else{
                            deviceData.setText(R.string.not_available);
                        }
                        break;
                    case 10:
                        deviceCategory.setText(R.string.operating_mode);
                        if (connect.currentDevice.getOperatingMode() != null && !connect.currentDevice.getOperatingMode().equalsIgnoreCase("null")){
                            deviceData.setText(connect.currentDevice.getOperatingMode());
                        }else{
                            deviceData.setText(R.string.os);
                        }
                        break;
                    case 11:
                        deviceCategory.setText(R.string.current_mode);
                        if (connect.currentDevice.getCurrentOperatingMode() != null && !connect.currentDevice.getCurrentOperatingMode().equalsIgnoreCase("null")){
                            deviceData.setText(connect.currentDevice.getCurrentOperatingMode());
                        }else{
                            deviceData.setText(R.string.not_available);
                        }
                        break;
                    case 12:
                        deviceCategory.setText(R.string.local_control);
                        if (connect.currentDevice.getLocalControl() != null){
                            localControlToggle.setChecked(connect.currentDevice.getLocalControl());
                        }
                        break;
                    case 13:
                        deviceCategory.setText(R.string.subnet);
                        deviceData.setText(String.valueOf(connect.currentDevice.getSubnet()));
                        break;
                    case 14:
                        deviceCategory.setText(R.string.error_state);
                        if (connect.currentDevice.getErrorState() != null && !connect.currentDevice.getCurrentOperatingMode().equalsIgnoreCase("null")){
                            deviceData.setText(connect.currentDevice.getErrorState());
                        }else{
                            deviceData.setText(R.string.no_error);
                        }
                        break;
                    default:
                        break;
                }
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                if (childPosition == 0){
                    return true;
                }
                return false;
            }
        };
        deviceDetailList.setAdapter(deviceDetailAdapter);
        deviceDetailList.setDividerHeight(1);
        deviceDetailList.setGroupIndicator(null);
        deviceDetailList.setChildIndicator(null);
        deviceDetailList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        //expand all groups in the list
        for (int i=0; i < deviceDetailAdapter.getGroupCount(); i++){
            deviceDetailList.expandGroup(i);
        }
        deviceDetailList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        deviceDetailList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                //get the position of the view out of all groups and rows
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    final int childPosition = ExpandableListView.getPackedPositionChild(id);

                    switch (childPosition){
                        case 0:
                            //Init a generic EditText to be added as a view to the alert dialog
                            final EditText input = new EditText(DeviceDetailsList.this);
                            AlertDialog.Builder renameAlert = new AlertDialog.Builder(DeviceDetailsList.this);
                            renameAlert.setView(input);
                            renameAlert.setTitle(getResources().getString(R.string.rename) + " " + getResources().getString(R.string.device));
                            renameAlert.setNegativeButton(R.string.cancel, null);
                            renameAlert.setPositiveButton(R.string.save, new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String previousName = connect.currentDevice.getName();
                                    connect.currentDevice.setName(input.getText().toString().replace("&#39;", "\'").replace("&#34;", "\""));
                                    TextView viewTitle = (TextView) findViewById(R.id.action_title);
                                    viewTitle.setText(connect.currentDevice.getName());
                                    TextView deviceNameInRow = (TextView) view.findViewById(R.id.device_data);
                                    deviceNameInRow.setText(connect.currentDevice.getName());
                                    new RenameDeviceTask().execute();
                                    Toast.makeText(DeviceDetailsList.this, previousName + " " + getResources().getString(R.string.renamed) + " " + connect.currentDevice.getName(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            renameAlert.show();
                            break;
                        default:
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
        viewTitle.setText(connect.currentDevice.getName());

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
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

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
        ImageButton menuButton = (ImageButton) findViewById(R.id.device_details_menu_icon);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceDetailsList.this, DeviceDetailsOverlay.class);
                startActivity(intent);
            }
        });

        ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_device_details_icon);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                new GetDeviceStatusTask().execute();
                Toast.makeText(DeviceDetailsList.this, getResources().getString(R.string.refreshing_device), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RenameDeviceTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connect = ConnectManager.getSharedInstance();
            connect.renameDevice(connect.currentDevice.getDeviceId(), connect.currentDevice.getName());
            return null;
        }
    }

    class GetDeviceStatusTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            connect.getDeviceStatus(connect.currentDevice.getDeviceId());
            return null;
        }
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage notification) {
        if (notification.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessage.ESLWebSocketMessageType.GetDeviceStatus.getValue())){
            if (count < 1){
                System.out.println("DEVICE STATUS IS RESPONDING!!!!!!!!!!!");

                JSONObject jsonMsg = null;
                String msg = notification.getMessage().toString();
                //try catch must be used when parsing a json formatted string
                try{
                    jsonMsg = new JSONObject(msg);

                    connect.currentDevice.setName(jsonMsg.getString("name"));
                    connect.currentDevice.setModel(jsonMsg.getString("model"));
                    connect.currentDevice.setSerialNo(jsonMsg.getString("serial_number"));
                    connect.currentDevice.setOsRev(jsonMsg.getString("os_version"));
                    connect.currentDevice.setBootRev(jsonMsg.getString("bootloader_version"));
                    connect.currentDevice.setRfRev(jsonMsg.getString("rf_module_version"));
                    connect.currentDevice.setRfFrequency(jsonMsg.getInt("rf_frequency"));
                    connect.currentDevice.setRfStatus(jsonMsg.getString("rf_status"));
                    connect.currentDevice.setPowerState(jsonMsg.getString("power_state"));
                    connect.currentDevice.setOperatingMode(jsonMsg.getString("operating_mode"));
                    connect.currentDevice.setCurrentOperatingMode(jsonMsg.getString("current_operating_mode"));
                    //value returned by device status is a string in this case
                    if(jsonMsg.getString("local_control").equalsIgnoreCase("Enabled")){
                        connect.currentDevice.setLocalControl(true);
                    }else{
                        connect.currentDevice.setLocalControl(false);
                    }
                    connect.currentDevice.setSubnet(jsonMsg.getInt("subnet_id"));
                    connect.currentDevice.setErrorState(jsonMsg.getString("error_state"));

                    //grab time and format it to be refreshed
                    int hour = Integer.valueOf(jsonMsg.getString("time_current_hour"));
                    int minutes = Integer.valueOf(jsonMsg.getString("time_current_minutes"));
                    int seconds = Integer.valueOf(jsonMsg.getString("time_current_seconds"));

                    //format the time received from the server and set the formattedTime property of device to it
                    SimpleDateFormat formatter;
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        //US Time
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        //Foreign Time
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    Calendar deviceTime = Calendar.getInstance();
                    deviceTime.set(Calendar.HOUR_OF_DAY, hour);
                    deviceTime.set(Calendar.MINUTE, minutes);
                    deviceTime.set(Calendar.SECOND, seconds);

                    String newDeviceTime = formatter.format(deviceTime.getTime());
                    connect.currentDevice.setFormattedTime(newDeviceTime);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceDetailAdapter.notifyDataSetChanged();
                            Toast.makeText(DeviceDetailsList.this, getResources().getString(R.string.device_refreshed), Toast.LENGTH_SHORT).show();
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }
                count++;
            }
        }
    }
}
