package com.ecotechmarine.ecosmartlive_android;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by EJ Mann on 10/15/13.
 */

public class ConnectManager {

    public static String webSocketHost = "";
    private static ConnectManager instance = null;
    public static final Preset newPreset = new Preset();
    public HashMap<Integer, PumpMode> pumpMap;
    public ArrayList<PumpMode> pumpSystemPresets;
    public ArrayList<PumpMode> pumpUserPresets;
    public PumpMode currentPulse;
    public DeviceGroup currentDeviceGroup;
    public Device currentDevice;
    public ArrayList<Device> allDevices;

    protected ConnectManager(){
        pumpMap = new HashMap<Integer, PumpMode>();
    }

    public static ConnectManager getSharedInstance(){
        if(instance == null){
            instance = new ConnectManager();
        }

        return instance;
    }

    public void sendColorChangeForGroupWithPreset(int groupId, Preset preset, float brightness){
        String url = String.format("http://%s/livedemo/channelchange/pro", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("deviceID",String.valueOf(groupId | 0xF0)));
        if (groupId == 240){
            postData.add(new BasicNameValuePair("target","A"));
        }else{
            postData.add(new BasicNameValuePair("target","G"));
        }
        postData.add(new BasicNameValuePair("uv",String.valueOf(preset.getUv())));
        postData.add(new BasicNameValuePair("rb",String.valueOf(preset.getRoyalBlue())));
        postData.add(new BasicNameValuePair("blue",String.valueOf(preset.getBlue())));
        postData.add(new BasicNameValuePair("coolWhite",String.valueOf(preset.getWhite())));
        postData.add(new BasicNameValuePair("green",String.valueOf(preset.getGreen())));
        postData.add(new BasicNameValuePair("hyperRed",String.valueOf(preset.getRed())));
        postData.add(new BasicNameValuePair("brightness", String.valueOf(brightness)));

        try {
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Color Change: ","Successful");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setNewPreset(float uv, float royal, float blue, float white, float green, float red){
        newPreset.setUv(uv);
        newPreset.setRoyalBlue(royal);
        newPreset.setBlue(blue);
        newPreset.setWhite(white);
        newPreset.setGreen(green);
        newPreset.setRed(red);

        Log.i("UV: ", String.valueOf(newPreset.getBlue()));
    }

    public Preset getNewPreset(){
        return newPreset;
    }

    public void savePreset(Preset preset){
        String url = String.format("http://%s/livedemo/savePreset", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("uv", String.valueOf(preset.getUv())));
        postData.add(new BasicNameValuePair("rb", String.valueOf(preset.getRoyalBlue())));
        postData.add(new BasicNameValuePair("blue", String.valueOf(preset.getBlue())));
        postData.add(new BasicNameValuePair("white", String.valueOf(preset.getWhite())));
        postData.add(new BasicNameValuePair("green", String.valueOf(preset.getGreen())));
        postData.add(new BasicNameValuePair("red", String.valueOf(preset.getRed())));
        postData.add(new BasicNameValuePair("presetName", String.valueOf(preset.getName())));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Preset Save: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Preset Save: ","Failed");
        }
    }

