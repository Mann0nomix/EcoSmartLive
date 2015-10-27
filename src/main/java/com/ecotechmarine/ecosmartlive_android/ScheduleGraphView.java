package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.OrientationEventListener;


public class ScheduleGraphView extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_point_graph);
        //Listen for the orientation to change and display graph in landscape for schedule view only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        OrientationEventListener orientationChange = new OrientationEventListener(getBaseContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                Configuration config = getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                    finish();
                }
            }
        };
        orientationChange.enable();
    }
}
