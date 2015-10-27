package com.ecotechmarine.ecosmartlive_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketClient;
import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage.ESLWebSocketMessageType;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessage;
import com.ecotechmarine.ecosmartlive_android.websocket.IESLWebSocketMessageReceiver;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by EJ Mann on 9/10/13.
 */

public class NavigationWheel extends NavigationUtility implements IESLWebSocketMessageReceiver {
    //Set up Global for getting Shared Preferences
    SharedPreferences sp;
    SharedPreferences.Editor edit;

    //Global for Connect Manager
    ConnectManager globalConnect;

    //Layout specific Variables
    DrawerLayout navWheel;
    RelativeLayout currentMainLayout;
    WebView dropcamView;
    ViewFlipper colorFlip;
    ViewFlipper screenFlip;
    ListView groupList;
    ExpandableListView mainMenuList;
    ListView settingsList;
    TextView circleDetail;
    TextView label5k;
    TextView label20k;

    //Arrays used for storing everything specific to groups and presets
    ArrayList<DeviceGroup> deviceGroups;
    ArrayList<Preset> systemPresets;
    ArrayList<Preset> userPresets;
    ArrayList<View> groupViews;
    ArrayList<String> headerItem;
    ArrayList<ArrayList<Preset>> presetTable;
    BaseExpandableListAdapter exAdapter;
    ExpandableListView presetList;
    ProgressBar presetLoading;
    Boolean isDirty = false;
    GroupPlayTask previousPlayTask;

    //This object is used for storing the most current selected group by the user
    DeviceGroup selectedGroup;
    String selectedGroupName;
    int previousID;

    //ListView related integers responsible for handling Preview and Preset Modes
    int currentChild = 0;
    int radionCount;
    int totalRadionProCount  = 0;
    int totalPumpCount = 0;
    int currentPreviewMode = -1;

    //create previous global variables to handle Grouplist dynamic info and timers
    TextView previousRadions;
    View previousRow;
    String saveCount;
    public Boolean isPlaying = false;
    Boolean searchHit = false;
    Boolean dropcamOn = false;
    Boolean ensureStop = false;
    String currentTime;
    CountDownTimer scheduleTimer;
    CountDownTimer updateColorTimer;
    CountDownTimer updatePulseTimer;

    //Layout specific variables for Kelvin and RGB Wheel
    ImageView kelvinCircle;
    ImageView rgbCircle;
    View barBright;
    View barBright2;
    FrameLayout kelvinHandler;
    FrameLayout rgbHandler;
    myView drawCircle;
    myView drawCircle2;
    myView pumpDrawCircle;
    PumpCircleColor pumpOutline;
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

    //Vortech Live Demo Circle Global Variables
    boolean pumpCircleIsBig = false;
    boolean isPumpCircleDragging;
    float pumpThumbTrackRadius;
    float pumpInnerRadius;
    float pumpOuterRadius;
    float pumpDegrees;
    float pumpPreviousDegrees;
    float pumpPreviousX;
    float pumpPreviousY;
    int pumpCircleCount = 0;
    int pumpLayoutCount = 0;
    int pumpIndex = 1;
    PumpMode.ModeEnum maxPumpMode;
    PumpMode.ModeEnum currentPumpMode;
    PumpMode currentPulse;
    Device selectedPump;
    //arraylists used to pass info to pump list
    ArrayList<String> allPumpNames;
    ArrayList<Integer> allPumpModelNums;
    ArrayList<Integer> allPumpIds;
    //Array Lists for Pump Presets
    ArrayList<String> headerPumpItem;
    ArrayList<ArrayList<PumpMode>> pumpPresetTable;
    int currentPumpTargetIndex;
    boolean allPumpsSelected = true;
    float currentSelectedPulseTime;
    int currentGroupPumpCount = 0;
    boolean onAllAquariums;

    //Handle dropcam specific globals
    Timer dropTimer;
    int dropcamToggle = 0;
    boolean dropcamLoading = false;
    boolean onDropcamView = false;

    //Get extras from previous intent and set as globals
    String userName;
    String loginEmail;

    //Group Stuff
    int groupListCount;
    String[] menuPresets;
    //set array to contain image resources (all resources can be stored as integers)
    Integer[] menuIcons = {R.drawable.icon_graph, R.drawable.icon_star, R.drawable.icon_preview, R.drawable.icon_calendar, R.drawable.icon_gear, R.drawable.icon_video};
    String[] menuOptions;
    String[] previewsList;
    String[] settingsDataArray;
    //Used to grab progressbar and textview on the actionbar that is displayed while the app loads
    ProgressBar loadingProgress;
    TextView viewTitle;
    ArrayAdapter<String> groupAdapter;
    BaseExpandableListAdapter mainMenuAdapter;

    // Web socket
    public static ESLWebSocketClient wsClient;
    private CountDownTimer countDownTimer;

    //Media Player
    MediaPlayer mPlayer;
    boolean mediaPlaying = false;

    //Settings View Global Variables
    CustomSettingsAdapter settings_setter;
    ToggleButton[] settingsToggle = new ToggleButton[3];
    RadioGroup[] settingsRadioGroup = new RadioGroup[3];
    RadioButton[] settingsRadio1 = new RadioButton[3];
    RadioButton[] settingsRadio2 = new RadioButton[3];
    RadioButton[] settingsRadio3 = new RadioButton[3];
    RadioButton[] settingsRadio4 = new RadioButton[3];
    RadioButton[] settingsRadio5 = new RadioButton[3];
    RadioButton[] settingsRadio6 = new RadioButton[3];
    RadioButton[] settingsRadio7 = new RadioButton[3];
    Calendar todayDate = Calendar.getInstance();

    //Schedule Data Point Globals
    OrientationEventListener orientationChange;
    ListenableHorizontalScrollView pointScroller;
    RelativeLayout scheduleLayout;
    ArrayList<View> viewArray;
    int dataPointWidth;
    Timer playTimer;
    Schedule userSchedule;
    boolean scheduleFirstClick = true;
    boolean groupSwitched = false;
    ScheduleDataPoint startDayPoint = new ScheduleDataPoint();
    ScheduleDataPoint startNightPoint = new ScheduleDataPoint();
    //used to determine when to have schedule view refresh
    boolean activityGoAhead = false;
    boolean nowScrolled = false;
    int nowTotalSeconds;
    boolean nowPointAdded = false;
    float newValue;
    int nowIndex;
    int nightIndex;
    int dayIndex;
    int nowPointDistance;
    int paramBrightness;
    int artificialBright;
    int naturalBright;
    int[] schedulePointTimes;
    RelativeLayout backgroundLayout;
    ImageView nightMoon;
    float maxPointBrightness;
    boolean firstNatural;
    boolean firstArtificial;
    Boolean schedulePlaying = false;
    ViewTreeObserver.OnGlobalLayoutListener scheduleListener;
    boolean onStartUp;
    boolean scheduleSeekChange;
    ScheduleDataPoint naturalNow1;
    ScheduleDataPoint naturalNow2;
    int lunarCurrentDay;
    Bitmap lunarPhaseImage;
    ArrayList<Device> allDevices;
    ArrayList<Device> allRadions;
    ArrayList<Device> allPumps;
    boolean presetNameFound = false;
    boolean onPumpCircle = false;
    int pumpLayoutCenterX;
    int pumpLayoutCenterY;

    //special intent used to block out user from hitting buttons before NavigationWheel is loaded
    Intent overlayIntent;

