package com.tyz.oliverweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {

    static boolean calledFlag = false;                      // Do not seek Location after initial query
    String[] days;                                          // Returned from JSON server
    ArrayList<String> mDayList = new ArrayList<String>();   // Parsed days
    ListView mUpdateView = null;                            // Content view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUpdateView = (ListView) findViewById(R.id.listview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, calledFlag ? R.string.loc_loaded : R.string.loc_needed, Snackbar.LENGTH_LONG)
                        .setAction("Thanks", null).show();
                Context c = getApplicationContext();
                PackageManager pm = c.getPackageManager();
                String pn = c.getPackageName();
                int hasFineLocPerm = pm.checkPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        pn);
                int hasCoarseLocPerm = pm.checkPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        pn);
                if (    (hasFineLocPerm != pm.PERMISSION_GRANTED) ||
                        (hasCoarseLocPerm != pm.PERMISSION_GRANTED))
                {
                    //Report error
                }else {

                    if (!calledFlag) requestLocationUpdate();
                }
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestLocationUpdate(){


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // respond to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //useLocation(location);
                if(!calledFlag) {
                    Toast.makeText(getApplicationContext(), R.string.loc_recieved + location.toString(),Toast.LENGTH_LONG).show();

                    //Begin query for Weather data immediately.
                    // Specification calls for Location Data but we do not
                    //  have API key and we are using a data source that
                    //  does not take location.

                    JSONFetcher mJSONFetcher = new JSONFetcher(0);
                    mJSONFetcher.execute();

                    Log.v("Location", location.toString());

                }
                calledFlag = true;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        // Permission is checked in caller - consider dummy lat/long
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);




    }

    void setAdapter(String s){
        days = s.split("[|]");
        ListView lv = (ListView) findViewById(R.id.listview);
        for (int i = 0; i < days.length; ++i) {
            mDayList.add(days[i]);
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, mDayList);
        lv.setAdapter(adapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void updateView(String s, int position) {
        final String TAG = "updateView";
        Log.v(TAG, "exiting");

    }
    public class JSONFetcher extends AsyncTask<String, Integer, String> {
        private final String TAG = "JSONFetcher";
        private int mPosition = 0;

        public JSONFetcher(int position) {
            mPosition = position;
        }

        @Override
        protected void onPreExecute() {
            final String TAG = "onPreExecute";
            Log.v(TAG, "Starting Async Execution");
        }

        @Override
        protected void onPostExecute(String s) {
            final String TAG = "onPostExecute";
            setAdapter(s);
            Log.v(TAG, "Finished Async Execution");

            Log.v(TAG, "Async done. View Updated to:" + mPosition);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final String TAG = "onProgressUpdate";
            Log.v(TAG, "Progress Report: " + values[0].toString());
            updateView("Working...", mPosition);
        }

        @Override
        protected String doInBackground(String... strings) {
            final String TAG = "doInBackground";
            DataLoader dl = new DataLoader(getApplicationContext());
            String asyncResult = dl.getJSONData(Constants.JSON_HOST, Constants.HTTP_PORT, Constants.JSON_PATH);
            if (asyncResult == getString(R.string.need_internet_permission)) return getString(R.string.need_internet_permission);
            String days = null;
            try {
                days = dl.parseTemps(asyncResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "exiting");

            return days;
        }

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
