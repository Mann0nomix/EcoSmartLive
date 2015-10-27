package com.ecotechmarine.ecosmartlive_android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScheduleTemplateMenu extends NavigationUtility {
    Bundle navBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_template_menu);
        navBundle = getIntent().getExtras();
        initActionBar();
        setActionBarDefaults();
        initScheduleView();
        initPager();
    }

    public void initScheduleView(){
        //set up button to go to template set up View
        Button tButton = (Button) findViewById(R.id.choose_template_button);
        final ViewPager schedFlip = (ViewPager) findViewById(R.id.schedulePager);
        tButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScheduleParamsView.class);
                intent.putExtra("groupId",navBundle.getInt("groupId"));
                intent.putExtra("groupName",navBundle.getString("groupName"));
                int schedIndex = schedFlip.getCurrentItem();
                //intent.putExtra("Title",selectedGroup.getName());
                switch(schedIndex){
                    case 0:
                        intent.putExtra("Index",0);
                        break;
                    case 1:
                        intent.putExtra("Index",1);
                        break;
                    case 2:
                        intent.putExtra("Index",2);
                        break;
                    case 3:
                        intent.putExtra("Index",3);
                        break;
                    case 4:
                        intent.putExtra("Index",4);
                        break;
                    default:
                        intent.putExtra("Index",5);
                        break;
                }
                startActivityForResult(intent, 0);
            }
        });

        WebView web1 = (WebView)findViewById(R.id.schedule1);
        String html1 = getString(R.string.high_growth_description);
        web1.loadData(html1,"text/html","utf-8");
        web1.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web1.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        WebView web2 = (WebView)findViewById(R.id.schedule2);
        String html2 = getString(R.string.radiant_color_description);
        web2.loadData(html2,"text/html","utf-8");
        web2.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web2.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        WebView web3 = (WebView)findViewById(R.id.schedule3);
        String html3 = getString(R.string.traditional_reef_description);
        web3.loadData(html3,"text/html","utf-8");
        web3.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web3.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        WebView web4 = (WebView)findViewById(R.id.schedule4);
        String html4 = getString(R.string.fresh_planted_description);
        web4.loadData(html4,"text/html","utf-8");
        web4.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web4.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        WebView web5 = (WebView)findViewById(R.id.schedule5);
        String html5 = getString(R.string.shallow_reef_description);
        web5.loadData(html5,"text/html","utf-8");
        web5.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web5.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        WebView web6 = (WebView)findViewById(R.id.schedule6);
        String html6 = getString(R.string.deep_water_reef_description);
        web6.loadData(html6,"text/html","utf-8");
        web6.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 11){
            web6.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void initPager(){
        SchedulePageAdapter schedAdapter = new SchedulePageAdapter();
        ViewPager schedPager = (ViewPager)findViewById(R.id.schedulePager);
        schedPager.setAdapter(schedAdapter);
        schedPager.setOffscreenPageLimit(6);
        schedPager.setCurrentItem(0);
    }

    public class SchedulePageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            return container.getChildAt(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == (o);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return 6;
        }
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        String groupName = navBundle.getString("groupName");
        viewTitle.setText(groupName);

        //grab the action buttons and save buttons
        TextView saveButton = (TextView)findViewById(R.id.save_display_only);
        TextView cancelButton = (TextView)findViewById(R.id.cancel_display_only);
        ImageView actionButton1 = (ImageView)findViewById(R.id.action_button1);
        ImageView actionButton2 = (ImageView)findViewById(R.id.action_button2);
        ImageView mainMenuButton = (ImageView)findViewById(R.id.main_menu_display_only);
        ImageView groupListButton = (ImageView)findViewById(R.id.group_list_display_only);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            setResult(RESULT_OK);
        }else{
            setResult(RESULT_CANCELED);
        }
    }
}