    //For saving activity to be finished in Warning View
    public static NavigationWheel wheelActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 11){
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        }
        initActionBar();
        setContentView(R.layout.navigation_wheel);

        wheelActivity = this;

        //fire activity that is displayed while connecting to websockets
        Intent intent = new Intent(NavigationWheel.this,Overlay.class);
        startActivity(intent);

        overlayIntent = new Intent(NavigationWheel.this, TransparentOverlay.class);
        startActivity(overlayIntent);

        //Define and initialize arrays to contain data used for ListViews from Main Menu Drawer
        menuPresets = getResources().getStringArray(R.array.menu_presets);
        previewsList = getResources().getStringArray(R.array.previewsList);
        settingsDataArray = getResources().getStringArray(R.array.settings_array);
        currentChild = 0;
        onStartUp = true;
        firstArtificial = true;
        firstNatural = true;
        scheduleSeekChange = false;
        allDevices = new ArrayList<Device>();
        allRadions = new ArrayList<Device>();
        allPumps = new ArrayList<Device>();

        globalConnect = ConnectManager.getSharedInstance();

        //initialize pump modes
        currentPumpMode = PumpMode.ModeEnum.ConstantSpeed;
        maxPumpMode = PumpMode.ModeEnum.ExpandingPulse;

        System.out.println("Current Pump Mode = " + currentPumpMode.getValue());
        System.out.println("Max Pump Mode = " + maxPumpMode.getValue());

        //set SharedPreferences and edit to be used globally
        sp = getSharedPreferences("USER_PREF",0);
        edit = sp.edit();
        userSchedule = new Schedule();

        //collect references to all necessary layouts and views
        navWheel = (DrawerLayout) findViewById(R.id.drawer_layout);
        currentMainLayout = (RelativeLayout) findViewById(R.id.nav_main_layout);
        colorFlip = (ViewFlipper) findViewById(R.id.switcher);
        screenFlip = (ViewFlipper)findViewById(R.id.screenFlip);
        mainMenuList = (ExpandableListView) findViewById(R.id.main_menu);
        kelvinCircle = (ImageView) findViewById(R.id.circle_slider_kelvin);
        rgbCircle = (ImageView) findViewById(R.id.circle_slider_rgb);
        presetList = (ExpandableListView) findViewById(R.id.preset_list);
        presetLoading = (ProgressBar) findViewById(R.id.preset_loading);
        dropcamView = (WebView)findViewById(R.id.dropcam_view);
        kelvinHandler = (FrameLayout)findViewById(R.id.kelvin_handler);
        rgbHandler = (FrameLayout)findViewById(R.id.rgb_handler);
        label5k = (TextView)findViewById(R.id.label5k);
        label20k = (TextView)findViewById(R.id.label20k);

        //activeGroupButton = (ImageView) findViewById(R.id.settings__active_img);

        //call all set up functions for main content
        //grab references to the UI on the action bar immediately after initting it so that you can display the progressbar immediately
        viewTitle = (TextView) findViewById(R.id.action_title);
        loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        loadingProgress.setVisibility(View.VISIBLE);
        viewTitle.setVisibility(View.INVISIBLE);
        viewArray = new ArrayList<View>();
        //new RadionPresetsTask().execute();
        //new GroupListTask().execute();
        initMainMenu();
        initButtonNavigation();
        //initialize brightness value to 50%
        brightValue = 1;

        groupListCount = 0;
        /*Button Programmatically Built
        rgbButton = new Button(this);
        ViewGroup.LayoutParams myParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        rgbButton.setLayoutParams(myParams);
        rgbButton.setText("RGB");
        rgbButton.setGravity(Gravity.BOTTOM);
        currentMainLayout.addView(rgbButton);
        */

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

        public myView(Context context, double thumbX, double thumbY) {
            super(context);
            this.thumbX = thumbX;
            this.thumbY = thumbY;
        }

        @Override
        public void onDraw(Canvas canvas) {
            float radius = (float)(21.5 * (metrics.densityDpi / 160f));
            if (circleIsBig || pumpCircleIsBig){
                radius = radius * 2.2f;
            }
            //drawing of circle
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeMiter(10);
            paint.setColor(Color.parseColor("#C8D4E0"));
            paint.setStrokeWidth(8.5f);
            paint.setAntiAlias(true);
            if (onPumpCircle){
                canvas.drawCircle((float)thumbX, (float)thumbY, radius, paint);
            }else{
                canvas.drawCircle(centerX + (float)thumbX, centerY + (float)thumbY, radius, paint);
            }
            //canvas.drawCircle((float)thumbX, (float)thumbY, 50, paint);

            //draw the object a 2nd time to fill the gap in the circle
            paint.setColor(Color.parseColor("#59000000"));
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(0f);
            paint.setStrokeMiter(0);
            paint.setStrokeJoin(Paint.Join.MITER);
            if (onPumpCircle){
                canvas.drawCircle((float)thumbX, (float)thumbY, radius, paint);
            }else{
                canvas.drawCircle(centerX + (float)thumbX, centerY + (float)thumbY, radius, paint);
            }
            //canvas.drawCircle((float)thumbX,(float)thumbY, 50, paint);
        }
    }

    class PumpCircleColor extends View {
        double thumbX;
        double thumbY;
        float radius;
        Paint paint = new Paint();

        public PumpCircleColor(Context context, double thumbX, double thumbY, float radius) {
            super(context);
            this.thumbX = thumbX;
            this.thumbY = thumbY;
            this.radius = radius;
        }

        @Override
        public void onDraw(Canvas canvas) {
            //Outer Circle Defining Color
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            switch (pumpIndex){
                case 1:
                    paint.setColor(Color.parseColor("#2BA64D"));
                    break;
                case 2:
                    paint.setColor(Color.parseColor("#354F8F"));
                    break;
                case 3:
                    paint.setColor(Color.parseColor("#E58DAF"));
                    break;
                case 4:
                    paint.setColor(Color.parseColor("#F3E857"));
                    break;
                case 5:
                    paint.setColor(Color.parseColor("#F3E857"));
                    break;
                case 6:
                    paint.setColor(Color.parseColor("#73415C"));
                    break;
                case 7:
                    paint.setColor(Color.parseColor("#73415C"));
                    break;
                default:
                    paint.setColor(Color.parseColor("#354F8F"));
                    break;
                /*
                case 1:
                    paint.setColor(getResources().getColor(R.color.LimeGreen));
                    break;
                case 2:
                    paint.setColor(getResources().getColor(R.color.RoyalBlue));
                    break;
                case 3:
                    paint.setColor(getResources().getColor(R.color.HotPink));
                    break;
                case 4:
                    paint.setColor(getResources().getColor(R.color.Gold));
                    break;
                case 5:
                    paint.setColor(getResources().getColor(R.color.Gold));
                    break;
                case 6:
                    paint.setColor(getResources().getColor(R.color.Purple));
                    break;
                case 7:
                    paint.setColor(getResources().getColor(R.color.Purple));
                    break;
                default:
                    paint.setColor(getResources().getColor(R.color.DarkCyan));
                    break;
                 */
            }
            paint.setStrokeWidth(43 * (metrics.densityDpi / 160f));
            paint.setAntiAlias(true);
            canvas.drawCircle((float)thumbX, (float)thumbY, (6 + radius) * (metrics.densityDpi / 160f), paint);
        }
    }

    // Exit PC control and close the web socket
    private void exitApplication(){
        if (dropTimer != null){
            dropTimer.cancel();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Disconnect from Websocket
                try{
                    if(wsClient != null){
                        wsClient.disconnect();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                ConnectManager cm = ConnectManager.getSharedInstance();
                cm.stopPCControl(30);
            }
        }).start();
    }

    //On Nav Wheel, Override Back Button to disconnect from the websocket, stopPCControl, and exit application
    @Override
    public void onBackPressed() {
        Log.i("OVERRIDE", "onBackPressed");
        exitApplication();
        super.onBackPressed();
    }

    @Override
    public void onStop()
    {
        stopMedia();
        if (updateColorTimer != null){
            updateColorTimer.cancel();
        }
        //stop pulse timer
        if (updatePulseTimer != null){
            updatePulseTimer.cancel();
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
        stopMedia();
        if (updateColorTimer != null){
            updateColorTimer.cancel();
        }
        //stop pulse timer
        if (updatePulseTimer != null){
            updatePulseTimer.cancel();
        }
        if (dropTimer != null){
            dropTimer.cancel();
        }
        Log.i("STOP", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (schedulePlaying){
            stopSchedulePlayback();
        }
        if (screenFlip.getDisplayedChild() == 3 && activityGoAhead){
            scheduleSeekChange = false;
            if (userSchedule.scheduleType != null) {
                final TextView modeHeader = (TextView)findViewById(R.id.schedule_mode_header);
                if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                    modeHeader.setText(R.string.natural_mode_options);
                }else{
                    modeHeader.setText(R.string.artificial_mode_options);
                    //grabbed saved value since activity gets cancelled from new and edit points
                    artificialBright = sp.getInt("artificialBrightness", 0);
                }
                //stopPCControl when the schedule resumes after edit or new point
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ConnectManager connectESL = ConnectManager.getSharedInstance();
                            connectESL.stopPCControl(0);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                startActivity(intent);
                new LoadScheduleDataTask().execute();
            }
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0){
            activityGoAhead = true;
        }
        //refresh groupAdapter when coming back from the wizard
        if (resultCode == RESULT_OK && requestCode == 1){
            new GroupListTask().execute();
            groupAdapter.notifyDataSetChanged();
        }
        if (resultCode == RESULT_OK && requestCode == 2){
            new GroupListTask().execute();
            //groupAdapter.notifyDataSetChanged();
        }
        if (resultCode == RESULT_OK && requestCode == 3){
            pumpIndex = data.getIntExtra("pumpIndex", 1);
            allPumpsSelected = data.getBooleanExtra("allPumpsSelected", true);
            currentPumpTargetIndex = data.getIntExtra("pumpSelectedIndex", 0);
            final TextView pumpsInGroup = (TextView) findViewById(R.id.pumps_in_group);
            if (allPumpsSelected){
                pumpsInGroup.setText(R.string.all_pumps);
            }else{
                pumpsInGroup.setText(allPumpNames.get(currentPumpTargetIndex));
            }
        }
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
                                drawCircle = new myView(NavigationWheel.this, thumbX, thumbY);
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
                        //stop pulse timer
                        if (updatePulseTimer != null){
                            updatePulseTimer.cancel();
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
                            degrees = (float) Math.toDegrees(Math.atan2(x - circleCenterPoint.x, circleCenterPoint.y - y));

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
                                drawCircle2 = new myView(NavigationWheel.this, thumbX,thumbY);
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
                        //stop pulse timer
                        if (updatePulseTimer != null){
                            updatePulseTimer.cancel();
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

    public void hideSliderUIforPulse(){
        ImageView incrementPulse = (ImageView) findViewById(R.id.increment_pulse);
        ImageView decrementPulse = (ImageView) findViewById(R.id.decrement_pulse);
        TextView pulseTimeTitle = (TextView) findViewById(R.id.pulse_time_title);
        TextView pulseTimeValue = (TextView) findViewById(R.id.dynamic_pulse_time);
        SeekBar pulseSlider = (SeekBar)findViewById(R.id.pulse_time_slider);

        incrementPulse.setVisibility(View.INVISIBLE);
        decrementPulse.setVisibility(View.INVISIBLE);
        pulseTimeTitle.setVisibility(View.INVISIBLE);
        pulseTimeValue.setVisibility(View.INVISIBLE);
        pulseSlider.setVisibility(View.INVISIBLE);
    }

    public void showSliderUIforPulse(){
        ImageView incrementPulse = (ImageView) findViewById(R.id.increment_pulse);
        ImageView decrementPulse = (ImageView) findViewById(R.id.decrement_pulse);
        TextView pulseTimeTitle = (TextView) findViewById(R.id.pulse_time_title);
        TextView pulseTimeValue = (TextView) findViewById(R.id.dynamic_pulse_time);
        SeekBar pulseSlider = (SeekBar)findViewById(R.id.pulse_time_slider);

        incrementPulse.setVisibility(View.VISIBLE);
        decrementPulse.setVisibility(View.VISIBLE);
        pulseTimeTitle.setVisibility(View.VISIBLE);
        pulseTimeValue.setVisibility(View.VISIBLE);
        pulseSlider.setVisibility(View.VISIBLE);
    }

    public void setPulseSliderForShortPulse(){
        //this is perfect, you cannot set a minimum in android seekbars
        //250ms - 2s
        final SeekBar pulseSlider = (SeekBar)findViewById(R.id.pulse_time_slider);
        final TextView pulseTimeValue = (TextView) findViewById(R.id.dynamic_pulse_time);
        pulseSlider.setMax(1750);   //1750 is max, starting number is 250. Max - starting number is ideal number
        pulseSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 250;
                if (progress == 1000 || progress == 2000){
                    int progressSeconds = (progress / 1000);
                    pulseTimeValue.setText(progressSeconds + "s");
                }else if(progress > 1000 && progress < 2000){
                    float progressSeconds = (progress / 1000.000f);
                    pulseTimeValue.setText(progressSeconds + "s");
                }else{
                    pulseTimeValue.setText(String.format("0.%d", progress) + "s");

                }
                currentSelectedPulseTime = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });
        pulseSlider.setProgress(750);

        ImageView pulseIncrement = (ImageView) findViewById(R.id.increment_pulse);
        pulseIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pulseSlider.incrementProgressBy(1);
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });

        final ImageView pulseDecrement = (ImageView) findViewById(R.id.decrement_pulse);
        pulseDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pulseSlider.incrementProgressBy(-1);
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });
    }

    public void setPulseSliderForLongPulse(){
        //2s - 120m
        final SeekBar pulseSlider = (SeekBar)findViewById(R.id.pulse_time_slider);
        final TextView pulseTimeValue = (TextView) findViewById(R.id.dynamic_pulse_time);
        pulseSlider.setMax(7198);  //7200 is max, starting number is 2. Max - starting number is ideal number
        pulseSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 2;
                if (progress < 60){
                    pulseTimeValue.setText(progress + "s");
                }else{
                    pulseTimeValue.setText((progress / 60) + "m");
                }
                currentSelectedPulseTime = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });
        pulseSlider.setProgress(0);
        //slider does not update if the value is 0 from short pulse. This is insurance code
        if (pulseSlider.getProgress() == 0){
            pulseTimeValue.setText("2s");
            currentSelectedPulseTime = 2;
        }

        ImageView pulseIncrement = (ImageView) findViewById(R.id.increment_pulse);
        pulseIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pulseSlider.incrementProgressBy(1);
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });

        final ImageView pulseDecrement = (ImageView) findViewById(R.id.decrement_pulse);
        pulseDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pulseSlider.incrementProgressBy(-1);
                setPulseParams();
                new SetPumpToModeTask().execute();
            }
        });
    }

    public void updatePumpDetail(){
        //exact number to multiple degrees by = 0.7083333333 repeating. Value 0.710 is rounded to give window for 100% to be a selection
        double touchValue = pumpDegrees * 0.710;
        double touchRatio = touchValue / 255;
        int percent = (int)(touchRatio * 100);
        TextView pumpCircleDetail = (TextView)findViewById(R.id.pump_circle_detail);
        pumpCircleDetail.setVisibility(View.VISIBLE);
        pumpCircleDetail.setText(percent + "%");
    }

    //Pump Live Demo Degree Functions
    public void setPulseParams(){
        double touchValue = pumpDegrees * 0.708333333;

        PumpModeParam param = new PumpModeParam();
        param.paramValue = (int)touchValue;

        switch (currentPulse.pumpModeId){
            case 1:
                //Constant Speed
                param.pumpModeParamId = 1;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }
                break;
            case 2:
                //Short Pulse
                param.pumpModeParamId = 2;
                //Specific Pulse Params for Expanding Pulse need to be initted and set
                PumpModeParam paramPulseTime = new PumpModeParam();
                paramPulseTime.pumpModeParamId = 3;
                paramPulseTime.paramValue = (int)currentSelectedPulseTime;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramPulseTime);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramPulseTime);
                }
                break;
            case 3:
                //Long Pulse
                param.pumpModeParamId = 4;
                paramPulseTime = new PumpModeParam();
                paramPulseTime.pumpModeParamId = 5;
                paramPulseTime.paramValue = (int)currentSelectedPulseTime;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramPulseTime);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramPulseTime);
                }
                break;
            case 4:
                //ReefCrest
                param.pumpModeParamId = 6;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }
                break;
            case 5:
                //Lagoon
                param.pumpModeParamId = 7;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }
                break;
            case 6:
                //Nutrient Transport
                param.pumpModeParamId = 8;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }
                break;
            case 7:
                //Tidal Swell
                param.pumpModeParamId = 9;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                }
                break;
            case 11:
                //Expanding Pulse
                param.pumpModeParamId = 17;
                //Specific Pulse Params for Expanding Pulse need to be initted and set
                PumpModeParam paramStartPulseRate = new PumpModeParam();
                paramStartPulseRate.pumpModeParamId = 18;
                paramStartPulseRate.paramValue = 250;
                PumpModeParam paramEndPulseRate = new PumpModeParam();
                paramEndPulseRate.pumpModeParamId = 19;
                paramEndPulseRate.paramValue = 2000;
                if (currentPulse.params != null){
                    currentPulse.params.clear();
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramStartPulseRate);
                    currentPulse.params.add(paramEndPulseRate);
                }else{
                    currentPulse.params = new ArrayList<PumpModeParam>();
                    //add params to current pulse
                    currentPulse.params.add(param);
                    currentPulse.params.add(paramStartPulseRate);
                    currentPulse.params.add(paramEndPulseRate);
                }
                break;
            default:
                break;
        }
        System.out.println("Param Value Passed = " + param.paramValue);

    }

    public class CustomSettingsAdapter extends ArrayAdapter<String>
    {
        public CustomSettingsAdapter(Context context, int textViewResourceId, String[] objects)
        {
            super(context, textViewResourceId, objects);
        }

        @Override
        public int getCount() {
            if (selectedGroup != null && selectedGroup.getGroupId() == 240){
                return super.getCount() - 2;
            }else{
                return super.getCount();
            }
        }

        @Override

        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            LayoutInflater inflater = NavigationWheel.this.getLayoutInflater();
            convertView = inflater.inflate(R.layout.settings_layout, parent, false);
            TextView header = (TextView) convertView.findViewById(R.id.settings_header);
            TextView toggleText = (TextView)convertView.findViewById(R.id.settings_toggle_text);
            TextView inTitle = (TextView)convertView.findViewById(R.id.settings_intensity_title);
            final TextView inValue = (TextView)convertView.findViewById(R.id.settings_intensity_value);
            SeekBar inSlider = (SeekBar)convertView.findViewById(R.id.settings_intensity_slider);
            TextView timeTitle = (TextView)convertView.findViewById(R.id.settings_time_title);
            settingsToggle[position] = (ToggleButton)convertView.findViewById(R.id.settings_toggle);
            settingsRadioGroup[position]  = (RadioGroup)convertView.findViewById(R.id.settings_segment);
            //get all radio buttons
            settingsRadio1[position] = (RadioButton) convertView.findViewById(R.id.settings_radio1);
            settingsRadio2[position] = (RadioButton) convertView.findViewById(R.id.settings_radio2);
            settingsRadio3[position] = (RadioButton) convertView.findViewById(R.id.settings_radio3);
            settingsRadio4[position] = (RadioButton) convertView.findViewById(R.id.settings_radio4);
            settingsRadio5[position] = (RadioButton) convertView.findViewById(R.id.settings_radio5);
            settingsRadio6[position] = (RadioButton) convertView.findViewById(R.id.settings_radio6);
            settingsRadio7[position] = (RadioButton) convertView.findViewById(R.id.settings_radio7);
            final int acYear = todayDate.get(Calendar.YEAR) - 2000;
            final int acMonth = todayDate.get(Calendar.MONTH);
            final int acDay = todayDate.get(Calendar.DAY_OF_MONTH);

            //Setting Index: 0 = EP, 1 = OT, 2 = AC, 3 = LP
            switch (position){
                case 0:
                    // Inflate the layout in each row.

                    if (selectedGroup != null && selectedGroup.getGroupId() == 240){
                        header.setText(R.string.ecosmart_participation);
                        timeTitle.setVisibility(View.GONE);
                        inSlider.setVisibility(View.GONE);
                        settingsRadioGroup[0].setVisibility(View.GONE);
                        inTitle.setVisibility(View.GONE);
                        inValue.setVisibility(View.GONE);

                        //handle users actions
                        settingsToggle[0].setChecked(GroupSettings.Type.EcoSmartParticipation.enabled);
                        settingsToggle[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                Bundle epBundle = new Bundle();
                                if (isChecked){
                                    epBundle.putBoolean("settingOn", true);
                                    GroupSettings.Type.EcoSmartParticipation.enabled = true;
                                }else{
                                    epBundle.putBoolean("settingOn", false);
                                    GroupSettings.Type.EcoSmartParticipation.enabled = false;
                                }
                                epBundle.putInt("groupId",240);
                                epBundle.putInt("settingIndex", GroupSettings.Type.EcoSmartParticipation.getIndex());

                                Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                intent.putExtras(epBundle);
                                startActivity(intent);
                            }
                        });
                    }else{
                        header.setText(R.string.override_timer);
                        timeTitle.setText(R.string.override_time);
                        toggleText.setText(R.string.override_length_for_radions);
                        inSlider.setVisibility(View.GONE);
                        inTitle.setVisibility(View.GONE);
                        inValue.setVisibility(View.GONE);

                        //set Default State of Override Timer View based off of data
                        settingsToggle[0] = (ToggleButton)convertView.findViewById(R.id.settings_toggle);
                        settingsToggle[0].setChecked(GroupSettings.Type.OverrideTimer.enabled);
                        if (GroupSettings.Type.OverrideTimer.enabled){
                            switch(GroupSettings.Type.OverrideTimer.duration - 1){
                                case 0:
                                    settingsRadio1[0].setChecked(true);
                                    break;
                                case 1:
                                    settingsRadio2[0].setChecked(true);
                                    break;
                                case 2:
                                    settingsRadio3[0].setChecked(true);
                                    break;
                                case 3:
                                    settingsRadio4[0].setChecked(true);
                                    break;
                                case 4:
                                    settingsRadio5[0].setChecked(true);
                                    break;
                                case 5:
                                    settingsRadio6[0].setChecked(true);
                                    break;
                                case 6:
                                    settingsRadio7[0].setChecked(true);
                                    break;
                                default:
                                    break;
                            }
                            settingsRadio1[0].setClickable(true);
                            settingsRadio2[0].setClickable(true);
                            settingsRadio3[0].setClickable(true);
                            settingsRadio4[0].setClickable(true);
                            settingsRadio5[0].setClickable(true);
                            settingsRadio6[0].setClickable(true);
                            settingsRadio7[0].setClickable(true);
                        }else{
                            settingsRadio1[0].setClickable(false);
                            settingsRadio2[0].setClickable(false);
                            settingsRadio3[0].setClickable(false);
                            settingsRadio4[0].setClickable(false);
                            settingsRadio5[0].setClickable(false);
                            settingsRadio6[0].setClickable(false);
                            settingsRadio7[0].setClickable(false);
                        }

                        settingsToggle[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (!isChecked) {
                                    Bundle epBundle = new Bundle();
                                    epBundle.putBoolean("settingOn", false);
                                    GroupSettings.Type.OverrideTimer.enabled = false;
                                    epBundle.putInt("groupId", selectedGroup.getGroupId());
                                    epBundle.putInt("settingIndex", GroupSettings.Type.OverrideTimer.getIndex());
                                    epBundle.putInt("otHours", 0);

                                    Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                    intent.putExtras(epBundle);
                                    startActivity(intent);

                                    settingsRadio1[0].setChecked(false);
                                    settingsRadio2[0].setChecked(false);
                                    settingsRadio3[0].setChecked(false);
                                    settingsRadio4[0].setChecked(false);
                                    settingsRadio5[0].setChecked(false);
                                    settingsRadio6[0].setChecked(false);
                                    settingsRadio7[0].setChecked(false);
                                    settingsRadio1[0].setClickable(false);
                                    settingsRadio2[0].setClickable(false);
                                    settingsRadio3[0].setClickable(false);
                                    settingsRadio4[0].setClickable(false);
                                    settingsRadio5[0].setClickable(false);
                                    settingsRadio6[0].setClickable(false);
                                    settingsRadio7[0].setClickable(false);
                                }else{
                                    GroupSettings.Type.OverrideTimer.enabled = true;
                                    settingsRadio1[0].setClickable(true);
                                    settingsRadio2[0].setClickable(true);
                                    settingsRadio3[0].setClickable(true);
                                    settingsRadio4[0].setClickable(true);
                                    settingsRadio5[0].setClickable(true);
                                    settingsRadio6[0].setClickable(true);
                                    settingsRadio7[0].setClickable(true);
                                }
                            }
                        });
                        settingsRadioGroup[0].setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (settingsToggle[0].isChecked()) {
                                    Bundle epBundle = new Bundle();
                                    epBundle.putBoolean("settingOn", true);
                                    GroupSettings.Type.OverrideTimer.enabled = true;
                                    epBundle.putInt("groupId", selectedGroup.getGroupId());
                                    epBundle.putInt("settingIndex", GroupSettings.Type.OverrideTimer.getIndex());
                                    if (settingsRadio1[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 1;
                                    }
                                    if (settingsRadio2[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 2;
                                    }
                                    if (settingsRadio3[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 3;
                                    }
                                    if (settingsRadio4[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 4;
                                    }
                                    if (settingsRadio5[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 5;
                                    }
                                    if (settingsRadio6[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 6;
                                    }
                                    if (settingsRadio7[0].isChecked()){
                                        GroupSettings.Type.OverrideTimer.duration = 7;
                                    }
                                    epBundle.putInt("otHours", GroupSettings.Type.OverrideTimer.duration);
                                    //settingsRadioGroup[0].check(settingsRadioGroup[0].getChildAt(GroupSettings.Type.OverrideTimer.duration).getId());
                                    switch(GroupSettings.Type.OverrideTimer.duration - 1){
                                        case 0:
                                            settingsRadio1[0].setChecked(true);
                                            break;
                                        case 1:
                                            settingsRadio2[0].setChecked(true);
                                            break;
                                        case 2:
                                            settingsRadio3[0].setChecked(true);
                                            break;
                                        case 3:
                                            settingsRadio4[0].setChecked(true);
                                            break;
                                        case 4:
                                            settingsRadio5[0].setChecked(true);
                                            break;
                                        case 5:
                                            settingsRadio6[0].setChecked(true);
                                            break;
                                        case 6:
                                            settingsRadio7[0].setChecked(true);
                                            break;
                                        default:
                                            break;
                                    }

                                    Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                    intent.putExtras(epBundle);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                    break;
                case 1:
                    header.setText(R.string.acclimate_timer);
                    settingsRadioGroup[1].removeViewAt(6);

                    //handle users actions
                    settingsToggle[1] = (ToggleButton)convertView.findViewById(R.id.settings_toggle);
                    settingsToggle[1].setChecked(GroupSettings.Type.AcclimateTimer.enabled);

                    //set initial intensity of slider for Acclimate Timer
                    int currentProgress = 0;
                    if (GroupSettings.Type.AcclimateTimer.intensity > 0){
                        currentProgress = (int)(GroupSettings.Type.AcclimateTimer.intensity * 100) / 2;
                    }
                    //Due to the server defaulting the intensity to 1.0 (50%), set the progress to 0 if it is false to override this setting
                    if (!GroupSettings.Type.AcclimateTimer.enabled){
                        currentProgress = 0;
                        GroupSettings.Type.AcclimateTimer.intensity = 0;
                    }
                    inSlider.setProgress(currentProgress);
                    inValue.setText(currentProgress + "%");

                    //set initial checked radio based on settings returned from server
                    if (GroupSettings.Type.AcclimateTimer.enabled){
                        switch(GroupSettings.Type.AcclimateTimer.duration - 1){
                            case 0:
                                settingsRadio1[1].setChecked(true);
                                break;
                            case 1:
                                settingsRadio2[1].setChecked(true);
                                break;
                            case 2:
                                settingsRadio3[1].setChecked(true);
                                break;
                            case 3:
                                settingsRadio4[1].setChecked(true);
                                break;
                            case 4:
                                settingsRadio5[1].setChecked(true);
                                break;
                            case 5:
                                settingsRadio6[1].setChecked(true);
                                break;
                            default:
                                break;
                        }
                        settingsRadio1[1].setClickable(true);
                        settingsRadio2[1].setClickable(true);
                        settingsRadio3[1].setClickable(true);
                        settingsRadio4[1].setClickable(true);
                        settingsRadio5[1].setClickable(true);
                        settingsRadio6[1].setClickable(true);
                    }else{
                        settingsRadio1[1].setClickable(false);
                        settingsRadio2[1].setClickable(false);
                        settingsRadio3[1].setClickable(false);
                        settingsRadio4[1].setClickable(false);
                        settingsRadio5[1].setClickable(false);
                        settingsRadio6[1].setClickable(false);
                    }
                    settingsToggle[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!isChecked){
                                Bundle epBundle = new Bundle();
                                epBundle.putBoolean("settingOn", false);
                                GroupSettings.Type.AcclimateTimer.enabled = false;
                                epBundle.putInt("groupId", selectedGroup.getGroupId());
                                epBundle.putInt("settingIndex",GroupSettings.Type.AcclimateTimer.getIndex());
                                epBundle.putInt("acStartMonth",acMonth);
                                epBundle.putInt("acStartDay",acDay);
                                epBundle.putInt("acStartYear",acYear);
                                epBundle.putInt("iPeriod", 0);
                                epBundle.putFloat("acIntensity", 0.0f);
                                Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                intent.putExtras(epBundle);
                                startActivity(intent);

                                settingsRadio1[1].setChecked(false);
                                settingsRadio2[1].setChecked(false);
                                settingsRadio3[1].setChecked(false);
                                settingsRadio4[1].setChecked(false);
                                settingsRadio5[1].setChecked(false);
                                settingsRadio6[1].setChecked(false);
                                settingsRadio1[1].setClickable(false);
                                settingsRadio2[1].setClickable(false);
                                settingsRadio3[1].setClickable(false);
                                settingsRadio4[1].setClickable(false);
                                settingsRadio5[1].setClickable(false);
                                settingsRadio6[1].setClickable(false);
                            }else{
                                GroupSettings.Type.AcclimateTimer.enabled = true;
                                settingsRadio1[1].setClickable(true);
                                settingsRadio2[1].setClickable(true);
                                settingsRadio3[1].setClickable(true);
                                settingsRadio4[1].setClickable(true);
                                settingsRadio5[1].setClickable(true);
                                settingsRadio6[1].setClickable(true);
                            }
                        }
                    });
                    //Handle Radio Group
                    settingsRadioGroup[1].setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if (settingsToggle[1].isChecked()) {
                                Bundle epBundle = new Bundle();
                                epBundle.putBoolean("settingOn", true);
                                GroupSettings.Type.AcclimateTimer.enabled = true;
                                epBundle.putInt("groupId", selectedGroup.getGroupId());
                                epBundle.putInt("settingIndex", GroupSettings.Type.AcclimateTimer.getIndex());
                                epBundle.putInt("acStartMonth", acMonth);
                                epBundle.putInt("acStartDay", acDay);
                                epBundle.putInt("acStartYear", acYear);

                                //Set duration based on which radio is checked
                                if (settingsRadio1[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 1;
                                }
                                if (settingsRadio2[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 2;
                                }
                                if (settingsRadio3[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 3;
                                }
                                if (settingsRadio4[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 4;
                                }
                                if (settingsRadio5[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 5;
                                }
                                if (settingsRadio6[1].isChecked()){
                                    GroupSettings.Type.AcclimateTimer.duration = 6;
                                }
                                switch(GroupSettings.Type.AcclimateTimer.duration - 1){
                                    case 0:
                                        settingsRadio1[1].setChecked(true);
                                        break;
                                    case 1:
                                        settingsRadio2[1].setChecked(true);
                                        break;
                                    case 2:
                                        settingsRadio3[1].setChecked(true);
                                        break;
                                    case 3:
                                        settingsRadio4[1].setChecked(true);
                                        break;
                                    case 4:
                                        settingsRadio5[1].setChecked(true);
                                        break;
                                    case 5:
                                        settingsRadio6[1].setChecked(true);
                                        break;
                                    default:
                                        break;
                                }
                                epBundle.putInt("iPeriod", GroupSettings.Type.AcclimateTimer.duration);
                                epBundle.putFloat("acIntensity",GroupSettings.Type.AcclimateTimer.intensity);

                                Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                intent.putExtras(epBundle);
                                startActivity(intent);
                            }
                        }
                    });
                    //Handle Intensity Slider
                    inSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            //update intensity value and textview value as progress is changed
                            int remainder = progress % 5;
                            progress = progress - remainder;
                            if (remainder == 0){
                                inValue.setText(progress + "%");
                            }else{
                                seekBar.setProgress(progress);
                            }
                            GroupSettings.Type.AcclimateTimer.intensity = (progress * 2.0f) / 100.0f;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            //send data to server every time you stop moving the slider,
                            if (settingsToggle[1].isChecked() && GroupSettings.Type.AcclimateTimer.duration > 0){
                                Bundle epBundle = new Bundle();
                                epBundle.putBoolean("settingOn",true);
                                GroupSettings.Type.AcclimateTimer.enabled = true;
                                epBundle.putInt("groupId",selectedGroup.getGroupId());
                                epBundle.putInt("settingIndex",GroupSettings.Type.AcclimateTimer.getIndex());
                                epBundle.putInt("acStartMonth",acMonth);
                                epBundle.putInt("acStartDay",acDay);
                                epBundle.putInt("acStartYear",acYear);
                                epBundle.putInt("iPeriod", GroupSettings.Type.AcclimateTimer.duration);
                                epBundle.putFloat("acIntensity", GroupSettings.Type.AcclimateTimer.intensity);

                                Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                                intent.putExtras(epBundle);
                                startActivity(intent);
                            }
                        }
                    });
                    break;
                case 2:
                    // Inflate the layout in each row.
                    header.setText(R.string.lunar_phases);
                    toggleText.setText(R.string.lunar_phase_explanation);
                    timeTitle.setVisibility(View.GONE);
                    settingsRadioGroup[2].setVisibility(View.GONE);
                    inSlider.setVisibility(View.GONE);
                    inTitle.setVisibility(View.GONE);
                    inValue.setVisibility(View.GONE);

                    //handle user actions
                    settingsToggle[2] = (ToggleButton)convertView.findViewById(R.id.settings_toggle);
                    settingsToggle[2].setChecked(GroupSettings.Type.LunarPhases.enabled);
                    settingsToggle[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Bundle epBundle = new Bundle();
                            if (isChecked){
                                epBundle.putBoolean("settingOn",true);
                                GroupSettings.Type.LunarPhases.enabled = true;
                            }else{
                                epBundle.putBoolean("settingOn",false);
                                GroupSettings.Type.LunarPhases.enabled = false;
                            }
                            epBundle.putInt("groupId",selectedGroup.getGroupId());
                            epBundle.putInt("settingIndex",GroupSettings.Type.LunarPhases.getIndex());

                            Intent intent = new Intent(getApplicationContext(), SettingsOverlay.class);
                            intent.putExtras(epBundle);
                            startActivity(intent);
                        }
                    });

                    break;
                default:
                    break;

            }

            // Inflate the layout in each row.
            /*LayoutInflater inflater = ScheduleParamsView.this.getLayoutInflater();
            View row = inflater.inflate(R.layout.schedule_list_item, parent, false);

            // Declare and define the TextView, "item." This is where
            // the name of each item will appear.
            TextView item = (TextView)row.findViewById(R.id.sched_list_text);
            item.setText(schedList[position]);

            // Declare and define the TextView, "icon." This is where
            // the icon in each row will appear.
            ImageView icon=(ImageView)row.findViewById(R.id.sched_list_img);
            icon.setImageResource(schedIcons[position]);*/

            return convertView;
        }
    }

    public class GroupAdapter extends ArrayAdapter<String>
    {

        public GroupAdapter(Context context, int textViewResourceId, String[] objects)
        {
            super(context, textViewResourceId, objects);

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater;
            TextView item;

            //final int targetID = deviceGroups.get(position).getGroupId();
           // TextView radionCount;

            /*final ImageView playImage = (ImageView) findViewById(R.id.play_img);
            final ImageView searchImage = (ImageView) findViewById(R.id.search_img);
            final ImageView playImage2 = (ImageView) findViewById(R.id.play_img2);
            final ImageView searchImage2 = (ImageView) findViewById(R.id.search_img2);*/

            String groupName = menuOptions[position];
            String radions = "";
            String pumps = "";
            DeviceGroup currentGroup = deviceGroups.get(position);

            if(deviceGroups != null && deviceGroups.get(position) != null){
                groupName = deviceGroups.get(position).getName();
                radions = String.valueOf(deviceGroups.get(position).getRadionCount());
                pumps = String.valueOf(deviceGroups.get(position).getPumpCount());
            }

            //Target ID is used to get the groupID specific to the Identify Task
            final int targetID = currentGroup.getGroupId();
            System.out.println(targetID);
            //target Position is used to get an index from 0 to n used for the schedule group play task
            final int tPosition = position;

            switch(position){
                case 0:
                    // Inflate the layout in each row.
                    inflater = NavigationWheel.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.group_list_item1, parent, false);
                    //add the row to the list to be manipulated outside of the class

                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    item = (TextView)convertView.findViewById(R.id.txtGroupList);
                    item.setText(groupName);

                    TextView deviceCountView = (TextView)convertView.findViewById(R.id.radionGroupList);
                    String deviceCountDisplayText = "";
                    switch(deviceGroups.get(position).getRadionCount()){
                        case 1:
                            deviceCountDisplayText = radions + " Radion";
                            break;
                        default:
                            deviceCountDisplayText = radions + " Radions";
                            break;
                    }

                    if (deviceGroups.get(position).getPumpCount() > 0){
                        switch (deviceGroups.get(position).getPumpCount()){
                            case 1:
                                deviceCountDisplayText = deviceCountDisplayText + ", " + pumps + " VorTech";
                                break;
                            default:
                                deviceCountDisplayText = deviceCountDisplayText + ", " + pumps + " VorTechs";
                                break;
                        }
                    }

                    deviceCountView.setText(deviceCountDisplayText);

                    //final ImageView activeGroupButton = (ImageView)convertView.findViewById(R.id.settings__active_img);
                    //final ImageView inactiveGroupButton = (ImageView)convertView.findViewById(R.id.settings_inactive_img);
                    //final TextView activeClicker = (TextView)convertView.findViewById(R.id.active_settings_clicker);
                    //final TextView inactiveClicker = (TextView)convertView.findViewById(R.id.inactive_settings_clicker);
                    final ImageView searchImage = (ImageView)convertView.findViewById(R.id.search_img);
                    //final ImageView playImage = (ImageView)convertView.findViewById(R.id.play_img);

                    /*inactiveClicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activeGroupButton.setVisibility(View.VISIBLE);
                            activeClicker.setVisibility(View.VISIBLE);
                            inactiveClicker.setVisibility(View.GONE);
                            inactiveGroupButton.setVisibility(View.GONE);
                            searchImage.setVisibility(View.VISIBLE);

                        }
                    });

                    activeClicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            inactiveGroupButton.setVisibility(View.VISIBLE);
                            inactiveClicker.setVisibility(View.VISIBLE);
                            activeGroupButton.setVisibility(View.GONE);
                            activeClicker.setVisibility(View.GONE);
                            searchImage.setVisibility(View.GONE);

                        }
                    }); */

                    searchImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new IdentifyTask(240).execute();
                        }
                    });
                    break;
                default:
                    // Inflate the layout in each row.
                    inflater = NavigationWheel.this.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.group_list_item2, parent, false);
                    groupViews.add(convertView);

                    // Declare and define the TextView, "item." This is where
                    // the name of each item will appear.
                    if (convertView != null){
                        item = (TextView)convertView.findViewById(R.id.txtGroupList2);
                        item.setText(groupName);
                    }

                    final TextView deviceCountView2 = (TextView)convertView.findViewById(R.id.radionGroupList2);
                    switch(deviceGroups.get(position).getRadionCount()){
                        case 1:
                            deviceCountDisplayText = radions + " Radion";
                            break;
                        default:
                            deviceCountDisplayText = radions + " Radions";
                            break;
                    }

                    if (deviceGroups.get(position).getPumpCount() > 0){
                        switch (deviceGroups.get(position).getPumpCount()){
                            case 1:
                                deviceCountDisplayText = deviceCountDisplayText + ", " + pumps + " VorTech";
                                break;
                            default:
                                deviceCountDisplayText = deviceCountDisplayText + ", " + pumps + " VorTechs";
                                break;
                        }
                    }

                    //set Text
                    deviceCountView2.setText(deviceCountDisplayText);

                    //This code is no longer needed because the text will never change due to no schedule playback on group list
                    /*if (deviceCountView2 != null && deviceCountView2.getText() != null){
                        saveCount = deviceCountView2.getText().toString();
                    }*/
                    previousRow = convertView;

                    final ImageView iconDeviceManager = (ImageView) convertView.findViewById(R.id.icon_device_manager);
                    iconDeviceManager.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //save Current Device Group to be used throughout Device Manager
                            ConnectManager connect = ConnectManager.getSharedInstance();
                            connect.saveCurrentDeviceGroup(deviceGroups.get(position));
                            connect.saveAllDevices(allDevices);
                            Intent intent = new Intent(NavigationWheel.this, DeviceManagerList.class);
                            startActivity(intent);
                        }
                    });

                    //final ImageView activeGroupButton2 = (ImageView)convertView.findViewById(R.id.settings__active_img2);
                    //final ImageView inactiveGroupButton2 = (ImageView)convertView.findViewById(R.id.settings_inactive_img2);
                    //final TextView activeClicker2 = (TextView)convertView.findViewById(R.id.active_settings_clicker2);
                    //final TextView inactiveClicker2 = (TextView)convertView.findViewById(R.id.inactive_settings_clicker2);
                    //final ImageView playImage2 = (ImageView) convertView.findViewById(R.id.play_img2);

                    /*playImage2.setImageResource(R.drawable.icon_play);
                    searchImage2.setImageResource(R.drawable.icon_locate_active);

                    inactiveClicker2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            activeGroupButton2.setVisibility(View.VISIBLE);
                            activeClicker2.setVisibility(View.VISIBLE);
                            inactiveClicker2.setVisibility(View.GONE);
                            inactiveGroupButton2.setVisibility(View.GONE);
                            playImage2.setVisibility(View.VISIBLE);
                            searchImage2.setVisibility(View.VISIBLE);
                        }
                    });

                    activeClicker2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            inactiveGroupButton2.setVisibility(View.VISIBLE);
                            inactiveClicker2.setVisibility(View.VISIBLE);
                            activeGroupButton2.setVisibility(View.GONE);
                            activeClicker2.setVisibility(View.GONE);
                            playImage2.setVisibility(View.GONE);
                            searchImage2.setVisibility(View.GONE);
                            playImage2.setSelected(true);

                        }
                    });

                    playImage2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setGroupDefault();
                            playImage2.setVisibility(View.VISIBLE);
                            searchImage2.setVisibility(View.VISIBLE);
                            activeClicker2.setVisibility(View.VISIBLE);
                            inactiveClicker2.setVisibility(View.INVISIBLE);
                            activeGroupButton2.setVisibility(View.VISIBLE);
                            inactiveGroupButton2.setVisibility(View.INVISIBLE);

                            playImage2.setVisibility(View.VISIBLE);
                            playImage2.setImageResource(R.drawable.icon_stop);
                            searchImage2.setVisibility(View.VISIBLE);
                            searchImage2.setImageResource(R.drawable.icon_locate_inactive);

                            final Calendar now = Calendar.getInstance();
                            now.set(Calendar.HOUR_OF_DAY,24);
                            now.set(Calendar.MINUTE, 0);

                            GroupPlayTask currentTask = new GroupPlayTask(tPosition);

                            if (!isPlaying){
                                //new GroupPlayTask(tPosition).execute();
                                currentTask.execute();
                                schedulePlaying = true;
                                scheduleTimer = new CountDownTimer(30500,18) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                        SimpleDateFormat formatter;
                                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                            formatter = new SimpleDateFormat("h:mm aa");
                                        }else{
                                            formatter = new SimpleDateFormat("k:mm");
                                        }
                                        currentTime = formatter.format(now.getTime());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (deviceCountView2 != null){
                                                    deviceCountView2.setText(currentTime);
                                                }
                                            }
                                        });
                                        if (!ensureStop){
                                            now.add(Calendar.MINUTE, 1);
                                        }
                                        if (currentTime.equalsIgnoreCase("11:59 PM")){
                                            ensureStop = true;
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        if (deviceCountView2 != null){
                                            deviceCountView2.setText(saveCount);
                                        }
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try{
                                                    ConnectManager connectESL = ConnectManager.getSharedInstance();
                                                    connectESL.stopPCControl(0);
                                                }catch(Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                        playImage2.setImageResource(R.drawable.icon_play);
                                        searchImage2.setImageResource(R.drawable.icon_locate_active);
                                        playImage2.setSelected(true);
                                        ensureStop = false;
                                        isPlaying = false;
                                    }
                                };
                                scheduleTimer.start();
                                isPlaying = true;
                            }else{
                                if (scheduleTimer != null){
                                    scheduleTimer.cancel();
                                    scheduleTimer.onFinish();
                                }
                                if (searchHit){
                                    //playImage2.setImageResource(R.drawable.icon_stop);
                                    //searchImage2.setImageResource(R.drawable.icon_locate_inactive);
                                    //new GroupPlayTask(tPosition).execute();
                                    currentTask.execute();
                                    schedulePlaying = true;
                                    scheduleTimer = new CountDownTimer(30500,18) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            currentTime = formatter.format(now.getTime());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (deviceCountView2 != null){
                                                        deviceCountView2.setText(currentTime);
                                                    }
                                                }
                                            });
                                            if (!ensureStop){
                                                now.add(Calendar.MINUTE, 1);
                                            }
                                            if (currentTime.equalsIgnoreCase("11:59 PM")){
                                                ensureStop = true;
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            if (deviceCountView2 != null){
                                                deviceCountView2.setText(saveCount);
                                            }
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try{
                                                        ConnectManager connectESL = ConnectManager.getSharedInstance();
                                                        connectESL.stopPCControl(0);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                            playImage2.setImageResource(R.drawable.icon_play);
                                            searchImage2.setImageResource(R.drawable.icon_locate_active);
                                            playImage2.setSelected(true);
                                            ensureStop = false;
                                            isPlaying = false;
                                        }
                                    };
                                }
                                if (targetID != previousID){
                                    //Stop previous timer
                                    previousPlayTask.cancel(true);
                                    //new GroupPlayTask(tPosition).execute();
                                    currentTask = new GroupPlayTask(tPosition);
                                    currentTask.execute();
                                    schedulePlaying = true;
                                    //create new timer for 2nd option
                                    scheduleTimer = new CountDownTimer(30500,18) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {

                                            SimpleDateFormat formatter;
                                            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                                formatter = new SimpleDateFormat("h:mm aa");
                                            }else{
                                                formatter = new SimpleDateFormat("k:mm");
                                            }
                                            currentTime = formatter.format(now.getTime());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (deviceCountView2 != null){
                                                        deviceCountView2.setText(currentTime);
                                                    }
                                                }
                                            });
                                            if (!ensureStop){
                                                now.add(Calendar.MINUTE, 1);
                                            }
                                            if (currentTime.equalsIgnoreCase("11:59 PM")){
                                                ensureStop = true;
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            if(deviceCountView2 != null){
                                                deviceCountView2.setText(saveCount);
                                            }
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try{
                                                        ConnectManager connectESL = ConnectManager.getSharedInstance();
                                                        connectESL.stopPCControl(0);
                                                    }catch(Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();
                                            playImage2.setImageResource(R.drawable.icon_play);
                                            searchImage2.setImageResource(R.drawable.icon_locate_active);
                                            playImage2.setSelected(true);
                                            ensureStop = false;
                                            isPlaying = false;
                                        }
                                    };
                                    scheduleTimer.start();
                                    isPlaying = true;
                                    searchHit = false;
                                }
                            }
                            previousID = targetID;
                            previousPlayTask = currentTask;
                        }
                    });

                    searchImage2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchHit = true;
                            if (scheduleTimer != null){
                                scheduleTimer.cancel();
                                scheduleTimer.onFinish();
                            }
                            playImage2.setImageResource(R.drawable.icon_play);
                            searchImage2.setImageResource(R.drawable.icon_locate_active);
                            playImage2.setSelected(true);
                            if (deviceCountView2 != null){
                                deviceCountView2.setText(saveCount);
                            }
                            new IdentifyTask(targetID).execute();
                        }
                    });*/

                    break;
            }
            return convertView;
        }
    }

    public void initButtonNavigation(){
        //buttons
        final Button kelvinButton = (Button) findViewById(R.id.kelvin_button);
        final Button rgbButton = (Button) findViewById(R.id.rgb_button);
        final Button sliderButton = (Button) findViewById(R.id.slider_button);
        final ImageView presetButtonView = (ImageView)findViewById(R.id.save_preset_button);
        final ImageView backToScheduleButton = (ImageView) findViewById(R.id.back_to_schedule_button);
        final ImageView dropcamBackground = (ImageView)findViewById(R.id.dropcam_background);
        final RelativeLayout newAquarium = (RelativeLayout)findViewById(R.id.my_aquariums_header);

        newAquarium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SaveNewAquarium.class);
                startActivityForResult(intent, 1);
            }
        });

        kelvinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorFlip.setDisplayedChild(0);
                //stop the timer if it's possibly running
                if (updateColorTimer != null){
                    updateColorTimer.cancel();
                }
                //stop pulse timer
                if (updatePulseTimer != null){
                    updatePulseTimer.cancel();
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
                //stop pulse timer
                if (updatePulseTimer != null){
                    updatePulseTimer.cancel();
                }
                ImageView amountBubble = (ImageView) findViewById(R.id.color_wheel_bubble);
                //make bubble visible
                amountBubble.setVisibility(View.VISIBLE);
                circleDetail.setVisibility(View.VISIBLE);

                //Handle the switching of the 2nd bar
                if (barBright2 == null){
                    LayoutInflater myBar2 = LayoutInflater.from(NavigationWheel.this);
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
                //stop pulse timer
                if (updatePulseTimer != null){
                    updatePulseTimer.cancel();
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

        backToScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (isLDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("LDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }
                if (isMDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("MDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }
                if (isTVDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("TVDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }
                if (isHDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("HDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }
                if (isXHDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("XHDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }
                if (isXXHDPI()){
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    String str_ScreenSize = "The Android Screen is: "
                            + dm.widthPixels
                            + " x "
                            + dm.heightPixels
                            + " with density of "
                            + dm.density
                            + " with DPI of "
                            + dm.densityDpi
                            + " with Scaled Density of "
                            + dm.scaledDensity;
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                    myNewAlert.setMessage(str_ScreenSize);
                    myNewAlert.setTitle("XXHDPI Device");
                    myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    myNewAlert.show();
                }*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectManager connect = ConnectManager.getSharedInstance();
                        connect.stopPCControl(0);
                    }
                }).start();
                Toast.makeText(NavigationWheel.this, getResources().getString(R.string.schedule) + " Mode On.", Toast.LENGTH_SHORT).show();
                //setContentView(R.layout.schedule_main_layout);
            }
        });

        SharedPreferences sp = getSharedPreferences("USER_PREF",0);
        String dropCamID = sp.getString("dropcamID", null);
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

    /*public void setGroupDefault(){

        final ImageView activeGroupButton = (ImageView)findViewById(R.id.settings__active_img);
        final ImageView inactiveGroupButton = (ImageView)findViewById(R.id.settings_inactive_img);
        final ImageView playImage = (ImageView) findViewById(R.id.play_img);
        final ImageView searchImage = (ImageView) findViewById(R.id.search_img);
        final TextView activeClicker = (TextView) findViewById(R.id.active_settings_clicker);
        final TextView inactiveClicker = (TextView) findViewById(R.id.inactive_settings_clicker);

        if(playImage != null){
            playImage.setVisibility(View.INVISIBLE);
        }
        if (searchImage != null){
            searchImage.setVisibility(View.INVISIBLE);
        }
        if (activeGroupButton != null){
            activeGroupButton.setVisibility(View.INVISIBLE);
        }
        if (inactiveGroupButton != null){
            inactiveGroupButton.setVisibility(View.VISIBLE);
        }
        if (activeClicker != null){
            activeClicker.setVisibility(View.INVISIBLE);
        }
        if (inactiveClicker != null){
            inactiveClicker.setVisibility(View.VISIBLE);
        }

        for(View item: groupViews){

            //Group buttons are the actual image, the clicker is a field that can be clicked by user that controls button
            final ImageView activeGroupButton2 = (ImageView)item.findViewById(R.id.settings__active_img2);
            final ImageView inactiveGroupButton2 = (ImageView)item.findViewById(R.id.settings_inactive_img2);
            final ImageView playImage2 = (ImageView) item.findViewById(R.id.play_img2);
            final ImageView searchImage2 = (ImageView) item.findViewById(R.id.search_img2);
            final TextView activeClicker2 = (TextView) item.findViewById(R.id.active_settings_clicker2);
            final TextView inactiveClicker2 = (TextView) item.findViewById(R.id.inactive_settings_clicker2);

            playImage2.setVisibility(View.INVISIBLE);
            searchImage2.setVisibility(View.INVISIBLE);
            activeGroupButton2.setVisibility(View.INVISIBLE);
            inactiveGroupButton2.setVisibility(View.VISIBLE);
            activeClicker2.setVisibility(View.INVISIBLE);
            inactiveClicker2.setVisibility(View.VISIBLE);

            playImage2.setImageResource(R.drawable.icon_play);
            searchImage2.setImageResource(R.drawable.icon_locate_active);
            if (previousRadions != null && saveCount != null){
                previousRadions.setText(saveCount);
            }

            playImage2.setSelected(true);

        }
    }*/

    public void initMainMenu(){
        //initialize the array adapter
        final String dropCamID = sp.getString("dropcamID",null);
        mainMenuAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                int totalGroupCount = 3;
                //Hide Vortech if no Vortech exists
                if (currentGroupPumpCount == 0){
                    totalGroupCount --;
                }

                //Hide Others Menu if there is no dropcam
                if (dropCamID == null){
                    totalGroupCount--;
                }
                return totalGroupCount;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                switch (groupPosition){
                    case 0:
                        return 5;
                    case 1:
                        if (currentGroupPumpCount == 0){
                            return 1;
                        }else{
                            return 2;
                        }
                    default:
                        if (dropCamID == null){
                            return 0;
                        }else{
                            return 1;
                        }
                }
            }

            @Override
            public Object getGroup(int groupPosition) {
                return null;
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return null;
            }

            @Override
            public long getGroupId(int groupPosition) {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                LayoutInflater inflater = NavigationWheel.this.getLayoutInflater();
                convertView = inflater.inflate(R.layout.main_menu_header, parent, false);
                TextView header = (TextView) convertView.findViewById(R.id.main_menu_header);
                switch (groupPosition){
                    case 0:
                        header.setText(R.string.radion);
                        if (Build.VERSION.SDK_INT < 14){
                            header.setText(header.getText().toString().toUpperCase());
                        }
                        break;
                    case 1:
                        //Handle situation where there could be either dropcam or vortech for this case
                        if (allPumps.size() == 0){
                            header.setText(R.string.other);
                            if (Build.VERSION.SDK_INT < 14){
                                header.setText(header.getText().toString().toUpperCase());
                            }
                        }else{
                            header.setText(R.string.vortech);
                            if (Build.VERSION.SDK_INT < 14){
                                header.setText(header.getText().toString().toUpperCase());
                            }
                        }
                        break;
                    default:
                        header.setText(R.string.other);
                        if (Build.VERSION.SDK_INT < 14){
                            header.setText(header.getText().toString().toUpperCase());
                        }
                        break;
                }
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                // Inflate the layout in each row.
                LayoutInflater inflater = NavigationWheel.this.getLayoutInflater();
                // Declare and define the TextView, "item." This is where
                // the name of each item will appear.
                View row = inflater.inflate(R.layout.main_menu_item, parent, false);
                // Declare and define the TextView, "icon." This is where
                // the icon in each row will appear.
                ImageView icon=(ImageView)row.findViewById(R.id.imgListIcon);
                TextView item = (TextView)row.findViewById(R.id.txtListText);

                if (childPosition == 3 && groupPosition == 0){
                    if (onAllAquariums){
                        //Gray out schedule main menu item and make it not clickable
                        if (Build.VERSION.SDK_INT < 11){
                            AlphaAnimation alpha = new AlphaAnimation(0.25F, 0.25F);
                            alpha.setDuration(0); // Make animation instant
                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                            icon.startAnimation(alpha);
                        }else{
                            icon.setAlpha(0.25f);
                        }
                        int color = item.getCurrentTextColor();
                        item.setTextColor(Color.argb(62, Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }

                switch (groupPosition){
                    case 0:
                        item.setText(menuPresets[childPosition]);
                        icon.setImageResource(menuIcons[childPosition]);
                        break;
                    case 1:
                        //Handle situation where it could be dropcam or vortech for the 2nd group menu item
                        if (currentGroupPumpCount == 0){
                            item.setText(R.string.dropcam);
                            icon.setImageResource(R.drawable.icon_video);
                        }else{
                            if (childPosition == 0){
                                item.setText(R.string.live_demo);
                                icon.setImageResource(R.drawable.icon_pump);
                            }else{
                                item.setText(R.string.presets);
                                icon.setImageResource(R.drawable.icon_star);
                            }
                        }
                        break;
                    default:
                        item.setText(R.string.dropcam);
                        icon.setImageResource(R.drawable.icon_video);
                        break;
                }
                return row;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };
        mainMenuList.setGroupIndicator(null);
        mainMenuList.setChildIndicator(null);
        mainMenuList.setDividerHeight(1);
        mainMenuList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        mainMenuList.setAdapter(mainMenuAdapter);
        for (int i = 0; i < mainMenuAdapter.getGroupCount(); i++){
            mainMenuList.expandGroup(i);
        }

        mainMenuList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //String selectedItem = mainMenuAdapter.getChil

                //handle stopping of dropcam and orientation boolean
                if (groupPosition == 0 && childPosition == 3 & onAllAquariums){
                    //nothing
                }else{
                    if (dropTimer!= null){
                        dropTimer.cancel();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (selectedGroupName != null){
                        viewTitle.setText(selectedGroupName);
                    }

                    switch (groupPosition){
                        case 0:
                            switch (childPosition) {
                                case 0:
                                    //Handle Color Channels Selection
                                    dropcamToggle = 0;
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);
                                    hideColorElements();
                                    screenFlip.setDisplayedChild(0);
                                    currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                                    colorFlip.setVisibility(View.VISIBLE);
                                    colorFlip.getChildAt(currentChild).setVisibility(View.VISIBLE);
                                    onPumpCircle = false;
                                    navWheel.closeDrawers();
                                    break;
                                case 1:
                                    //Handle Presets List Selection
                                    //do not call finish so that you can return to the slider with the back button
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);
                                    if (Build.VERSION.SDK_INT < 11) {
                                        for (int i = 0; i < exAdapter.getGroupCount(); i++){
                                            presetList.expandGroup(i);
                                        }
                                    }
                                    if (isDirty) {
                                        systemPresets.clear();
                                        userPresets.clear();
                                        new RadionPresetsTask().execute();
                                    } else {
                                        screenFlip.setDisplayedChild(1);
                                        initRadionPresetsView();
                                        currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                                        hideColorElements();
                                        navWheel.closeDrawers();
                                    }
                                    break;
                                case 2:
                                    //Handle Preview Modes Selection
                                    //do not call finish so that you can return to the slider with the back button
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);
                                    //intent = new Intent(getApplicationContext(), PreviewModeView.class);
                                    //startActivity(intent);
                                    screenFlip.setDisplayedChild(2);
                                    currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                                    hideColorElements();
                                    navWheel.closeDrawers();
                                    break;
                                case 3:
                                    //Handle Schedule Selection
                                    //do not call finish so that you can return to the slider with the back button
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);
                                    screenFlip.setDisplayedChild(3);
                                    hideColorElements();
                                    navWheel.closeDrawers();
                                    onDropcamView = false;

                                    //Actions must be paired
                                    RelativeLayout schedulePlaybackOverlay = (RelativeLayout) findViewById(R.id.schedule_playback_overlay);
                                    schedulePlaybackOverlay.setVisibility(View.GONE);
                                    if (scheduleFirstClick || groupSwitched) {
                                        Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                                        startActivity(intent);
                                        new LoadScheduleDataTask().execute();
                                    }

                                    //Listen for the orientation to change and display graph in landscape for schedule view only
                                /*setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                orientationChange = new OrientationEventListener(NavigationWheel.this) {
                                    @Override
                                    public void onOrientationChanged(int orientation) {
                                        if (!onDropcamView){
                                            Configuration config = getResources().getConfiguration();
                                            if (config.orientation == Configuration.ORIENTATION_qaLANDSCAPE){
                                                Intent intent = new Intent(NavigationWheel.this, ScheduleGraphView.class);
                                                startActivity(intent);
                                            }
                                        }

                                    }
                                };
                                orientationChange.enable();*/
                                    break;
                                case 4:
                                    //Handle Settings Selection
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);
                                    screenFlip.setDisplayedChild(4);
                                    hideColorElements();
                                    navWheel.closeDrawers();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case 1:
                            switch (childPosition){
                                case 0:
                                    if (currentGroupPumpCount == 0){
                                        //Handle Dropcam Selection
                                        //do not call finish so that you can return to the slider with the back button
                                        //Log.i("MENU ITEM SELECTED:", selectedItem);
                                        screenFlip.setDisplayedChild(5);
                                        hideColorElements();
                                        navWheel.closeDrawers();
                                        viewTitle.setText(R.string.dropcam);
                                        initDropCam();
                                    }else{
                                        //default to Constant Speed on start of Live Demo
                                        pumpIndex = 1;
                                        allPumpsSelected = true;
                                        hideSliderUIforPulse();

                                        TextView pulseName = (TextView) findViewById(R.id.pulse_name);
                                        final ImageView pulseInfo = (ImageView) findViewById(R.id.pulse_info);
                                        new GetPumpModesTask().execute();
                                        pulseName.setText(R.string.constant_speed);
                                        pulseInfo.setVisibility(View.INVISIBLE);
                                        if (pumpOutline != null){
                                            pumpOutline.invalidate();
                                        }

                                        onPumpCircle = true;
                                        //Get the pump mode for the default pulse which is Constant Speed
                                        new GetPumpModesTask().execute();
                                        screenFlip.setDisplayedChild(6);
                                        hideColorElements();
                                        navWheel.closeDrawers();

                                        final RelativeLayout pumpLayout = (RelativeLayout) findViewById(R.id.pump_live_demo);
                                        ImageView pumpBackground = (ImageView) findViewById(R.id.pump_background);
                                        if (Build.VERSION.SDK_INT < 11){
                                            AlphaAnimation alpha = new AlphaAnimation(0.8F, 0.8F);
                                            alpha.setDuration(0); // Make animation instant
                                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                            pumpBackground.startAnimation(alpha);
                                        }else{
                                            pumpBackground.setAlpha(0.8f);
                                        }

                                        final ImageView pumpScheduleIcon = (ImageView) findViewById(R.id.pump_schedule_button);
                                        pumpScheduleIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new StopPCControlForPumpTask().execute();
                                                Toast.makeText(NavigationWheel.this, getResources().getString(R.string.schedule) + " Mode On.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        final TextView feedModeIcon = (TextView) findViewById(R.id.feed_mode_icon);
                                        feedModeIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                currentPulse.pumpModeId = 13;
                                                new SetPumpToModeTask().execute();
                                            }
                                        });

                                        final ImageView pumpPresetIcon = (ImageView) findViewById(R.id.pump_save_preset_button);
                                        pumpPresetIcon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(NavigationWheel.this, SavePresetView.class);
                                                intent.putExtra("onPumpSave", true);
                                                intent.putExtra("currentPulseID", currentPulse.pumpModeId);
                                                startActivity(intent);
                                            }
                                        });

                                        pulseInfo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (pumpIndex > 0 && pumpIndex < 8){
                                                    Intent intent = new Intent(NavigationWheel.this, PulseInfoOverlay.class);
                                                    intent.putExtra("pumpIndex", pumpIndex);
                                                    startActivity(intent);
                                                }
                                            }
                                        });

                                        //Must use this to get the exact dimensions for the relative layout's center
                                        pumpLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                            @Override
                                            public void onGlobalLayout() {
                                                pumpLayoutCenterX = pumpLayout.getWidth() / 2;
                                                pumpLayoutCenterY = pumpLayout.getHeight() / 2;
                                                if (pumpLayoutCount < 1){
                                                    pumpOutline = new PumpCircleColor(NavigationWheel.this, pumpLayoutCenterX, pumpLayoutCenterY, thumbTrackRadius);
                                                    pumpLayout.addView(pumpOutline);
                                                    pumpLayoutCount++;
                                                }
                                            }
                                        });

                                        pumpLayout.setOnTouchListener(new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                //Center point used for both circles
                                                Point pumpCircleCenterPoint = new Point();

                                                //circle logic for pumps
                                                final ImageView pumpCircle = (ImageView) findViewById(R.id.pump_circle);

                                                //handle the centerpoint for different screen resolutions
                                                int pumpCircleWidth= pumpCircle.getWidth();
                                                int pumpRadius = pumpCircleWidth / 2;

                                                System.out.println("Width of Pump Circle = " + pumpCircleWidth);
                                                System.out.println("Pump Circle Radius from Width = " + pumpCircleWidth / 2);
                                                System.out.println("Pump Layout Height = " + pumpLayoutCenterX);
                                                System.out.println("Pump Layout Width = " + pumpLayoutCenterY);

                                                //set center point for all circles
                                                pumpCircleCenterPoint.set(pumpLayoutCenterX, pumpLayoutCenterY);

                                                //set the value of the radii for the touch and drawing of circles
                                                pumpThumbTrackRadius = (float)pumpRadius - (55 * metrics.density);   //Center of drawn gray circle (I use this as a source for the outer and inner radius)
                                                pumpInnerRadius = pumpThumbTrackRadius - 50;   //inner outline of touch circle
                                                pumpOuterRadius = pumpThumbTrackRadius + 80;  //outline of touch circle

                                                System.out.println("Pump Outer Radius = " + pumpOuterRadius);
                                                System.out.println("Pump Inner Radius = " + pumpInnerRadius);
                                                System.out.println("Pump Thumb Track Radius = " + pumpThumbTrackRadius);

                                                //Begin Touch Logic
                                                int action = event.getAction();
                                                switch(action){
                                                    case MotionEvent.ACTION_DOWN:
                                                        if (currentPulse.pumpModeId == 13){
                                                            currentPulse.pumpModeId = pumpIndex;
                                                        }
                                                        // Convert pixels from MouseEvent to dpi
                                                        float px = event.getX();
                                                        float py = event.getY();

                                                        Log.i("Pump Touch Spot: ", px + ", " + py);

                                                        int x = (int)px;
                                                        int y = (int)py;

                                                        double t = Math.pow((double)(x - pumpCircleCenterPoint.x), 2.0f) + Math.pow((double)(y - pumpCircleCenterPoint.y), 2.0f);
                                                        boolean isOnColorTrack = (t < Math.pow((double)pumpOuterRadius, 2.0f) && t > Math.pow((double)pumpInnerRadius, 2.0f));
                                                        boolean isWithinGrayCircle = (t < Math.pow((double)pumpInnerRadius, 2.0f));

                                                        if (isOnColorTrack){
                                                            isPumpCircleDragging = true;
                                                            pumpDegrees = (float) Math.toDegrees(Math.atan2(x - pumpCircleCenterPoint.x, pumpCircleCenterPoint.y - y));
                                                            if(pumpDegrees < 0.0){
                                                                pumpDegrees += 360.0f;
                                                            }
                                                            //save the previous degrees for the Kelvin wheel AFTER you handle the negative values
                                                            pumpPreviousDegrees = pumpDegrees;

                                                            //create timer on down touch that starts and updates the degrees once every second (AFTER negative values are handled)
                                                            updatePulseTimer = new CountDownTimer(60000,1000){
                                                                @Override
                                                                public void onTick(long millisUntilFinished) {
                                                                    setPulseParams();
                                                                    new SetPumpToModeTask().execute();
                                                                }

                                                                @Override
                                                                public void onFinish() {

                                                                }
                                                            }.start();

                                                            //manage shifting of degrees for drawing of the circle
                                                            float shiftedDegrees = pumpDegrees - 90.0f;
                                                            if(shiftedDegrees < 0.0f){
                                                                shiftedDegrees = 360.0f + shiftedDegrees;
                                                            }

                                                            //draw the circle based off of the density of the different phones
                                                            double thumbX = pumpLayoutCenterX + ((double)pumpThumbTrackRadius * Math.cos(Math.PI * (double)shiftedDegrees/180.0f));
                                                            double thumbY = pumpLayoutCenterY + ((double)pumpThumbTrackRadius * Math.sin(Math.PI * (double)shiftedDegrees/180.0f));

                                                            //save the thumb values so that you can set the position of the circle when you return to this view
                                                            pumpPreviousX = (int)thumbX;
                                                            pumpPreviousY = (int)thumbY;

                                                            if (pumpCircleCount < 1){
                                                                pumpDrawCircle = new myView(NavigationWheel.this, thumbX, thumbY);
                                                                pumpLayout.addView(pumpDrawCircle);
                                                                pumpDrawCircle.bringToFront();
                                                                pumpCircleCount++;
                                                            }else{
                                                                if (pumpDrawCircle != null){
                                                                    //change the value of the drawcircle's x and y if the circle has already been drawn
                                                                    pumpDrawCircle.thumbX = thumbX;
                                                                    pumpDrawCircle.thumbY = thumbY;
                                                                    pumpDrawCircle.invalidate();
                                                                }
                                                            }

                                                            updatePumpDetail();
                                                        }

                                                        if (isWithinGrayCircle && !isPumpCircleDragging){
                                                            //change color of header
                                                            TextView pumpModeHeader = (TextView)findViewById(R.id.mode_header);
                                                            pumpModeHeader.setTextColor(Color.parseColor("#FFFFFFFF"));

                                                            //change opacity of the circle
                                                            if (Build.VERSION.SDK_INT < 11){
                                                                AlphaAnimation alpha = new AlphaAnimation(0.92F, 0.92F);
                                                                alpha.setDuration(0); // Make animation instant
                                                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                                                pumpCircle.startAnimation(alpha);
                                                            }else{
                                                                pumpCircle.setAlpha(0.95f);
                                                            }
                                                        }
                                                        break;
                                                    case MotionEvent.ACTION_MOVE:
                                                        pumpCircleIsBig = true;

                                                        // Convert pixels from MouseEvent to dpi
                                                        px = event.getX();
                                                        py = event.getY();

                                                        x = (int)px;
                                                        y = (int)py;

                                                        t = Math.pow((x - pumpCircleCenterPoint.x), 2.0) + Math.pow((y - pumpCircleCenterPoint.y), 2.0);
                                                        isOnColorTrack = (t < Math.pow((double)pumpOuterRadius, 2.0) && t > Math.pow((double)pumpInnerRadius, 2.0));
                                                        isWithinGrayCircle = (t < Math.pow((double)pumpInnerRadius, 2.0));

                                                        if (isOnColorTrack){
                                                            pumpDegrees = (float) Math.toDegrees(Math.atan2(x - pumpCircleCenterPoint.x, pumpCircleCenterPoint.y - y));

                                                            if(pumpDegrees < 0.0){
                                                                pumpDegrees += 360.0f;
                                                            }
                                                            pumpPreviousDegrees = pumpDegrees;

                                                            float shiftedDegrees = pumpDegrees - 90.0f;
                                                            if(shiftedDegrees < 0.0f){
                                                                shiftedDegrees = 360.0f + shiftedDegrees;
                                                            }

                                                            //draw the circle based off of the density of the different phones
                                                            double thumbX = pumpLayoutCenterX + ((double)pumpThumbTrackRadius * Math.cos(Math.PI * (double)shiftedDegrees/180.0f));
                                                            double thumbY = pumpLayoutCenterY + ((double)pumpThumbTrackRadius * Math.sin(Math.PI * (double)shiftedDegrees/180.0f));

                                                            //drawCircle = new myView(NavigationWheel.this, thumbX, thumbY);
                                                            //call this to redraw the circle
                                                            if (pumpDrawCircle != null){
                                                                pumpDrawCircle.thumbX = thumbX;
                                                                pumpDrawCircle.thumbY = thumbY;
                                                            }

                                                            //change opacity of circle
                                                            if (Build.VERSION.SDK_INT < 11){
                                                                AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                                                                alpha.setDuration(0); // Make animation instant
                                                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                                                pumpCircle.startAnimation(alpha);
                                                            }else{
                                                                pumpCircle.setAlpha(1f);
                                                            }

                                                            updatePumpDetail();
                                                        }

                                                        //If you are moving & you are suddenly off the color track, resize the circle
                                                        if (!isOnColorTrack){
                                                            pumpCircleIsBig = false;
                                                        }
                                                        //make sure the circle always redraws
                                                        if (pumpDrawCircle != null){
                                                            pumpDrawCircle.invalidate();
                                                        }

                                                        if (isWithinGrayCircle && !isPumpCircleDragging){
                                                            float newY = event.getY();
                                                        }
                                                        break;
                                                    case MotionEvent.ACTION_UP:
                                                        pumpCircleIsBig = false;

                                                        if (updatePulseTimer != null){
                                                            updatePulseTimer.cancel();
                                                        }

                                                        // Convert pixels from MouseEvent to dpi
                                                        px = event.getX();
                                                        py = event.getY();

                                                        x = (int)px;
                                                        y = (int)py;

                                                        t = Math.pow((x - pumpCircleCenterPoint.x), 2.0) + Math.pow((y - pumpCircleCenterPoint.y), 2.0);
                                                        isOnColorTrack = (t < Math.pow((double)pumpOuterRadius, 2.0) && t > Math.pow((double)pumpInnerRadius, 2.0));
                                                        isWithinGrayCircle = (t < Math.pow((double)pumpInnerRadius, 2.0));

                                                        if (isOnColorTrack){
                                                            pumpDegrees = (float) Math.toDegrees(Math.atan2(x - pumpCircleCenterPoint.x, pumpCircleCenterPoint.y - y));

                                                            if(pumpDegrees < 0.0){
                                                                pumpDegrees += 360.0f;
                                                            }
                                                            pumpDegrees = pumpPreviousDegrees;

                                                            float shiftedDegrees = pumpDegrees - 90.0f;
                                                            if(shiftedDegrees < 0.0f){
                                                                shiftedDegrees = 360.0f + shiftedDegrees;
                                                            }

                                                            //draw the circle based off of the density of the different phones
                                                            double thumbX = pumpLayoutCenterX + ((double)pumpThumbTrackRadius * Math.cos(Math.PI * (double)shiftedDegrees/180.0f));
                                                            double thumbY = pumpLayoutCenterY + ((double)pumpThumbTrackRadius * Math.sin(Math.PI * (double)shiftedDegrees/180.0f));

                                                            if (pumpDrawCircle != null){
                                                                pumpDrawCircle.invalidate();
                                                            }
                                                            pumpPreviousX = (int)thumbX;
                                                            pumpPreviousY = (int)thumbY;

                                                            setPulseParams();
                                                            new SetPumpToModeTask().execute();
                                                        }

                                                        if (isWithinGrayCircle && !isPumpCircleDragging){
                                                            TextView pulseName = (TextView) findViewById(R.id.pulse_name);
                                                            //increment the index on every touch
                                                            pumpIndex++;
                                                            if (pumpIndex == 8){
                                                                //do this to skip.
                                                                pumpIndex = 11;
                                                            }
                                                            //If the index grows beyond expanding pulse, reset it to 1 for Constant Speed
                                                            if (pumpIndex > 11){
                                                                //reset the pumpIndex to 1 if the index goes beyond expanding pulse
                                                                pumpIndex = 1;
                                                            }
                                                            System.out.println("Pump Index = " + pumpIndex);
                                                            new GetPumpModesTask().execute();

                                                            //condition when the user touches the inside of the circle
                                                            switch (pumpIndex){
                                                                case 1:
                                                                    pulseName.setText(R.string.constant_speed);
                                                                    pulseInfo.setVisibility(View.INVISIBLE);
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                                case 2:
                                                                    pulseName.setText(R.string.short_pulse);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    showSliderUIforPulse();
                                                                    setPulseSliderForShortPulse();
                                                                    break;
                                                                case 3:
                                                                    pulseName.setText(R.string.long_pulse);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    showSliderUIforPulse();
                                                                    setPulseSliderForLongPulse();
                                                                    break;
                                                                case 4:
                                                                    pulseName.setText(R.string.reefcrest);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                                case 5:
                                                                    pulseName.setText(R.string.lagoon);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    pumpOutline.invalidate();
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                                case 6:
                                                                    pulseName.setText(R.string.nutrient_transport);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                                case 7:
                                                                    pulseName.setText(R.string.tidal_swell);
                                                                    pulseInfo.setVisibility(View.VISIBLE);
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                                default:
                                                                    pulseName.setText(R.string.expanding_pulse);
                                                                    pulseInfo.setVisibility(View.INVISIBLE);
                                                                    hideSliderUIforPulse();
                                                                    break;
                                                            }
                                                            pumpOutline.invalidate();

                                                            //change color of header
                                                            TextView pumpModeHeader = (TextView)findViewById(R.id.mode_header);
                                                            pumpModeHeader.setTextColor(Color.parseColor("#FF555555"));

                                                            //change opacity of circle
                                                            if (Build.VERSION.SDK_INT < 11){
                                                                AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                                                                alpha.setDuration(0); // Make animation instant
                                                                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                                                                pumpCircle.startAnimation(alpha);
                                                            }else{
                                                                pumpCircle.setAlpha(1f);
                                                            }
                                                        }

                                                        //Reset the boolean and make sure drag mode gets turn off
                                                        isPumpCircleDragging = false;

                                                        break;
                                                    default:
                                                        break;
                                                }
                                                return true;
                                            }
                                        });
                                    }
                                    break;
                                default:
                                    //Handle Pump Presets List Selection
                                    //do not call finish so that you can return to the slider with the back button
                                    //Log.i("MENU ITEM SELECTED:", selectedItem);

                                    if (Build.VERSION.SDK_INT < 11) {
                                        //code to expand all preset groups
                                        for (int i = 0; i < exAdapter.getGroupCount(); i++){
                                            presetList.expandGroup(i);
                                        }
                                    }

                                    if (globalConnect.pumpSystemPresets != null){
                                        globalConnect.pumpSystemPresets.clear();
                                    }
                                    if (globalConnect.pumpUserPresets != null){
                                        globalConnect.pumpUserPresets.clear();
                                    }
                                    presetList.setVisibility(View.INVISIBLE);
                                    presetLoading.setVisibility(View.VISIBLE);
                                    new LoadPumpPresetsTask().execute();

                                    if (isDirty) {
                                        if (globalConnect.pumpSystemPresets != null){
                                            globalConnect.pumpSystemPresets.clear();
                                        }
                                        if (globalConnect.pumpUserPresets != null){
                                            globalConnect.pumpUserPresets.clear();
                                        }
                                        presetList.setVisibility(View.INVISIBLE);
                                        presetLoading.setVisibility(View.VISIBLE);
                                        new LoadPumpPresetsTask().execute();
                                    } else {
                                        screenFlip.setDisplayedChild(1);
                                        currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                                        hideColorElements();
                                        navWheel.closeDrawers();
                                    }
                                    break;
                            }
                            break;
                        default:
                            //Handle Dropcam Selection
                            //do not call finish so that you can return to the slider with the back button
                            //Log.i("MENU ITEM SELECTED:", selectedItem);
                            screenFlip.setDisplayedChild(5);
                            hideColorElements();
                            navWheel.closeDrawers();
                            viewTitle.setText(R.string.dropcam);
                            initDropCam();
                            break;
                    }
                    stopMedia();
                }

                return true;
            }
        });

        /*presetsAdapter = new CustomAdapter(this, R.layout.main_menu_item, menuPresets);
        mainMenuList.setAdapter(presetsAdapter);
        mainMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                String selectedItem = presetsAdapter.getItem(position);

                //handle stopping of dropcam and orientation boolean
                if (dropTimer!= null){
                    dropTimer.cancel();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                if (selectedGroupName != null){
                    viewTitle.setText(selectedGroupName);
                }
                switch (position) {
                    case 0:
                        //Handle Color Channels Selection
                        dropcamToggle = 0;
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        hideColorElements();
                        screenFlip.setDisplayedChild(0);
                        currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                        colorFlip.setVisibility(View.VISIBLE);
                        colorFlip.getChildAt(currentChild).setVisibility(View.VISIBLE);
                        navWheel.closeDrawers();
                        break;
                    case 1:
                        //Handle Presets List Selection
                        //do not call finish so that you can return to the slider with the back button
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        if (Build.VERSION.SDK_INT < 11){
                            presetList.expandGroup(0);
                            presetList.expandGroup(1);
                        }
                        if (isDirty){
                            systemPresets.clear();
                            userPresets.clear();
                            new RadionPresetsTask().execute();
                        }
                        else{
                            screenFlip.setDisplayedChild(1);
                            currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                            hideColorElements();
                            navWheel.closeDrawers();
                        }
                        break;
                    case 2:
                        //Handle Preview Modes Selection
                        //do not call finish so that you can return to the slider with the back button
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        //intent = new Intent(getApplicationContext(), PreviewModeView.class);
                        //startActivity(intent);
                        screenFlip.setDisplayedChild(2);
                        currentMainLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.new_wheel_bg));
                        hideColorElements();
                        navWheel.closeDrawers();
                        break;
                    case 3:
                        //Handle Schedule Selection
                        //do not call finish so that you can return to the slider with the back button
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        screenFlip.setDisplayedChild(3);
                        hideColorElements();
                        navWheel.closeDrawers();
                        onDropcamView = false;

                        //Actions must be paired
                        RelativeLayout schedulePlaybackOverlay = (RelativeLayout)findViewById(R.id.schedule_playback_overlay);
                        schedulePlaybackOverlay.setVisibility(View.GONE);
                        if (scheduleFirstClick || groupSwitched){
                            Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                            startActivity(intent);
                            new LoadScheduleDataTask().execute();
                        }

                        //Listen for the orientation to change and display graph in landscape for schedule view only
                        /*setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationChange = new OrientationEventListener(NavigationWheel.this) {
                            @Override
                            public void onOrientationChanged(int orientation) {
                                if (!onDropcamView){
                                    Configuration config = getResources().getConfiguration();
                                    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
                                        Intent intent = new Intent(NavigationWheel.this, ScheduleGraphView.class);
                                        startActivity(intent);
                                    }
                                }

                            }
                        };
                        orientationChange.enable();
                        break;
                    case 4:
                        //Handle Settings Selection
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        screenFlip.setDisplayedChild(4);
                        hideColorElements();
                        navWheel.closeDrawers();
                        break;
                    default:
                        //Handle Dropcam Selection
                        //do not call finish so that you can return to the slider with the back button
                        Log.i("MENU ITEM SELECTED:", selectedItem);
                        screenFlip.setDisplayedChild(5);
                        hideColorElements();
                        navWheel.closeDrawers();
                        viewTitle.setText(R.string.dropcam);
                        initDropCam();
                        break;
                }
                stopMedia();
            }
        });*/
    }

    public void initSettings(){
        settings_setter = new CustomSettingsAdapter(this,R.layout.settings_layout, settingsDataArray);
        settingsList = (ListView) findViewById(R.id.settings_list);
        settingsList.setAdapter(settings_setter);
        settingsList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        settingsList.setDivider(null);
        settingsList.setDividerHeight(0);
    }

    public void initDropCam(){
        //grab them shared preferences and dat dropcamID
        SharedPreferences sp = getSharedPreferences("USER_PREF",0);
        String dropCamID = sp.getString("dropcamID",null);
        onDropcamView = true;
        //allow sensored landscape or portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        //concenate the id to the end of the url
        if (dropCamID != null){
            final String dropCamIDURL = "https://nexusapi.dropcam.com/get_image?width=1200&uuid=" + dropCamID;
            final ProgressBar dropProgress = (ProgressBar)findViewById(R.id.dropcam_progress);

            //handle the settings of the dropcam webview only once
            dropcamView.setBackgroundColor(0);
            dropcamView.getSettings().setSupportZoom(true);
            dropcamView.getSettings().setBuiltInZoomControls(true);
            if (Build.VERSION.SDK_INT >= 11){
                dropcamView.getSettings().setDisplayZoomControls(false);
            }
            dropcamView.getSettings().setLoadWithOverviewMode(true);
            dropcamView.setInitialScale(0);
            dropcamView.loadUrl(dropCamIDURL);

            //start the timer to refresh the webview
            dropTimer = new Timer();
            dropTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    dropcamLoading = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dropProgress.setVisibility(View.VISIBLE);
                            dropcamView.reload();
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dropProgress.setVisibility(View.INVISIBLE);
                                            dropcamLoading = false;
                                        }
                                    });
                                }
                            }, 2000);
                        }
                    });
                }
            }, 0, 10000);
        }
    }

    public void initGroupList(){
        groupViews = new ArrayList<View>();
        //Link list view object to the ArrayAdapter to create table/list view!
        //new GroupListTask().execute();
        groupList = (ListView) findViewById(R.id.group_list);
        navWheel.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                stopMedia();
                //stopColorTimer if it's possibly running
                if (updateColorTimer != null){
                    updateColorTimer.cancel();
                }
                //stop pulse timer
                if (updatePulseTimer != null){
                    updatePulseTimer.cancel();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (scheduleTimer != null && isPlaying) {
                    scheduleTimer.cancel();
                    scheduleTimer.onFinish();
                }
                //setGroupDefault();
                stopMedia();
            }
        });
        //ArrayList<String> groupOptions = new ArrayList<String>();

        if(deviceGroups != null && deviceGroups.size() > 0){
            menuOptions = new String[deviceGroups.size()];
            for(int i = 0; i < deviceGroups.size();i++){
                //Make sure the apostrophes get displayed correctly
                menuOptions[i] = deviceGroups.get(i).getName().replace("&#39;", "\'").replace("&#34;","\"");
            }
        }
        else{
            menuOptions = new String[]{};
        }

        groupAdapter = new GroupAdapter(this, R.layout.group_list_item1, menuOptions);
        groupList.setAdapter(groupAdapter);

        displayLoginOnList();
        if(deviceGroups != null && deviceGroups.size() > 0){
            groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long id) {
                    String selectedItem = groupAdapter.getItem(position);
                    groupSwitched = true;
                    //set Group Name to be displayed on action bar when selected
                    selectedGroupName = deviceGroups.get(position).getName().replace("&#39;", "\'").replace("&#34;","\"");
                    if (viewTitle.getText() != null){
                        String titleName = viewTitle.getText().toString();
                        if (!titleName.equalsIgnoreCase("Dropcam") && selectedGroupName != null){
                            viewTitle.setText(selectedGroupName);
                        }
                    }
                    //reset dropcam toggle and timer and change background back to coral image
                    dropcamToggle = 0;
                    currentMainLayout.setBackgroundResource(R.drawable.new_wheel_bg);
                    if (dropTimer != null){
                        dropTimer.cancel();
                    }

                    //close the drawers and save the selected group for use throughout this activity
                    Log.i("GROUP ITEM SELECTED:", selectedItem);
                    navWheel.closeDrawers();

                    //Go back to Color Wheel For all Selections
                    screenFlip.setDisplayedChild(0);
                    colorFlip.setVisibility(View.VISIBLE);
                    colorFlip.getChildAt(currentChild).setVisibility(View.VISIBLE);

                    if (position == 0){
                        onAllAquariums = true;

                        //if you click all groups, send user back to the color channels
                        if (viewTitle.getText() != null){
                            viewTitle.setText(selectedGroupName);
                        }
                        //always set orientation to portrait for color wheel view
                        scheduleSeekChange = false;
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        if (pointScroller != null){
                            pointScroller.fullScroll(View.FOCUS_LEFT);
                            pointScroller.currentIndex = 0;
                        }
                    }else{
                        onAllAquariums = false;
                        mainMenuAdapter.notifyDataSetChanged();
                        //presetsAdapter.notifyDataSetChanged();
                    }
                    //save the selected group to be used throughout the program
                    selectedGroup = deviceGroups.get(position);

                    //save the total count of radions to be used in schedule programming
                    edit.putInt("selectedRadionCount", selectedGroup.getRadionCount());
                    edit.putString("selectedGroupName", selectedGroup.getName());
                    edit.putInt("groupID", selectedGroup.getGroupId());
                    edit.commit();

                    if (screenFlip.getDisplayedChild() == 3 && position != 0){
                        screenFlip.setDisplayedChild(0);
                        colorFlip.setVisibility(View.VISIBLE);
                        colorFlip.getChildAt(currentChild).setVisibility(View.VISIBLE);
                    }

                    //clear the arrays on every group click so that you refresh the data for the list
                    if (allPumpNames != null){
                        allPumpNames.clear();
                    }
                    if (allPumpModelNums != null){
                        allPumpModelNums.clear();
                    }
                    if (allPumpIds != null){
                        allPumpIds.clear();
                    }
                    if (allPumps != null){
                        allPumps.clear();
                    }

                    //get the device status at the start of the app
                    getDeviceStatusInGroup(selectedGroup.getGroupId());
                    getDeviceStatusForPump(selectedGroup.getGroupId());

                    GroupSettingsDownload newSettingsDownload = new GroupSettingsDownload();
                    newSettingsDownload.execute();
                }
            });
            groupList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                    //Allow renaming of all groups except for All Aquariums (item 0)
                    if (position != 0){
                        //Init a generic EditText to be added as a view to the alert dialog
                        final EditText input = new EditText(NavigationWheel.this);
                        AlertDialog.Builder renameAlert = new AlertDialog.Builder(NavigationWheel.this);
                        renameAlert.setView(input);
                        renameAlert.setTitle(getResources().getString(R.string.rename) + " " + getResources().getString(R.string.group));
                        renameAlert.setNegativeButton(R.string.cancel, null);
                        renameAlert.setPositiveButton(R.string.save, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String previousName = deviceGroups.get(position).getName().replace("&#39;", "\'").replace("&#34;","\"");
                                deviceGroups.get(position).setName(input.getText().toString().replace("&#39;", "\'").replace("&#34;","\""));
                                Thread renameThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ConnectManager connect = ConnectManager.getSharedInstance();
                                        connect.renameGroup(deviceGroups.get(position).getGroupId(), deviceGroups.get(position).getGroupId(), deviceGroups.get(position).getName());
                                    }
                                });
                                Thread afterRenameThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                groupAdapter.notifyDataSetChanged();
                                                groupList.performItemClick(groupList, position, groupList.getItemIdAtPosition(position));
                                            }
                                        });
                                    }
                                });
                                try{
                                    //This pattern will run the afterThread AFTER the renameThread is finished
                                    renameThread.start();
                                    renameThread.join();
                                    afterRenameThread.start();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }

                                Toast.makeText(NavigationWheel.this, previousName + " " + getResources().getString(R.string.renamed) + " " + deviceGroups.get(position).getName().replace("&#39;","\'"), Toast.LENGTH_LONG).show();
                            }
                        });
                        renameAlert.show();
                    }
                    return true;
                }
            });

            //All code below is outside of the onclick listener and will only run at the start of the app
            groupList.performItemClick(groupList, 1, groupList.getItemIdAtPosition(1));

            //Hide UV Bar after loading of grouplist
            final ImageButton upUV = (ImageButton) findViewById(R.id.uv_left_up_button);
            final ImageButton downUV = (ImageButton) findViewById(R.id.uv_left_down_button);
            final SeekBar uvBar = (SeekBar) findViewById(R.id.uv_left_track);
            final TextView uvLabel = (TextView) findViewById(R.id.uv_label);
            final TextView uvText = (TextView) findViewById(R.id.uv_text);

            //hide UV UI if the RadionProCount is < 1
            if (totalRadionProCount < 1){
                uvBar.setVisibility(View.GONE);
                uvText.setVisibility(View.GONE);
                uvLabel.setVisibility(View.GONE);
                upUV.setVisibility(View.GONE);
                downUV.setVisibility(View.GONE);
                edit.putInt("hasPro", 0);
            }else{
                edit.putInt("hasPro", 1);
            }
            edit.commit();
            //set the make the progress bar disappear when the app is finished loading
            viewTitle.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        }
    }

    class GroupSettingsDownload extends AsyncTask<Void,Void,Void>{
        String[] settingsMsg;
        @Override
        protected Void doInBackground(Void... params) {
            if (selectedGroup.getGroupId() != 240){
                ConnectManager connectESl = ConnectManager.getSharedInstance();
                settingsMsg = connectESl.loadGroupSettings(selectedGroup.getGroupId());
                //Grab the settings that come in through the JSON data
                if (settingsMsg != null && settingsMsg.length > 0){
                    GroupSettings.Type.EcoSmartParticipation.enabled = Boolean.valueOf(settingsMsg[0]);
                    GroupSettings.Type.OverrideTimer.enabled = Boolean.valueOf(settingsMsg[1]);
                    GroupSettings.Type.OverrideTimer.duration = Integer.valueOf(settingsMsg[2]);
                    GroupSettings.Type.AcclimateTimer.enabled = Boolean.valueOf(settingsMsg[4]);
                    GroupSettings.Type.AcclimateTimer.duration = Integer.valueOf(settingsMsg[8]);
                    GroupSettings.Type.AcclimateTimer.intensity = Float.valueOf(settingsMsg[9]);
                    GroupSettings.Type.LunarPhases.enabled = Boolean.valueOf(settingsMsg[10]);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Refresh settings adapter when group item is clicked
                    settings_setter.notifyDataSetChanged();
                }
            });
        }
    }

    public void initPumpPresetsView(){
        setPumpPresetHeaders();
        setPumpPresetRows();

        presetList.setGroupIndicator(null);
        presetList.setChildIndicator(null);
        presetList.setDividerHeight(1);
        presetList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        presetList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

        exAdapter = new BaseExpandableListAdapter() {

            @Override
            public int getGroupCount() {
                if (globalConnect.pumpUserPresets.size() < 1){
                    return 1;
                }
                return headerPumpItem.size();
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                super.registerDataSetObserver(observer);
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {
                super.unregisterDataSetObserver(observer);
            }

            @Override
            public int getChildrenCount(int i) {
                return (pumpPresetTable.get(i).size());
            }

            @Override
            public Object getGroup(int i) {
                return headerPumpItem.get(i);
            }

            @Override
            public Object getChild(int i, int i2) {
                return pumpPresetTable.get(i).get(i2).name;
            }

            @Override
            public long getGroupId(int i) {
                return i;
            }

            @Override
            public long getChildId(int i, int i2) {
                if (globalConnect.pumpUserPresets.size() < 1){
                    return -1L;
                }
                return i2;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

                final TextView newView = (TextView)getLayoutInflater().inflate(R.layout.header_item, null);
                //TextView header = (TextView)newView.findViewById(R.id.tv);
                if (newView != null){
                    newView.setText(headerPumpItem.get(i));
                }

                //generate margins (to do this, I had to typecast the view to a relative layout, configure the settings, then recast it to textview

                if (i > 0){
                    if (newView != null){
                        newView.setPadding(0,30,0,25);
                    }
                }

                //keep all groups expanded at all times
                presetList.expandGroup(i);
                return newView;
            }

            @Override
            public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
                //TextView newView = (TextView)getLayoutInflater().inflate(R.layout.child_item,null);
                TextView result;
                result = (TextView) getLayoutInflater().inflate(R.layout.child_item, viewGroup, false);
                //return newView;
                if (result != null){
                    result.setText((pumpPresetTable.get(i)).get(i2).name);
                }
                return result;
            }

            @Override
            public boolean isChildSelectable(int i, int i2) {
                return true;
            }
        };

        //final ArrayAdapter<String> presetAdapter = new SystemAdapter(this,R.layout.list_item, systemPresetsArray);
        presetList.setAdapter(exAdapter);
        presetList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        presetList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                final int index = expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(i, i2));

                //final boolean itemChecked = expandableListView.isItemChecked(index);
                if (!expandableListView.isItemChecked(index)) {
                    expandableListView.setItemChecked(index, true);
                    expandableListView.setFocusable(false);
                }
                currentPulse = pumpPresetTable.get(i).get(i2);
                new SetPumpToModeTask().execute();
                exAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT < 11) {
                    //expand all groups in presets list
                    for (int j = 0; j < exAdapter.getGroupCount(); j++){
                        presetList.expandGroup(i);
                    }
                }
                return true;
            }
        });
        presetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get the position of the view out of all groups and rows
                final int pos = parent.getPositionForView(view);
                System.out.println("Position for each element in Pump Preset List = " + pos);
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    final int childPosition = ExpandableListView.getPackedPositionChild(id);

                    System.out.println("Group Position of Long Click = " + groupPosition);
                    System.out.println("Child Position of Long Click = " + childPosition);

                    if (groupPosition != 0){
                        //convert the View Position into an index that correlates with the structure of the user preset array
                        AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(NavigationWheel.this);
                        DeleteAlert.setMessage(R.string.delete_preset_msg);
                        DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + pumpPresetTable.get(groupPosition).get(childPosition).name + "\"");
                        DeleteAlert.setNegativeButton(R.string.no, null);
                        DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (pumpPresetTable.get(groupPosition) != null) {
                                    pumpPresetTable.get(groupPosition).get(childPosition).deleteFlag = true;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            globalConnect.savePumpPreset(pumpPresetTable.get(groupPosition).get(childPosition));
                                        }
                                    }).start();
                                    Toast.makeText(NavigationWheel.this, pumpPresetTable.get(groupPosition).get(childPosition).name + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                    pumpPresetTable.get(groupPosition).remove(childPosition);
                                    exAdapter.notifyDataSetChanged();
                                    if (Build.VERSION.SDK_INT < 11) {
                                        //expand all groups in presets list
                                        for (int j = 0; j < exAdapter.getGroupCount(); j++){
                                            presetList.expandGroup(j);
                                        }
                                    }
                                }
                            }
                        });
                        DeleteAlert.show();
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });
    }

    public void initRadionPresetsView(){
        //Link list view object to the ArrayAdapter to create table/list view!

        setHeaderData();
        setRowData();

        presetList.setGroupIndicator(null);
        presetList.setChildIndicator(null);
        presetList.setDividerHeight(1);
        presetList.setChildDivider(getResources().getDrawable(R.color.DarkGray));
        presetList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

        exAdapter = new BaseExpandableListAdapter() {

            @Override
            public int getGroupCount() {
                if (userPresets.size() < 1){
                    return 1;
                }
                return headerItem.size();
            }

            @Override
            public int getChildrenCount(int i) {
                return (presetTable.get(i)).size();
            }

            @Override
            public Object getGroup(int i) {
                return headerItem.get(i);
            }

            @Override
            public Object getChild(int i, int i2) {
                return (presetTable.get(i)).get(i2).getName();
            }

            @Override
            public long getGroupId(int i) {
                return i;
            }

            @Override
            public long getChildId(int i, int i2) {
                if (userPresets.size() < 1){
                    return -1L;
                }
                return (presetTable.get(i)).get(i2).getPresetId();
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

                final TextView newView = (TextView)getLayoutInflater().inflate(R.layout.header_item,null);
                //TextView header = (TextView)newView.findViewById(R.id.tv);
                if (newView != null){
                    newView.setText(headerItem.get(i));
                }

                //generate margins (to do this, I had to typecast the view to a relative layout, configure the settings, then recast it to textview

                if (i > 0){
                    if (newView != null){
                        newView.setPadding(0,30,0,25);
                    }
                }

                //keep all groups expanded at all times
                presetList.expandGroup(i);
                return newView;
            }

            @Override
            public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
                //TextView newView = (TextView)getLayoutInflater().inflate(R.layout.child_item,null);
                TextView result;
                result = (TextView) getLayoutInflater().inflate(R.layout.child_item,viewGroup, false);
                //return newView;
                if (result != null){
                    result.setText((presetTable.get(i)).get(i2).getName());
                }
                return result;
            }

            @Override
            public boolean isChildSelectable(int i, int i2) {
                return true;
            }
        };

        //final ArrayAdapter<String> presetAdapter = new SystemAdapter(this,R.layout.list_item, systemPresetsArray);
        presetList.setAdapter(exAdapter);
        presetList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                final int index = expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(i, i2));

                //final boolean itemChecked = expandableListView.isItemChecked(index);
                if (!expandableListView.isItemChecked(index)) {
                    expandableListView.setItemChecked(index, true);
                    expandableListView.setFocusable(false);
                }
                new PresetLoad(i, i2, true).execute();
                exAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT < 11){
                    presetList.expandGroup(0);
                    presetList.expandGroup(1);
                }
                return true;
            }
        });
        presetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get the position of the view out of all groups and rows
                final int pos = parent.getPositionForView(view);
                if (pos > 15) {
                    //convert the View Position into an index that correlates with the structure of the user preset array
                    final int upIndex = pos - 16;
                    AlertDialog.Builder DeleteAlert = new AlertDialog.Builder(NavigationWheel.this);
                    DeleteAlert.setMessage(R.string.delete_preset_msg);
                    DeleteAlert.setTitle(getResources().getString(R.string.delete) + " \"" + presetTable.get(1).get(upIndex).getName() + "\"");
                    DeleteAlert.setNegativeButton(R.string.no, null);
                    DeleteAlert.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (presetTable.get(1) != null && userPresets != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ConnectManager connect = ConnectManager.getSharedInstance();
                                            connect.deletePreset(presetTable.get(1).get(upIndex));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                Toast.makeText(NavigationWheel.this, presetTable.get(1).get(upIndex).getName() + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                presetTable.get(1).remove(upIndex);
                                exAdapter.notifyDataSetChanged();
                                if (Build.VERSION.SDK_INT < 11){
                                    presetList.expandGroup(0);
                                    presetList.expandGroup(1);
                                }
                                isDirty = true;
                            }
                        }
                    });
                    DeleteAlert.show();
                }
                return true;
            }
        });
    }

    public void initPreviewModeView(){
        final Integer[] imageIds = {R.drawable.button_thunderstorms, R.drawable.button_demonstration,R.drawable.button_cloud_cover,R.drawable.button_disco_time};
        final BaseAdapter imageAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public Object getItem(int position) {
                return imageIds[position];
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View iview;
                ImageView previewImage = new ImageView(NavigationWheel.this);
                TextView previewText = new TextView(NavigationWheel.this);
                if (convertView == null){
                    LayoutInflater inflater = NavigationWheel.this.getLayoutInflater();
                    iview = inflater.inflate(R.layout.preview_item, parent, false);
                    previewImage = (ImageView)iview.findViewById(R.id.preview_image);
                    previewText = (TextView)iview.findViewById(R.id.preview_item_text);
                }else{
                    iview = convertView;
                }
                previewImage.setImageResource(imageIds[position]);
                switch (position){
                    case 0:
                        previewText.setText(R.string.thunderstorms);
                        break;
                    case 1:
                        previewText.setText(R.string.demonstration);
                        break;
                    case 2:
                        previewText.setText(R.string.cloud_cover);
                        break;
                    case 3:
                        previewText.setText(R.string.disco_time);
                        //adjust width for disco time image because by default it is stretched out
                        previewImage.getLayoutParams().width = previewImage.getLayoutParams().width - (int)(10 * metrics.density);
                        break;
                }
                RelativeLayout modeLayout = (RelativeLayout)findViewById(R.id.mode_layout);
                TextView modeTitle = (TextView)findViewById(R.id.mode_title);
                //set the height to dynamically change per device
                int modeLayoutHeight = modeLayout.getMeasuredHeight() / 2;
                int modeTitleHeight = modeTitle.getMeasuredHeight() + 25;
                iview.getLayoutParams().height = modeLayoutHeight - modeTitleHeight;
                return iview;
            }
        };
        final GridView previewsGrid = (GridView)findViewById(R.id.previews_grid);
        previewsGrid.setAdapter(imageAdapter);

        previewsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {

                if(currentPreviewMode == position){
                    //previewsGrid.setItemChecked(currentPreviewMode, false);
                    new PreviewLoad(currentPreviewMode, false).execute();
                    previewsGrid.getChildAt(position).setSelected(false);
                    stopMedia();
                    currentPreviewMode = -1;
                }else{
                    currentPreviewMode = position;
                    new PreviewLoad(currentPreviewMode, true).execute();
                    previewsGrid.getChildAt(position).setSelected(true);

                }

                //Handle Thunderstorm media
                if (position == 0){
                    if (mediaPlaying){
                        stopMedia();
                        mediaPlaying = false;
                    }else{
                        if (mPlayer != null){
                            mPlayer.start();
                        }else{
                            mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.thunderstorm);
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (mPlayer != null && mediaPlaying){
                                        mPlayer.start();
                                    }
                                }
                            },5000);
                        }
                        mediaPlaying = true;
                    }
                }else{
                    stopMedia();
                    mediaPlaying = false;
                }
            }

        });
        //Link list view object to the ArrayAdapter to create table/list view!
        /*final ListView previewModeList = (ListView) findViewById(R.id.previews_list);
        previewModeList.setDivider(new ColorDrawable(getResources().getColor(R.color.DarkGray)));
        previewModeList.setDividerHeight(1);
        previewModeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final ArrayAdapter<String> previewAdapter = new ArrayAdapter<String>(this,R.layout.list_item,previewsList);
        previewModeList.setAdapter(previewAdapter);
        previewModeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long id) {
                if(currentPreviewMode == id){
                    previewModeList.setItemChecked(currentPreviewMode, false);
                    new PreviewLoad(currentPreviewMode, false).execute();
                    currentPreviewMode = -1;
                }else{
                    currentPreviewMode = (int)id;
                    new PreviewLoad((int)id, true).execute();
                }

                //Handle Thunderstorm media
                if (position == 0){
                    if (mediaPlaying){
                        stopMedia();
                        mediaPlaying = false;
                    }else{
                        if (mPlayer != null){
                            mPlayer.start();
                        }else{
                            mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.thunderstorm);
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (mPlayer != null){
                                        mPlayer.start();
                                    }
                                }
                            },5000);
                            mediaPlaying = true;
                        }
                    }
                }else{
                    stopMedia();
                    mediaPlaying = false;
                }
            }
        });*/
    }

    public void stopMedia(){
        if (mPlayer != null){
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    class LoadScheduleDataTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            //reset now scrolled boolean so that the default scrolling will trigger for the now point
            //scheduleLoading = true;
            nowScrolled = false;
            //make sure to reset the current time every time you refresh the schedule page!
            //nowTotalSeconds = 0;
            //nowTotalSeconds = 33000;
            //nowTotalSeconds = 36000;
            //nowTotalSeconds = 43200;
            //nowTotalSeconds = 46800;
            //nowTotalSeconds = 50400;
            nowTotalSeconds = (todayDate.get(Calendar.HOUR_OF_DAY) * 3600) + (todayDate.get(Calendar.MINUTE) * 60) + todayDate.get(Calendar.SECOND);
            //nowTotalSeconds = 72000;
            //nowTotalSeconds = 73500;
            //nowTotalSeconds = 76000;
            //nowTotalSeconds = 82800;
            //nowTotalSeconds = 86300;
            ConnectManager connect = ConnectManager.getSharedInstance();
            userSchedule = new Schedule();
            userSchedule = connect.loadScheduleForGroup(sp.getInt("groupID",0));

            for (int i = 0; i < userSchedule.dataPoints.size(); i++){
                //grab the two points from the full list of data points that surround the now point to be used for natural mode calculation
                if (userSchedule.dataPoints.get(i).totalSeconds >= nowTotalSeconds){
                    if (i != 0){
                        naturalNow1 = userSchedule.dataPoints.get(i-1);
                        naturalNow2 = userSchedule.dataPoints.get(i);
                    }else{
                        naturalNow1 = userSchedule.dataPoints.get(0);
                        naturalNow2 = userSchedule.dataPoints.get(1);
                    }
                    break;
                }
            }

            //Sort data logic
            if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                //Sort logic for filtered Data Points
                for (int i = 0; i < userSchedule.filteredDataPoints.size(); i++){
                    if (userSchedule.filteredDataPoints.get(i).isStartDay){
                        startDayPoint = userSchedule.filteredDataPoints.get(i);
                    }
                    if (userSchedule.filteredDataPoints.get(i).isStartNight){
                        startNightPoint = userSchedule.filteredDataPoints.get(i);
                    }
                }

                Collections.sort(userSchedule.filteredDataPoints, new Comparator<ScheduleDataPoint>() {
                    @Override
                    public int compare(ScheduleDataPoint lhs, ScheduleDataPoint rhs) {
                        if(rhs.totalSeconds == 0){
                            return 0;
                        }else{
                            return lhs.totalSeconds - rhs.totalSeconds;
                        }
                    }
                });

                //Sort logic for all datapoints (do this so you can react off of numbers for the now intensity in natural mode
                Collections.sort(userSchedule.dataPoints,new Comparator<ScheduleDataPoint>() {
                    @Override
                    public int compare(ScheduleDataPoint lhs, ScheduleDataPoint rhs) {
                        if(rhs.totalSeconds == 0){
                            return 0;
                        }else{
                            return lhs.totalSeconds - rhs.totalSeconds;
                        }
                    }
                });

                for(int i = 0; i < userSchedule.filteredDataPoints.size(); i++) {
                    ScheduleDataPoint dataPoint = userSchedule.filteredDataPoints.get(i);
                    if (dataPoint.isStartDay){
                        //set Start Day Index
                        dayIndex = i;
                    }
                    if (dataPoint.isStartNight){
                        //set Start Night Index
                        nightIndex = i;
                    }

                    //Handle setting the presetName based off of the id the schedule data point has
                    if(dataPoint.presetId > 0){
                        //loop through all of the system presets to see if they are equal to the data point's preset
                        for (int j = 0; j < presetTable.get(0).size(); j++){
                            Preset p = presetTable.get(0).get(j);
                            if (p.presetId == dataPoint.presetId && !presetNameFound){
                                //it must be -1 due to the indexing logic of the list
                                dataPoint.presetName = p.getName();
                                presetNameFound = true;
                            }
                        }

                        //if a preset was not found in the system presets, loop thru user presets to find a preset with the preset id
                        for (int j = 0; j < presetTable.get(1).size(); j++){
                            Preset p = presetTable.get(1).get(j);
                            //check for user preset
                            if (p.presetId == dataPoint.presetId && !presetNameFound){
                                dataPoint.presetName = p.getName();
                                presetNameFound = true;
                            }
                        }
                    }else{
                        //if preset id is 0 or less, set it to custom as the preset name
                        dataPoint.presetName = getResources().getString(R.string.custom);
                    }

                    //calculate intensity for natural mode
                    float maxIntensity = 480.0f;
                    if(selectedGroup.getRadionProCount() > 0){
                        maxIntensity = 630.0f;
                    }

                    // Calculate relative intensity (for all colors)
                    if (!dataPoint.isNightMode){
                        dataPoint.brightness = userSchedule.paramOverallBrightness;
                    }

                    //special loop to grab relative intensities to use for now point

                    float fIntensity = ((dataPoint.brightness) * (((dataPoint.royalBlue1) * ((selectedGroup.getRadionProCount() > 0 ? 40 : 30))) + ((dataPoint.blue) * (24)) + ((dataPoint.white) * (40)) + ((dataPoint.green) * (16)) + ((dataPoint.red) * ((selectedGroup.getRadionProCount() > 0 ? 15 : 10))) + ((dataPoint.uv) * ((selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)))));
                    float scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    int iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensity = iIntensity;

                    // Calculate relative intensity (royal blue)
                    fIntensity = (dataPoint.brightness * (dataPoint.royalBlue1 * (selectedGroup.getRadionProCount() > 0 ? 40 : 30)));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityRB = iIntensity;

                    // Calculate relative intensity (blue)
                    fIntensity = (dataPoint.brightness * (dataPoint.blue * 24));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityBlue = iIntensity;

                    // Calculate relative intensity (white)
                    fIntensity = (dataPoint.brightness * (dataPoint.white * 40));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityWhite = iIntensity;

                    // Calculate relative intensity (green)
                    fIntensity = (dataPoint.brightness * (dataPoint.green * 16));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityGreen = iIntensity;

                    // Calculate relative intensity (red)
                    fIntensity = (dataPoint.brightness * (dataPoint.red * (selectedGroup.getRadionProCount() > 0 ? 15 : 10)));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityRed = iIntensity;

                    // Calculate relative intensity (uv)
                    fIntensity = (dataPoint.brightness * (dataPoint.uv * (selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)));
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityUV = iIntensity;

                    // Calcuate relative intensity (acclimate)
                    if ((dataPoint.isStartDay || dataPoint.isStartNight || !dataPoint.isNightMode) && GroupSettings.Type.AcclimateTimer.enabled && GroupSettings.Type.AcclimateTimer.intensity > 0) {
                        fIntensity = ((dataPoint.brightness) * (((dataPoint.royalBlue1) * ((selectedGroup.getRadionProCount() > 0 ? 40 : 30))) + ((dataPoint.blue) * (24)) + ((dataPoint.white) * (40)) + ((dataPoint.green) * (16)) + ((dataPoint.red) * ((selectedGroup.getRadionProCount() > 0 ? 15 : 10))) + ((dataPoint.uv) * ((selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)))));
                        fIntensity = fIntensity * GroupSettings.Type.AcclimateTimer.intensity;
                        scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                        iIntensity = Math.round(scaledFIntensity);

                        if (scaledFIntensity > 0 && iIntensity == 0) {
                            iIntensity = 1;
                        }
                        dataPoint.relativeIntensityAcclimate = iIntensity;
                    } else {
                        dataPoint.relativeIntensityAcclimate = 0;
                    }

                    //reset the preset name found at the end of the loop for a single data point so that the search for the preset match occurs for every single datapoint
                    presetNameFound = false;
                }
            }else{
                for (int i = 0; i < userSchedule.dataPoints.size(); i++){
                    if (userSchedule.dataPoints.get(i).isStartDay){
                        startDayPoint = userSchedule.dataPoints.get(i);
                    }
                    if (userSchedule.dataPoints.get(i).isStartNight){
                        startNightPoint = userSchedule.dataPoints.get(i);
                    }
                    if (i != 0){
                        if (userSchedule.dataPoints.get(i).brightness > userSchedule.dataPoints.get(i-1).brightness && !userSchedule.dataPoints.get(i).isNightMode){
                            maxPointBrightness = userSchedule.dataPoints.get(i).brightness;
                        }
                    }else{
                        maxPointBrightness = userSchedule.dataPoints.get(i).brightness;
                    }
                }
                Collections.sort(userSchedule.dataPoints,new Comparator<ScheduleDataPoint>() {
                    @Override
                    public int compare(ScheduleDataPoint lhs, ScheduleDataPoint rhs) {
                        if(rhs.totalSeconds == 0){
                            return 0;
                        }else{
                            return lhs.totalSeconds - rhs.totalSeconds;
                        }
                    }
                });
            }

            float maxIntensity = 480.0f;
            if(selectedGroup.getRadionProCount() > 0){
                maxIntensity = 630.0f;
            }

            //handle relative intensity calculations for both Natural and artificial
            for(int i = 0; i < userSchedule.dataPoints.size(); i++) {
                ScheduleDataPoint dataPoint = userSchedule.dataPoints.get(i);
                if (dataPoint.isStartDay){
                    //set Start Day Index
                    dayIndex = i;
                }
                if (dataPoint.isStartNight){
                    //set Start Night Index
                    nightIndex = i;
                }

                //Handle setting the presetName based off of the id the schedule data point has
                if(dataPoint.presetId > 0){
                    //loop through all of the system presets to see if they are equal to the data point's preset
                    for (int j = 0; j < presetTable.get(0).size(); j++){
                        Preset p = presetTable.get(0).get(j);
                        if (p.presetId == dataPoint.presetId && !presetNameFound){
                            //it must be -1 due to the indexing logic of the list
                            dataPoint.presetName = p.getName();
                            presetNameFound = true;
                        }
                    }

                    //if a preset was not found in the system presets, loop thru user presets to find a preset with the preset id
                    for (int j = 0; j < presetTable.get(1).size(); j++){
                        Preset p = presetTable.get(1).get(j);
                        //check for user preset
                        if (p.presetId == dataPoint.presetId && !presetNameFound){
                            dataPoint.presetName = p.getName();
                            presetNameFound = true;
                        }
                    }
                }else{
                    //if preset id is 0 or less, set it to custom as the preset name
                    dataPoint.presetName = getResources().getString(R.string.custom);
                }

                //handle relative intensity calculation for artificial mode
                float fIntensity = ((dataPoint.brightness) * (((dataPoint.royalBlue1) * ((selectedGroup.getRadionProCount() > 0 ? 40 : 30))) + ((dataPoint.blue) * (24)) + ((dataPoint.white) * (40)) + ((dataPoint.green) * (16)) + ((dataPoint.red) * ((selectedGroup.getRadionProCount() > 0 ? 15 : 10))) + ((dataPoint.uv) * ((selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)))));
                float scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                int iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensity = iIntensity;

                // Calculate relative intensity (royal blue)
                fIntensity = (dataPoint.brightness * (dataPoint.royalBlue1 * (selectedGroup.getRadionProCount() > 0 ? 40 : 30)));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityBlue = iIntensity;

                // Calculate relative intensity (blue)
                fIntensity = (dataPoint.brightness * (dataPoint.blue * 24));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityBlue = iIntensity;

                // Calculate relative intensity (white)
                fIntensity = (dataPoint.brightness * (dataPoint.white * 40));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityWhite = iIntensity;

                // Calculate relative intensity (green)
                fIntensity = (dataPoint.brightness * (dataPoint.green * 16));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityGreen = iIntensity;

                // Calculate relative intensity (red)
                fIntensity = (dataPoint.brightness * (dataPoint.red * (selectedGroup.getRadionProCount() > 0 ? 15 : 10)));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityRed = iIntensity;

                // Calculate relative intensity (uv)
                fIntensity = (dataPoint.brightness * (dataPoint.uv * (selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)));
                scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                iIntensity = Math.round(scaledFIntensity);

                if (scaledFIntensity > 0 && iIntensity == 0) {
                    iIntensity = 1;
                }
                dataPoint.relativeIntensityUV = iIntensity;

                // Calcuate relative intensity (acclimate)
                if ((dataPoint.isStartDay || dataPoint.isStartNight || !dataPoint.isNightMode) && GroupSettings.Type.AcclimateTimer.enabled && GroupSettings.Type.AcclimateTimer.intensity > 0) {
                    fIntensity = ((dataPoint.brightness) * (((dataPoint.royalBlue1) * ((selectedGroup.getRadionProCount() > 0 ? 40 : 30))) + ((dataPoint.blue) * (24)) + ((dataPoint.white) * (40)) + ((dataPoint.green) * (16)) + ((dataPoint.red) * ((selectedGroup.getRadionProCount() > 0 ? 15 : 10))) + ((dataPoint.uv) * ((selectedGroup.getRadionProCount() > 0 ? 22.5f : 0)))));
                    fIntensity = fIntensity * GroupSettings.Type.AcclimateTimer.intensity;
                    scaledFIntensity = (fIntensity / maxIntensity) * 100.0f;
                    iIntensity = Math.round(scaledFIntensity);

                    if (scaledFIntensity > 0 && iIntensity == 0) {
                        iIntensity = 1;
                    }
                    dataPoint.relativeIntensityAcclimate = iIntensity;
                } else {
                    dataPoint.relativeIntensityAcclimate = 0;
                }

                //reset the preset name found at the end of the loop for a single data point so that the search for the preset match occurs for every single datapoint
                presetNameFound = false;
            }

            //grab image to be used for nighttime points if lunarphases is enabled
            if (GroupSettings.Type.LunarPhases.enabled){
                final String imageURL = String.format("http://%s/static/img/lunarphases/%d.jpg", ConnectManager.webSocketHost, lunarCurrentDay);
                try{
                    URL url = new URL(imageURL);
                    lunarPhaseImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new InitScheduleTask().execute();
        }
    }

    class InitScheduleTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initScheduleView();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //at the end of the task, ensure boolean switches are turned to false to prevent task from repeating
                    groupSwitched = false;
                    activityGoAhead = false;
                    nowPointAdded = false;

                    //wait a second for schedule to load and then scroll to the now point when available
                    CountDownTimer nowScroller = new CountDownTimer(2000, 200) {
                        public void onTick(long millisUntilFinished) {
                            pointScroller.fullScroll(View.FOCUS_LEFT);
                            pointScroller.currentIndex = 0;
                        }

                        public void onFinish() {
                            if (!scheduleSeekChange){
                                pointScroller.smoothScrollTo(nowPointDistance, 0);
                            }

                            //make sure the distance scrolled is reset before scrolling to the now point
                            if (RefreshOverlay.refreshActivity != null){
                                RefreshOverlay.refreshActivity.finish();
                            }
                            if (scheduleFirstClick){
                                Toast.makeText(NavigationWheel.this, getResources().getString(R.string.schedule_tooltip), Toast.LENGTH_LONG).show();
                            }
                            //If it is the first time the user is on the schedule view, inform them of how to use the app
                            scheduleFirstClick = false;
                        }
                    };
                    nowScroller.start();

                    //store all the times of all data points into the array to be used for checking user input on new and edit points
                    schedulePointTimes = new int[userSchedule.dataPoints.size()];
                    for (int i = 0; i < userSchedule.dataPoints.size(); i++){
                        //store all the times of all data points into the array to be used for checking user input on new and edit points
                        schedulePointTimes[i] = userSchedule.dataPoints.get(i).totalSeconds;
                    }
                }
            });
        }
    }

    public void stopSchedulePlayback(){
        final RelativeLayout schedulePlaybackOverlay = (RelativeLayout)findViewById(R.id.schedule_playback_overlay);
        //Handle Schedule Playback on view
        final ImageButton playSchedule = (ImageButton)findViewById(R.id.schedule_play_button);
        final ImageButton stopSchedule = (ImageButton)findViewById(R.id.schedule_stop_button);
        final TextView scheduleTime = (TextView)findViewById(R.id.schedule_playback_time);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ConnectManager connectESL = ConnectManager.getSharedInstance();
                    connectESL.stopPCControl(0);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        if (playTimer != null){
            playTimer.cancel();
        }
        scheduleTime.setText("12:00 AM");
        schedulePlaybackOverlay.setVisibility(View.INVISIBLE);
        schedulePlaybackOverlay.clearAnimation();
        playSchedule.setVisibility(View.VISIBLE);
        stopSchedule.setVisibility(View.INVISIBLE);
    }

    public void initScheduleView(){
        //grab this relative layout at start of schedule to be used to change background
        backgroundLayout = (RelativeLayout) findViewById(R.id.schedule_main_layout);
        nightMoon = (ImageView) findViewById(R.id.night_moon);
        pointScroller = (ListenableHorizontalScrollView) findViewById(R.id.point_scroll_view);
        final RelativeLayout schedulePlaybackOverlay = (RelativeLayout)findViewById(R.id.schedule_playback_overlay);
        final LinearLayout scheduleTouchGrid = (LinearLayout)findViewById(R.id.schedule_point_list);
        final TextView modeHeader = (TextView)findViewById(R.id.schedule_mode_header);
        nightMoon.setFadingEdgeLength(200);
        nightMoon.setVerticalFadingEdgeEnabled(true);
        nightMoon.setHorizontalFadingEdgeEnabled(true);

        if (GroupSettings.Type.LunarPhases.enabled && lunarPhaseImage != null){
            nightMoon.setImageBitmap(lunarPhaseImage);
        }else{
            nightMoon.setImageResource(R.drawable.moon);
        }

        if (pointScroller != null){
            //pointScroller.removeAllViews();
            //If the array happens to be null (which it shouldn't), allocate memory
            if (viewArray != null) {
                scheduleTouchGrid.removeAllViews();
                viewArray.clear();
                viewArray = new ArrayList<View>();
            }else{
                viewArray = new ArrayList<View>();
            }
        }

        //ImageView allocation for images on schedule options
        ImageView cloudSun = (ImageView)findViewById(R.id.cloud_sun_image);
        ImageView cloudCloud = (ImageView)findViewById(R.id.cloud_cloud_image);
        ImageView stormSun = (ImageView)findViewById(R.id.storm_sun_image);
        ImageView stormCloud = (ImageView)findViewById(R.id.storm_cloud_image);

        //slider allocation for overall brightness

        SeekBar overallBright = (SeekBar)findViewById(R.id.overall_brightness_slider);
        final TextView overallBrightValue = (TextView)findViewById(R.id.overall_brightness_value);
        //paramBrightness = 2; //default to 100% if artificial mode is loaded
        //Do this for both artificial and natural mode
        //set default to whatever the overall brightness is that comes back in natty mode

        if (onStartUp){
            if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                firstNatural = false;
                paramBrightness = (int)(userSchedule.paramOverallBrightness * 100 / 2);
            }else{
                firstArtificial = false;
                paramBrightness = (int)(maxPointBrightness * 100 / 2);
                //save to save preferences since the view will get stopped by the next activity for edit and new points
                edit.putInt("artificialBrightness", paramBrightness);
                edit.commit();
            }
            overallBright.setProgress(paramBrightness);
            overallBrightValue.setText(paramBrightness + "%");
        }else{
            if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                naturalBright = (int)(userSchedule.paramOverallBrightness * 100 / 2);
                overallBright.setProgress(naturalBright);
                overallBrightValue.setText(naturalBright + "%");
            }else{
                if (firstArtificial){
                    //artificialBright = (int)(maxPointBrightness * 100 / 2);
                    //save to save preferences since the view will get stopped by the next activity for edit and new points
                    edit.putInt("artificialBrightness", artificialBright);
                    edit.commit();
                }
                firstArtificial = false;
                overallBright.setProgress(sp.getInt("artificialBrightness",0));
                overallBrightValue.setText(sp.getInt("artificialBrightness",0) + "%");
            }
        }
        onStartUp = false;

        System.out.println("Max Point Brightness" + maxPointBrightness);
        System.out.println("Param Brightness " + paramBrightness);

        //slider allocation for cloud probability
        SeekBar cloudSlider = (SeekBar)findViewById(R.id.cloud_probability_slider);
        TextView cloudHeader = (TextView)findViewById(R.id.cloud_probability_header);
        final TextView cloudValue = (TextView)findViewById(R.id.cloud_probability_value);
        if (userSchedule.scheduleType.equalsIgnoreCase("N")){
            //only set text value and progress if in natural mode
            cloudSlider.setProgress(userSchedule.paramCloudFrequency * 10);
            cloudValue.setText((userSchedule.paramCloudFrequency * 10) + "%");
        }

        //slider allocation for storm probability
        SeekBar stormSlider = (SeekBar)findViewById(R.id.storm_probability_slider);
        TextView stormHeader = (TextView)findViewById(R.id.storm_probability_header);
        final TextView stormValue = (TextView)findViewById(R.id.storm_probability_value);
        if (userSchedule.scheduleType.equalsIgnoreCase("N")){
            //only set text value & progress if in natural mode
            stormSlider.setProgress(userSchedule.paramStormFrequency * 10);
            stormValue.setText((userSchedule.paramStormFrequency * 10) + "%");
        }

        //slider allocation for depth offset
        SeekBar depthSlider = (SeekBar)findViewById(R.id.depth_offset_slider);
        TextView depthHeader = (TextView)findViewById(R.id.depth_offset_header);
        final TextView depthValue = (TextView)findViewById(R.id.depth_offset_value);
        if (userSchedule.scheduleType.equalsIgnoreCase("N")){
            //only set text value and progress if in natural mode
            depthSlider.setProgress(userSchedule.paramDepthOffset);
            if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                depthValue.setText(userSchedule.paramDepthOffset + "ft");
            }else{
                int convertedMeters = (int)(userSchedule.paramDepthOffset * 0.3048);
                depthValue.setText(convertedMeters + "m");
            }
        }

        if (userSchedule.scheduleType.equalsIgnoreCase("N")){
            modeHeader.setText(R.string.natural_mode_options);
            cloudSlider.setVisibility(View.VISIBLE);
            cloudHeader.setVisibility(View.VISIBLE);
            cloudValue.setVisibility(View.VISIBLE);
            cloudSun.setVisibility(View.VISIBLE);
            cloudCloud.setVisibility(View.VISIBLE);

            stormSlider.setVisibility(View.VISIBLE);
            stormHeader.setVisibility(View.VISIBLE);
            stormValue.setVisibility(View.VISIBLE);
            stormSun.setVisibility(View.VISIBLE);
            stormCloud.setVisibility(View.VISIBLE);

            depthSlider.setVisibility(View.VISIBLE);
            depthHeader.setVisibility(View.VISIBLE);
            depthValue.setVisibility(View.VISIBLE);
        }else{
            modeHeader.setText(R.string.artificial_mode_options);

            cloudSlider.setVisibility(View.GONE);
            cloudHeader.setVisibility(View.GONE);
            cloudValue.setVisibility(View.GONE);
            cloudSun.setVisibility(View.GONE);
            cloudCloud.setVisibility(View.GONE);

            stormSlider.setVisibility(View.GONE);
            stormHeader.setVisibility(View.GONE);
            stormValue.setVisibility(View.GONE);
            stormSun.setVisibility(View.GONE);
            stormCloud.setVisibility(View.GONE);

            depthSlider.setVisibility(View.GONE);
            depthHeader.setVisibility(View.GONE);
            depthValue.setVisibility(View.GONE);
        }

        //Handle initial set up of Data Point ScrollView
        if (userSchedule.scheduleType.equalsIgnoreCase("N")){
            for (int i = 0; i < userSchedule.filteredDataPoints.size() + 1; i++){
                View dataPointView = getLayoutInflater().inflate(R.layout.schedule_data_point, null);
                viewArray.add(dataPointView);
                //set Alpha for all elements in the array
                if (Build.VERSION.SDK_INT < 11){
                    if (i == 0){
                        AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                        TextView dataPointText = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataPointText.setTypeface(null, Typeface.BOLD);
                        TextView dataPointIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        dataPointIntensity.setTypeface(null,Typeface.BOLD);
                    }else{
                        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                    }
                }else{
                    //handle fading and lighting of each view
                    if (i == 0){
                        viewArray.get(i).setAlpha(1f);
                        TextView dataPointText = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataPointText.setTypeface(null, Typeface.BOLD);
                        TextView dataPointIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        dataPointIntensity.setTypeface(null,Typeface.BOLD);
                    }else{
                        viewArray.get(i).setAlpha(0.5f);

                    }
                }

                //If now point is added, make sure to use the schedule - 1 element afterwards, if not, then use the current element
                if (!nowPointAdded){
                    if (i == userSchedule.filteredDataPoints.size()){
                        nowPointAdded = true;
                        nowIndex = i;
                        //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                        if (Build.VERSION.SDK_INT < 11){
                            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                            alpha.setDuration(0); // Make animation instant
                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                            viewArray.get(i).startAnimation(alpha);
                        }else{
                            viewArray.get(i).setAlpha(0.5f);

                        }
                        //Format the Time to be displayed on now point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataTime.setText(R.string.now);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.filteredDataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.filteredDataPoints.get(i-1).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }


                        //Image for now point
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                        if (userSchedule.filteredDataPoints.get(i-1).isStartDay || userSchedule.filteredDataPoints.get(i-1).isStartNight){
                            if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.filteredDataPoints.get(i-1).isStartDay){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }else{
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }else{
                                if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.filteredDataPoints.get(i-1).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }

                                    if (nightIndex > dayIndex && dayIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i-1 > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for now point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(R.string.custom);
                        //Relative Intensity for now point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        dataIntensity.setText("Relative %");
                        int nowIntensity = 0;
                        int diffSeconds = Math.abs(userSchedule.filteredDataPoints.get(0).totalSeconds - userSchedule.filteredDataPoints.get(userSchedule.filteredDataPoints.size()-1).totalSeconds);
                        int diffIntensity = Math.abs(userSchedule.filteredDataPoints.get(0).relativeIntensity - userSchedule.dataPoints.get(userSchedule.filteredDataPoints.size()-1).relativeIntensity);
                        double intenseRate = (double) diffIntensity / diffSeconds;
                        int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.filteredDataPoints.get(userSchedule.filteredDataPoints.size()-1).totalSeconds);
                        if (userSchedule.filteredDataPoints.get(0).relativeIntensity < userSchedule.filteredDataPoints.get(userSchedule.filteredDataPoints.size()-1).relativeIntensity){
                            nowIntensity = userSchedule.filteredDataPoints.get(userSchedule.filteredDataPoints.size()-1).relativeIntensity - (int)(nowSecondsAndIndexDifference * intenseRate);
                        }else{
                            nowIntensity = userSchedule.filteredDataPoints.get(userSchedule.filteredDataPoints.size()-1).relativeIntensity + (int)(nowSecondsAndIndexDifference * intenseRate);
                        }
                        if (nowIntensity > 0){
                            dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }else{
                        //Format the Time to be displayed on each data point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        Calendar timeConvert = Calendar.getInstance();
                        timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.filteredDataPoints.get(i).startTimeHour);
                        timeConvert.set(Calendar.MINUTE, userSchedule.filteredDataPoints.get(i).startTimeMinute);
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        String displayTime = formatter.format(timeConvert.getTime());
                        dataTime.setText(displayTime);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.filteredDataPoints.get(i).isStartDay){
                            dataStatus.setText(R.string.start_day);
                        }else if(userSchedule.filteredDataPoints.get(i).isStartNight){
                            dataStatus.setText(R.string.start_night);
                        }

                        //Image for each data point
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        if (userSchedule.filteredDataPoints.get(i).isStartDay || userSchedule.filteredDataPoints.get(i).isStartNight){
                            if (userSchedule.filteredDataPoints.get(i).isStartDay){
                                dataImage.setImageResource(R.drawable.icon_sunrise_orange);
                            }else{
                                dataImage.setImageResource(R.drawable.icon_sunset_blue);
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                                if (userSchedule.filteredDataPoints.get(i).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i > nightIndex && i < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }

                                    if (nightIndex > dayIndex && dayIndex > i){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i && i < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.filteredDataPoints.get(i).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for each data point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(userSchedule.filteredDataPoints.get(i).presetName);
                        //Relative Intensity for each data point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        if (userSchedule.filteredDataPoints.get(i).relativeIntensity > 0){
                            dataIntensity.setText(String.valueOf(userSchedule.filteredDataPoints.get(i).relativeIntensity) + "%");
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }
                }else{
                    //Format the Time to be displayed on each data point
                    TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                    Calendar timeConvert = Calendar.getInstance();
                    timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.filteredDataPoints.get(i-1).startTimeHour);
                    timeConvert.set(Calendar.MINUTE, userSchedule.filteredDataPoints.get(i-1).startTimeMinute);
                    SimpleDateFormat formatter;
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    String displayTime = formatter.format(timeConvert.getTime());
                    dataTime.setText(displayTime);
                    //Status for each data point
                    TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                    if (userSchedule.filteredDataPoints.get(i-1).isStartDay){
                        dataStatus.setText(R.string.start_day);
                    }else if(userSchedule.filteredDataPoints.get(i-1).isStartNight){
                        dataStatus.setText(R.string.start_night);
                    }else{
                        if (userSchedule.filteredDataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.filteredDataPoints.get(i).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }
                    }

                    //Image for each data point
                    ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                    if (userSchedule.filteredDataPoints.get(i-1).isStartDay || userSchedule.filteredDataPoints.get(i-1).isStartNight){
                        if (userSchedule.filteredDataPoints.get(i-1).isStartDay){
                            dataImage.setImageResource(R.drawable.icon_sunrise_orange);
                        }else{
                            dataImage.setImageResource(R.drawable.icon_sunset_blue);
                        }
                    }else{
                        if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                            if (userSchedule.filteredDataPoints.get(i-1).isNightMode){
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }else{
                                if(nightIndex < dayIndex && dayIndex < i-1){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if (nightIndex < dayIndex && nightIndex > i-1){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }

                                if (nightIndex > dayIndex && dayIndex > i-1){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex > dayIndex && i-1 > nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }

                    //Custom or Preset for each data point
                    TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                    dataPointType.setText(userSchedule.filteredDataPoints.get(i-1).presetName);
                    //Relative Intensity for each data point
                    TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                    if (userSchedule.filteredDataPoints.get(i-1).relativeIntensity > 0){
                        dataIntensity.setText(String.valueOf(userSchedule.filteredDataPoints.get(i-1).relativeIntensity) + "%");
                    }else{
                        dataIntensity.setText(R.string.off);
                    }
                }

                //NOW POINT FOR NATURAL MODE
                //Make now point be the first point if it is before all other data points && = midnight or above
                if (!nowPointAdded && nowTotalSeconds < userSchedule.filteredDataPoints.get(0).totalSeconds){
                    nowPointAdded = true;
                    nowIndex = i;
                    //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                    if (Build.VERSION.SDK_INT < 11){
                        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                    }else{
                        viewArray.get(i).setAlpha(0.5f);
                    }
                    //Format the Time to be displayed on now point
                    TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                    dataTime.setText(R.string.now);
                    //Status for each data point
                    TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                    if (userSchedule.filteredDataPoints.get(i).stormFrequency > 0){
                        int stormChance = userSchedule.filteredDataPoints.get(i).stormFrequency * 10;
                        dataStatus.setText(stormChance + "%");
                    }else if(userSchedule.filteredDataPoints.get(i).cloudFrequency > 0){
                        dataStatus.setText(R.string.cloudy);
                    }else{
                        dataStatus.setText(R.string.clear);
                    }

                    //Image for now point
                    ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                    if (userSchedule.filteredDataPoints.get(i).isStartDay || userSchedule.filteredDataPoints.get(i).isStartNight){
                        if (userSchedule.filteredDataPoints.get(i).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                            if (userSchedule.filteredDataPoints.get(i).isStartDay){
                                dataImage.setImageResource(R.drawable.icon_sun_yellow);
                            }else{
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }else{
                        if (userSchedule.filteredDataPoints.get(i).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                            if (userSchedule.filteredDataPoints.get(i).isNightMode){
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }else{
                                if(nightIndex < dayIndex && dayIndex < i){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex < dayIndex && i > nightIndex && i < dayIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if (nightIndex < dayIndex && nightIndex > i){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }

                                if (nightIndex > dayIndex && dayIndex > i){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if(nightIndex > dayIndex && dayIndex < i && i < nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex > dayIndex && i > nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }

                    //Custom or Preset for now point
                    TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                    dataPointType.setText(R.string.custom);
                    //Relative Intensity for now point
                    TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                    int nowIntensity = 0;
                    int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                    int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                    double intenseRate = (double)diffIntensity / diffSeconds;
                    int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(0).totalSeconds);
                    nowIntensity = (int)(nowSecondsAndIndexDifference * intenseRate);
                    if (nowIntensity > 0){
                        dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                    }else{
                        dataIntensity.setText(R.string.off);
                    }
                }

                //Handle the addition of the now point if it is in between the beginning and end of the schedule list of points
                if (!nowPointAdded && i != 0 && i != userSchedule.filteredDataPoints.size()){
                    //if the current time is greater than the previous element and less than the current element, replace current element
                    if (nowTotalSeconds > userSchedule.filteredDataPoints.get(i-1).totalSeconds && nowTotalSeconds < userSchedule.filteredDataPoints.get(i).totalSeconds){
                        nowPointAdded = true;
                        nowIndex = i;
                        //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                        if (Build.VERSION.SDK_INT < 11){
                            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                            alpha.setDuration(0); // Make animation instant
                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                            viewArray.get(i).startAnimation(alpha);
                        }else{
                            viewArray.get(i).setAlpha(0.5f);
                        }
                        //Format the Time to be displayed on now point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataTime.setText(R.string.now);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.filteredDataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.filteredDataPoints.get(i-1).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }

                        //Image for now point (In this case, we want to use the previous images so that it doesnt borrow from start night point)
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                        if (userSchedule.filteredDataPoints.get(i-1).isStartDay || userSchedule.filteredDataPoints.get(i-1).isStartNight){

                            if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.filteredDataPoints.get(i-1).isStartDay){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }else{
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }else{
                                if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }else{
                            if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency == 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.filteredDataPoints.get(i-1).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }

                                    if (nightIndex > dayIndex && dayIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i-1 > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.filteredDataPoints.get(i-1).cloudFrequency > 0 && userSchedule.filteredDataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for now point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(R.string.custom);
                        //Relative Intensity for now point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        int nowIntensity = 0;
                        //Use the calculated natural now points to get the correct intensity for the now point in natural mode
                        int diffSeconds = Math.abs(naturalNow1.totalSeconds - naturalNow2.totalSeconds);
                        int diffIntensity = Math.abs(naturalNow1.relativeIntensity - naturalNow2.relativeIntensity);
                        double intenseRate = (double)diffIntensity / diffSeconds;
                        int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - naturalNow1.totalSeconds);
                        if (naturalNow1.relativeIntensity > naturalNow2.relativeIntensity){
                            nowIntensity = (int)(naturalNow1.relativeIntensity - (nowSecondsAndIndexDifference * intenseRate));
                        }else{
                            nowIntensity = (int)(naturalNow1.relativeIntensity + (nowSecondsAndIndexDifference * intenseRate));
                        }
                        if (nowIntensity > 0){
                            dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }
                }
                //Finally, Add the element to the linearlayout
                scheduleTouchGrid.addView(viewArray.get(i));
            }
        }else{
            for (int i = 0; i < userSchedule.dataPoints.size() + 1; i++){
                viewArray.add(getLayoutInflater().inflate(R.layout.schedule_data_point, null));
                //set the alpha for each data point in artificial mode
                if (Build.VERSION.SDK_INT < 11){
                    if (i == 0){
                        AlphaAnimation alpha = new AlphaAnimation(1F, 1F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                        TextView dataPointText = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_time);
                        dataPointText.setTypeface(null, Typeface.BOLD);
                        TextView dataPointIntensity = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_intensity);
                        dataPointIntensity.setTypeface(null,Typeface.BOLD);
                    }else{
                        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                    }
                }else{
                    if (i == 0){
                        viewArray.get(i).setAlpha(1f);
                        TextView dataPointText = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_time);
                        dataPointText.setTypeface(null, Typeface.BOLD);
                        TextView dataPointIntensity = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_intensity);
                        dataPointIntensity.setTypeface(null,Typeface.BOLD);
                    }else{
                        viewArray.get(i).setAlpha(0.5f);
                    }
                }

                //If now point is added, make sure to user the schedule - 1 element after words, if not, then use the current element
                if (!nowPointAdded){
                    if (i == userSchedule.dataPoints.size()){
                        nowPointAdded = true;
                        nowIndex = i;
                        //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                        if (Build.VERSION.SDK_INT < 11){
                            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                            alpha.setDuration(0); // Make animation instant
                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                            viewArray.get(i).startAnimation(alpha);
                        }else{
                            viewArray.get(i).setAlpha(0.5f);

                        }
                        //Format the Time to be displayed on now point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataTime.setText(R.string.now);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.dataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.dataPoints.get(i-1).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.dataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }

                        //Image for now point
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                        if (userSchedule.dataPoints.get(i-1).isStartDay || userSchedule.dataPoints.get(i-1).isStartNight){
                            if (userSchedule.dataPoints.get(i-1).cloudFrequency == 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.dataPoints.get(i-1).isStartDay){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }else{
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }else{
                                if (userSchedule.dataPoints.get(i-1).cloudFrequency > 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i-1).cloudFrequency == 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.dataPoints.get(i-1).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && dayIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i-1 > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.dataPoints.get(i-1).cloudFrequency > 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for now point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(R.string.custom);
                        //Relative Intensity for now point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        int nowIntensity = 0;
                        if(nowIndex > 0 && nowIndex < userSchedule.dataPoints.size()){
                            int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex-1).totalSeconds - userSchedule.dataPoints.get(nowIndex).totalSeconds);
                            int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - userSchedule.dataPoints.get(nowIndex).relativeIntensity);
                            double intenseRate = (double)diffIntensity / diffSeconds;
                            int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(nowIndex-1).totalSeconds);
                            if (userSchedule.dataPoints.get(nowIndex-1).relativeIntensity > userSchedule.dataPoints.get(nowIndex).relativeIntensity){
                                nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - (nowSecondsAndIndexDifference * intenseRate));
                            }else{
                                nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity + (nowSecondsAndIndexDifference * intenseRate));
                            }
                        }else{
                            //Conditions if now point comes first or last
                            if (nowIndex == userSchedule.dataPoints.size()){
                                int diffSeconds = Math.abs(userSchedule.dataPoints.get(0).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                int diffIntensity = Math.abs(userSchedule.dataPoints.get(0).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                                double intenseRate = (double) diffIntensity / diffSeconds;
                                int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                if (userSchedule.dataPoints.get(0).relativeIntensity < userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity){
                                    nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity - (int)(nowSecondsAndIndexDifference * intenseRate);
                                }else{
                                    nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity + (int)(nowSecondsAndIndexDifference * intenseRate);
                                }
                            }
                            if (nowIndex == 0){
                                int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                                double intenseRate = (double)diffIntensity / diffSeconds;
                                int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(0).totalSeconds);
                                nowIntensity = (int)(nowSecondsAndIndexDifference * intenseRate);
                            }
                        }
                        if (nowIntensity > 0){
                            dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }else{
                        //Format the Time to be displayed on each data point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        Calendar timeConvert = Calendar.getInstance();
                        timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.dataPoints.get(i).startTimeHour);
                        timeConvert.set(Calendar.MINUTE, userSchedule.dataPoints.get(i).startTimeMinute);
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        String displayTime = formatter.format(timeConvert.getTime());
                        dataTime.setText(displayTime);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.dataPoints.get(i).isStartDay){
                            dataStatus.setText(R.string.start_day);
                        }else if(userSchedule.dataPoints.get(i).isStartNight){
                            dataStatus.setText(R.string.start_night);
                        }else{
                            if (userSchedule.dataPoints.get(i).stormFrequency > 0){
                                int stormChance = userSchedule.dataPoints.get(i).stormFrequency * 10;
                                dataStatus.setText(stormChance + "%");
                            }else if(userSchedule.dataPoints.get(i).cloudFrequency > 0){
                                dataStatus.setText(R.string.cloudy);
                            }else{
                                dataStatus.setText(R.string.clear);
                            }
                        }

                        //Image for each data point
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        if (userSchedule.dataPoints.get(i).isStartDay || userSchedule.dataPoints.get(i).isStartNight){
                            if (userSchedule.dataPoints.get(i).isStartDay){
                                dataImage.setImageResource(R.drawable.icon_sunrise_orange);
                            }else{
                                dataImage.setImageResource(R.drawable.icon_sunset_blue);
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i).cloudFrequency == 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                                if (userSchedule.dataPoints.get(i).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i > nightIndex && i < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }

                                    if (nightIndex > dayIndex && dayIndex > i){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i && i < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.dataPoints.get(i).cloudFrequency > 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for each data point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(userSchedule.dataPoints.get(i).presetName);
                        //Relative Intensity for each data point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        if (userSchedule.dataPoints.get(i).relativeIntensity > 0){
                            dataIntensity.setText(String.valueOf(userSchedule.dataPoints.get(i).relativeIntensity) + "%");
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }
                }else{
                    //Format the Time to be displayed on each data point
                    TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                    Calendar timeConvert = Calendar.getInstance();
                    timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.dataPoints.get(i-1).startTimeHour);
                    timeConvert.set(Calendar.MINUTE, userSchedule.dataPoints.get(i-1).startTimeMinute);
                    SimpleDateFormat formatter;
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        formatter = new SimpleDateFormat("h:mm aa");
                    }else{
                        formatter = new SimpleDateFormat("k:mm");
                    }
                    String displayTime = formatter.format(timeConvert.getTime());
                    dataTime.setText(displayTime);
                    //Status for each data point
                    TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                    if (userSchedule.dataPoints.get(i-1).isStartDay){
                        dataStatus.setText(R.string.start_day);
                    }else if(userSchedule.dataPoints.get(i-1).isStartNight){
                        dataStatus.setText(R.string.start_night);
                    }else{
                        if (userSchedule.dataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.dataPoints.get(i-1).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.dataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }
                    }

                    //Image for each data point
                    ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                    if (userSchedule.dataPoints.get(i-1).isStartDay || userSchedule.dataPoints.get(i-1).isStartNight){
                        if (userSchedule.dataPoints.get(i-1).isStartDay){
                            dataImage.setImageResource(R.drawable.icon_sunrise_orange);
                        }else{
                            dataImage.setImageResource(R.drawable.icon_sunset_blue);
                        }
                    }else{
                        if (userSchedule.dataPoints.get(i-1).cloudFrequency == 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                            if (userSchedule.dataPoints.get(i-1).isNightMode){
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }else{
                                if(nightIndex < dayIndex && dayIndex < i-1){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if (nightIndex < dayIndex && nightIndex > i-1){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }

                                if (nightIndex > dayIndex && dayIndex > i-1){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex > dayIndex && i-1 > nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i-1).cloudFrequency > 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }

                    //Custom or Preset for each data point
                    TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                    dataPointType.setText(userSchedule.dataPoints.get(i-1).presetName);
                    //Relative Intensity for each data point
                    TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                    if (userSchedule.dataPoints.get(i-1).relativeIntensity > 0){
                        dataIntensity.setText(String.valueOf(userSchedule.dataPoints.get(i-1).relativeIntensity) + "%");
                    }else{
                        dataIntensity.setText(R.string.off);
                    }
                }

                //NOW POINT FOR ARTIFICIAL MODE
                //Make now point be the first point if it is before all other data points && = midnight or above
                if (!nowPointAdded && nowTotalSeconds < userSchedule.dataPoints.get(0).totalSeconds){
                    nowPointAdded = true;
                    nowIndex = i;
                    //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                    if (Build.VERSION.SDK_INT < 11){
                        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                        alpha.setDuration(0); // Make animation instant
                        alpha.setFillAfter(true); // Tell it to persist after the animation ends
                        viewArray.get(i).startAnimation(alpha);
                    }else{
                        viewArray.get(i).setAlpha(0.5f);
                    }
                    //Format the Time to be displayed on now point
                    TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                    dataTime.setText(R.string.now);
                    //Status for each data point
                    TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                    if (userSchedule.dataPoints.get(i).stormFrequency > 0){
                        int stormChance = userSchedule.dataPoints.get(i).stormFrequency * 10;
                        dataStatus.setText(stormChance + "%");
                    }else if(userSchedule.dataPoints.get(i).cloudFrequency > 0){
                        dataStatus.setText(R.string.cloudy);
                    }else{
                        dataStatus.setText(R.string.clear);
                    }

                    //Image for now point
                    ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                    if (userSchedule.dataPoints.get(i).isStartDay || userSchedule.dataPoints.get(i).isStartNight){
                        if (userSchedule.dataPoints.get(i).cloudFrequency == 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                            if (userSchedule.dataPoints.get(i).isStartDay){
                                dataImage.setImageResource(R.drawable.icon_sun_yellow);
                            }else{
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i).cloudFrequency > 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }else{
                        if (userSchedule.dataPoints.get(i).cloudFrequency == 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                            if (userSchedule.dataPoints.get(i).isNightMode){
                                dataImage.setImageResource(R.drawable.icon_moon);
                            }else{
                                if(nightIndex < dayIndex && dayIndex < i){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex < dayIndex && i > nightIndex && i < dayIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if (nightIndex < dayIndex && nightIndex > i){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }

                                if (nightIndex > dayIndex && dayIndex > i){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                                if(nightIndex > dayIndex && dayIndex < i && i < nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }
                                if (nightIndex > dayIndex && i > nightIndex){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i).cloudFrequency > 0 && userSchedule.dataPoints.get(i).stormFrequency == 0){
                                dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                            }else{
                                //stormfrequency > 0
                                dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                            }
                        }
                    }

                    //Custom or Preset for now point
                    TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                    dataPointType.setText(R.string.custom);
                    //Relative Intensity for now point
                    TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                    dataIntensity.setText("Relative %");
                    int nowIntensity = 0;
                    if(nowIndex > 0 && nowIndex < userSchedule.dataPoints.size()){
                        int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex-1).totalSeconds - userSchedule.dataPoints.get(nowIndex).totalSeconds);
                        int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - userSchedule.dataPoints.get(nowIndex).relativeIntensity);
                        double intenseRate = (double)diffIntensity / diffSeconds;
                        int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(nowIndex-1).totalSeconds);
                        if (userSchedule.dataPoints.get(nowIndex-1).relativeIntensity > userSchedule.dataPoints.get(nowIndex).relativeIntensity){
                            nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - (nowSecondsAndIndexDifference * intenseRate));
                        }else{
                            nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity + (nowSecondsAndIndexDifference * intenseRate));
                        }
                    }else{
                        //Conditions if now point comes first or last
                        if (nowIndex == userSchedule.dataPoints.size()){
                            int diffSeconds = Math.abs(userSchedule.dataPoints.get(0).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                            int diffIntensity = Math.abs(userSchedule.dataPoints.get(0).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                            double intenseRate = (double) diffIntensity / diffSeconds;
                            int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                            if (userSchedule.dataPoints.get(0).relativeIntensity < userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity){
                                nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity - (int)(nowSecondsAndIndexDifference * intenseRate);
                            }else{
                                nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity + (int)(nowSecondsAndIndexDifference * intenseRate);
                            }
                        }
                        if (nowIndex == 0){
                            int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                            int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                            double intenseRate = (double)diffIntensity / diffSeconds;
                            int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(0).totalSeconds);
                            nowIntensity = (int)(nowSecondsAndIndexDifference * intenseRate);
                        }
                    }
                    if (nowIntensity > 0){
                        dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                    }else{
                        dataIntensity.setText(R.string.off);
                    }
                }

                //Handle the addition of the now point if it is in between the beginning and end of the schedule list of points
                if (!nowPointAdded && i != 0){
                    //if the current time is greater than the previous element and less than the current element, replace current element
                    if (nowTotalSeconds > userSchedule.dataPoints.get(i-1).totalSeconds && nowTotalSeconds < userSchedule.dataPoints.get(i).totalSeconds){
                        nowPointAdded = true;
                        nowIndex = i;
                        //ADDITION OF NOW POINT TO BE USED IN SCHEDULE FOR NATURAL MODE
                        if (Build.VERSION.SDK_INT < 11){
                            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
                            alpha.setDuration(0); // Make animation instant
                            alpha.setFillAfter(true); // Tell it to persist after the animation ends
                            viewArray.get(i).startAnimation(alpha);
                        }else{
                            viewArray.get(i).setAlpha(0.5f);
                        }
                        //Format the Time to be displayed on now point
                        TextView dataTime = (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_time);
                        dataTime.setText(R.string.now);
                        //Status for each data point
                        TextView dataStatus= (TextView)viewArray.get(i).findViewById(R.id.datapoint_display_status);
                        if (userSchedule.dataPoints.get(i-1).stormFrequency > 0){
                            int stormChance = userSchedule.dataPoints.get(i-1).stormFrequency * 10;
                            dataStatus.setText(stormChance + "%");
                        }else if(userSchedule.dataPoints.get(i-1).cloudFrequency > 0){
                            dataStatus.setText(R.string.cloudy);
                        }else{
                            dataStatus.setText(R.string.clear);
                        }

                        //Image for now point
                        ImageView dataImage = (ImageView) viewArray.get(i).findViewById(R.id.datapoint_display_image);
                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                        if (userSchedule.dataPoints.get(i-1).isStartDay || userSchedule.dataPoints.get(i-1).isStartNight){

                            if (userSchedule.dataPoints.get(i-1).cloudFrequency == 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.dataPoints.get(i-1).isStartDay){
                                    dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                }else{
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }
                            }else{
                                if (userSchedule.dataPoints.get(i-1).cloudFrequency > 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }else{
                            if (userSchedule.dataPoints.get(i-1).cloudFrequency == 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                if (userSchedule.dataPoints.get(i-1).isNightMode){
                                    dataImage.setImageResource(R.drawable.icon_moon);
                                }else{
                                    if(nightIndex < dayIndex && dayIndex < i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex < dayIndex && i-1 > nightIndex && i-1 < dayIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if (nightIndex < dayIndex && nightIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }

                                    if (nightIndex > dayIndex && dayIndex > i-1){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                    if(nightIndex > dayIndex && dayIndex < i-1 && i-1 < nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_sun_yellow);
                                    }
                                    if (nightIndex > dayIndex && i-1 > nightIndex){
                                        dataImage.setImageResource(R.drawable.icon_moon);
                                    }
                                }
                            }else{
                                if (userSchedule.dataPoints.get(i-1).cloudFrequency > 0 && userSchedule.dataPoints.get(i-1).stormFrequency == 0){
                                    dataImage.setImageResource(R.drawable.button_cloud_cover_white);
                                }else{
                                    //stormfrequency > 0
                                    dataImage.setImageResource(R.drawable.icon_thunderstorm_white);
                                }
                            }
                        }

                        //Custom or Preset for now point
                        TextView dataPointType= (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_type);
                        dataPointType.setText(R.string.custom);
                        //Relative Intensity for now point
                        TextView dataIntensity = (TextView) viewArray.get(i).findViewById(R.id.datapoint_display_intensity);
                        int nowIntensity = 0;
                        if(nowIndex > 0 && nowIndex < userSchedule.dataPoints.size()){
                            int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex-1).totalSeconds - userSchedule.dataPoints.get(nowIndex).totalSeconds);
                            int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - userSchedule.dataPoints.get(nowIndex).relativeIntensity);
                            double intenseRate = (double)diffIntensity / diffSeconds;
                            int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(nowIndex-1).totalSeconds);
                            if (userSchedule.dataPoints.get(nowIndex-1).relativeIntensity > userSchedule.dataPoints.get(nowIndex).relativeIntensity){
                                nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity - (nowSecondsAndIndexDifference * intenseRate));
                            }else{
                                nowIntensity = (int)(userSchedule.dataPoints.get(nowIndex-1).relativeIntensity + (nowSecondsAndIndexDifference * intenseRate));
                            }
                        }else{
                            //Conditions if now point comes first or last
                            if (nowIndex == userSchedule.dataPoints.size()){
                                int diffSeconds = Math.abs(userSchedule.dataPoints.get(0).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                int diffIntensity = Math.abs(userSchedule.dataPoints.get(0).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                                double intenseRate = (double) diffIntensity / diffSeconds;
                                int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                if (userSchedule.dataPoints.get(0).relativeIntensity < userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity){
                                    nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity - (int)(nowSecondsAndIndexDifference * intenseRate);
                                }else{
                                    nowIntensity = userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity + (int)(nowSecondsAndIndexDifference * intenseRate);
                                }
                            }
                            if (nowIndex == 0){
                                int diffSeconds = Math.abs(userSchedule.dataPoints.get(nowIndex).totalSeconds - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).totalSeconds);
                                int diffIntensity = Math.abs(userSchedule.dataPoints.get(nowIndex).relativeIntensity - userSchedule.dataPoints.get(userSchedule.dataPoints.size()-1).relativeIntensity);
                                double intenseRate = (double)diffIntensity / diffSeconds;
                                int nowSecondsAndIndexDifference = Math.abs(nowTotalSeconds - userSchedule.dataPoints.get(0).totalSeconds);
                                nowIntensity = (int)(nowSecondsAndIndexDifference * intenseRate);
                            }
                        }
                        if (nowIntensity > 0){
                            dataIntensity.setText(String.valueOf(nowIntensity + "%"));
                        }else{
                            dataIntensity.setText(R.string.off);
                        }
                    }
                }
                scheduleTouchGrid.addView(viewArray.get(i));
            }

        }

        //HANDLE THE DEFAULT SETTINGS OF THE FIRST ITEM IN THE ARRAY SINCE WE ARE ALWAYS STARTING FROM IT (ANDROID VERSION ONLY)
        //Bold Text on First Item of array as default
        /*TextView dataPointText = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_time);
        dataPointText.setTypeface(null, Typeface.BOLD);
        TextView dataPointIntensity = (TextView) viewArray.get(0).findViewById(R.id.datapoint_display_intensity);
        dataPointIntensity.setTypeface(null,Typeface.BOLD);*/

        //Set the attributes of the now point similar the first element in the schedule
        /*if (userSchedule.scheduleType.equalsIgnoreCase("N")) {
            if(userSchedule.filteredDataPoints.get(0).cloudFrequency > 0 || userSchedule.filteredDataPoints.get(0).stormFrequency > 0){
                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                NavigationWheel.wheelActivity.nightMoon.setVisibility(View.INVISIBLE);
                if (nightIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.INVISIBLE);
                }
                if (dayIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.INVISIBLE);
                }
                if (nightIndex != 0 && dayIndex != 0){
                    if (nightIndex > dayIndex){
                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                        NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                    }
                    if (dayIndex > nightIndex){
                        backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                        nightMoon.setVisibility(View.INVISIBLE);
                    }
                }
            } else if (userSchedule.filteredDataPoints.get(0).isStartDay){
                backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                nightMoon.setVisibility(View.INVISIBLE);
            }else if (userSchedule.filteredDataPoints.get(0).isNightMode) {
                NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
            } else {
                if (nightIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                }
                if (dayIndex == 0){
                    backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                    nightMoon.setVisibility(View.INVISIBLE);
                }

                if (nightIndex != 0 && dayIndex != 0){
                    if (nightIndex > dayIndex){
                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                        NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                    }
                    if (dayIndex > nightIndex){
                        backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                        nightMoon.setVisibility(View.INVISIBLE);
                    }
                }
            }
        } else {
            if(userSchedule.dataPoints.get(0).stormFrequency > 0 || userSchedule.dataPoints.get(0).cloudFrequency > 0){
                backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                nightMoon.setVisibility(View.INVISIBLE);
                if (nightIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.INVISIBLE);
                }
                if (dayIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.INVISIBLE);
                }
                if (nightIndex != 0 && dayIndex != 0){
                    if (nightIndex > dayIndex){
                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudynight_bg);
                        NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                    }
                    if (dayIndex > nightIndex){
                        backgroundLayout.setBackgroundResource(R.drawable.schedule_view_cloudy_bg);
                        nightMoon.setVisibility(View.INVISIBLE);
                    }
                }
            } else if (userSchedule.dataPoints.get(0).isStartDay){
                backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                nightMoon.setVisibility(View.INVISIBLE);
            } else if (userSchedule.dataPoints.get(0).isNightMode) {
                backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                nightMoon.setVisibility(View.VISIBLE);
            } else {
                if (nightIndex == 0){
                    NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                    NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                }
                if (dayIndex == 0){
                    backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                    nightMoon.setVisibility(View.INVISIBLE);
                }
                if (nightIndex != 0 && dayIndex != 0){
                    if (nightIndex > dayIndex){
                        NavigationWheel.wheelActivity.backgroundLayout.setBackgroundResource(R.drawable.schedule_view_night_bg);
                        NavigationWheel.wheelActivity.nightMoon.setVisibility(View.VISIBLE);
                    }
                    if (dayIndex > nightIndex){
                        backgroundLayout.setBackgroundResource(R.drawable.schedule_view_day_bg);
                        nightMoon.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }*/

        //set the click listener to handle the first element of the array
        viewArray.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar timeConvert = Calendar.getInstance();
                Intent intent = new Intent(getApplicationContext(), SchedulePointView.class);
                if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                    timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.filteredDataPoints.get(0).startTimeHour);
                    timeConvert.set(Calendar.MINUTE, userSchedule.filteredDataPoints.get(0).startTimeMinute);
                    //userSchedule related properties that must be passed
                    intent.putExtra("editUV",userSchedule.filteredDataPoints.get(0).uv);
                    intent.putExtra("editRB",userSchedule.filteredDataPoints.get(0).royalBlue1);
                    intent.putExtra("editRB2",userSchedule.filteredDataPoints.get(0).royalBlue2);
                    intent.putExtra("editBlue",userSchedule.filteredDataPoints.get(0).blue);
                    intent.putExtra("editWhite",userSchedule.filteredDataPoints.get(0).white);
                    intent.putExtra("editGreen",userSchedule.filteredDataPoints.get(0).green);
                    intent.putExtra("editRed",userSchedule.filteredDataPoints.get(0).red);
                    intent.putExtra("editBright",userSchedule.filteredDataPoints.get(0).brightness);
                    intent.putExtra("editCloud",userSchedule.filteredDataPoints.get(0).cloudFrequency);
                    intent.putExtra("editStorm",userSchedule.filteredDataPoints.get(0).stormFrequency);
                    intent.putExtra("editTimeHours",userSchedule.filteredDataPoints.get(0).startTimeHour);
                    intent.putExtra("editTimeMins",userSchedule.filteredDataPoints.get(0).startTimeMinute);
                    intent.putExtra("editTimeSeconds",userSchedule.filteredDataPoints.get(0).startTimeSeconds);
                    intent.putExtra("editPresetId",userSchedule.filteredDataPoints.get(0).presetId);
                    intent.putExtra("editPresetName",userSchedule.filteredDataPoints.get(0).presetName);
                    intent.putExtra("editIsNightMode",userSchedule.filteredDataPoints.get(0).isNightMode);
                    intent.putExtra("editIsStartDay",userSchedule.filteredDataPoints.get(0).isStartDay);
                    intent.putExtra("editIsStartNight",userSchedule.filteredDataPoints.get(0).isStartNight);
                    intent.putExtra("editDataPointId",userSchedule.filteredDataPoints.get(0).scheduleDataPointId);
                }else{
                    timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.dataPoints.get(0).startTimeHour);
                    timeConvert.set(Calendar.MINUTE, userSchedule.dataPoints.get(0).startTimeMinute);
                    //userSchedule related properties that must be passed
                    intent.putExtra("editUV",userSchedule.dataPoints.get(0).uv);
                    intent.putExtra("editRB",userSchedule.dataPoints.get(0).royalBlue1);
                    intent.putExtra("editRB2",userSchedule.dataPoints.get(0).royalBlue2);
                    intent.putExtra("editBlue",userSchedule.dataPoints.get(0).blue);
                    intent.putExtra("editWhite",userSchedule.dataPoints.get(0).white);
                    intent.putExtra("editGreen",userSchedule.dataPoints.get(0).green);
                    intent.putExtra("editRed",userSchedule.dataPoints.get(0).red);
                    intent.putExtra("editBright",userSchedule.dataPoints.get(0).brightness);
                    intent.putExtra("editCloud",userSchedule.dataPoints.get(0).cloudFrequency);
                    intent.putExtra("editStorm",userSchedule.dataPoints.get(0).stormFrequency);
                    intent.putExtra("editTimeHours",userSchedule.dataPoints.get(0).startTimeHour);
                    intent.putExtra("editTimeMins",userSchedule.dataPoints.get(0).startTimeMinute);
                    intent.putExtra("editTimeSeconds",userSchedule.dataPoints.get(0).startTimeSeconds);
                    intent.putExtra("editPresetId",userSchedule.dataPoints.get(0).presetId);
                    intent.putExtra("editPresetName",userSchedule.dataPoints.get(0).presetName);
                    intent.putExtra("editIsNightMode",userSchedule.dataPoints.get(0).isNightMode);
                    intent.putExtra("editIsStartDay",userSchedule.dataPoints.get(0).isStartDay);
                    intent.putExtra("editIsStartNight",userSchedule.dataPoints.get(0).isStartNight);
                    intent.putExtra("editDataPointId",userSchedule.dataPoints.get(0).scheduleDataPointId);
                }
                SimpleDateFormat formatter = new SimpleDateFormat("h:mm aa");
                ///MIGHT NOT WORK / TEST OUT
                String editPointTime = formatter.format(timeConvert.getTime());
                intent.putExtra("isEditPoint",true);
                intent.putExtra("userScheduleId",userSchedule.scheduleId);
                intent.putExtra("userScheduleType",userSchedule.scheduleType);
                intent.putExtra("editIndex",0); //used to grab specific point
                intent.putExtra("editPointTime",editPointTime);
                intent.putExtra("editPointHours",timeConvert.get(Calendar.HOUR_OF_DAY));
                intent.putExtra("editPointMins",timeConvert.get(Calendar.MINUTE));
                intent.putExtra("isEditPoint",true);
                startActivityForResult(intent,0);
            }
        });

        if (scheduleSeekChange){
            //set the on click listener to user the first elements data
            viewArray.get(0).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Calendar timeConvert = Calendar.getInstance();
                    if (userSchedule.scheduleType.equalsIgnoreCase("N")) {
                        timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.filteredDataPoints.get(0).startTimeHour);
                        timeConvert.set(Calendar.MINUTE, userSchedule.filteredDataPoints.get(0).startTimeMinute);
                    }else{
                        timeConvert.set(Calendar.HOUR_OF_DAY, userSchedule.dataPoints.get(0).startTimeHour);
                        timeConvert.set(Calendar.MINUTE, userSchedule.dataPoints.get(0).startTimeMinute);
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("h:mm aa");
                    String editPointTime = formatter.format(timeConvert.getTime());

                    if (userSchedule.scheduleType.equalsIgnoreCase("N")){
                        if (!userSchedule.filteredDataPoints.get(0).isStartDay && !userSchedule.filteredDataPoints.get(0).isStartNight){
                            AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                            myNewAlert.setMessage(R.string.delete_data_point);
                            myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                            myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ConnectManager connect = ConnectManager.getSharedInstance();
                                            connect.deleteScheduleDataPoint(userSchedule.filteredDataPoints.get(0));
                                        }
                                    }).start();
                                    NavigationWheel.wheelActivity.refreshSchedulePoints();
                                    dialog.cancel();
                                }
                            });

                            myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                            myNewAlert.show();
                        }
                    }else{
                        if (!userSchedule.dataPoints.get(0).isStartDay && !userSchedule.dataPoints.get(0).isStartNight){
                            AlertDialog.Builder myNewAlert = new AlertDialog.Builder(NavigationWheel.this);
                            myNewAlert.setMessage(R.string.delete_data_point);
                            myNewAlert.setTitle(getResources().getString(R.string.delete) + " " + getResources().getString(R.string.point) + " " + editPointTime);

                            myNewAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ConnectManager connect = ConnectManager.getSharedInstance();
                                            connect.deleteScheduleDataPoint(userSchedule.dataPoints.get(0));
                                        }
                                    }).start();
                                    NavigationWheel.wheelActivity.refreshSchedulePoints();
                                    dialog.cancel();
                                }
                            });

                            myNewAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                            myNewAlert.show();
                        }
                    }
                    return true;
                }
            });
        }

        pointScroller.setViewArray(viewArray);
        scheduleLayout = (RelativeLayout)findViewById(R.id.schedule_mode_options);
        scheduleListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Handle Scroll View after Array gets processed
                if (scheduleLayout != null) {
                    pointScroller.setScheduleLayout(scheduleLayout);
                    //if the dataPointWidth is 0, then we need to get the size of a single view, else never fire this to avoid null value
                    dataPointWidth = viewArray.get(0).getWidth() / 2;
                    if (nowIndex == 0) {
                        dataPointWidth = viewArray.get(1).getWidth() / 2;
                    }

                    //Calculate the now point distance in the global listener of the layout to be used to immediately scroll to the now point on initial loading of schedule
                    nowPointDistance = viewArray.get(nowIndex).getLeft() + dataPointWidth;


                    //scroll to now point every time schedule view is loaded
                    pointScroller.setDefaults(scheduleLayout.getWidth() / 2, dataPointWidth, userSchedule, nowIndex, nightIndex, dayIndex);

                    //ensure this only runs one time for performance
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        scheduleLayout.getViewTreeObserver().removeOnGlobalLayoutListener(scheduleListener);
                    } else {
                        scheduleLayout.getViewTreeObserver().removeGlobalOnLayoutListener(scheduleListener);
                    }
                }
            }
        };
        scheduleLayout.getViewTreeObserver().addOnGlobalLayoutListener(scheduleListener);

        //Wire up the sliders
        overallBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int remainder = progress % 5;
                progress = progress - remainder;
                overallBrightValue.setText(progress + "%");
                seekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("Seek Bar Progress" + seekBar.getProgress());
                newValue = ((float)seekBar.getProgress() * 2.0f / 100);
                System.out.println("New Value to be params brightness = " + newValue);
                Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                startActivity(intent);
                if (pointScroller != null){
                    pointScroller.fullScroll(View.FOCUS_LEFT);
                    pointScroller.currentIndex = 0;
                    scheduleSeekChange = true;
                }
                if (userSchedule.scheduleType.equalsIgnoreCase("A")){
                    //save brightness for artificial
                    artificialBright = seekBar.getProgress();
                    //save to save preferences since the view will get stopped by the next activity for edit and new points
                    edit.putInt("artificialBrightness", artificialBright);
                    edit.commit();
                    for(int i = 0; i < userSchedule.dataPoints.size(); i++){
                        ScheduleDataPoint dataPoint = userSchedule.dataPoints.get(i);
                        if(!dataPoint.isNightMode){
                            dataPoint.brightness = newValue;

                            // Only update the data point on the server if we are using artificial
                            new UpdateScheduleDataPointTask(dataPoint).execute();
                        }
                        //Do a loadschedule data task on the final item
                        if (i == userSchedule.dataPoints.size() - 1){
                            new LoadScheduleDataTask().execute();
                        }
                    }
                }else{
                    //save brightness for natural
                    naturalBright = seekBar.getProgress();
                    if(newValue != userSchedule.paramOverallBrightness){
                        userSchedule.paramOverallBrightness = newValue;
                        new UpdateNaturalModeParamsTask().execute();
                    }
                }
            }
        });

        cloudSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int remainder = progress % 10;
                progress = progress - remainder;
                if (remainder == 0){
                    cloudValue.setText(progress + "%");
                }else{
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int cloudProgress = seekBar.getProgress() / 10;
                System.out.println("Cloud Value = " + cloudProgress);
                if(cloudProgress != userSchedule.paramCloudFrequency){
                    Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                    startActivity(intent);
                    if (pointScroller != null){
                        pointScroller.fullScroll(View.FOCUS_LEFT);
                        pointScroller.currentIndex = 0;
                        scheduleSeekChange = true;
                    }
                    userSchedule.paramCloudFrequency = cloudProgress;
                    new UpdateNaturalModeParamsTask().execute();
                }
            }
        });

        stormSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int remainder = progress % 10;
                progress = progress - remainder;
                if (remainder == 0){
                    stormValue.setText(progress + "%");
                }else{
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int stormProgress = seekBar.getProgress() / 10;
                System.out.println("Storm Value = " + stormProgress);
                if(stormProgress != userSchedule.paramStormFrequency){
                    Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                    startActivity(intent);
                    if (pointScroller != null){
                        pointScroller.fullScroll(View.FOCUS_LEFT);
                        pointScroller.currentIndex = 0;
                        scheduleSeekChange = true;
                    }
                    userSchedule.paramStormFrequency = stormProgress;
                    new UpdateNaturalModeParamsTask().execute();
                }
            }
        });

        depthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int remainder = progress % 3;
                progress = progress - remainder;
                if (remainder == 0){
                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                        depthValue.setText(progress + " ft");
                    }else{
                        int convertedMeters = (int)(progress * 0.3048);
                        depthValue.setText(convertedMeters + " m");
                    }
                }else{
                    seekBar.setProgress(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int depthOffsetProgress = seekBar.getProgress();
                System.out.println("Depth Value = " + depthOffsetProgress);
                if(depthOffsetProgress != userSchedule.paramDepthOffset){
                    Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
                    startActivity(intent);
                    if (pointScroller != null){
                        pointScroller.fullScroll(View.FOCUS_LEFT);
                        pointScroller.currentIndex = 0;
                        scheduleSeekChange = true;
                    }
                    userSchedule.paramDepthOffset = depthOffsetProgress;
                    new UpdateNaturalModeParamsTask().execute();
                }
            }
        });

        //Handle Navigation
        ImageButton navMenu = (ImageButton)findViewById(R.id.schedule_navigation_menu);
        navMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (schedulePlaying){
                    stopSchedulePlayback();
                }
                Intent intent = new Intent(getApplicationContext(),ScheduleNavigationOverlay.class);
                Bundle schedBundle = new Bundle();
                schedBundle.putInt("groupId", selectedGroup.getGroupId());
                schedBundle.putString("groupName", selectedGroup.getName());
                schedBundle.putString("scheduleType", userSchedule.scheduleType);
                intent.putExtras(schedBundle);
                startActivityForResult(intent, 0);
            }
        });

        ImageButton newPoint = (ImageButton)findViewById(R.id.schedule_new_point);
        newPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (schedulePlaying){
                    stopSchedulePlayback();
                }
                Intent intent = new Intent(NavigationWheel.this,SchedulePointView.class);
                intent.putExtra("schedulePointTimes", schedulePointTimes);
                intent.putExtra("scheduleType",userSchedule.scheduleType);
                intent.putExtra("scheduleId",userSchedule.scheduleId);
                intent.putExtra("startDayTime", startDayPoint.totalSeconds);
                intent.putExtra("startNightTime", startNightPoint.totalSeconds);
                startActivityForResult(intent, 0);
            }
        });

        //Handle Schedule Playback on view
        final ImageButton playSchedule = (ImageButton)findViewById(R.id.schedule_play_button);
        final ImageButton stopSchedule = (ImageButton)findViewById(R.id.schedule_stop_button);
        final TextView scheduleTime = (TextView)findViewById(R.id.schedule_playback_time);
        stopSchedule.setVisibility(View.INVISIBLE);

        playSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedulePlaybackOverlay.setVisibility(View.VISIBLE);
                Drawable playbackBackground = schedulePlaybackOverlay.getBackground();
                if (Build.VERSION.SDK_INT < 11){
                    AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
                    alpha.setDuration(0); // Make animation instant
                    alpha.setFillAfter(true); // Tell it to persist after the animation ends
                    playbackBackground.setAlpha(180);
                }else{
                    playbackBackground.setAlpha(180);
                }
                stopSchedule.setVisibility(View.VISIBLE);
                playSchedule.setVisibility(View.INVISIBLE);
                scheduleTime.setText("12:00 AM");

                final Calendar now = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY,24);
                now.set(Calendar.MINUTE, 0);

                GroupPlayTask playbackTask = new GroupPlayTask(selectedGroup.getGroupId());
                playbackTask.execute();
                schedulePlaying = true;
                playTimer = new Timer();
                playTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        SimpleDateFormat formatter;
                        if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                            formatter = new SimpleDateFormat("h:mm aa");
                        }else{
                            formatter = new SimpleDateFormat("k:mm");
                        }
                        currentTime = formatter.format(now.getTime());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (scheduleTime != null) {
                                    scheduleTime.setText(currentTime);
                                }
                            }
                        });
                        if (!ensureStop) {
                            now.add(Calendar.MINUTE, 1);
                        }else{
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        ConnectManager connectESL = ConnectManager.getSharedInstance();
                                        connectESL.stopPCControl(0);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    playSchedule.setVisibility(View.VISIBLE);
                                    schedulePlaybackOverlay.setVisibility(View.GONE);
                                    if (Build.VERSION.SDK_INT < 11){
                                        schedulePlaybackOverlay.clearAnimation();
                                    }
                                    stopSchedule.setVisibility(View.INVISIBLE);
                                    if (scheduleTime != null){
                                        scheduleTime.setText("12:00 AM");
                                    }
                                }
                            });
                            ensureStop = false;
                            playTimer.cancel();
                        }
                        if (currentTime.equalsIgnoreCase("11:59 PM")) {
                            ensureStop = true;
                        }
                    }
                }, 1000, (long)20.83);
            }
        });

        stopSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTimer.cancel();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ConnectManager connectESL = ConnectManager.getSharedInstance();
                            connectESL.stopPCControl(0);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                playSchedule.setVisibility(View.VISIBLE);
                schedulePlaybackOverlay.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT < 11){
                    schedulePlaybackOverlay.clearAnimation();
                }
                stopSchedule.setVisibility(View.INVISIBLE);
                ensureStop = false;
                scheduleTime.setText("12:00 AM");
            }
        });
    }

    public class UpdateScheduleDataPointTask extends AsyncTask<Void,Void,Void>{
        ScheduleDataPoint dataPoint = new ScheduleDataPoint();
        UpdateScheduleDataPointTask(ScheduleDataPoint dataPoint){
            this.dataPoint = dataPoint;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.updateScheduleDataPoint(dataPoint, userSchedule.scheduleId);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class UpdateNaturalModeParamsTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.updateScheduleParams(sp.getInt("groupId",0),userSchedule.scheduleId,userSchedule.paramOverallBrightness,userSchedule.paramDepthOffset,userSchedule.paramCloudFrequency,userSchedule.paramStormFrequency);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new LoadScheduleDataTask().execute();
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

        final TextView uvText = (TextView) findViewById(R.id.uv_text);

        uvBar.setProgress(50);
        royalBar.setProgress(50);
        blueBar.setProgress(50);
        whiteBar.setProgress(50);
        greenBar.setProgress(50);
        redBar.setProgress(50);
        brightBar.setProgress(50);

        uvLabel.setText("50%");
        royalLabel.setText("50%");
        blueLabel.setText("50%");
        whiteLabel.setText("50%");
        greenLabel.setText("50%");
        redLabel.setText("50%");
        brightLabel.setText("50%");

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

    public void getSliderValues(){

        final SeekBar uvBar = (SeekBar) findViewById(R.id.uv_left_track);
        final SeekBar royalBar = (SeekBar) findViewById(R.id.royal_blue_left_track);
        final SeekBar blueBar = (SeekBar) findViewById(R.id.blue_left_track);
        final SeekBar whiteBar = (SeekBar) findViewById(R.id.white_left_track);
        final SeekBar greenBar = (SeekBar) findViewById(R.id.green_left_track);
        final SeekBar redBar = (SeekBar) findViewById(R.id.red_left_track);
        final SeekBar brightBar = (SeekBar) findViewById(R.id.brightness_track);

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
                    colorchange.sendColorChangeForSliders(selectedGroup.getGroupId(),uvValue,royalValue,blueValue,whiteValue,greenValue,redValue,brightValue);

                    //create new preset to be sent to SavePresetView
                    ConnectManager connect = ConnectManager.getSharedInstance();
                    connect.setNewPreset(uvValue,royalValue,blueValue,whiteValue,greenValue,redValue);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void clickActionBar(View v){
        switch(v.getId()){
            case R.id.action_button1:
                if (schedulePlaying){
                    stopSchedulePlayback();
                }
                stopMedia();
                if (Build.VERSION.SDK_INT >= 14){
                    if(!(navWheel.isDrawerOpen(Gravity.START))){
                        navWheel.closeDrawers();
                        navWheel.openDrawer(Gravity.START);
                    }else{
                        navWheel.closeDrawer(Gravity.START);
                    }
                }else{
                    if(!(navWheel.isDrawerOpen(Gravity.LEFT))){
                        navWheel.closeDrawers();
                        navWheel.openDrawer(Gravity.LEFT);
                    }else{
                        navWheel.closeDrawer(Gravity.LEFT);

                    }
                }
                break;
            case R.id.action_button2:
                if (schedulePlaying){
                    stopSchedulePlayback();
                }
                stopMedia();
                if (Build.VERSION.SDK_INT >= 14){
                    if(!(navWheel.isDrawerOpen(Gravity.END))){
                        navWheel.closeDrawers();
                        navWheel.openDrawer(Gravity.END);
                    }else{
                        navWheel.closeDrawer(Gravity.END);
                    }
                }else{
                    if(!(navWheel.isDrawerOpen(Gravity.RIGHT))){
                        navWheel.closeDrawers();
                        navWheel.openDrawer(Gravity.RIGHT);
                    }else{
                        navWheel.closeDrawer(Gravity.RIGHT);
                    }
                }
                break;
        }
    }

    public void displayLoginOnList(){
        SharedPreferences sp = getSharedPreferences("USER_PREF",0);
        userName = sp.getString("userName",null);
        loginEmail = sp.getString("userEmail", null);
        //loginExtras = getIntent().getExtras();
        //loginEmail = loginExtras.getString("email");
        TextView userView = (TextView) findViewById(R.id.userFooter);
        TextView passView = (TextView) findViewById(R.id.emailFooter);
        userView.setText(userName);
        passView.setText(loginEmail);
    }

    public void hideColorElements(){
        colorFlip.getChildAt(0).setVisibility(View.GONE);
        colorFlip.getChildAt(1).setVisibility(View.GONE);
        colorFlip.getChildAt(2).setVisibility(View.GONE);
    }

    public void signOut(View v){
        exitApplication();
        new StopPCControlTask().execute();
        //close Navigation Wheel and return to login view
        Intent intent = new Intent(getApplicationContext(), LoginView.class);
        startActivity(intent);
        finish();
    }

    public class StopPCControlTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.stopPCControl(30);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //open up the Shared Preferences Editor with the USER PREFS and remove them
            edit.clear();
            edit.commit();
        }
    }

    public class StopPCControlForPumpTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.stopPCControl(0);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void getDeviceStatusInGroup(int groupId){
        //used to count the pumps in each group every time a group is switched
        currentGroupPumpCount = 0;
        if (wsClient != null){
            //stop listening once you are connected
            //wsClient.deleteWebSocketMessageReceiver(NavigationWheel.wheelActivity);
            wsClient.addWebSocketMessageReceiver(NavigationWheel.wheelActivity);
        }
        for (Device device: allDevices){
            /*System.out.println("Device Id = " + device.getParentGroupId());
            System.out.println("Device Type = " + device.getDeviceType());
            System.out.println("Selected Group id = " + selectedGroup.getGroupId());*/
            if (device.getParentGroupId() == groupId && device.getDeviceType().equalsIgnoreCase("L")){
                //must do this since the thread will yet about the variable not being final
                final Device currentDevice = device;
                System.out.println("Current Device ID for Status = " + currentDevice.getDeviceId());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectManager cm = ConnectManager.getSharedInstance();
                        cm.getDeviceStatus(currentDevice.getDeviceId());
                    }
                }).start();

                break;
            }
        }
    }

    public void getDeviceStatusForPump(int groupId){
        //allocate specific arrays to be passing device data to pump list
        allPumpNames = new ArrayList<String>();
        allPumpModelNums = new ArrayList<Integer>();
        allPumpIds = new ArrayList<Integer>();
        for (Device device: allDevices){
            /*System.out.println("Device Id = " + device.getParentGroupId());
            System.out.println("Device Type = " + device.getDeviceType());
            System.out.println("Selected Group id = " + selectedGroup.getGroupId());*/
            if (device.getParentGroupId() == groupId && device.getDeviceType().equalsIgnoreCase("P") || groupId == 240 && device.getDeviceType().equalsIgnoreCase("P")){
                //must do this since the thread will yet about the variable not being final
                //final Device lastMatchedPump = device;
                allPumps.add(device);
                allPumpNames.add(device.getName());
                allPumpModelNums.add(device.getModelNumber());
                allPumpIds.add(device.getDeviceId());
                System.out.println("All pumps = " + allPumps.get(allPumps.indexOf(device)).getName());
            }
        }

        //refresh Main Menu so that if there is no Vortech, the Vortech Menu does not show
        currentGroupPumpCount = allPumps.size();
        System.out.println("Total pumps in group = " + currentGroupPumpCount);
        if (mainMenuAdapter != null){
            mainMenuAdapter.notifyDataSetChanged();
            mainMenuAdapter.notifyDataSetInvalidated();
            //You must collapse and expand the group in order to prevent view recycler bug
            for (int i = 0; i < mainMenuAdapter.getGroupCount(); i++){
                mainMenuList.collapseGroup(i);
                mainMenuList.expandGroup(i);
            }
        }

        final TextView pumpsInGroup = (TextView) findViewById(R.id.pumps_in_group);
        if (!allPumpNames.isEmpty() && !allPumpModelNums.isEmpty() && !allPumpIds.isEmpty()){
            pumpsInGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NavigationWheel.this, PumpSelectionList.class);
                    //Don't forget the damn try / catch with JSON Arrays!
                    intent.putExtra("allPumpNames", allPumpNames);
                    intent.putExtra("allPumpModelNums", allPumpModelNums);
                    intent.putExtra("allPumpIds", allPumpIds);
                    intent.putExtra("pumpIndex", pumpIndex);
                    startActivityForResult(intent, 3);
                }
            });
        }else{
            pumpsInGroup.setOnClickListener(null);
        }
        pumpsInGroup.setText(R.string.all_pumps);
    }

    public void loadWheelData(){
        //make sure this runs in the background on API 11 and lower

        new GroupListTask().execute();
        /*if (Build.VERSION.SDK_INT < 11){
            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            }).start();
        }else{
            new RadionPresetsTask().execute();
            new GroupListTask().execute();
        }*/

    }

    //attempt at using AsyncTask
    public class GroupListTask extends AsyncTask<Void,Void,Void> {

        //Async Task to do operation in background queue
        protected Void doInBackground(Void... arg0){
            try {
                Log.i("mydebug", "Load Group List");
                SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
                String wsHost = sp.getString("wsHost", null);
                JSONParser jParser = new JSONParser();
                String json = jParser.getJSONFromUrl(String.format("http://%s/mobile/devices/all", wsHost));
                JSONObject jsonData = jParser.getJSONObject(json);

                boolean groupListReceived = false;

                if(jsonData == null){
                    try{
                        String email = sp.getString("userEmail", null);
                        String password = sp.getString("userPass", null);
                        String secureLoginUrl = String.format("http://%s/j_spring_security_check", ConnectManager.webSocketHost);
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("j_username", email));
                        nameValuePairs.add(new BasicNameValuePair("j_password", password));

                        String response = jParser.postJSONToUrl(secureLoginUrl, nameValuePairs, false);
                        if(response != null){
                            json = jParser.getJSONFromUrl(String.format("http://%s/mobile/devices/all", wsHost));
                            jsonData = jParser.getJSONObject(json);
                            if(jsonData != null && jsonData.has("result")){
                                groupListReceived = true;
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else if(jsonData != null && jsonData.has("result")){
                    groupListReceived = true;
                }

                if(groupListReceived){
                    deviceGroups = new ArrayList<DeviceGroup>();

                    JSONArray devices = jsonData.getJSONArray("result");
                    int totalDeviceCount = devices.length();
                    totalRadionProCount = 0;
                    totalPumpCount = 0;

                    if(totalDeviceCount == 0){
                        groupListReceived = false;
                    }
                    else{
                        for(int i = 0; i < devices.length(); i++){
                            JSONObject d = (JSONObject)devices.get(i);

                            Device device = new Device();
                            device.setSerialNo(d.getString("serialNumber"));
                            device.setDeviceType(d.getString("deviceType"));
                            String model = d.getString("model");
                            device.setModel(d.getString("model"));
                            device.setModelNumber(d.getInt("modelNumber"));
                            device.setDeviceId(d.getInt("deviceId"));
                            device.setName(d.getString("name"));
                            device.setParentGroupId(d.getInt("parentGroupId"));

                            if(model.contains("Pro")){
                                device.setPro(true);
                                totalRadionProCount++;
                            }
                            else{
                                device.setPro(false);
                            }

                            if (model.contains("MP")){
                                device.setIsPump(true);
                                totalPumpCount++;
                            }else{
                                device.setIsPump(false);
                            }

                            device.setOsRev(d.getString("osVersion"));
                            device.setBootRev(d.getString("bootloaderVersion"));
                            device.setRfRev(d.getString("rfModuleVersion"));
                            device.setRfFrequency(d.getInt("rfFrequency"));

                            //New Device Properties being saved for Device Manager
                            device.setOperatingMode(d.getString("operatingMode"));
                            device.setCurrentOperatingMode(d.getString("currentOperatingMode"));
                            device.setErrorState(d.getString("errorState"));
                            device.setOperatingMode(d.getString("operatingMode"));
                            device.setLocalControl(d.getBoolean("localControl"));
                            device.setSubnet(d.getInt("subnetId"));
                            device.setMotorTemp(d.getInt("motorTemp"));
                            device.setRfStatus(d.getString("rfStatus"));
                            device.setPowerState(d.getString("powerState"));
                            device.setOperatingState(d.getString("currentOperatingState"));

                            //break down the time string from server and save to device properties
                            String time = d.getString("lastTime");
                            if(time != null && time.length() != 0){
                                String[] timeTokens = time.split(":");
                                if (timeTokens.length == 3){
                                    int hour = Integer.valueOf(timeTokens[0]);
                                    int minutes = Integer.valueOf(timeTokens[1]);

                                    device.setTimeHour(hour);
                                    device.setTimeMinute(minutes);
                                    device.setTimeSecond(0);

                                    //format the time received from the server and set the formattedTime property of device to it
                                    SimpleDateFormat formatter;
                                    if (Locale.getDefault().toString().equalsIgnoreCase("en_US")){
                                        //US Time
                                        formatter = new SimpleDateFormat("h:mm aa");
                                    }else{
                                        //Foreign Time
                                        formatter = new SimpleDateFormat("k:mm");
                                    }
                                    Calendar deviceTime = Calendar.getInstance();
                                    deviceTime.set(Calendar.HOUR_OF_DAY, hour);
                                    deviceTime.set(Calendar.MINUTE, minutes);
                                    deviceTime.set(Calendar.SECOND, 0);
                                    String newDeviceTime = formatter.format(deviceTime.getTime());
                                    //set the Formatted Time property to the formatted time that is parsed
                                    device.setFormattedTime(newDeviceTime);
                                }else{
                                    device.setFormattedTime(getResources().getString(R.string.not_available));
                                }
                            }

                            //added for Settings Page Data
                            //device.setAcclimationDay(d.getInt("aclimateCurrentDay"));
                            //device.setAcIntensity(d.getInt("acclimateCurrentIntensity"));
                            //device.setAcPeriod(d.getInt("aclimatePeriod"));

                            allDevices.add(device);

                            if(deviceGroups.size() == 0){
                                if(d.has("groups")){
                                    JSONArray dg = d.getJSONArray("groups");
                                    if(dg != null && dg.length() > 0){
                                        for(int j = 0; j < dg.length();j++){
                                            JSONObject deviceGroup = (JSONObject)dg.get(j);
                                            if(deviceGroup != null){
                                                DeviceGroup group = new DeviceGroup();
                                                group.setName(deviceGroup.getString("name").replace("&#39;","\'").replace("&#34;","\""));
                                                group.setGroupId(deviceGroup.getInt("groupId"));
                                                deviceGroups.add(group);
                                            }
                                        }
                                    }
                                }
                            }

                            for(DeviceGroup existingGroup: deviceGroups){
                                if(existingGroup.getGroupId() == device.getParentGroupId()){
                                    radionCount = existingGroup.getRadionCount();
                                    int radionProCount = existingGroup.getRadionProCount();
                                    int pumpCount = existingGroup.getPumpCount();
                                    //increment the radion count if the current device in the loop is not a pump
                                    if (!device.isPump()){
                                        existingGroup.setRadionCount(radionCount + 1);
                                    }
                                    if(device.isPro()){
                                        existingGroup.setRadionProCount(radionProCount + 1);
                                    }
                                    //increment pump count by one
                                    if (device.isPump()){
                                        existingGroup.setPumpCount(pumpCount + 1);
                                    }
                                    break;
                                }
                            }
                        }

                        if(deviceGroups.size() == 0){
                            Intent intent = new Intent(getApplicationContext(), WarningView.class);
                            startActivity(intent);
                        }
                        else{
                            // Now check for empty groups
                            ArrayList<DeviceGroup> groupsToRemove = new ArrayList<DeviceGroup>();
                            for(int i = 0; i < deviceGroups.size(); i++){
                                DeviceGroup group = deviceGroups.get(i);
                                if(group.getRadionCount() == 0 && group.getRadionProCount() == 0){
                                    groupsToRemove.add(group);
                                }
                            }

                            if(groupsToRemove.size() > 0){
                                for(int i = 0; i < groupsToRemove.size(); i++){
                                    deviceGroups.remove(groupsToRemove.get(i));
                                }
                            }

                            int totalRadionCount = totalDeviceCount - totalPumpCount;

                            DeviceGroup allGroups = new DeviceGroup();
                            allGroups.setGroupId(240);
                            allGroups.setName(getResources().getString(R.string.all_groups));
                            allGroups.setRadionCount(totalRadionCount);
                            allGroups.setRadionProCount(totalRadionProCount);
                            allGroups.setPumpCount(totalPumpCount);
                            deviceGroups.add(0, allGroups);
                        }
                    }
                }

                if(!groupListReceived){
                    Intent intent2 = new Intent(getApplicationContext(), SaveNewAquarium.class);
                    //intent2.putExtra("msg",getResources().getString(R.string.empty_profile));
                    //intent2.putExtra("msg2", getResources().getString(R.string.info_empty_profile));
                    intent2.putExtra("onLaunch", true);
                    startActivityForResult(intent2, 2);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new GroupListManager().execute();
        }
    }

    public class GroupListManager extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            //NEVER CHANGE THIS (This has to be here)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //runs the check login function that alerts user if wrong login info was entered
                    initGroupList();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Run on UI main thread due to UI manipulation deep within code
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (TransparentOverlay.transparentOverlayActivity != null){
                        TransparentOverlay.transparentOverlayActivity.finish();
                    }
                    initPreviewModeView();
                    //initPager();
                    initSliders();
                    initSettings();
                    new RadionPresetsTask().execute();
                }
            });
        }
    }

    //attempt at using AsyncTask
    public class RadionPresetsTask extends AsyncTask<Void,Void,Void> {

        //Async Task to do operation in background queue
        protected Void doInBackground(Void... arg0){

            try {
                Log.i("mydebug", "Load Presets");
                SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
                String wsHost = sp.getString("wsHost", null);
                JSONParser jParser = new JSONParser();
                String json = jParser.getJSONFromUrl(String.format("http://%s/livedemo/loadPreset", wsHost));
                JSONObject jsonData = jParser.getJSONObject(json);

                boolean presetsReceived = false;

                if(jsonData == null){
                    try{
                        String email = sp.getString("userEmail", null);
                        String password = sp.getString("userPass", null);
                        String secureLoginUrl = String.format("http://%s/j_spring_security_check", ConnectManager.webSocketHost);
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("j_username", email));
                        nameValuePairs.add(new BasicNameValuePair("j_password", password));

                        String response = jParser.postJSONToUrl(secureLoginUrl, nameValuePairs, false);
                        if(response != null){
                            json = jParser.getJSONFromUrl(String.format("http://%s/livedemo/loadPreset", wsHost));
                            jsonData = jParser.getJSONObject(json);
                            if(jsonData != null && jsonData.has("result")){
                                presetsReceived = true;
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else if(jsonData != null && jsonData.has("result")){
                    presetsReceived = true;
                }

                if (presetsReceived){
                    systemPresets = new ArrayList<Preset>();
                    userPresets = new ArrayList<Preset>();

                    JSONArray jsonPresets = jsonData.getJSONArray("result");

                    for(int i = 0; i < jsonPresets.length(); i++){
                        JSONObject p = (JSONObject)jsonPresets.get(i);

                        Preset preset = new Preset();
                        preset.setPresetId(p.getInt("presetId"));
                        preset.setName(p.getString("name"));
                        preset.setRoyalBlue((float)p.getDouble("royalBlue1"));
                        preset.setBlue((float)p.getDouble("blue"));
                        preset.setWhite((float)p.getDouble("coolWhite"));
                        preset.setRed((float)p.getDouble("hyperRed"));
                        preset.setGreen((float)p.getDouble("green"));
                        preset.setUv((float)p.getDouble("uv"));

                        Boolean systemPreset = p.getBoolean("systemPreset");
                        if (systemPreset){
                            systemPresets.add(preset);
                        }else{
                            userPresets.add(preset);
                        }
                    }
                }

                if(systemPresets == null || (systemPresets != null && (systemPresets.size() == 0 || systemPresets.isEmpty()))){
                    Intent intent = new Intent(getApplicationContext(), WarningView.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //runs the check login function that alerts user if wrong login info was entered
                            initRadionPresetsView();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (exAdapter != null){
                exAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT < 11){
                    presetList.expandGroup(0);
                    presetList.expandGroup(1);
                }
            }

            if(isDirty){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        screenFlip.setDisplayedChild(1);
                        colorFlip.setVisibility(View.INVISIBLE);
                        navWheel.closeDrawers();
                    }
                });

                isDirty = false;
            }
        }
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

            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendColorChangeForSliders(selectedGroup.getGroupId(), uv,royal,blue,0,green,red,brightValue);
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
                connect.sendColorChangeForSliders(selectedGroup.getGroupId(), previous.getUv(),previous.getRoyalBlue(),previous.getBlue(),previous.getWhite(),previous.getGreen(),previous.getRed(),brightness);
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

            float uv, royal, blue, white, green,red;
            uv = (float)touchUV;
            royal = (float)touchRoyal;
            blue = (float)touchBlue;
            white = (float)touchWhite;
            green = (float)touchGreen;
            red = (float)touchRed;

            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendColorChangeForSliders(selectedGroup.getGroupId(),uv,royal,blue,white,green,red,brightValue);
                connect.setNewPreset(uv, royal, blue, white, green, red);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class PresetLoad extends AsyncTask<Void,Void,Void>{
        int i;
        int i2;
        boolean b;

        public PresetLoad(int i, int i2,boolean b){
            this.i = i;
            this.i2 = i2;
            this.b = b;
        }

        protected Void doInBackground(Void... arg0){
            try{
                //create ConnectManager Object to send color change POST
                ConnectManager connectESL = ConnectManager.getSharedInstance();
                connectESL.sendColorChangeForGroupWithPreset(selectedGroup.getGroupId(), presetTable.get(i).get(i2), 1.0f);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class PreviewLoad extends AsyncTask<Void,Void,Void>{
        int position;
        boolean b;

        public PreviewLoad(int position,boolean b){
            this.position = position;
            this.b = b;
        }

        protected Void doInBackground(Void... arg0){
            try{
                ConnectManager connectESL = ConnectManager.getSharedInstance();
                connectESL.togglePreviewModeForGroup(selectedGroup.getGroupId(), this.position, this.b);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class IdentifyTask extends AsyncTask<Void,Void,Void>{
        int targetID;
        public IdentifyTask(int targetID){
            this.targetID = targetID;
        }

        protected Void doInBackground(Void... arg0){
            String target = "A";
            if (targetID != 240){
                target = "G";
            }
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.sendIdentifyForTarget(target, String.valueOf(targetID));
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class GroupPlayTask extends AsyncTask<Void,Void,Void>{
        int groupID;

        public GroupPlayTask(int groupID){
            this.groupID = groupID;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.startSchedulePlaybackForGroup(groupID);
                Log.i("Schedule Playing: ", String.valueOf(groupID));
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            schedulePlaying = false;
        }
    }

    public void setHeaderData(){
        //initialize ArrayList object
        headerItem = new ArrayList<String>();

        //Add the categories to the header list
        headerItem.add(getResources().getString(R.string.default_presets));
        headerItem.add(getResources().getString(R.string.my_presets));
    }

    public void setPumpPresetHeaders(){
        //initialize ArrayList object
        headerPumpItem = new ArrayList<String>();

        //Add the categories to the header list
        headerPumpItem.add(getResources().getString(R.string.default_pump_presets));
        headerPumpItem.add(getResources().getString(R.string.my_pump_presets));
    }

    public void setPumpPresetRows(){
        //initialize row of string data and Arraylist holding to hold the other list of strings
        pumpPresetTable = new ArrayList<ArrayList<PumpMode>>();
        pumpPresetTable.add(globalConnect.pumpSystemPresets);
        pumpPresetTable.add(globalConnect.pumpUserPresets);
    }

    public void setRowData(){
        //initialize row of string data and Arraylist holding to hold the other list of strings
        presetTable = new ArrayList<ArrayList<Preset>>();
        presetTable.add(systemPresets);
        presetTable.add(userPresets);
    }

    public void refreshSchedulePoints(){
        Intent intent = new Intent(NavigationWheel.this, RefreshOverlay.class);
        startActivity(intent);
        new LoadScheduleDataTask().execute();
    }

    //===== Liva Demo AsyncTasks ======
    public class LoadPumpModesTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.loadPumpModes();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class GetPumpModesTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                currentPulse = connect.getPumpMode(pumpIndex);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            System.out.println("LETS GO!!! \nPump Mode Display Name = " + currentPulse.displayName);
            System.out.println("Pump Mode Display Hex = " + currentPulse.displayColorHex);
        }
    }

    public class SetPumpToModeTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                if (allPumpsSelected){
                    connect.setToPumpMode(currentPulse, "G", selectedGroup.getGroupId());
                }else{
                    connect.setToPumpMode(currentPulse, "D", allPumpIds.get(currentPumpTargetIndex));
                }
                globalConnect.saveCurrentPulse(currentPulse);
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public class LoadPumpPresetsTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                ConnectManager connect = ConnectManager.getSharedInstance();
                connect.loadPumpPresets();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Global Pump User Presets Array Size = " + globalConnect.pumpUserPresets.size());
                    exAdapter.notifyDataSetChanged();
                    initPumpPresetsView();
                    presetLoading.setVisibility(View.INVISIBLE);
                    presetList.setVisibility(View.VISIBLE);
                }
            });

        }
    }



    //Screen Size & Density public methods
    public boolean isLargeScreen(){
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public boolean isNormalScreen(){
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public boolean isSmallScreen(){
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public boolean isXLargeScreen(){
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public boolean isLDPI(){
        return getResources().getDisplayMetrics().density == .75;
    }
    public boolean isMDPI(){
        return getResources().getDisplayMetrics().density == 1;
    }

    public boolean isTVDPI(){
        return getResources().getDisplayMetrics().density > 1 && getResources().getDisplayMetrics().density < 1.5;
    }

    public boolean isHDPI(){
        return getResources().getDisplayMetrics().density == 1.5;
    }

    public boolean isXHDPI(){
        return getResources().getDisplayMetrics().density == 2;
    }

    public boolean isXXHDPI(){
        return getResources().getDisplayMetrics().density == 3;
    }

    public boolean isSW720(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels == 720;
    }

    /**
     * Tests whether or not the network/internet is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onWebSocketMessageReceived(IESLWebSocketMessage eslWebSocketMessage) {
        if (eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessageType.GetDeviceStatus.getValue())){
            System.out.println("DEVICE STATUS IS RESPONDING!!!!!!!!!!!");
            JSONObject jsonMsg = null;
            String msg = eslWebSocketMessage.getMessage().toString();
            //try catch must be used when parsing a json formatted string
            try{
                jsonMsg = new JSONObject(msg);
                System.out.println("Normal JSON Message for Device Status = " + jsonMsg.getInt("lunar_phases_current_day"));
                lunarCurrentDay = jsonMsg.getInt("lunar_phases_current_day");
            }catch(Exception e){
                e.printStackTrace();
            }

            if (wsClient != null){
                //stop listening after you receive your device status
                wsClient.deleteWebSocketMessageReceiver(NavigationWheel.wheelActivity);
            }
        }

        if(eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessageType.ConnectedToEsl.getValue())){
            // CONNECTED TO ESL - GOOD TO GO!

            //This was for the old version of connecting to the websocket: ignore
            Log.e("Websocket", "Navigation Wheel is listening to the websocket");
            if(countDownTimer != null){
                countDownTimer.cancel();
            }
        }

        if (eslWebSocketMessage.getMessageType().toString().equalsIgnoreCase(ESLWebSocketMessageType.SetGroupSettingComplete.getValue())){
            System.out.println("Group Settings Collected!");
        }


    }

    private void connectToEsl(){
        // Check the internet connection
        if(isNetworkAvailable()){
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");

            try{
                // Open a web socket connection
                SharedPreferences sp = getSharedPreferences("USER_PREF", 0);
                String wsHost = sp.getString("wsHost", null);
                String wsPort = sp.getString("wsPort", "8881");
                Long userId = Long.parseLong(sp.getString("userID", null));
                wsClient = ESLWebSocketClient.sharedInstance(String.format("%s:%s", wsHost, wsPort), userId);
                wsClient.addWebSocketMessageReceiver(this);
                wsClient.connect();

                // Start our initial connection timer
                countDownTimer = new CountDownTimer(12000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        // NOT CONNECTED TO ESL
                        // Display warning screen using the WarningModeClientNotConnected message type
                        Intent intent = new Intent(getApplicationContext(), WarningView.class);
                        intent.putExtra("msg",getResources().getString(R.string.warning_not_connect));
                        intent.putExtra("msg2",getResources().getString(R.string.warning_not_connect2));
                        startActivity(intent);
                        finish();
                    }
                }.start();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            // NO INTERNET
            // Display warning screen using the WarningModeNoInternet message type
            Intent intent = new Intent(getApplicationContext(), WarningView.class);
            intent.putExtra("msg",getResources().getString(R.string.warning_no_internet));
            intent.putExtra("msg2",getResources().getString(R.string.warning_no_internet2));
            startActivity(intent);
            finish();
        }
    }
}