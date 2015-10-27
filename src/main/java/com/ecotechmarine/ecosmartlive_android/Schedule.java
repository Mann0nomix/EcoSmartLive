package com.ecotechmarine.ecosmartlive_android;

import java.util.ArrayList;

/**
 * Created by emann on 3/24/14.
 */
public class Schedule {
    String scheduleType;
    int scheduleId;
    ArrayList<ScheduleDataPoint> dataPoints;
    ArrayList<ScheduleDataPoint> filteredDataPoints;
    float paramOverallBrightness;
    int paramDepthOffset;
    int paramDepthOffsetPrevious = 0;
    int paramStormFrequency;
    int paramCloudFrequency;

    public Schedule(){
        dataPoints = new ArrayList<ScheduleDataPoint>();
        filteredDataPoints = new ArrayList<ScheduleDataPoint>();
    }

}