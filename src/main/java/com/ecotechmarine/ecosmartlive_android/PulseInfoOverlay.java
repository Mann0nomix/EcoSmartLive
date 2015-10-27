package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PulseInfoOverlay extends Activity {

    int pumpIndex = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pulse_info_overlay);

        //grab any intent values passed into the overlay
        if (getIntent() != null){
            pumpIndex = getIntent().getIntExtra("pumpIndex", 0);
        }

        //grab layouts
        RelativeLayout pulseInfoOverlay = (RelativeLayout) findViewById(R.id.pulse_info_overlay);
        TextView pulseTitle = (TextView) findViewById(R.id.pulse_title);
        ImageView pulseImage = (ImageView) findViewById(R.id.pulse_image);
        TextView pulseInfoText = (TextView) findViewById(R.id.pulse_info_text);

        //Handle proper display based on pumpIndex
        switch (pumpIndex){
            //1 is constant speed so we ignore it
            case 2:
                pulseTitle.setText(R.string.short_pulse);
                pulseImage.setImageResource(R.drawable.icon_graph_2);
                pulseInfoText.setText(R.string.short_pulse_description);
                break;
            case 3:
                pulseTitle.setText(R.string.long_pulse);
                pulseImage.setImageResource(R.drawable.icon_graph_3);
                pulseInfoText.setText(R.string.long_pulse_description);
                break;
            case 4:
                pulseTitle.setText(R.string.reefcrest);
                pulseImage.setImageResource(R.drawable.icon_graph_4);
                pulseInfoText.setText(R.string.reefcrest_description);
                break;
            case 5:
                pulseTitle.setText(R.string.lagoon);
                pulseImage.setImageResource(R.drawable.icon_graph_5);
                pulseInfoText.setText(R.string.lagoon_description);
                break;
            case 6:
                pulseTitle.setText(R.string.nutrient_transport);
                pulseImage.setImageResource(R.drawable.icon_graph_6);
                pulseInfoText.setText(R.string.nutrient_transport_description);
                break;
            case 7:
                pulseTitle.setText(R.string.tidal_swell);
                pulseImage.setImageResource(R.drawable.icon_graph_7);
                pulseInfoText.setText(R.string.tidal_swell_description);
                break;
            default:
                break;
        }

        //If overlay is tapped, finish the overlay view
        pulseInfoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
