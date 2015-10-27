package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class LegacyMenuOverlay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legacy_menu_overlay);
        initButtonNavigation();
    }

    public void initButtonNavigation(){
        Button radionSelection = (Button)findViewById(R.id.radion_nav_button);
        radionSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LegacyMenuOverlay.this, LegacyRadion.class);
                startActivity(intent);
                finish();
            }
        });

        Button vortechSelection = (Button) findViewById(R.id.vortech_nav_button);
        vortechSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LegacyMenuOverlay.this, LegacyVortech.class);
                startActivity(intent);
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
}
