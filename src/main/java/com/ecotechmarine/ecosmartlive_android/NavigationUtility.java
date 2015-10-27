package com.ecotechmarine.ecosmartlive_android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by EJ Mann on 9/19/13.
 */

public class NavigationUtility extends ActionBarActivity {

    public static Context context;

    public NavigationUtility(){

    }

    public void initActionBar(){
        //make sure SupportActionBar is used when the API is below 11
        if(Build.VERSION.SDK_INT < 11){
            //set actionbar title
            getSupportActionBar().setDisplayOptions(getSupportActionBar().DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D4F8B")));
        }else{
            //getActionBar().setDisplayOptions(getActionBar().DISPLAY_SHOW_CUSTOM);
            //getActionBar().setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);
            getActionBar().setCustomView(R.layout.custom_actionbar);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D4F8B")));
        }
        //Actionbar Stuff
        ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        loadingProgress.setVisibility(View.INVISIBLE);
    }

}
