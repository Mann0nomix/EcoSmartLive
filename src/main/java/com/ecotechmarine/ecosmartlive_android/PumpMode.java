package com.ecotechmarine.ecosmartlive_android;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by emann on 6/18/14.
 */
public class PumpMode {

    //constructor
    public PumpMode(){}

    int pumpModeId;
    int commandId;
    int defaultTimeMinutes;
    int minTimeMinutes;
    int maxTimeMinutes;
    ArrayList<PumpModeParam> params;
    String pumpModeCode;
    String displayName;
    String displayColorHex;
    String syncToPumpName;

    //For presets
    long userId;
    long pumpPresetId;
    String name;
    boolean systemPreset;
    boolean deleteFlag;

    public enum ModeEnum {
        ConstantSpeed(1),
        ShortPulse(2),
        LongPulse(3),
        ReefCrest(4),
        Lagoon(5),
        NutrientTransport(6),
        TidalSwell(7),
        Transition(8),
        Sync(9),
        AntiSync(10),
        ExpandingPulse(11),
        EcoSmartBack(12),
        FeedMode(13);

        private final int modeValue;
        ModeEnum(int modeValue) {
            this.modeValue = modeValue;
        }
        public int getValue() {
            return modeValue;
        }
    }

    public enum ModeParamEnum {
        ConstantSpeedSpeed(1),
        ShortPulseSpeed(2),
        ShortPulseTime(3),
        LongPulseSpeed(4),
        LongPulseTime(5),
        ReefCrestSpeed(6),
        LagoonSpeed(7),
        NutrientTransportSpeed(8),
        TidalSwellSpeed(9),
        TransitionRampType(10),
        TransitionStartSpeed(11),
        TransitionEndSpeed(12),
        SyncSpeed(13),
        SyncSlaveTo(14),
        AntiSyncSpeed(15),
        AntiSyncSlaveTo(16),
        ExpandingPulseSpeed(17),
        ExpandingPulseStartPulseRate(18),
        ExpandingPulseEndPulseRate(19);

        private final int modeValue;
        ModeParamEnum(int modeValue) {
            this.modeValue = modeValue;
        }
        public int getValue() {
            return modeValue;
        }
    }

    public JSONObject convertToJSON(){
        //create JSONArray to store pump mode params in
        JSONArray modeParams = new JSONArray();

        for (int i = 0; i < params.size(); i++) {
            PumpModeParam param = params.get(i);
            JSONObject modeParam = new JSONObject();

            try{
                modeParam.put("pumpModeParamId", param.pumpModeParamId);
                modeParam.put("paramValue", param.paramValue);
                modeParams.put(modeParam);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //create JSONObject (dictionary) to hold key to value map for all data
        JSONObject body = new JSONObject();

        try{
            body.put("pumpModeId", pumpModeId);
            body.put("pumpModeCode", pumpModeCode);
            body.put("params", modeParams);
        }catch(Exception e){
            e.printStackTrace();
        }

        return body;
    }

    public JSONObject convertToJSONForPreset(){
        //create JSONArray to store pump mode params in
        JSONArray modeParams = new JSONArray();

        for (int i = 0; i < params.size(); i++) {
            PumpModeParam param = params.get(i);
            JSONObject modeParam = new JSONObject();

            try{
                modeParam.put("pumpPresetId", param.pumpPresetId);
                modeParam.put("pumpModeParamId", param.pumpModeParamId);
                modeParam.put("paramValue", param.paramValue);
                modeParams.put(modeParam);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //create JSONObject (dictionary) to hold key to value map for all data
        JSONObject body = new JSONObject();

        try{
            body.put("userId", userId);
            body.put("pumpPresetId", pumpPresetId);
            body.put("name", name);
            body.put("pumpModeId", pumpModeId);
            body.put("systemPreset", systemPreset);
            body.put("params", modeParams);
            body.put("deleteFlag", deleteFlag);
        }catch(Exception e){
            e.printStackTrace();
        }

        return body;
    }

}
