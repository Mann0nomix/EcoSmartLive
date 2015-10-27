package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class SettingsOverlay extends Activity {

    Bundle settingsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsData = getIntent().getExtras();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_overlay);
        SettingsTimeTask settingsTask = new SettingsTimeTask();
        settingsTask.execute();
    }

    class SettingsTimeTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            ConnectManager connectESL = ConnectManager.getSharedInstance();
            //setting Index is used to determine what actions should be taken based off of the view switch selected
            //Key: 0 = EP, 1 = OT, 2 = AC, 3 = LP
            switch (settingsData.getInt("settingIndex")){
                case 0:
                    connectESL.saveEcoSmartParticipation(settingsData.getBoolean("settingOn"),settingsData.getInt("groupId"));
                    break;
                case 1:
                    connectESL.saveOverrideTimer(settingsData.getBoolean("settingOn"),settingsData.getInt("otHours"),settingsData.getInt("groupId"));
                    break;
                case 2:
                    connectESL.saveAcclimateTimer(settingsData.getBoolean("settingOn"),settingsData.getInt("acStartMonth"),settingsData.getInt("acStartDay"),settingsData.getInt("acStartYear"),settingsData.getInt("iPeriod"),settingsData.getFloat("acIntensity"),settingsData.getInt("groupId"));
                    break;
                case 3:
                    connectESL.saveLunarPhase(settingsData.getBoolean("settingOn"),settingsData.getInt("groupId"));
                    break;
                default:
                    break;
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
