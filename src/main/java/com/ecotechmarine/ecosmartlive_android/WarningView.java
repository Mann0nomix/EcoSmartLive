package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class WarningView extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warning_view);

        Intent intent = getIntent();
        Bundle messages = intent.getExtras();

        //set alpha of warning overlay if API < 11
        if (Build.VERSION.SDK_INT < 11){
            View warningOverlay = findViewById(R.id.warning_overlay);
            AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            warningOverlay.startAnimation(alpha);
        }

        if (messages != null){
            if (messages.getString("msg") != null && messages.getString("msg2") != null){
                String warning = messages.getString("msg");
                String warning2 = messages.getString("msg2");

                TextView warningText = (TextView) findViewById(R.id.warning_text);
                warningText.setText(warning);

                TextView warningText2 = (TextView) findViewById(R.id.warning_text2);
                warningText2.setText(warning2);
            }
        }
        if(TransparentOverlay.transparentOverlayActivity != null){
            TransparentOverlay.transparentOverlayActivity.finish();
        }
        //close overlay activity if it's open
        if (Overlay.overlayActivity != null){
            Overlay.overlayActivity.cancelTimer();
            Overlay.overlayActivity.finish();
        }
        //ensure that the navWheel gets cancelled when things don't go right!
        if (NavigationWheel.wheelActivity != null){
            NavigationWheel.wheelActivity.finish();
        }
        //close login view if it's open
        if (LoginView.loginActivity != null){
            LoginView.loginActivity.finish();
        }

        Button signout = (Button) findViewById(R.id.sign_out_button);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TransparentOverlay.transparentOverlayActivity != null){
                    TransparentOverlay.transparentOverlayActivity.finish();
                }
                //double check to ensure current overlay activity is closed
                if (Overlay.overlayActivity != null){
                    Overlay.overlayActivity.cancelTimer();
                    Overlay.overlayActivity.finish();
                }
                //double check to ensure current navigationwheel gets closed
                if (NavigationWheel.wheelActivity != null){
                    NavigationWheel.wheelActivity.finish();
                }
                //Double check to ensure current login view gets closed
                if (LoginView.loginActivity != null){
                    LoginView.loginActivity.finish();
                }

                Intent intent = new Intent(getApplicationContext(),LoginView.class);
                startActivity(intent);
                //disconnect from websocket if it exists
                try{
                    if(NavigationWheel.wsClient != null){
                        NavigationWheel.wsClient.disconnect();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(TransparentOverlay.transparentOverlayActivity != null){
            TransparentOverlay.transparentOverlayActivity.finish();
        }
        //Make sure everything gets closed in here as a triple check
        if(Overlay.overlayActivity != null){
            Overlay.overlayActivity.finish();
            Overlay.overlayActivity.cancelTimer();
        }
        //ensure that the navWheel gets cancelled when things don't go right!
        if (NavigationWheel.wheelActivity != null){
            NavigationWheel.wheelActivity.finish();
        }
        //ensure login view gets closed
        if (LoginView.loginActivity != null){
            LoginView.loginActivity.finish();
        }

        //disconnect from websocket if it exists
        try{
            if(NavigationWheel.wsClient != null){
                NavigationWheel.wsClient.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onBackPressed();
    }
}
