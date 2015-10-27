package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.Bundle;


public class RefreshOverlay extends Activity {
    public static RefreshOverlay refreshActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_overlay);
        refreshActivity = this;
    }

    @Override
    public void onBackPressed() {}
}
