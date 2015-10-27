package com.ecotechmarine.ecosmartlive_android;

/**
 * Created by emann on 3/24/14.
 */
public class ScheduleDataPoint {

    long scheduleDataPointId;
    int startTimeHour;
    int startTimeMinute;
    int startTimeSeconds;
    int presetId;
    boolean isStartDay;
    boolean isStartNight;
    boolean isNightMode;
    long startTimeMS;
    float uv;
    float royalBlue1;
    float royalBlue2;
    float blue;
    float white;
    float green;
    float red;
    float brightness;
    int stormFrequency;
    int cloudFrequency;
    String presetName;
    int totalSeconds;
    int relativeIntensity;
    int relativeIntensityRB;
    int relativeIntensityBlue;
    int relativeIntensityWhite;
    int relativeIntensityGreen;
    int relativeIntensityRed;
    int relativeIntensityUV;
    int relativeIntensityAcclimate;

    public ScheduleDataPoint(){
    }
}
