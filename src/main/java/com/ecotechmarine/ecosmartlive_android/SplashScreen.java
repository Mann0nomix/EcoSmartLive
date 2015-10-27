package com.ecotechmarine.ecosmartlive_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by EJ Mann on 12/2/13.
 */

public class SplashScreen extends Activity {
    final String loginUrl = "http://qabeta.ecosmartlive.com/login/thinclient";
    //final String loginUrl = "http://www.ecosmartlive.com/172.20.3.184";
    public static boolean loginSuccess;
    public static Context context;
    SharedPreferences sp;
    String existingUser;
    String existingEmail;
    String existingPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        context = SplashScreen.this;
        checkForInternet();
    }

    /**
     * Tests whether or not the network/internet is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkForInternet(){
        // Check the internet connection
        if(!isNetworkAvailable()){
            // NO INTERNET
            // Display warning screen using the WarningModeNoInternet message type
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent2 = new Intent(getApplicationContext(), WarningView.class);
                    intent2.putExtra("msg",getResources().getString(R.string.warning_no_internet));
                    intent2.putExtra("msg2", getResources().getString(R.string.warning_no_internet2));
                    startActivity(intent2);
                    finish();
                }
            },3000);
        }else{
            sp = getSharedPreferences("USER_PREF",0);
            existingUser = sp.getString("userName", null);
            existingEmail = sp.getString("userEmail", null);
            existingPass = sp.getString("userPass", null);

            if (existingEmail != null || existingPass != null || existingUser != null){
                //start loginTask automatically if data persists from last login
                new loginTask().execute();
            }else{
                //if no existing users && the network is available, take user directly to the login view
                Intent intent = new Intent(getApplicationContext(), LoginView.class);
                Log.i(this.toString(), "Intent created. Moving to LoginView");
                startActivity(intent);
                finish();
            }
        }
    }

    //Override Back Button to do nothing
    @Override
    public void onBackPressed() {

    }

    //attempt at using AsyncTask
    public class loginTask extends AsyncTask<Void,Void,Void> {

        //store input from user into variables to be user in other functions
        String loginEmail = existingEmail;
        String loginPass = existingPass;

        //Async Task to do operation in background queue
        protected Void doInBackground(Void... arg0){

            try {
                Log.i("mydebug", "Login Splash");
                //return function to loginSuccess that checks the JSON data for valid login information
                loginSuccess = loginToEslWithEmail();

                if(loginSuccess){
                    //loginToEslSecuredWithEmail();
                }

                //code to run on main thread
                SplashScreen.this.runOnUiThread(new Runnable() {
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
                            edit.putString("wsHost","");
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
                JSONParser jParser = new JSONParser();

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
            }catch(Exception e){
                e.printStackTrace();
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
                //If the login is changed when relogging, say invalid login and return the user to the login View
                /*AlertDialog.Builder myNewAlert = new AlertDialog.Builder(SplashScreen.this);
                myNewAlert.setMessage(R.string.invalid_login);
                myNewAlert.setMessage("Invalid Login");
                myNewAlert.setTitle(R.string.invalid_login_title);
                myNewAlert.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
                myNewAlert.show();*/
                //Throw the user back to the login view
                Intent intent = new Intent(getApplicationContext(), LoginView.class);
                Log.i(this.toString(), "Intent created. Moving to Login View");
                startActivity(intent);
                finish();
                Log.e("login", "error");
            }
        }
    }

}
