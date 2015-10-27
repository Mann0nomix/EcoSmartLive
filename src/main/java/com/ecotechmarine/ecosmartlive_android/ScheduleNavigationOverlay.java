package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ScheduleNavigationOverlay extends Activity {
    Bundle schedBundle;
    int groupId;
    String groupName;
    String scheduleType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_navigation_overlay);
        initButtonNavigation();
        schedBundle = getIntent().getExtras();
        if (schedBundle != null){
            groupId = schedBundle.getInt("groupId");
            groupName = schedBundle.getString("groupName");
            scheduleType = schedBundle.getString("scheduleType");
        }
    }

    public void initButtonNavigation(){
        Button loadTemplate = (Button)findViewById(R.id.template_nav_button);
        loadTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ScheduleTemplateMenu.class);
                Bundle navBundle = new Bundle();
                navBundle.putInt("groupId", groupId);
                navBundle.putString("groupName", groupName);
                intent.putExtras(navBundle);
                startActivityForResult(intent,0);
                //The one performance issue here is that even if the user cancels the temple, the schedule screen will refresh (not a big deal)
            }
        });

        Button programSchedule = (Button) findViewById(R.id.program_nav_button);
        programSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ConnectManager connect = ConnectManager.getSharedInstance();
                            connect.sendProgramGroup(groupId, scheduleType);
                        }
                    }).start();
                }catch(Exception e){
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), ProgramSchedule.class);
                startActivityForResult(intent,0);
                finish();
            }
        });

        Button cancelView = (Button) findViewById(R.id.cancel_nav_button);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            setResult(RESULT_OK);
            //finish after you get the result, just for this class only
            finish();
        }else{
            setResult(RESULT_CANCELED);
            //finish after you get the result, for this class only
            finish();
        }
    }
}