package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EJ Mann on 9/10/13.
 */

//Connection / JSON Parsing related libraries

//begin Main Activity code
public class LoginView extends Activity{
    final String loginUrl = "http://qabeta.ecosmartlive.com/login/thinclient";
    //final String loginUrl = "http://www.ecosmartlive.com/172.20.3.184";
    public static boolean loginSuccess;
    public static Context context;
    SharedPreferences sp;
    ProgressBar loginProgress;
    ImageView loginLogo;
    Boolean emailClicked;

    //declare variables to store text field views
    EditText emailView;
    EditText passwordView;
    String loginEmail;
    String loginPass;

    //For saving activity to be finished in Warning View
    public static LoginView loginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);

        loginActivity = this;
        emailClicked = false;

        //get sharedpreferences that will be saved into edittext fields to save users last login credentials
        SharedPreferences sp = getSharedPreferences("USER_PREF",0);
        String existingEmail = sp.getString("userEmail", null);
        String existingPass = sp.getString("userPass", null);

        //set global context to Main Activity view
        context = LoginView.this;

        //initialize views when the loginView is created
        emailView = (EditText) findViewById(R.id.enter_username);
        passwordView = (EditText) findViewById(R.id.enter_password);
        loginLogo = (ImageView)findViewById(R.id.login_logo);

        //final InputMethodManager imm = (InputMethodManager) LoginView.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        /*emailView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();

                return true;
            }
        });*/

        /*emailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_NEXT){
                    //passwordView.requestFocus();
                    emailClicked = true;
                    imm.showSoftInput(v,InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });

        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    loginLogo.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });*/

        //set password and username default text format
        emailView.setGravity(Gravity.CENTER);
        passwordView.setTypeface(Typeface.DEFAULT);
        passwordView.setGravity(Gravity.CENTER);
        loginProgress = (ProgressBar) findViewById(R.id.login_progress);
        loginProgress.setVisibility(View.INVISIBLE);

        //set EditText Views to user's last login credentials
        if (existingEmail != null && existingPass != null){
            emailView.setText(existingEmail);
            passwordView.setText(existingPass);
        }
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

    //execute and handle all login related tasks
    public void executeLogin(View v){
        //check for internet and fire warning if internet is still not available
        checkForInternet();
    }

    private void checkForInternet(){
        // Check the internet connection
        if(!isNetworkAvailable()){
            // NO INTERNET
            // Display warning screen using the WarningModeNoInternet message type
            Intent intent2 = new Intent(getApplicationContext(), WarningView.class);
            intent2.putExtra("msg",getResources().getString(R.string.warning_no_internet));
            intent2.putExtra("msg2",getResources().getString(R.string.warning_no_internet2));
            startActivity(intent2);
            finish();
        }else{
            //make sure you have a context object that always represents the view that gets passed in
            final Context context = this;
            Button myButton = (Button) findViewById(R.id.button);

            //store input from user into variables to be user in other functions
            if (emailView.getText() != null && passwordView.getText()!= null){
                loginEmail = emailView.getText().toString();
                loginPass = passwordView.getText().toString();

                //check if username or password were left blank on front end
                if(loginEmail.equals("") || loginPass.equals("")){
                    AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context)
                            .setMessage(R.string.invalid_login)
                            .setTitle(R.string.invalid_login_title)
                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    dialog.cancel();
                                }
                            });
                    myNewAlert.show();
                }else{
                    //change the text and background color of the button
                    myButton.setText("");
                    loginProgress.setVisibility(View.VISIBLE);

                    //start connecting to the server
                    new loginTask().execute();
                }
            }else{
                AlertDialog.Builder myNewAlert = new AlertDialog.Builder(context)
                        .setMessage(R.string.invalid_login)
                        .setTitle(R.string.invalid_login_title)
                        .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dialog.cancel();
                            }
                        });
                myNewAlert.show();
            }
        }
    }

    //Override back button to perform stopPCControl
    @Override
    public void onBackPressed() {
        //Disconnect from Websocket if it exists
        try{
            //Shutdown the overlay view if the back button is pressed on the login view while the overlay is still up
            if (Overlay.overlayActivity != null){
                Overlay.overlayActivity.finish();
            }
            if(NavigationWheel.wsClient != null){
                NavigationWheel.wsClient.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectManager cm = ConnectManager.getSharedInstance();
                cm.stopPCControl(30);
            }
        }).start();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Button myButton = (Button) findViewById(R.id.button);
        myButton.setText(R.string.login);
    }

    //attempt at using AsyncTask
    public class loginTask extends AsyncTask<Void,Void,Void>{

        //store input from user into variables to be user in other functions

        //Async Task to do operation in background queue
        protected Void doInBackground(Void... arg0){

            try {
                //return function to loginSuccess that checks the JSON data for valid login information
                loginSuccess = loginToEslWithEmail();

                if(loginSuccess){
                    //loginToEslSecuredWithEmail();
                }

                //code to run on main thread
                LoginView.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //runs the check login function that alerts user if wrong login info was entered
                        checkLoginInfo();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected boolean loginToEslWithEmail(){
            boolean checkData;

            //run function that checks if the JSON data returned is usable
            JSONParser jParser = new JSONParser();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("l", Base64.encodeToString(loginEmail.getBytes(), Base64.DEFAULT)));
            nameValuePairs.add(new BasicNameValuePair("l2", Base64.encodeToString(loginPass.getBytes(), Base64.DEFAULT)));
            nameValuePairs.add(new BasicNameValuePair("l3", Base64.encodeToString("b83a8b01-ae64-495f-8d6f-f75e9358f752".getBytes(), Base64.DEFAULT)));
            nameValuePairs.add(new BasicNameValuePair("s", "android"));

            String json = jParser.postJSONToUrl(loginUrl, nameValuePairs, true);
            if(json != null && json.length() > 0){
                JSONObject jsonData = jParser.getJSONObject(json);

                //check if status is part of the JSON object then get it
                String status = null;

                if (jsonData != null){
                    if(jsonData.has("status")){
                        //exception handling required with JSONObject
                        try {
                            status = jsonData.getString("status");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

                //check if status is a null object or has a value of null and react appropriately
                if(status == null || (status.equalsIgnoreCase("null"))){
                    //return false if failed to connect
                    checkData = false;
                }
                else{
                    //success
                    try {
                        JSONObject result = jsonData.getJSONObject("result");

                        //getSharedPreferences
                        sp = getSharedPreferences("USER_PREF",0);
                        SharedPreferences.Editor edit = sp.edit();

                        //store user ID from JSONData in Shared Preferences
                        String userid = result.getString("user_id");
                        edit.putString("userID",userid);
                        Log.i("loginID: ", userid);

                        //store web site host from JSONData in Shared Preferences
                        String wsHost = result.getString("wsendpoint");
                        Log.i("wsHost (full): ", wsHost);
                        if(wsHost != null && wsHost.indexOf(':') != -1)
                        {
                            String [] tokens = wsHost.split(":");
                            edit.putString("wsHost",tokens[0]);
                            Log.i("wsHost: ", tokens[0]);
                            ConnectManager.webSocketHost = tokens[0];
                            if(tokens.length > 1)
                            {
                                edit.putString("wsPort",tokens[1]);
                                Log.i("wsPort: ", tokens[1]);
                            }
                        }
                        else
                        {
                            edit.putString("wsHost",wsHost);
                        }

                        //store user name from JSONData in Shared Preferences
                        String userName = result.getString("user_name");
                        edit.putString("userName",userName);
                        Log.i("User Name: ", userName);

                        if(result.has("dropcam_id")){
                            String dropCamID = result.getString("dropcam_id");
                            Log.i("Drop Cam ID", dropCamID);
                            edit.putString("dropcamID",dropCamID);
                        }

                        //Don't forget to commit the preferences!
                        edit.commit();

                        checkData = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        checkData = false;
                    }

                }
                return checkData;
            }
            return false;
        }

        protected void loginToEslSecuredWithEmail(){
            try{
                String secureLoginUrl = String.format("http://%s/j_spring_security_check", ConnectManager.webSocketHost);
                //run function that checks if the JSON data returned is usable
                JSONParser jParser = new JSONParser();

                //Old list
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("j_username", loginEmail));
                nameValuePairs.add(new BasicNameValuePair("j_password", loginPass));

                String response = jParser.postJSONToUrl(secureLoginUrl, nameValuePairs, false);
                if(response == null){
                    // Display warning screen using the WarningModeClientNotConnected message type
                    Intent intent = new Intent(getApplicationContext(), WarningView.class);
                    intent.putExtra("msg",getResources().getString(R.string.warning_not_connect));
                    intent.putExtra("msg2",getResources().getString(R.string.warning_not_connect2));
                    startActivity(intent);
                    finish();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        protected void checkLoginInfo(){
            if (loginSuccess){
                //Store loginPass and loginEmail into SharedPreferences on successful login
                //getSharedPreferences
                sp = getSharedPreferences("USER_PREF",0);
                SharedPreferences.Editor edit = sp.edit();

                edit.putString("userEmail", loginEmail);
                edit.putString("userPass", loginPass);
                edit.commit();

                //use an intent object start the Color Wheel Activity on successful login
                Intent intent = new Intent(getApplicationContext(), NavigationWheel.class);
                Log.i(this.toString(), "Intent created. Moving to Navigation Wheel");
                startActivity(intent);
                finish();

            }else{
                AlertDialog.Builder myNewAlert = new AlertDialog.Builder(LoginView.this);
                myNewAlert.setMessage(R.string.invalid_login);
                myNewAlert.setTitle(R.string.invalid_login_title);
                myNewAlert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                myNewAlert.show();
                Log.e("login", "error");
                //reset button to say login
                Button myButton = (Button) findViewById(R.id.button);
                myButton.setText(R.string.login);
                loginProgress.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }
}