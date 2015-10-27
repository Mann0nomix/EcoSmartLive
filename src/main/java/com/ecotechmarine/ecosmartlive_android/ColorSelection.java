package com.ecotechmarine.ecosmartlive_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;


public class ColorSelection extends NavigationUtility {

    //Set up Global for getting Shared Preferences
    SharedPreferences sp;
    SharedPreferences.Editor edit;

    //Layout specific Variables
    RelativeLayout currentMainLayout;
    ViewFlipper colorFlip;
    TextView circleDetail;
    TextView label5k;
    TextView label20k;
    Boolean isDirty = false;

    //This object is used for storing the most current selected group by the user
    String selectedGroupName;

    //ListView related integers responsible for handling Preview and Preset Modes
    int currentChild = 0;
    Boolean dropcamOn = false;
    CountDownTimer updateColorTimer;

    //Layout specific variables for Kelvin and RGB Wheel
    ImageView kelvinCircle;
    ImageView rgbCircle;
    View barBright;
    View barBright2;
    FrameLayout kelvinHandler;
    FrameLayout rgbHandler;
    myView drawCircle;
    myView drawCircle2;
    Boolean circleIsBig = false;
    int circleCount = 0;
    int rgbCircleCount = 0;
    int rgbButtonCount = 0;
    int brightY;

    //Booleans and variables used for maintaining the state of touch circles
    //use globals to keep metrics and resources consistent
    DisplayMetrics metrics;
    //These items are going to be used to get a more accurate touch on the circles post beta
    int centerX;
    int centerY;
    int centerScreenX;
    int centerScreenY;
    float degrees;
    String previousKelvinText;
    String previousRGBText;
    float previousKelvinDegrees;
    float previousRGBDegrees;
    float previousKelvinX = 0;
    float previousKelvinY = 0;
    float previousRGBX = 0;
    float previousRGBY = 0;
    int previousRGBBrightY;
    float previousKelvinBrightY;
    float brightValue;
    float previousBrightValueRGB;
    float previousBrightValueKelvin;
    float brightRatio;
    float innerRadius;
    float outerRadius;
    float thumbTrackRadius; //old 162
    float physBarStartY;
    boolean isCircleDragging;
    boolean isBarDragging;
    RelativeLayout physicalBar;
    RelativeLayout physicalBar2;

    //Handle dropcam specific globals
    Timer dropTimer;
    int dropcamToggle = 0;

    //intent variables
    int customUV;
    int customRB;
    int customBlue;
    int customWhite;
    int customGreen;
    int customRed;
    int customBright;
    String chosenTime;
    boolean isEditPoint;

