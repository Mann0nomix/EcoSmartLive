package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.Bundle;


public class TransparentOverlay extends Activity {
    public static TransparentOverlay transparentOverlayActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_overlay);
        transparentOverlayActivity = this;
    }
}
