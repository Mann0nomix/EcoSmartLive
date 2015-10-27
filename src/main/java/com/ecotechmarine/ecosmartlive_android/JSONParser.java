package com.ecotechmarine.ecosmartlive_android;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by EJ Mann on 9/10/13.
 */

public class JSONParser {

    //declare variables and objects to be used within our Parser class
    InputStream is = null;
    JSONObject jObj = null;
    static BasicCookieStore cookieStore;

    // constructor

    static{
        cookieStore = new BasicCookieStore();

    }

    public JSONParser() {

    }

    // HTTP POST
    public String postJSONToUrl(String url, List<NameValuePair> nameValuePairs, boolean withResponse) {

        String json = null;

        // Making HTTP request
        try {

            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

            HttpClient httpclient = new DefaultHttpClient(params);
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            HttpPost httppost = new HttpPost(url);
            // Add the param data
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity httpEntity = response.getEntity();

            if(withResponse){
                is = httpEntity.getContent();
            }
            else{
                httpEntity.consumeContent();

                int responseCode = response.getStatusLine().getStatusCode();
                if(responseCode >= 200 && responseCode < 300){
                    return "success";
                }
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        return json;
    }

    // HTTP GET
    public String getJSONFromUrl(String url) {

        String json = null;
        // Making HTTP request
        try {

            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpClient httpclient = new DefaultHttpClient(params);
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            HttpGet httpget = new HttpGet(url);

            // Execute HTTP Get Request
            HttpResponse response = httpclient.execute(httpget, httpContext);
            HttpEntity httpEntity = response.getEntity();
            is = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();

        }
    catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (ClientProtocolException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    catch (Exception e) {

            Log.e("Buffer Error", "Error converting result " + e.toString());

        }

        return json;
    }


    //Make sure a JSON object is returned and the resulting string from the connection is passed in
    public JSONObject getJSONObject(String json){

        // grab the raw the JSON data after connecting to the server
        try {

            jObj = new JSONObject(json);

        } catch (JSONException e) {

            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jObj;
    }

    // HTTP POST
    public String postJSONObjectToUrl(String url, JSONObject jsonObject, boolean withResponse) {

        String json = null;

        // Making HTTP request
        try {

            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

            HttpClient httpclient = new DefaultHttpClient(params);
            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json");

            // Add the param data
            httppost.setEntity(new StringEntity(jsonObject.toString()));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity httpEntity = response.getEntity();

            if(withResponse){
                is = httpEntity.getContent();
            }
            else{
                httpEntity.consumeContent();

                int responseCode = response.getStatusLine().getStatusCode();
                if(responseCode >= 200 && responseCode < 300){
                    return "success";
                }
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        return json;
    }
}
