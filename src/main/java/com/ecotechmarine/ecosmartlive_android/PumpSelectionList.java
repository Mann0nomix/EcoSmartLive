package com.ecotechmarine.ecosmartlive_android;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PumpSelectionList extends NavigationUtility {
    ArrayList<String> allPumpNames;
    ArrayList<Integer> allPumpModelNums;
    ArrayList<Integer> allPumpIds;
    int pumpIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pump_selection_list);
        if (getIntent() != null){
            allPumpNames = getIntent().getStringArrayListExtra("allPumpNames");
            allPumpModelNums = getIntent().getIntegerArrayListExtra("allPumpModelNums");
            allPumpIds = getIntent().getIntegerArrayListExtra("allPumpIds");
            pumpIndex = getIntent().getIntExtra("pumpIndex", 0);
        }
        System.out.println("Pump First Name = " + allPumpNames.get(0));
        System.out.println("Pump First Num = " + allPumpModelNums.get(0));
        System.out.println("Pump First ID = " + allPumpIds.get(0));
        initActionBar();
        setActionBarDefaults();
        initGroupPumpList();
    }

    public void initGroupPumpList(){
        ListView groupPumpList = (ListView) findViewById(R.id.pump_selection_listview);
        groupPumpList.setAdapter(new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return allPumpNames.size() + 1;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                switch (position){
                    case 0:
                        convertView = View.inflate(PumpSelectionList.this, R.layout.pump_selection_header, null);
                        break;
                    default:
                        convertView = View.inflate(PumpSelectionList.this, R.layout.pump_selection_item, null);
                        TextView pumpName = (TextView)convertView.findViewById(R.id.pump_name);
                        pumpName.setText(allPumpNames.get(position - 1));
                        TextView pumpModelNum = (TextView)convertView.findViewById(R.id.pump_model_number);
                        switch (allPumpModelNums.get(position - 1)){
                            //cases to handle pumps
                            case 10:
                                pumpModelNum.setText(R.string.MP10wES);
                                break;
                            case 40:
                                pumpModelNum.setText(R.string.MP40wES);
                                break;
                            case 60:
                                pumpModelNum.setText(R.string.MP60wES);
                                break;
                            default:
                                break;
                        }
                        ImageView identifyIcon = (ImageView)convertView.findViewById(R.id.identify_icon);
                        identifyIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.out.println("Identify Time!");
                                new IdentifyTask(position - 1).execute();
                            }
                        });
                        break;
                }
                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });
        groupPumpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("allPumpsSelected", true);
                    returnIntent.putExtra("pumpIndex", pumpIndex);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }else{
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("allPumpsSelected", false);
                    returnIntent.putExtra("pumpSelectedIndex", position - 1);
                    returnIntent.putExtra("pumpIndex", pumpIndex);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        viewTitle.setText(R.string.pumps);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);

        //Hide action buttons
        mainMenuButton.setVisibility(View.INVISIBLE);
        groupListButton.setVisibility(View.INVISIBLE);

        //Make save buttons visible
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setText(R.string.back);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public class IdentifyTask extends AsyncTask<Void,Void,Void> {
        int targetID;
        public IdentifyTask(int targetID){
            this.targetID = targetID;
        }

        protected Void doInBackground(Void... arg0){
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendIdentifyForTarget("D", String.valueOf(allPumpIds.get(targetID)));
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
