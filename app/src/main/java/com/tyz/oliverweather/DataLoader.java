package com.tyz.oliverweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by daniel on 12/23/15.
 */


public class DataLoader {

    static long lastLoadTime = 0;
    static String lastResults = "";
    final long fetchDelayMillis = 60 * 1000 * Constants.REFRESH_DELAY_SECONDS; //Thousand millis * seconds * REFRESH
    Context mContext = null;

    public DataLoader(Context c) {
        mContext = c;
    }

    //Acquire JSON items from JSON Arg and Parse into display strings.
    public String parseTemps(String InJSONStr) throws JSONException {
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);

        String currentDate = dateFormat.format(lastLoadTime);

        /**
         * Assemble Current weather and current date forecast
         */
        JSONObject jObj = new JSONObject(InJSONStr);
        jObj = jObj.getJSONObject("weather");
        JSONArray jArrDay1 = jObj.getJSONArray("curren_weather"); //Note "curren" is from JSON.
        JSONArray jArrDay2 = jObj.getJSONArray("forecast");
        jObj = jArrDay1.getJSONObject(0);
        String humidity = jObj.get("humidity").toString();
        String pressure = jObj.get("pressure").toString();
        String temp = jObj.get("temp").toString();
        String tempUnit = jObj.get("temp_unit").toString();

        String wind = jArrDay1.getJSONObject(0).get("wind").toString();
        String text = jObj.get("weather_text").toString();
        StringBuilder todayConcat = new StringBuilder(String.format("Current Weather, %s. \n\t%s\n\tHumidity will be %s \n" +
                        "\tPressure will be %s\n" +
                        "\tTemperature will be %s%s.\n " +
                        "Wind %s",
                currentDate, text, humidity, pressure, temp, tempUnit, wind));

        jObj = jArrDay2.getJSONObject(0);
        String todayDate = jObj.get("date").toString();
        JSONArray jArr = jObj.getJSONArray("day");
        jObj = jArr.getJSONObject(0);
        text = jObj.get("weather_text").toString();
        wind = jArr.getJSONObject(0).get("wind").toString();
        jObj = jArrDay2.getJSONObject(0);

        temp = jObj.get("day_max_temp").toString();
        tempUnit = jObj.get("temp_unit").toString();
        todayConcat.append(String.format("\n|\n\nForecast for %s \n\tWe expect %s, \n\tTemperature highs of %s%s \n" +
                        "Wind %s.",
                todayDate, text, temp, tempUnit, wind));

        /**
         * Assemble tomorrow's forecast
         */
        jObj = jArrDay2.getJSONObject(1);
        String tomorrowDate = jObj.get("date").toString();
        jArr = jObj.getJSONArray("day");
        jObj = jArr.getJSONObject(0);
        text = jObj.get("weather_text").toString();
        wind = jArr.getJSONObject(0).get("wind").toString();
        jObj = jArrDay2.getJSONObject(0);

        temp = jObj.get("day_max_temp").toString();
        tempUnit = jObj.get("temp_unit").toString();

        String tomorrowConcat = String.format("\n" +
                        "\nWeather for %s \n\tWe expect %s, \n\tTemperature highs of %s%s \n" +
                        "Wind %s.",
                tomorrowDate, text, temp, tempUnit, wind);


        // All values are gathered assembled here. They are parsed into separate strings at UI level
        return todayConcat + "|" + tomorrowConcat;
    }

    // Retrieve JSON data from HTTP source.
    public String getJSONData(String host, int port, String path) {
        final String TAG = "getJSONData";

        PackageManager pm = mContext.getPackageManager();
        String pn = mContext.getPackageName();
        int hasFineLocPerm = pm.checkPermission(
                Manifest.permission.INTERNET,
                pn);
        if(hasFineLocPerm != pm.PERMISSION_GRANTED){
            return mContext.getString(R.string.need_internet_permission);
        }

        Date date = new Date();

        long now = date.getTime();
        if (now < (lastLoadTime + fetchDelayMillis) && lastLoadTime > 0) {
            long nextRefresh = (now - ((lastLoadTime + fetchDelayMillis) / 1000));
            Log.v(TAG, "Using old values. Next refresh " + nextRefresh);
            return lastResults;
        }
        lastLoadTime = now;
        URL url = null;
        try {
            url = new URL("http", host, port, path);
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException: unable to create url from:" + host + "." + port + "." + path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection conn;
        BufferedReader in = null;
        try {
            conn = url.openConnection(); // Can fail with npe
            conn.setDoInput(true);
            conn.setAllowUserInteraction(true);
            conn.connect();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException: unable to open a connection on url:" + (url == null ? null : url.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = in != null ? in.readLine() : null) != null) { // Can fail with npe
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close(); // Can fail with npe
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lastResults = sb.toString();
        return lastResults;
    }



}