    public void deletePreset(Preset preset){
        String url = String.format("http://%s/livedemo/deletePreset", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("uv",String.valueOf(preset.getUv())));
        postData.add(new BasicNameValuePair("rb",String.valueOf(preset.getRoyalBlue())));
        postData.add(new BasicNameValuePair("blue",String.valueOf(preset.getBlue())));
        postData.add(new BasicNameValuePair("white",String.valueOf(preset.getWhite())));
        postData.add(new BasicNameValuePair("green",String.valueOf(preset.getGreen())));
        postData.add(new BasicNameValuePair("red",String.valueOf(preset.getRed())));
        postData.add(new BasicNameValuePair("presetName", String.valueOf(preset.getName())));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Preset Delete: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Preset Delete: ","Failed");
        }
    }

    public void sendColorChangeForSliders(int groupId, float uv, float royal, float blue, float white, float green, float red, float brightness){
        String url = String.format("http://%s/livedemo/channelchange/pro", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("deviceID",String.valueOf(groupId | 0xF0)));
        if (groupId == 240){
            postData.add(new BasicNameValuePair("target","A"));
        }else{
            postData.add(new BasicNameValuePair("target","G"));
        }
        postData.add(new BasicNameValuePair("uv",String.valueOf(uv)));
        postData.add(new BasicNameValuePair("rb",String.valueOf(royal)));
        postData.add(new BasicNameValuePair("blue",String.valueOf(blue)));
        postData.add(new BasicNameValuePair("coolWhite",String.valueOf(white)));
        postData.add(new BasicNameValuePair("green",String.valueOf(green)));
        postData.add(new BasicNameValuePair("hyperRed",String.valueOf(red)));
        postData.add(new BasicNameValuePair("brightness", String.valueOf(brightness)));

        try {
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Color Change: ","Successful");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void togglePreviewModeForGroup(int groupId, int previewID, boolean start){
        String url = String.format("http://%s/livedemo/togglepreview", ConnectManager.webSocketHost);
        String previewMode = "";
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        //Change the mode based off the ID that gets passed in through the function
        switch (previewID){
            case 0:
                previewMode = "thunderstorms";
                break;
            case 1:
                previewMode = "demo";
                break;
            case 2:
                previewMode = "clouds";
                break;
            case 3:
                previewMode = "disco";
                break;
        }

        //add the data to the array of data to be passed
        postData.add(new BasicNameValuePair("deviceID", String.valueOf(groupId | 0xF0)));
        if (groupId == 240){
            postData.add(new BasicNameValuePair("target","A"));
        }else{
            postData.add(new BasicNameValuePair("target","G"));
        }
        postData.add(new BasicNameValuePair("mode",previewMode));
        postData.add(new BasicNameValuePair("start", String.valueOf(start)));

        //work dat JSON
        try {
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Preview Displayed: ","Successful");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendIdentifyForTarget(String target, String targetID){
        String url = String.format("http://%s/device/identify?target=%s&targetId=%s&bypass=%s", ConnectManager.webSocketHost, target, targetID, targetID.contains("sn"));
        try{
            JSONParser postESL = new JSONParser();
            postESL.getJSONFromUrl(url);
            Log.i("Identified: ",target + " with ID of " + targetID);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void resetToDefaultSchedule(int scheduleID, int groupID, String schedType, float brightness, String startDay, String startNight){
        String url = String.format("http://%s/profileschedule/resetToDefaultSchedule/mobile", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("scheduleId",String.valueOf(scheduleID)));
        postData.add(new BasicNameValuePair("targetId",String.valueOf(groupID | 0xf0)));
        if (groupID == 240){
            postData.add(new BasicNameValuePair("target","A"));
        }else{
            postData.add(new BasicNameValuePair("target","G"));
        }
        postData.add(new BasicNameValuePair("scheduleType", schedType));
        postData.add(new BasicNameValuePair("brightness", String.valueOf(brightness)));
        postData.add(new BasicNameValuePair("startDay", startDay));
        postData.add(new BasicNameValuePair("startNight", startNight));
        postData.add(new BasicNameValuePair("isOffline", "false"));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendProgramGroup(int groupID, String schedType){
        String url = String.format("http://%s/devicelist/programgroup", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("groupId",String.valueOf(groupID | 0xF0)));
        postData.add(new BasicNameValuePair("scheduleType",schedType));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void startSchedulePlaybackForGroup(int groupID){
        String url = String.format("http://%s/schedule/playback/start", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("targetID","240"));
        postData.add(new BasicNameValuePair("groupID",String.valueOf(groupID | 0xF0)));
        postData.add(new BasicNameValuePair("mode","A"));
        postData.add(new BasicNameValuePair("duration",String.valueOf(30)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopPCControl(int timeOut){
        String url = String.format("http://%s/device/exitpcmodex", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("targetId","240"));
        postData.add(new BasicNameValuePair("target","A"));
        postData.add(new BasicNameValuePair("timeout",String.valueOf(timeOut)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ======== Settings View Commands ========

    public String[] loadGroupSettings(int groupId){
        String url = String.format("http://%s/advancedSettings?targetId=%d&target=G", ConnectManager.webSocketHost, (groupId | 0xF0));
        String json = "";
        String jsonResults = "";
        String jsonResultsNoBrackets;
        String[] settingsData = {""};

        try{
            JSONParser jParse = new JSONParser();
            json = jParse.getJSONFromUrl(url);
            JSONObject jsonData = jParse.getJSONObject(json);

            if (jsonData != null){
                jsonResults = jsonData.getString("result");
                jsonResultsNoBrackets = jsonResults.substring(2, jsonResults.length() - 2);
                settingsData = jsonResultsNoBrackets.split("\",\""); 
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return settingsData;
    }

    public void saveOverrideTimer(boolean settingOn, int otHours, int groupId){
        String url = String.format("http://%s/advancedSettings/OT", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("ot",String.valueOf(settingOn)));
        postData.add(new BasicNameValuePair("otHours",String.valueOf(otHours)));
        postData.add(new BasicNameValuePair("target",String.valueOf("G")));
        postData.add(new BasicNameValuePair("targetId",String.valueOf((groupId | 0xF0))));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Setting Saved: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Setting Saved: ","Failed");
        }
    }

    public void saveAcclimateTimer(boolean settingOn, int startMonth, int startDay, int startYear, int acPeriod, float acIntensity, int groupId){
        String url = String.format("http://%s/advancedSettings/AC", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("ac",String.valueOf(settingOn)));
        postData.add(new BasicNameValuePair("acStartMonth",String.valueOf(startMonth)));
        postData.add(new BasicNameValuePair("acStartDay",String.valueOf(startDay)));
        postData.add(new BasicNameValuePair("acStartYear",String.valueOf(startYear)));
        postData.add(new BasicNameValuePair("acPeriod",String.valueOf(acPeriod)));
        postData.add(new BasicNameValuePair("acIntensity",String.valueOf(acIntensity)));
        postData.add(new BasicNameValuePair("target",String.valueOf("G")));
        postData.add(new BasicNameValuePair("targetId",String.valueOf((groupId | 0xF0))));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Setting Saved: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Setting Saved: ","Failed");
        }
    }

    public void saveLunarPhase(boolean settingOn, int groupId){
        String url = String.format("http://%s/advancedSettings/LP", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("lp",String.valueOf(settingOn)));
        postData.add(new BasicNameValuePair("target",String.valueOf("G")));
        postData.add(new BasicNameValuePair("targetId",String.valueOf((groupId | 0xF0))));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Setting Saved: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Setting Saved: ","Failed");
        }
    }

    public void saveEcoSmartParticipation(boolean settingOn, int groupId){
        String url = String.format("http://%s/advancedSettings/EP", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();

        postData.add(new BasicNameValuePair("ep",String.valueOf(settingOn)));
        postData.add(new BasicNameValuePair("target",String.valueOf("G")));
        postData.add(new BasicNameValuePair("targetId", String.valueOf((groupId | 0xF0))));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Setting Saved: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Setting Saved: ","Failed");
        }
    }

    // ======== Schedule Data Point View Commands ========

    public Schedule loadScheduleForGroup(int groupId){
        String url = String.format("http://%s/profileschedule/G/%d/A", ConnectManager.webSocketHost, (groupId | 0xF0));
        String json;
        Schedule schedule = new Schedule();

        try{
            JSONParser jParse = new JSONParser();
            json = jParse.getJSONFromUrl(url);
            JSONObject jsonData = jParse.getJSONObject(json);

            if (jsonData != null){
                String result = jsonData.getString("result");
                JSONObject belowResult = new JSONObject(result);
                schedule.scheduleId = belowResult.getInt("schedule_id");
                schedule.scheduleType = belowResult.getString("schedule_type");

                //Special Natural Mode properties
                if (schedule.scheduleType.equalsIgnoreCase("N")){
                    schedule.paramOverallBrightness = (float)belowResult.getDouble("param_brightness");
                    schedule.paramDepthOffset = belowResult.getInt("param_depth_offset");
                    schedule.paramDepthOffsetPrevious = 0;
                    schedule.paramStormFrequency = belowResult.getInt("param_storm_frequency");
                    schedule.paramCloudFrequency = belowResult.getInt("param_cloud_frequency");
                }

                //Data point Array Breakdown
                JSONArray arrayJSON = belowResult.getJSONArray("data_points");
                for (int i = 0; i < arrayJSON.length(); i++){
                    String dataPointData = arrayJSON.getString(i);
                    JSONObject dataPointKey = new JSONObject(dataPointData);
                    System.out.println(dataPointKey);

                    ScheduleDataPoint dataPoint = new ScheduleDataPoint();
                    dataPoint.scheduleDataPointId = dataPointKey.getLong("user_profile_schedule_data_point_id");
                    dataPoint.startTimeHour = dataPointKey.getInt("start_time_hr");
                    dataPoint.startTimeMinute = dataPointKey.getInt("start_time_min");
                    dataPoint.startTimeSeconds = dataPointKey.getInt("start_time_sec");
                    dataPoint.presetId = dataPointKey.getInt("preset_id");
                    dataPoint.isStartDay = dataPointKey.getBoolean("is_start_day");
                    dataPoint.isStartNight = dataPointKey.getBoolean("is_start_night");
                    dataPoint.isNightMode = dataPointKey.getBoolean("is_night_mode");
                    dataPoint.startTimeMS = dataPointKey.getLong("start_time");
                    dataPoint.royalBlue1 = (float)dataPointKey.getDouble("royal_blue_1");
                    dataPoint.royalBlue2 = (float)dataPointKey.getDouble("royal_blue_2");
                    dataPoint.blue = (float)dataPointKey.getDouble("blue");
                    dataPoint.white = (float)dataPointKey.getDouble("white");
                    dataPoint.green = (float)dataPointKey.getDouble("green");
                    dataPoint.red = (float)dataPointKey.getDouble("red");
                    dataPoint.uv = (float)dataPointKey.getDouble("uv");
                    dataPoint.brightness = (float)dataPointKey.getDouble("brightness");
                    dataPoint.stormFrequency = dataPointKey.getInt("storm_frequency");
                    dataPoint.cloudFrequency = dataPointKey.getInt("cloud_frequency");
                    dataPoint.presetName = "Custom";
                    dataPoint.totalSeconds = (dataPoint.startTimeHour * 3600) + (dataPoint.startTimeMinute * 60) + dataPoint.startTimeSeconds;

                    schedule.dataPoints.add(dataPoint);

                    if (schedule.scheduleType.equalsIgnoreCase("N") && dataPoint.isNightMode){
                        schedule.filteredDataPoints.add(dataPoint);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return schedule;
    }

    public void insertScheduleDataPoint(ScheduleDataPoint dataPoint, int scheduleId){
        //GET that is treated like a POST
        String url = String.format("http://%s/profileschedule/insertDataPoint?uvv=%f&rbv=%f&rb2v=%f&bv=%f&cwv=%f&gv=%f&hrv=%f&brightv=%f&cloudsv=%d&stormsv=%d&timeH=%s&timeM=%s&timeS=%s&presetId=%d&isNightMode=%s&isStartDay=%s&isStartNight=%s&profileId=%d&isOffline=false",
                ConnectManager.webSocketHost,
                dataPoint.uv,
                dataPoint.royalBlue1,
                dataPoint.royalBlue2,
                dataPoint.blue,
                dataPoint.white,
                dataPoint.green,
                dataPoint.red,
                dataPoint.brightness,
                dataPoint.cloudFrequency,
                dataPoint.stormFrequency,
                String.valueOf(dataPoint.startTimeHour),
                String.valueOf(dataPoint.startTimeMinute),
                String.valueOf(dataPoint.startTimeSeconds),
                dataPoint.presetId,
                (dataPoint.isNightMode ? "true" : "false"),
                (dataPoint.isStartDay ? "true" : "false"),
                (dataPoint.isStartNight ? "true" : "false"),
                scheduleId);

        /*System.out.println(dataPoint.uv);
        System.out.println(dataPoint.royalBlue1);
        System.out.println(dataPoint.royalBlue2);
        System.out.println(dataPoint.blue);
        System.out.println(dataPoint.white);
        System.out.println(dataPoint.green);
        System.out.println(dataPoint.red);
        System.out.println(dataPoint.brightness);
        System.out.println(dataPoint.cloudFrequency);
        System.out.println(dataPoint.stormFrequency);
        System.out.println(dataPoint.startTimeHour);
        System.out.println(dataPoint.startTimeMinute);
        System.out.println(dataPoint.startTimeSeconds);
        System.out.println(dataPoint.presetId);
        System.out.println(dataPoint.isNightMode);
        System.out.println(dataPoint.isStartDay);
        System.out.println(dataPoint.isStartNight);
        System.out.println(scheduleId);
        System.out.println(dataPoint.scheduleDataPointId);*/

        JSONParser jParse = new JSONParser();
        jParse.getJSONFromUrl(url);
    }

    public void updateScheduleDataPoint(ScheduleDataPoint dataPoint, int scheduleId){
        //GET that is treated like a POST
        String url = String.format("http://%s/profileschedule/updateDataPoint/mobile?uvv=%f&rbv=%f&rb2v=%f&bv=%f&cwv=%f&gv=%f&hrv=%f&brightv=%f&cloudsv=%d&stormsv=%d&timeH=%s&timeM=%s&timeS=%s&presetId=%d&isNightMode=%s&isStartDay=%s&isStartNight=%s&profileId=%d&id=%d&lastPoint=true&isOffline=false",
                ConnectManager.webSocketHost,
                dataPoint.uv,
                dataPoint.royalBlue1,
                dataPoint.royalBlue2,
                dataPoint.blue,
                dataPoint.white,
                dataPoint.green,
                dataPoint.red,
                dataPoint.brightness,
                dataPoint.cloudFrequency,
                dataPoint.stormFrequency,
                String.valueOf(dataPoint.startTimeHour),
                String.valueOf(dataPoint.startTimeMinute),
                String.valueOf(dataPoint.startTimeSeconds),
                dataPoint.presetId,
                (dataPoint.isNightMode ? "true" : "false"),
                (dataPoint.isStartDay ? "true" : "false"),
                (dataPoint.isStartNight ? "true" : "false"),
                scheduleId,
                dataPoint.scheduleDataPointId);

        /*System.out.println(dataPoint.uv);
        System.out.println(dataPoint.royalBlue1);
        System.out.println(dataPoint.royalBlue2);
        System.out.println(dataPoint.blue);
        System.out.println(dataPoint.white);
        System.out.println(dataPoint.green);
        System.out.println(dataPoint.red);
        System.out.println(dataPoint.brightness);
        System.out.println(dataPoint.cloudFrequency);
        System.out.println(dataPoint.stormFrequency);
        System.out.println(dataPoint.startTimeHour);
        System.out.println(dataPoint.startTimeMinute);
        System.out.println(dataPoint.startTimeSeconds);
        System.out.println(dataPoint.presetId);
        System.out.println(dataPoint.isNightMode);
        System.out.println(dataPoint.isStartDay);
        System.out.println(dataPoint.isStartNight);
        System.out.println(scheduleId);
        System.out.println(dataPoint.scheduleDataPointId);*/

        try{
            JSONParser jParse = new JSONParser();
            jParse.getJSONFromUrl(url);
            //JSONObject jsonData = jParse.getJSONObject(json);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void deleteScheduleDataPoint(ScheduleDataPoint dataPoint){
        String url = String.format("http://%s/profileschedule/deleteDataPoint",ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("id",String.valueOf(dataPoint.scheduleDataPointId)));
        postData.add(new BasicNameValuePair("offline","false"));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Data Point Deleted: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Data Point Deleted: ","Failed");
        }
    }

    public void updateScheduleParams(int groupId, int scheduleId, float brightness, int depthOffset, int cloudFrequency, int stormFrequency){
        String url = String.format("http://%s/profileschedule/params",ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("groupId",String.valueOf(groupId)));
        postData.add(new BasicNameValuePair("scheduleId",String.valueOf(scheduleId)));
        postData.add(new BasicNameValuePair("brightness",String.valueOf(brightness)));
        postData.add(new BasicNameValuePair("depth",String.valueOf(depthOffset)));
        postData.add(new BasicNameValuePair("clouds",String.valueOf(cloudFrequency)));
        postData.add(new BasicNameValuePair("storms",String.valueOf(stormFrequency)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Updated Schedule Params: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Updated Schedule Params ","Failed");
        }
    }

    //========== Aquarium Wizard Commands ==========
    public void discoverDevices(){
        String url = String.format("http://%s/devicelist/discover/combo", ConnectManager.webSocketHost);

        try{
            JSONParser jParse = new JSONParser();
            jParse.getJSONFromUrl(url);
            //JSONObject jsonData = jParse.getJSONObject(json);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void assignAndGroupDevicesForGroupName(String groupName, int groupId, String[] serialNumbers){
        String url = String.format("http://%s/newAquariumWizard/assignAndGroup", ConnectManager.webSocketHost);

        JSONObject jsonObject = new JSONObject();

        try{
            // Build the JSON request
            jsonObject.put("action", "new");
            jsonObject.put("groupId", String.valueOf(groupId));
            jsonObject.put("groupName", groupName);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < serialNumbers.length; i++){
                jsonArray.put(serialNumbers[i]);
            }
            jsonObject.put("serialNumbers", jsonArray);

            // Send the JSON request
            JSONParser postESL = new JSONParser();
            postESL.postJSONObjectToUrl(url, jsonObject, false);

            Log.i("Group Assignment: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Group Assignment: ", "Failed");
        }
    }

    public void processAndSetRT(){
        String url = String.format("http://%s/bridges/processAndSetRT", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("blank",""));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url,postData, false);
            Log.i("Updated Schedule Params: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Updated Schedule Params ","Failed");
        }
    }

    public void getDeviceStatus(int deviceId){
        String url = String.format("http://%s/devicelist/getDeviceStatus?deviceID=%d", ConnectManager.webSocketHost, deviceId);
        String json = "";

        try{
            JSONParser jParse = new JSONParser();
            json = jParse.getJSONFromUrl(url);
            //JSONObject jsonData = jParse.getJSONObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // ========= Legacy Wizard Commands =============

    public void updateRadionFirmware(int deviceId){
        String url = String.format("http://%s/devicelist/updatefirmware/%d", ConnectManager.webSocketHost, deviceId);

        try{
            JSONParser jParse = new JSONParser();
            jParse.getJSONFromUrl(url);
            //JSONObject jsonData = jParse.getJSONObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updatePumpFirmware(int deviceId, int updateType){
        String url = String.format("http://%s/pump/D/%d/updatefirmware?ut=%d", ConnectManager.webSocketHost, deviceId, updateType);

        try{
            JSONParser jParse = new JSONParser();
            jParse.getJSONFromUrl(url);
            //JSONObject jsonData = jParse.getJSONObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setBridgeMode(int mode){
        String url = String.format("http://%s/deviceNotFoundWizard/usbmode", ConnectManager.webSocketHost);

        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("mode",String.valueOf(mode)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("ReefLink Switch to Bridge Mode: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("ReefLink Switch to Bridge Mode: ","Failed");
        }
    }

    // ========= Pump Live Demo & Preset Commands ============

    //Uses ConnectManager as singleton to pass currentPulse to Save Preset View
    public void saveCurrentPulse(PumpMode pumpMode){
        currentPulse = pumpMode;
    }

    public void loadPumpModes(){
        String url = String.format("http://%s/pump/modes", ConnectManager.webSocketHost);
        String json;

        try{
            JSONParser jParse = new JSONParser();
            json = jParse.getJSONFromUrl(url);
            JSONObject jsonData = jParse.getJSONObject(json);

            if (jsonData != null){
                String result = jsonData.getString("result");
                System.out.println("Load Pump Mode Result = " + result);
                JSONArray arrModes = new JSONArray(result);
                if (arrModes.length() > 0){
                    pumpMap = new HashMap<Integer, PumpMode>();
                }
                for (int i = 0; i < arrModes.length(); i++){
                    //Pump Mode
                    PumpMode pumpMode = new PumpMode();
                    pumpMode.displayName = arrModes.getJSONObject(i).getString("displayName");
                    pumpMode.displayColorHex = arrModes.getJSONObject(i).getString("displayColor");
                    pumpMode.pumpModeId = arrModes.getJSONObject(i).getInt("pumpModeId");
                    pumpMode.commandId = arrModes.getJSONObject(i).getInt("commandId");
                    pumpMode.pumpModeCode = arrModes.getJSONObject(i).getString("modeCode");
                    pumpMode.defaultTimeMinutes = arrModes.getJSONObject(i).getInt("defaultTimeMinutes");
                    pumpMode.minTimeMinutes = arrModes.getJSONObject(i).getInt("minTimeMinutes");
                    pumpMode.maxTimeMinutes = arrModes.getJSONObject(i).getInt("maxTimeMinutes");

                    System.out.println("Pump Display Name = " + pumpMode.displayName);
                    System.out.println("Pump Display Hex = " + pumpMode.displayColorHex);
                    System.out.println("Pump Mode Id = " + pumpMode.pumpModeId);
                    System.out.println("Pump Max Minutes = " + pumpMode.maxTimeMinutes);

                    //Pump Mode Params
                    ArrayList<PumpModeParam> params = new ArrayList<PumpModeParam>();
                    JSONArray dataPointParams = arrModes.getJSONObject(i).getJSONArray("params");
                    System.out.println("Data Point Params Array = " + dataPointParams);
                    if (dataPointParams.length() > 0){
                        for (int j = 0; j < dataPointParams.length(); j++){
                            PumpModeParam param = new PumpModeParam();
                            //param.dataPointId = dataPointParams.getJSONObject(j).getLong("dataPointId");
                            //param.paramValue = dataPointParams.getJSONObject(j).getInt("paramValue");
                            param.pumpModeParamId = dataPointParams.getJSONObject(j).getInt("pumpModeParamId");
                            param.paramIndex = dataPointParams.getJSONObject(j).getInt("paramIndex");
                            param.displayName = dataPointParams.getJSONObject(j).getString("displayName");
                            param.paramCode = dataPointParams.getJSONObject(j).getString("paramCode");
                            param.minValue = dataPointParams.getJSONObject(j).getInt("minValue");
                            param.maxValue = dataPointParams.getJSONObject(j).getInt("maxValue");
                            params.add(param);

                            System.out.println("Param Pump Mode ID = " + param.pumpModeParamId);
                            System.out.println("Param Data Point ID = " + param.dataPointId);
                            System.out.println("Param Data Point Display Name = " + param.displayName);
                            System.out.println("Param Code = " + param.paramCode);
                            System.out.println("Param Max Value = " + param.maxValue);
                        }
                        pumpMode.params = params;
                        System.out.println("Pump Mode Params = " + pumpMode.params);
                    }

                    pumpMap.put(pumpMode.pumpModeId, pumpMode);
                    System.out.println("Full Pump Map = " + pumpMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public PumpMode getPumpMode(int pumpModeId){
        PumpMode pumpMode;
        if (pumpMap.isEmpty()){
            loadPumpModes();
        }

        pumpMode = pumpMap.get(pumpModeId);
        return pumpMode;
    }

    public void setToPumpMode(PumpMode pumpMode, String target, int targetId){
        String url = String.format("http://%s/pump/%s/%d/control", ConnectManager.webSocketHost, target, targetId);

        try{
            // Send the JSON request
            JSONParser postESL = new JSONParser();
            postESL.postJSONObjectToUrl(url, pumpMode.convertToJSON(), false);
            Log.i("Set to Pump Mode: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Set to Pump Mode: ", "Failed");
        }
    }

    public void savePumpPreset(PumpMode pumpMode){
        //boolean success = true;

        String url = String.format("http://%s/pump/preset", ConnectManager.webSocketHost);

        try{
            JSONObject pumpJSON = pumpMode.convertToJSONForPreset();

            // Send the JSON request
            JSONParser postESL = new JSONParser();
            postESL.postJSONObjectToUrl(url, pumpJSON, false);
            Log.i("Save Pump Preset: ","Successful");
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Save Pump Preset: ","Failed");
            //success = false;
        }
    }

    public void loadPumpPresets(){
        String url = String.format("http://%s/pump/presets", ConnectManager.webSocketHost);
        String json;

        try{
            JSONParser jParse = new JSONParser();
            json = jParse.getJSONFromUrl(url);
            JSONObject jsonData = jParse.getJSONObject(json);

            if (jsonData != null){
                String result = jsonData.getString("result");
                System.out.println("Load Pump Presets Result = " + result);
                JSONArray presets = new JSONArray(result);

                pumpSystemPresets = new ArrayList<PumpMode>();
                pumpUserPresets = new ArrayList<PumpMode>();

                for (int i = 0; i < presets.length(); i++){
                    //Pump Mode
                    PumpMode pumpMode = new PumpMode();
                    pumpMode.pumpPresetId = presets.getJSONObject(i).getLong("pumpPresetId");
                    pumpMode.name = presets.getJSONObject(i).getString("name");
                    pumpMode.pumpModeId = presets.getJSONObject(i).getInt("pumpModeId");
                    pumpMode.pumpModeCode = presets.getJSONObject(i).getString("pumpModeCode");
                    pumpMode.commandId = presets.getJSONObject(i).getInt("pumpModeCommandId");
                    pumpMode.displayName = presets.getJSONObject(i).getString("pumpModeDisplayName");
                    pumpMode.displayColorHex = presets.getJSONObject(i).getString("pumpModeDisplayColor");

                    System.out.println("Pump Preset Display Name = " + pumpMode.displayName);
                    System.out.println("Pump Preset Display Hex = " + pumpMode.displayColorHex);
                    System.out.println("Pump Preset Id = " + pumpMode.pumpPresetId);
                    System.out.println("Pump Preset Name = " + pumpMode.name);

                    //Pump Mode Params
                    ArrayList<PumpModeParam> params = new ArrayList<PumpModeParam>();
                    JSONArray dataPointParams = presets.getJSONObject(i).getJSONArray("params");
                    System.out.println("Data Point Params Array = " + dataPointParams);
                    if (dataPointParams.length() > 0){
                        for (int j = 0; j < dataPointParams.length(); j++){
                            PumpModeParam param = new PumpModeParam();
                            //param.dataPointId = dataPointParams.getJSONObject(j).getLong("dataPointId");
                            //param.paramValue = dataPointParams.getJSONObject(j).getInt("paramValue");
                            param.pumpModeParamId = dataPointParams.getJSONObject(j).getInt("pumpModeParamId");
                            param.pumpPresetId = dataPointParams.getJSONObject(j).getInt("pumpPresetId");
                            param.paramValue = dataPointParams.getJSONObject(j).getInt("paramValue");
                            params.add(param);

                            System.out.println("Param Preset Pump Mode ID = " + param.pumpModeParamId);
                            System.out.println("Param Preset ID = " + param.pumpPresetId);
                            System.out.println("Param Preset Value = " + param.paramValue);
                        }
                        pumpMode.params = params;
                        System.out.println("Pump Preset Mode Params = " + pumpMode.params);
                    }

                    //Grab System Preset Boolean and use this as a check to save the pump mode to either system Pump Presets or User Pump Presets
                    boolean systemPreset = presets.getJSONObject(i).getBoolean("systemPreset");
                    System.out.println("System Preset Boolean = " + systemPreset);
                    if (systemPreset){
                        pumpMode.systemPreset = true;
                        pumpSystemPresets.add(pumpMode);
                    }else{
                        pumpUserPresets.add(pumpMode);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // ========= Device Manager Commands ============

    //Uses ConnectManager as singleton to pass currentDeviceGroup to Device Manager and Detail Lists
    public void saveCurrentDeviceGroup(DeviceGroup currentDeviceGroup){this.currentDeviceGroup = currentDeviceGroup;}
    public void saveCurrentDevice(Device currentDevice){this.currentDevice = currentDevice;}
    public void saveAllDevices(ArrayList<Device> allDevices){this.allDevices = allDevices;}

    public void renameGroup(int groupId, int deviceId, String groupName){
        String url = String.format("http://%s/device/name", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target",String.valueOf("G" + groupId)));
        postData.add(new BasicNameValuePair("deviceID",String.valueOf(deviceId)));
        postData.add(new BasicNameValuePair("name",String.valueOf(groupName)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Group Renaming: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Group Renaming: ","Failed");
        }
    }

    public void renameDevice(int deviceId, String deviceName){
        String url = String.format("http://%s/device/name", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target",String.valueOf("D")));
        postData.add(new BasicNameValuePair("deviceID",String.valueOf(deviceId)));
        postData.add(new BasicNameValuePair("name",String.valueOf(deviceName)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Device Renaming: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Device Renaming: ","Failed");
        }
    }

    public void setTimeForGroup(int groupId, String time){
        String url = String.format("http://%s/device/time", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target",String.valueOf("D")));
        postData.add(new BasicNameValuePair("deviceID",String.valueOf(groupId)));
        postData.add(new BasicNameValuePair("time", time));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Time Saved: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Time Saved: ","Failed");
        }

        //Make sure that the current device always uses this new time!
        currentDevice.setFormattedTime(time);
    }

    //Call to remove an aquarium
    public void unassignDevice(String serialNumber){
        String url = String.format("http://%s/groupManager/removeGroup", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("serialNoArray", serialNumber));

        System.out.println("List of Serial Numbers with \"-\" : " + serialNumber);

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Device Unassignment: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Device Unassignment: ","Failed");
        }
    }

    public void softResetDevice(int deviceId){
        String url = String.format("http://%s/device/softreset", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target", "D"));
        postData.add(new BasicNameValuePair("targetId", String.valueOf(deviceId)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Soft Reset of Device: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Soft Reset of Device: ","Failed");
        }
    }

    public void factoryResetDevice(int deviceId){
        String url = String.format("http://%s/device/reset", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target", "D"));
        postData.add(new BasicNameValuePair("targetId", String.valueOf(deviceId)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Factory Reset of Device: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Factory Reset of Device: ","Failed");
        }
    }

    public void toggleLocalControl(Boolean enabled, int deviceId){
        String url = String.format("http://%s/device/localcontrol", ConnectManager.webSocketHost);
        ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("target", "D"));
        postData.add(new BasicNameValuePair("targetId", String.valueOf(deviceId)));
        postData.add(new BasicNameValuePair("val", String.valueOf(enabled)));

        try{
            JSONParser postESL = new JSONParser();
            postESL.postJSONToUrl(url, postData, false);
            Log.i("Local Control Toggle: ","Successful");
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Local Control Toggle: ","Failed");
        }
    }
}