    //For saving activity to be finished in Warning View
    public static NavigationWheel wheelActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_selection_view);
        isEditPoint = getIntent().getBooleanExtra("isEditPoint",false);
        chosenTime = getIntent().getStringExtra("chosenTime");
        initActionBar();
        setActionBarDefaults();

        currentChild = 0;

        //set SharedPreferences and edit to be used globally
        sp = getSharedPreferences("USER_PREF",0);
        edit = sp.edit();

        //collect references to all necessary layouts and views
        currentMainLayout = (RelativeLayout) findViewById(R.id.nav_main_layout);
        colorFlip = (ViewFlipper) findViewById(R.id.switcher);
        kelvinCircle = (ImageView) findViewById(R.id.circle_slider_kelvin);
        rgbCircle = (ImageView) findViewById(R.id.circle_slider_rgb);
        kelvinHandler = (FrameLayout)findViewById(R.id.kelvin_handler);
        rgbHandler = (FrameLayout)findViewById(R.id.rgb_handler);
        label5k = (TextView)findViewById(R.id.label5k);
        label20k = (TextView)findViewById(R.id.label20k);

        //intent variables
        customUV = getIntent().getIntExtra("customUV",50);
        customRB = getIntent().getIntExtra("customRB",50);
        customBlue = getIntent().getIntExtra("customBlue",50);
        customWhite = getIntent().getIntExtra("customWhite",50);
        customGreen = getIntent().getIntExtra("customGreen",50);
        customRed = getIntent().getIntExtra("customRed",50);
        customBright = getIntent().getIntExtra("customBright",50);

        //call all set up functions for main content
        initButtonNavigation();
        initSliders();

        //Start on Slider View for this view
        Button sliderButton = (Button) findViewById(R.id.slider_button);
        circleDetail = (TextView) findViewById(R.id.circle_detail);
        sliderButton.performClick();

        //initialize brightness value to 50%
        brightValue = 1;

        LayoutInflater myBar = LayoutInflater.from(this);
        barBright = myBar.inflate(R.layout.bar_bright, null);
        if (barBright != null){
            kelvinHandler.addView(barBright);
            barBright.bringToFront();
        }
        physicalBar = (RelativeLayout)findViewById(R.id.physicalBar);
        if (Build.VERSION.SDK_INT < 11){
            TextView alphaBackground = (TextView) findViewById(R.id.alpha_background);
            alphaBackground.setBackgroundColor(Color.argb(115, 0, 0, 0));
        }
    }

    //custom view that handles drawing of circles
    class myView extends View {

        double thumbX;
        double thumbY;
        Paint paint = new Paint();

        public myView(Context context,double thumbX,double thumbY) {
            super(context);
            this.thumbX = thumbX;
            this.thumbY = thumbY;
        }

        @Override
        public void onDraw(Canvas canvas) {

            float radius = (float)(21.5 * (metrics.densityDpi / 160f));
            if (circleIsBig){
                radius = radius * 2.2f;
            }
            //drawing of circle
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeMiter(10);
            paint.setColor(Color.parseColor("#C8D4E0"));
            paint.setStrokeWidth(8.5f);
            paint.setAntiAlias(true);
            canvas.drawCircle(centerX + (float)thumbX, centerY + (float)thumbY, radius, paint);

            //draw the object a 2nd time to fill the gap in the circle
            paint.setColor(Color.parseColor("#59000000"));
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(0f);
            paint.setStrokeMiter(0);
            paint.setStrokeJoin(Paint.Join.MITER);
            canvas.drawCircle(centerX + (float)thumbX, centerY + (float)thumbY, radius, paint);
        }
    }

    @Override
    public void onStop()
    {
        if (updateColorTimer != null){
            updateColorTimer.cancel();
        }
        if (dropTimer != null){
            dropTimer.cancel();
        }
        Log.i("STOP", "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        if (updateColorTimer != null){
            updateColorTimer.cancel();
        }
        if (dropTimer != null){
            dropTimer.cancel();
        }
        Log.i("STOP", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        //code to get screen size with metrics
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        centerX = currentMainLayout.getWidth() / 2;
        centerY = currentMainLayout.getHeight() / 2;
        centerScreenX = metrics.widthPixels / 2;
        centerScreenY = metrics.heightPixels / 2;

        Log.i("Kelvin Center Point of Screen: ", (centerScreenX) + ", " + (centerScreenY));
        Log.i("Kelvin Center Point of Main Layout: ", (centerX) + ", " + (centerY));

        //This has to be here so that every time you jump views, you reset the reference of circleDetail
        circleDetail = (TextView) findViewById(R.id.circle_detail);

        //definite and initialize all point and radius related variables
        //Center point used for both circles
        final Point circleCenterPoint = new Point();

        //handle the centerpoint for different screen resolutions
        int kelvinHeight=(int)(kelvinCircle.getLayoutParams().height  / (metrics.densityDpi / 160f));
        int kelvinWidth=(int)(kelvinCircle.getLayoutParams().width / (metrics.densityDpi / 160f));
        int radius = kelvinWidth / 2;

        //set center point for all circles
        circleCenterPoint.set(radius, radius);

        //set the value of the radii for the touch and drawing of circles
        innerRadius = (float)kelvinWidth * .278f;   //inner outline of touch circle
        outerRadius = (float)kelvinWidth * .47f;  //outline of touch circle
        thumbTrackRadius = (float)kelvinWidth * .325f;
        //float delta = (outerRadius - innerRadius) / 2;
        //thumbTrackRadius = innerRadius;
        physBarStartY = physicalBar.getTop();

        kelvinCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch(action){
                    case MotionEvent.ACTION_DOWN:

                        // Convert pixels from MouseEvent to dpi
                        float px = event.getX() / (metrics.densityDpi / 160f);
                        float py = event.getY() / (metrics.densityDpi / 160f);

                        Log.i("Kelvin Touch Spot: ", px + ", " + py);

                        int x = (int)px;
                        int y = (int)py;

                        double t = Math.pow((double)(x - circleCenterPoint.x), 2.0f) + Math.pow((double)(y - circleCenterPoint.y), 2.0f);
                        boolean isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0f) && t > Math.pow((double)innerRadius, 2.0f));
                        boolean isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0f));

                        if (isOnColorTrack && !isBarDragging){
                            isCircleDragging = true;
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x, circleCenterPoint.y - y));
                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            //save the previous degrees for the Kelvin wheel AFTER you handle the negative values
                            previousKelvinDegrees = degrees;

                            //create timer on down touch that starts and updates the degrees once every second (AFTER negative values are handled)
                            updateColorTimer = new CountDownTimer(60000,1000){
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    KelvinUpdate(degrees);
                                }

                                @Override
                                public void onFinish() {

                                }
                            }.start();

                            //manage shifting of degrees for drawing of the circle
                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            //draw the circle based off of the density of the different phones
                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            //save the thumb values so that you can set the position of the circle when you return to this view
                            previousKelvinX = (int)thumbX;
                            previousKelvinY = (int)thumbY;

                            //ensure that only 1 circle is drawn throughout the lifetime of the program
                            if (circleCount < 1){
                                drawCircle = new myView(ColorSelection.this, thumbX, thumbY);
                                kelvinHandler.addView(drawCircle);
                                drawCircle.bringToFront();
                                circleCount++;
                            }else{
                                if (drawCircle != null && !isBarDragging){
                                    //change the value of the drawcircle's x and y if the circle has already been drawn
                                    drawCircle.thumbX = thumbX;
                                    drawCircle.thumbY = thumbY;
                                    drawCircle.invalidate();
                                }
                            }
                        }

                        if (isWithinGrayCircle && !isCircleDragging){
                            isBarDragging = true;
                            float newY = event.getY();
                            previousKelvinBrightY = moveBrightBar(newY);
                            new brightChange(brightValue).execute();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        circleIsBig = true;

                        // Convert pixels from MouseEvent to dpi
                        px = event.getX() / (metrics.densityDpi / 160f);
                        py = event.getY() / (metrics.densityDpi / 160f);

                        x = (int)px;
                        y = (int)py;

                        t = Math.pow((x - circleCenterPoint.x), 2.0) + Math.pow((y - circleCenterPoint.y), 2.0);
                        isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0) && t > Math.pow((double)innerRadius, 2.0));
                        isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0));

                        if (isOnColorTrack && !isBarDragging){
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x, circleCenterPoint.y - y));

                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            previousKelvinDegrees = degrees;
                            KelvinTextUpdate(degrees);

                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            //drawCircle = new myView(NavigationWheel.this, thumbX, thumbY);
                            //call this to redraw the circle
                            if (drawCircle != null){
                                drawCircle.thumbX = thumbX;
                                drawCircle.thumbY = thumbY;
                            }
                        }

                        //If you are moving & you are suddenly off the color track, resize the circle
                        if (!isOnColorTrack){
                            circleIsBig = false;
                        }
                        //make sure the circle always redraws
                        if (drawCircle != null && !isBarDragging){
                            drawCircle.invalidate();
                        }

                        if (isWithinGrayCircle && !isCircleDragging){
                            float newY = event.getY();
                            previousKelvinBrightY = moveBrightBar(newY);
                        }

                        break;
                    case MotionEvent.ACTION_UP:

                        circleIsBig = false;

                        if (updateColorTimer != null){
                            updateColorTimer.cancel();
                        }

                        // Convert pixels from MouseEvent to dpi
                        px = event.getX() / (metrics.densityDpi / 160f);
                        py = event.getY() / (metrics.densityDpi / 160f);

                        x = (int)px;
                        y = (int)py;

                        t = Math.pow((x - circleCenterPoint.x), 2.0) + Math.pow((y - circleCenterPoint.y), 2.0);
                        isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0) && t > Math.pow((double)innerRadius, 2.0));
                        isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0));

                        if (isOnColorTrack && !isBarDragging){
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x,circleCenterPoint.y - y));

                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            degrees = previousKelvinDegrees;

                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            if (drawCircle != null){
                                drawCircle.invalidate();
                            }
                            previousKelvinX = (int)thumbX;
                            previousKelvinY = (int)thumbY;

                            KelvinUpdate(degrees);

                        }

                        if (isWithinGrayCircle && !isCircleDragging){
                            new brightChange(brightValue).execute();
                        }

                        //Reset the boolean and make sure drag mode gets turn off
                        isCircleDragging = false;
                        isBarDragging = false;

                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        rgbCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                float px = event.getX() / (metrics.densityDpi / 160f);
                float py = event.getY() / (metrics.densityDpi / 160f);

                switch(action){
                    case MotionEvent.ACTION_DOWN:

                        // Convert pixels from MouseEvent to dpi
                        int x = (int)px;
                        int y = (int)py;

                        double t = Math.pow((x - circleCenterPoint.x), 2.0) + Math.pow((y - circleCenterPoint.y), 2.0);
                        boolean isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0) && t > Math.pow((double)innerRadius, 2.0));
                        boolean isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0));

                        if (isOnColorTrack && !isBarDragging){
                            //start dragging mode of circle on down touch of color track
                            isCircleDragging = true;
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x,circleCenterPoint.y - y));

                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            previousRGBDegrees = degrees;

                            //create timer on down touch that starts and updates the degrees once every second
                            updateColorTimer = new CountDownTimer(60000,1000){
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    RGBUpdate(degrees);
                                }

                                @Override
                                public void onFinish() {
                                }
                            }.start();

                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            if (rgbCircleCount < 1){
                                drawCircle2 = new myView(ColorSelection.this, thumbX,thumbY);
                                rgbHandler.addView(drawCircle2);
                                drawCircle2.bringToFront();
                                rgbCircleCount++;
                            }else{
                                if (drawCircle2 != null){
                                    drawCircle2.thumbX = thumbX;
                                    drawCircle2.thumbY = thumbY;
                                    drawCircle2.invalidate();
                                }
                            }

                            previousRGBX = (int)thumbX;
                            previousRGBY = (int)thumbY;
                        }

                        if (isWithinGrayCircle  &!isCircleDragging){
                            //start bar dragging mode of bar on down touch of gray circle
                            isBarDragging = true;
                            float newY = event.getY();
                            previousRGBBrightY = moveBrightBar2(newY);
                            new brightChange(brightValue).execute();
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:

                        circleIsBig = true;

                        px = event.getX() / (metrics.densityDpi / 160f);
                        py = event.getY() / (metrics.densityDpi / 160f);

                        x = (int)px;
                        y = (int)py;

                        t = Math.pow((x - circleCenterPoint.x), 2.0) + Math.pow((y - circleCenterPoint.y), 2.0);
                        isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0) && t > Math.pow((double)innerRadius, 2.0));
                        isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0));

                        if (isOnColorTrack && !isBarDragging){

                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x,circleCenterPoint.y - y));

                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            previousRGBDegrees = degrees;

                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            RGBTextUpdate(degrees);

                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            if (drawCircle2 != null){
                                drawCircle2.thumbX = thumbX;
                                drawCircle2.thumbY = thumbY;
                            }
                        }

                        if (!isOnColorTrack){
                            circleIsBig = false;
                        }
                        //make sure the circle always redraws
                        if (drawCircle2 != null && !isBarDragging){
                            drawCircle2.invalidate();
                        }

                        if (isWithinGrayCircle && !isCircleDragging){
                            float newY = event.getY();
                            previousRGBBrightY = moveBrightBar2(newY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        circleIsBig = false;

                        if (updateColorTimer != null){
                            updateColorTimer.cancel();
                        }

                        px = event.getX() / (metrics.densityDpi / 160f);
                        py = event.getY() / (metrics.densityDpi / 160f);

                        x = (int)px;
                        y = (int)py;

                        t = Math.pow((x - circleCenterPoint.x), 2.0) + Math.pow((y - circleCenterPoint.y), 2.0);
                        isOnColorTrack = (t < Math.pow((double)outerRadius, 2.0) && t > Math.pow((double)innerRadius, 2.0));
                        isWithinGrayCircle = (t < Math.pow((double)innerRadius, 2.0));

                        if (isOnColorTrack && !isBarDragging){
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x,circleCenterPoint.y - y));

                            if(degrees < 0.0){
                                degrees += 360.0f;
                            }
                            degrees = previousRGBDegrees;

                            float shiftedDegrees = degrees - 90.0f;
                            if(shiftedDegrees < 0.0f){
                                shiftedDegrees = 360.0f + shiftedDegrees;
                            }

                            double thumbX = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.cos(Math.PI * (double)shiftedDegrees/180.0f);
                            double thumbY = ((10 * metrics.density) + (112 * (metrics.density - 1)) + (double)thumbTrackRadius) * Math.sin(Math.PI * (double)shiftedDegrees/180.0f);

                            if (drawCircle2 != null){
                                drawCircle2.invalidate();
                            }

                            previousRGBX = (int)thumbX;
                            previousRGBY = (int)thumbY;

                            RGBUpdate(degrees);
                        }

                        if (isWithinGrayCircle && !isCircleDragging){
                            new brightChange(brightValue).execute();
                        }

                        //Reset boolean to make sure dragging mode is stopped
                        isCircleDragging = false;
                        isBarDragging = false;

                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public int moveBrightBar(final float newY){
        final TextView brightText = (TextView) findViewById(R.id.barText);

        // Convert pixels from MouseEvent to dpi
        brightY = (int)newY - (int)(169 * metrics.density);

        //Maximum numbers should be 50 * density
        float brightMax = 50 * metrics.density;
        float brightMin = -1 * (50 * metrics.density);
        int brightTotal = (int)brightMax * 2;

        //handle the minimum brightY value allowed
        if (brightY <= brightMax + 30 * metrics.density && brightY > brightMax){
            brightY = (int)brightMax;
        }
        //handle the maximum brightY value allowed
        if (brightY >= brightMin + (-1 * (30 * metrics.density)) && brightY < brightMin){
            brightY = (int)brightMax * -1;
        }
        //match to handle the touch that is within bounds of the max and min
        if ((brightY <= brightMax) && (brightY >= brightMin)){
            //Create animation that lasts afterwards moving to brightY
            TranslateAnimation anim = new TranslateAnimation(0, 0, brightY, brightY);
            anim.setFillAfter(true);
            anim.setDuration(0);
            barBright.startAnimation(anim);
            brightRatio = Math.abs(brightY - brightMax) / brightTotal;
            int percent = (int) (brightRatio * 100);
            brightText.setText(percent + "%");
            brightValue = brightRatio * 2;
            previousBrightValueKelvin = brightValue;
        }

        return brightY;
    }

    public int moveBrightBar2(final float newY){
        final TextView brightText = (TextView) findViewById(R.id.barText2);

        // Convert pixels from MouseEvent to dpi
        brightY = (int)newY - (int)(169 * metrics.density);

        //Maximum numbers should be 50 * density
        float brightMax = 50 * metrics.density;
        float brightMin = -1 * (50 * metrics.density);
        int brightTotal = (int)brightMax * 2;

        //handle the minimum brightY value allowed
        if (brightY <= brightMax + 30 * metrics.density && brightY > brightMax){
            brightY = (int)brightMax;
        }
        //handle the maximum brightY value allowed
        if (brightY >= brightMin + (-1 * (30 * metrics.density)) && brightY < brightMin){
            brightY = (int)brightMax * -1;
        }
        //match to handle the touch that is within bounds of the max and min
        if ((brightY <= brightMax) && (brightY >= brightMin)){
            //Create animation that lasts afterwards moving to brightY
            TranslateAnimation anim = new TranslateAnimation(0, 0, brightY, brightY);
            anim.setFillAfter(true);
            anim.setDuration(0);
            barBright2.startAnimation(anim);
            brightRatio = Math.abs(brightY - brightMax) / brightTotal;
            int percent = (int) (brightRatio * 100);
            brightText.setText(percent + "%");
            brightValue = brightRatio * 2;
            previousBrightValueRGB = brightValue;
        }

        return brightY;
    }

    public void KelvinTextUpdate(float degrees){
        if (degrees >= 0 && degrees <= 45){
            circleDetail.setText("5,000");

        }else if (degrees > 45 && degrees <= 50){
            circleDetail.setText("5,500");

        }else if (degrees > 50 && degrees <= 60){
            circleDetail.setText("6,000");

        }else if (degrees > 60 && degrees <= 70){
            circleDetail.setText("6,500");

        }else if (degrees > 70 && degrees <= 80){
            circleDetail.setText("7,000");

        }else if (degrees > 80 && degrees <= 90){
            circleDetail.setText("7,500");

        }else if (degrees > 90 && degrees <= 100){
            circleDetail.setText("8,000");

        }else if (degrees > 100 && degrees <= 110){
            circleDetail.setText("8,500");

        }else if (degrees > 110 && degrees <= 120){
            circleDetail.setText("9,000");

        }else if (degrees > 120 && degrees <= 130){
            circleDetail.setText("9,500");

        }else if (degrees > 130 && degrees <= 140){
            circleDetail.setText("10,000");

        }else if (degrees > 140 && degrees <= 150){
            circleDetail.setText("10,500");

        }else if (degrees > 150 && degrees <= 160){
            circleDetail.setText("11,000");

        }else if (degrees > 160 && degrees <= 170){
            circleDetail.setText("11,500");

        }else if (degrees > 170 && degrees <= 180){
            circleDetail.setText("12,000");

        }else if (degrees > 180 && degrees <= 190){
            circleDetail.setText("12,500");

        }else if (degrees > 190 && degrees <= 200){
            circleDetail.setText("13,000");

        }else if (degrees > 200 && degrees <= 210){
            circleDetail.setText("13,500");

        }else if (degrees > 210 && degrees <= 220){
            circleDetail.setText("14,000");

        }else if (degrees > 220 && degrees <= 230){
            circleDetail.setText("14,500");

        }else if (degrees > 230 && degrees <= 240){
            circleDetail.setText("15,000");

        }else if (degrees > 240 && degrees <= 250){
            circleDetail.setText("15,500");

        }else if (degrees > 250 && degrees <= 260){
            circleDetail.setText("16,000");

        }else if (degrees > 260 && degrees <= 270){
            circleDetail.setText("16,500");

        }else if (degrees > 270 && degrees <= 280){
            circleDetail.setText("17,000");

        }else if (degrees > 280 && degrees <= 290){
            circleDetail.setText("17,500");

        }else if (degrees > 290 && degrees <= 300){
            circleDetail.setText("18,000");

        }else if (degrees > 300 && degrees <= 310){
            circleDetail.setText("18,500");

        }else if (degrees > 310 && degrees <= 315){
            circleDetail.setText("19,000");

        }else if (degrees > 315 && degrees <= 320){
            circleDetail.setText("19,500");

        }else if (degrees > 320 && degrees < 360){
            circleDetail.setText("20,000");

        }
    }

    public void KelvinUpdate(float degrees){

        if (degrees >= 0 && degrees <= 45){

            new kelvinColorChange(2.0, 2.0, 0.35, 0.75, 0.35, 0.35).execute();   //accurate
            circleDetail.setText("5,000");

        }else if (degrees > 45 && degrees <= 50){

            new kelvinColorChange(2.0, 2.0, 0.6, 1.0, 0.6, 0.6).execute();
            circleDetail.setText("5,500");

        }else if (degrees > 50 && degrees <= 60){

            new kelvinColorChange(2.0, 2.0, 0.85, 1.25, 0.85, 0.85).execute();
            circleDetail.setText("6,000");

        }else if (degrees > 60 && degrees <= 70){

            new kelvinColorChange(2.0, 2.0, 1.1, 1.5, 1.1, 1.1).execute();
            circleDetail.setText("6,500");

        }else if (degrees > 70 && degrees <= 80){

            new kelvinColorChange(2.0, 2.0, 1.35, 1.75, 1.35, 1.35).execute(); //accurate
            circleDetail.setText("7,000");

        }else if (degrees > 80 && degrees <= 90){

            new kelvinColorChange(2.0, 2.0, 1.42, 1.79, 1.42, 1.42).execute();
            circleDetail.setText("7,500");

        }else if (degrees > 90 && degrees <= 100){

            new kelvinColorChange(2.0, 2.0, 1.49, 1.83, 1.49, 1.49).execute();
            circleDetail.setText("8,000");

        }else if (degrees > 100 && degrees <= 110){

            new kelvinColorChange(2.0, 2.0, 1.56, 1.87, 1.56, 1.56).execute();
            circleDetail.setText("8,500");

        }else if (degrees > 110 && degrees <= 120){

            new kelvinColorChange(2.0, 2.0, 1.63, 1.91, 1.63, 1.63).execute();
            circleDetail.setText("9,000");

        }else if (degrees > 120 && degrees <= 130){

            new kelvinColorChange(2.0, 2.0, 1.70, 1.95, 1.70, 1.70).execute();
            circleDetail.setText("9,500");

        }else if (degrees > 130 && degrees <= 140){

            new kelvinColorChange(2.0, 2.0, 1.75, 2.0, 1.75, 1.75).execute();    //accurate
            circleDetail.setText("10,000");

        }else if (degrees > 140 && degrees <= 150){

            new kelvinColorChange(2.0, 2.0, 1.81, 2.0, 1.81, 1.81).execute();
            circleDetail.setText("10,500");

        }else if (degrees > 150 && degrees <= 160){

            new kelvinColorChange(2.0, 2.0, 1.87, 2.0, 1.87, 1.87).execute();
            circleDetail.setText("11,000");

        }else if (degrees > 160 && degrees <= 170){

            new kelvinColorChange(2.0, 2.0, 1.93, 2.0, 1.93, 1.93).execute();
            circleDetail.setText("11,500");

        }else if (degrees > 170 && degrees <= 180){

            new kelvinColorChange(2.0, 2.0, 2.0, 2.0, 2.0, 2.0).execute();      //accurate
            circleDetail.setText("12,000");

        }else if (degrees > 180 && degrees <= 190){

            new kelvinColorChange(1.74, 1.74, 2.0, 1.74, 2.0, 2.0).execute();
            circleDetail.setText("12,500");

        }else if (degrees > 190 && degrees <= 200){

            new kelvinColorChange(1.46, 1.46, 2.0, 1.46, 2.0, 2.0).execute();
            circleDetail.setText("13,000");

        }else if (degrees > 200 && degrees <= 210){

            new kelvinColorChange(1.18, 1.18, 2.0, 1.18, 2.0, 2.0).execute();
            circleDetail.setText("13,500");

        }else if (degrees > 210 && degrees <= 220){

            new kelvinColorChange(0.9, 0.9, 2.0, 0.9, 2.0, 2.0).execute();     //accurate except UV
            circleDetail.setText("14,000");

        }else if (degrees > 220 && degrees <= 230){

            new kelvinColorChange(0.85, 0.85, 2.0, 0.85, 2.0, 2.0).execute();
            circleDetail.setText("14,500");

        }else if (degrees > 230 && degrees <= 240){

            new kelvinColorChange(0.8, 0.8, 2.0, 0.8, 2.0, 2.0).execute();
            circleDetail.setText("15,000");

        }else if (degrees > 240 && degrees <= 250){

            new kelvinColorChange(0.75, 0.75, 2.0, 0.75, 2.0, 2.0).execute();
            circleDetail.setText("15,500");

        }else if (degrees > 250 && degrees <= 260){

            new kelvinColorChange(0.7, 0.7, 2.0, 0.7, 2.0, 2.0).execute();
            circleDetail.setText("16,000");

        }else if (degrees > 260 && degrees <= 270){

            new kelvinColorChange(0.65, 0.65, 2.0, 0.65, 2.0, 2.0).execute();
            circleDetail.setText("16,500");

        }else if (degrees > 270 && degrees <= 280){

            new kelvinColorChange(0.6, 0.6, 2.0, 0.6, 2.0, 2.0).execute();
            circleDetail.setText("17,000");

        }else if (degrees > 280 && degrees <= 290){

            new kelvinColorChange(0.55, 0.55, 2.0, 0.55, 2.0, 2.0).execute();
            circleDetail.setText("17,500");

        }else if (degrees > 290 && degrees <= 300){

            new kelvinColorChange(0.5, 0.5, 2.0, 0.5, 2.0, 2.0).execute();    //accurate
            circleDetail.setText("18,000");

        }else if (degrees > 300 && degrees <= 310){

            new kelvinColorChange(0.47, 0.47, 2.0, 0.47, 2.0, 2.0).execute();
            circleDetail.setText("18,500");

        }else if (degrees > 310 && degrees <= 315){

            new kelvinColorChange(0.43, 0.43, 2.0, 0.43, 2.0, 2.0).execute();
            circleDetail.setText("19,000");

        }else if (degrees > 315 && degrees <= 320){

            new kelvinColorChange(0.39, 0.39, 2.0, 0.39, 2.0, 2.0).execute();
            circleDetail.setText("19,500");

        }else if (degrees > 320 && degrees < 360){

            new kelvinColorChange(0.35, 0.35, 2.0, 0.35, 2.0, 2.0).execute();  //accurate
            circleDetail.setText("20,000");

        }

        previousKelvinText = circleDetail.getText().toString();
    }

    public void RGBTextUpdate(float degrees){
        if (degrees >= 0 && degrees <= 20){
            circleDetail.setText(R.string.red);

        }else if (degrees > 20 && degrees <= 30){
            circleDetail.setText(R.string.red);

        }else if (degrees > 30 && degrees <= 40){
            circleDetail.setText(R.string.red);

        }else if (degrees > 40 && degrees <= 52){
            circleDetail.setText(R.string.red);

        }else if (degrees > 52 && degrees <= 65){
            circleDetail.setText(R.string.orange);

        }else if (degrees > 65 && degrees <= 72){
            circleDetail.setText(R.string.orange);

        }else if (degrees > 72 && degrees <= 90){
            circleDetail.setText(R.string.orange);

        }else if (degrees > 90 && degrees <= 102){
            circleDetail.setText(R.string.orange);

        }else if (degrees > 102 && degrees <= 115){
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 115 && degrees <= 127){
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 127 && degrees <= 140){
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 150 && degrees <= 160){
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 160 && degrees <= 170){
            circleDetail.setText(R.string.green);

        }else if (degrees > 170 && degrees <= 180){
            circleDetail.setText(R.string.green);

        }else if (degrees > 180 && degrees <= 190){
            circleDetail.setText(R.string.green);

        }else if (degrees > 190 && degrees <= 200){
            circleDetail.setText(R.string.green);

        }else if (degrees > 200 && degrees <= 210){
            circleDetail.setText(R.string.blue);

        }else if (degrees > 210 && degrees <= 220){
            circleDetail.setText(R.string.blue);

        }else if (degrees > 220 && degrees <= 232){
            circleDetail.setText(R.string.blue);

        }else if (degrees > 232 && degrees <= 245){
            circleDetail.setText(R.string.blue);

        }else if (degrees > 245 && degrees <= 257){
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 257 && degrees <= 270){
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 270 && degrees <= 282){
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 282 && degrees <= 295){
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 295 && degrees <= 307){
            circleDetail.setText(R.string.violet);

        }else if (degrees > 307 && degrees <= 320){
            circleDetail.setText(R.string.violet);

        }else if (degrees > 320 && degrees <= 330){
            circleDetail.setText(R.string.violet);

        }else if (degrees > 330 && degrees <= 340){
            circleDetail.setText(R.string.violet);

        }else if (degrees > 340 && degrees < 360){
            circleDetail.setText(R.string.violet);

        }
    }

    public void RGBUpdate(float degrees){

        if (degrees >= 0 && degrees <= 20){

            new rgbColorChange(240, 0,0,0,0).execute();
            circleDetail.setText(R.string.red);

        }else if (degrees > 20 && degrees <= 30){

            new rgbColorChange(255, 0,0,0,0).execute();
            circleDetail.setText(R.string.red);

        }else if (degrees > 30 && degrees <= 40){

            new rgbColorChange(255, 32,0,0,0).execute();
            circleDetail.setText(R.string.red);

        }else if (degrees > 40 && degrees <= 52){

            new rgbColorChange(255, 96,0,0,0).execute();
            circleDetail.setText(R.string.red);

        }else if (degrees > 52 && degrees <= 65){

            new rgbColorChange(255, 64,0,0,0).execute();
            circleDetail.setText(R.string.orange);

        }else if (degrees > 65 && degrees <= 72){

            new rgbColorChange(255, 128,0,0,0).execute();
            circleDetail.setText(R.string.orange);

        }else if (degrees > 72 && degrees <= 90){

            new rgbColorChange(255, 164,0,0,0).execute();
            circleDetail.setText(R.string.orange);

        }else if (degrees > 90 && degrees <= 102){

            new rgbColorChange(255, 192,0,0,0).execute();
            circleDetail.setText(R.string.orange);

        }else if (degrees > 102 && degrees <= 115){

            new rgbColorChange(255,244,0,0,0).execute();
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 115 && degrees <= 127){

            new rgbColorChange(255, 255,0,0,0).execute();
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 127 && degrees <= 140){

            new rgbColorChange(196, 255,0,0,0).execute();
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 150 && degrees <= 160){

            new rgbColorChange(128, 255,0,0,0).execute();
            circleDetail.setText(R.string.yellow);

        }else if (degrees > 160 && degrees <= 170){

            new rgbColorChange(64, 255,0,0,0).execute();
            circleDetail.setText(R.string.green);

        }else if (degrees > 170 && degrees <= 180){

            new rgbColorChange(0, 255,0,0,0).execute();
            circleDetail.setText(R.string.green);

        }else if (degrees > 180 && degrees <= 190){

            new rgbColorChange(0, 192,0,33,0).execute();
            circleDetail.setText(R.string.green);

        }else if (degrees > 190 && degrees <= 200){

            new rgbColorChange(0, 128,64,0,0).execute();
            circleDetail.setText(R.string.green);

        }else if (degrees > 200 && degrees <= 210){

            new rgbColorChange(0, 64,196,0,0).execute();
            circleDetail.setText(R.string.blue);

        }else if (degrees > 210 && degrees <= 220){

            new rgbColorChange(0, 0,255,0,0).execute();
            circleDetail.setText(R.string.blue);

        }else if (degrees > 220 && degrees <= 232){

            new rgbColorChange(32, 0,255,0,0).execute();
            circleDetail.setText(R.string.blue);

        }else if (degrees > 232 && degrees <= 245){

            new rgbColorChange(64, 0,255,32,0).execute();
            circleDetail.setText(R.string.blue);

        }else if (degrees > 245 && degrees <= 257){

            new rgbColorChange(96, 0,255,64,0).execute();
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 257 && degrees <= 270){

            new rgbColorChange(128, 0,255,128,0).execute();
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 270 && degrees <= 282){

            new rgbColorChange(164, 255,0,64,0).execute();
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 282 && degrees <= 295){

            new rgbColorChange(192, 0,255,32,64).execute();
            circleDetail.setText(R.string.indigo);

        }else if (degrees > 295 && degrees <= 307){

            new rgbColorChange(192, 0,255,0,128).execute();
            circleDetail.setText(R.string.violet);

        }else if (degrees > 307 && degrees <= 320){

            new rgbColorChange(192, 0,255,0,255).execute();
            circleDetail.setText(R.string.violet);

        }else if (degrees > 320 && degrees <= 330){

            new rgbColorChange(192, 0,196,0,64).execute();
            circleDetail.setText(R.string.violet);

        }else if (degrees > 330 && degrees <= 340){

            new rgbColorChange(224, 0,64,0,32).execute();
            circleDetail.setText(R.string.violet);

        }else if (degrees > 340 && degrees < 360){

            new rgbColorChange(224, 0,32,0,0).execute();
            circleDetail.setText(R.string.violet);

        }

        previousRGBText = circleDetail.getText().toString();
    }

    public void initButtonNavigation(){
        //buttons
        final Button kelvinButton = (Button) findViewById(R.id.kelvin_button);
        final Button rgbButton = (Button) findViewById(R.id.rgb_button);
        final Button sliderButton = (Button) findViewById(R.id.slider_button);
        final ImageView presetButtonView = (ImageView)findViewById(R.id.save_preset_button);
        final ImageView backToScheduleButton = (ImageView) findViewById(R.id.back_to_schedule_button);
        final ImageView dropcamBackground = (ImageView)findViewById(R.id.dropcam_background);

        kelvinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorFlip.setDisplayedChild(0);
                //stop the timer if it's possibly running
                if (updateColorTimer != null){
                    updateColorTimer.cancel();
                }
                ImageView amountBubble = (ImageView) findViewById(R.id.color_wheel_bubble);

                //make bubble visible
                amountBubble.setVisibility(View.VISIBLE);
                circleDetail.setVisibility(View.VISIBLE);

                //Make kelvin text labels for 20k and 5k visible
                label5k.setVisibility(View.VISIBLE);
                label20k.setVisibility(View.VISIBLE);

                //default state for when navigating to the blue color wheel
                if (previousKelvinX != 0){

                    if (drawCircle != null){
                        drawCircle.thumbX = previousKelvinX;
                        drawCircle.thumbY = previousKelvinY;
                        drawCircle.invalidate();
                    }

                    circleDetail.setText(previousKelvinText);
                    //brightValue is the global used in all color change tasks. Set it to the previous saved value
                    brightValue = previousBrightValueKelvin;
                    KelvinUpdate(previousKelvinDegrees);
                }else{
                    circleDetail.setText("");
                }
                //change background of buttons
                kelvinButton.setBackgroundResource(R.drawable.nav_button_on);
                rgbButton.setBackgroundResource(R.drawable.nav_button_off);
                sliderButton.setBackgroundResource(R.drawable.nav_button_off);
                currentChild = 0;
            }
        });

        rgbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rgbButtonCount++;
                colorFlip.setDisplayedChild(1);
                if (updateColorTimer != null){
                    updateColorTimer.cancel();
                }
                ImageView amountBubble = (ImageView) findViewById(R.id.color_wheel_bubble);
                //make bubble visible
                amountBubble.setVisibility(View.VISIBLE);
                circleDetail.setVisibility(View.VISIBLE);

                //Handle the switching of the 2nd bar
                if (barBright2 == null){
                    LayoutInflater myBar2 = LayoutInflater.from(ColorSelection.this);
                    barBright2 = myBar2.inflate(R.layout.bar_bright2, null);
                    if (barBright2 != null){
                        rgbHandler.addView(barBright2);
                        barBright2.bringToFront();
                    }
                    physicalBar2 = (RelativeLayout) findViewById(R.id.physicalBar2);
                    physicalBar2.setGravity(Gravity.CENTER);
                }

                //Make kelvin text labels for 20k and 5k disappear
                label5k.setVisibility(View.INVISIBLE);
                label20k.setVisibility(View.INVISIBLE);

                //default state for when navigating to the RGB color wheel
                if (previousRGBX != 0){

                    if (drawCircle2 != null){
                        drawCircle2.thumbX = previousRGBX;
                        drawCircle2.thumbY = previousRGBY;
                        drawCircle2.invalidate();
                    }

                    circleDetail.setText(previousRGBText);
                    //brightValue is the global used in all color change tasks. Set it to the previous saved value
                    brightValue = previousBrightValueRGB;
                    RGBUpdate(previousRGBDegrees);

                }else{
                    circleDetail.setText("");
                }

                //change background of buttons that you are on or off
                kelvinButton.setBackgroundResource(R.drawable.nav_button_off);
                rgbButton.setBackgroundResource(R.drawable.nav_button_on);
                sliderButton.setBackgroundResource(R.drawable.nav_button_off);
                currentChild = 1;
            }
        });

        sliderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorFlip.setDisplayedChild(2);
                //stop updateColorTimer if it's possibly running
                if (updateColorTimer != null){
                    updateColorTimer.cancel();
                }
                ImageView amountBubble = (ImageView) findViewById(R.id.color_wheel_bubble);

                //hide bubble
                amountBubble.setVisibility(View.GONE);
                circleDetail.setVisibility(View.GONE);

                //Make kelvin text labels for 20k and 5k disappear
                label5k.setVisibility(View.INVISIBLE);
                label20k.setVisibility(View.INVISIBLE);

                kelvinButton.setBackgroundResource(R.drawable.nav_button_off);
                rgbButton.setBackgroundResource(R.drawable.nav_button_off);
                sliderButton.setBackgroundResource(R.drawable.nav_button_on);
                currentChild = 2;
                getSliderValues();
            }
        });

        presetButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDirty = true;

                //launch preset save activity
                Intent intent = new Intent(getApplicationContext(), SavePresetView.class);
                startActivity(intent);

                //reset dropcam toggle and timer and change background back to coral image
                dropcamToggle = 0;
                currentMainLayout.setBackgroundResource(R.drawable.new_wheel_bg);
                if (dropTimer != null){
                    dropTimer.cancel();
                }
            }
        });
        presetButtonView.setVisibility(View.GONE);

        backToScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectManager connect = ConnectManager.getSharedInstance();
                        connect.stopPCControl(0);
                    }
                }).start();
                Toast.makeText(getBaseContext(), getResources().getString(R.string.schedule) + " Mode On.", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences sp = getSharedPreferences("USER_PREF",0);
        String dropCamID = sp.getString("dropcamID",null);
        if (Build.VERSION.SDK_INT < 11){
            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            dropcamBackground.startAnimation(alpha);
        }else{
            dropcamBackground.setAlpha(0.5f);
        }

        if (dropCamID != null){
            dropcamBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dropcamToggle++;
                    SharedPreferences sp = getSharedPreferences("USER_PREF",0);
                    String dropCamID = sp.getString("dropcamID",null);
                    if (dropCamID != null){
                        if (dropcamToggle % 2 != 0){
                            dropcamOn = true;
                            final String dropCamIDURL = "https://nexusapi.dropcam.com/get_image?width=1200&uuid=" + dropCamID;
                            dropTimer = new Timer();
                            dropTimer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (dropcamOn) {
                                                    URL url = new URL(dropCamIDURL);
                                                    final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                                                    connection.setDoInput(true);
                                                    connection.connect();
                                                    InputStream input = connection.getInputStream();
                                                    final Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (dropcamOn) {
                                                                Drawable dr = new BitmapDrawable(myBitmap);
                                                                currentMainLayout.setBackgroundDrawable(dr);
                                                            }
                                                        }
                                                    });
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            }, 0, 5000);
                            if (Build.VERSION.SDK_INT < 11){
                                AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                                alpha.setDuration(0); // Make animation instant
                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                dropcamBackground.startAnimation(alpha);
                            }else{
                                dropcamBackground.setAlpha(1f);
                            }
                        }else{
                            dropcamOn = false;
                            if (dropTimer != null){
                                dropTimer.cancel();
                            }
                            currentMainLayout.setBackgroundResource(R.drawable.new_wheel_bg);
                            if (Build.VERSION.SDK_INT < 11){
                                AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                                alpha.setDuration(0); // Make animation instant
                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                dropcamBackground.startAnimation(alpha);
                            }else{
                                dropcamBackground.setAlpha(0.5f);
                            }
                        }
                    }
                }
            });
        }else{
            dropcamBackground.setVisibility(View.INVISIBLE);
        }
    }

    public void initSliders(){
        final ImageButton upUV = (ImageButton) findViewById(R.id.uv_left_up_button);
        final ImageButton downUV = (ImageButton) findViewById(R.id.uv_left_down_button);
        final ImageButton upRoyal = (ImageButton) findViewById(R.id.royal_blue_up_button);
        final ImageButton downRoyal = (ImageButton) findViewById(R.id.royal_blue_down_button);
        final ImageButton upBlue = (ImageButton) findViewById(R.id.blue_up_button);
        final ImageButton downBlue = (ImageButton) findViewById(R.id.blue_down_button);
        final ImageButton upWhite = (ImageButton) findViewById(R.id.white_up_button);
        final ImageButton downWhite = (ImageButton) findViewById(R.id.white_down_button);
        final ImageButton upGreen = (ImageButton) findViewById(R.id.green_up_button);
        final ImageButton downGreen = (ImageButton) findViewById(R.id.green_down_button);
        final ImageButton upRed = (ImageButton) findViewById(R.id.red_up_button);
        final ImageButton downRed = (ImageButton) findViewById(R.id.red_down_button);
        final ImageButton upBright = (ImageButton) findViewById(R.id.brightness_up_button);
        final ImageButton downBright = (ImageButton) findViewById(R.id.brightness_down_button);
        final SeekBar uvBar = (SeekBar) findViewById(R.id.uv_left_track);

        final SeekBar royalBar = (SeekBar) findViewById(R.id.royal_blue_left_track);
        final SeekBar blueBar = (SeekBar) findViewById(R.id.blue_left_track);
        final SeekBar whiteBar = (SeekBar) findViewById(R.id.white_left_track);
        final SeekBar greenBar = (SeekBar) findViewById(R.id.green_left_track);
        final SeekBar redBar = (SeekBar) findViewById(R.id.red_left_track);
        final SeekBar brightBar = (SeekBar) findViewById(R.id.brightness_track);

        final TextView uvLabel = (TextView) findViewById(R.id.uv_label);
        final TextView royalLabel = (TextView)findViewById(R.id.royal_blue_label);
        final TextView blueLabel = (TextView) findViewById(R.id.blue_label);
        final TextView whiteLabel = (TextView) findViewById(R.id.white_label);
        final TextView greenLabel = (TextView) findViewById(R.id.green_label);
        final TextView redLabel = (TextView) findViewById(R.id.red_label);
        final TextView brightLabel = (TextView) findViewById(R.id.brightness_label);

        final TextView uvText = (TextView)findViewById(R.id.uv_text);

        //set Default values for progress bar and corresponding text view
        uvBar.setProgress(customUV);
        royalBar.setProgress(customRB);
        blueBar.setProgress(customBlue);
        whiteBar.setProgress(customWhite);
        greenBar.setProgress(customGreen);
        redBar.setProgress(customRed);
        brightBar.setProgress(customBright);

        uvLabel.setText(String.valueOf(customUV + "%"));
        royalLabel.setText(String.valueOf(customRB + "%"));
        blueLabel.setText(String.valueOf(customBlue + "%"));
        whiteLabel.setText(String.valueOf(customWhite + "%"));
        greenLabel.setText(String.valueOf(customGreen + "%"));
        redLabel.setText(String.valueOf(customRed + "%"));
        brightLabel.setText(String.valueOf(customBright + "%"));

        //hide UV Bar if user does not have a Pro
        if (sp.getInt("hasPro",0) < 1){
            uvBar.setVisibility(View.GONE);
            uvText.setVisibility(View.GONE);
            uvLabel.setVisibility(View.GONE);
            upUV.setVisibility(View.GONE);
            downUV.setVisibility(View.GONE);
        }

        uvBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                uvLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        royalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                royalLabel.setText(String.valueOf(progress) + "%");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                blueLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        whiteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                whiteLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                greenLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                redLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        brightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                brightLabel.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSliderValues();
            }
        });

        upUV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uvBar.incrementProgressBy(1);
                if (uvBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downUV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uvBar.incrementProgressBy(-1);
                if (uvBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upRoyal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                royalBar.incrementProgressBy(1);
                if (royalBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downRoyal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                royalBar.incrementProgressBy(-1);
                if (royalBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blueBar.incrementProgressBy(1);
                if (blueBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blueBar.incrementProgressBy(-1);
                if (blueBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteBar.incrementProgressBy(1);
                if (whiteBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteBar.incrementProgressBy(-1);
                if (whiteBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                greenBar.incrementProgressBy(1);
                if (greenBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                greenBar.incrementProgressBy(-1);
                if (greenBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redBar.incrementProgressBy(1);
                if (redBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redBar.incrementProgressBy(-1);
                if (redBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        upBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brightBar.incrementProgressBy(1);
                if (brightBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });

        downBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brightBar.incrementProgressBy(-1);
                if (brightBar.getProgress() % 5 == 0){
                    getSliderValues();
                }
            }
        });
    }

    public void storeColorValues(int uvValue, int royalValue, int blueValue, int whiteValue, int greenValue, int redValue, int brightValue){
        customUV = uvValue;
        customRB = royalValue;
        customBlue = blueValue;
        customWhite = whiteValue;
        customGreen = greenValue;
        customRed = redValue;
        customBright = brightValue;
    }

    public void getSliderValues(){

        final SeekBar uvBar = (SeekBar) findViewById(R.id.uv_left_track);
        final SeekBar royalBar = (SeekBar) findViewById(R.id.royal_blue_left_track);
        final SeekBar blueBar = (SeekBar) findViewById(R.id.blue_left_track);
        final SeekBar whiteBar = (SeekBar) findViewById(R.id.white_left_track);
        final SeekBar greenBar = (SeekBar) findViewById(R.id.green_left_track);
        final SeekBar redBar = (SeekBar) findViewById(R.id.red_left_track);
        final SeekBar brightBar = (SeekBar) findViewById(R.id.brightness_track);

        storeColorValues(uvBar.getProgress(),royalBar.getProgress(),blueBar.getProgress(),whiteBar.getProgress(),greenBar.getProgress(),redBar.getProgress(),brightBar.getProgress());

        final float uvValue = (float)uvBar.getProgress() / 100 * 2;
        final float royalValue = (float)royalBar.getProgress() / 100 * 2;
        final float blueValue = (float)blueBar.getProgress() / 100 * 2;
        final float whiteValue = (float)whiteBar.getProgress() / 100 * 2;
        final float greenValue = (float)greenBar.getProgress() / 100 * 2;
        final float redValue = (float)redBar.getProgress() / 100 * 2;
        final float brightValue = (float)brightBar.getProgress() / 100 * 2;

        new Thread(new Runnable() {
            @Override public void run() {
                try{
                    ConnectManager colorchange = ConnectManager.getSharedInstance();
                    colorchange.sendColorChangeForSliders(sp.getInt("groupID",0),uvValue,royalValue,blueValue,whiteValue,greenValue,redValue,brightValue);

                    //create new preset to be sent to SavePresetView
                    ConnectManager connect = ConnectManager.getSharedInstance();
                    connect.setNewPreset(uvValue,royalValue,blueValue,whiteValue,greenValue,redValue);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class rgbColorChange extends AsyncTask<Void,Void,Void>{

        float touchUV;
        float touchRoyal;
        float touchBlue;
        float touchGreen;
        float touchRed;

        public rgbColorChange(int red, int green, int blue, int royal, int uv){

            this.touchUV = uv;
            this.touchRoyal = royal;
            this.touchBlue = blue;
            this.touchGreen = green;
            this.touchRed = red;
        }

        @Override
        protected Void doInBackground(Void... params) {

            float uv,royal,blue,green,red;
            uv = (touchUV / 255 * 100) * 2 / 100;
            royal = (touchRoyal / 255 * 100) * 2 / 100;
            blue = (touchBlue/ 255 * 100) * 2 / 100;
            green = (touchGreen / 255 * 100) * 2 / 100;
            red = (touchRed / 255 * 100) * 2 / 100;

            storeColorValues((int)uv * 100 / 2, (int)royal * 100 / 2, (int)blue * 100 / 2, 0, (int)green * 100 / 2, (int)red * 100 / 2, (int)brightValue * 100 / 2);

            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendColorChangeForSliders(sp.getInt("groupID",0), uv,royal,blue,0,green,red,brightValue);
                connect.setNewPreset(uv, royal, blue, 0, green, red);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class brightChange extends AsyncTask<Void,Void,Void>{

        double touchBright;
        public brightChange(double brightness){
            this.touchBright = brightness;
        }

        @Override
        protected Void doInBackground(Void... params) {
            float brightness;
            brightness = (float)touchBright;



            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                Preset previous = connect.getNewPreset();
                storeColorValues((int)previous.getUv() * 100 / 2,(int)previous.getRoyalBlue() * 100 / 2,(int)previous.getBlue() * 100 / 2,(int)previous.getWhite() * 100 / 2,(int)previous.getGreen() * 100 / 2,(int)previous.getRed() * 100 / 2,(int)brightness * 100 / 2);
                connect.sendColorChangeForSliders(sp.getInt("groupID",0), previous.getUv(),previous.getRoyalBlue(),previous.getBlue(),previous.getWhite(),previous.getGreen(),previous.getRed(),brightness);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class kelvinColorChange extends AsyncTask<Void,Void,Void>{

        double touchUV;
        double touchRoyal;
        double touchBlue;
        double touchGreen;
        double touchRed;
        double touchWhite;

        public kelvinColorChange(double red, double green, double blue, double white, double royal, double uv){

            this.touchUV = uv;
            this.touchRoyal = royal;
            this.touchBlue = blue;
            this.touchWhite = white;
            this.touchGreen = green;
            this.touchRed = red;
        }

        @Override
        protected Void doInBackground(Void... params) {

            float uv,royal,blue,white, green,red;
            uv = (float)touchUV;
            royal = (float)touchRoyal;
            blue = (float)touchBlue;
            white = (float)touchWhite;
            green = (float)touchGreen;
            red = (float)touchRed;

            storeColorValues((int)uv * 100 / 2, (int)royal * 100 / 2, (int)blue * 100 / 2, (int)white * 100 / 2, (int)green * 100 / 2, (int)red * 100 / 2, (int)brightValue * 100 / 2);

            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendColorChangeForSliders(sp.getInt("groupID",0),uv,royal,blue,white,green,red,brightValue);
                connect.setNewPreset(uv, royal, blue, white, green, red);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void setActionBarDefaults(){
        //set Default title of main content
        TextView viewTitle = (TextView)findViewById(R.id.action_title);
        if (isEditPoint){
            viewTitle.setText(R.string.edit_point);
        }else{
            viewTitle.setText(R.string.new_point);
        }
        viewTitle.append(" " + chosenTime);

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
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("customUV",customUV);
                intent.putExtra("customRB",customRB);
                intent.putExtra("customBlue",customBlue);
                intent.putExtra("customWhite",customWhite);
                intent.putExtra("customGreen",customGreen);
                intent.putExtra("customRed",customRed);
                intent.putExtra("customBright",customBright);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
}
