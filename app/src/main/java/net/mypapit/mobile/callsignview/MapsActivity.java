/*
 * This file is part of MYCallsign
 *
 * Copyright (c) 2016 Mohammad Hafiz bin Ismail <mypapit@gmail.com>
 * MYCallsign is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Last Modified 6/26/16 8:02 PM
 *  Info url :
 *  https://github.com/mypapit/mycallsign-android
 *  http://code.google.com/p/mycallsign-android/
 *  https://blog.mypapit.net
 *  http://kirostudio.com
 *
 */

package net.mypapit.mobile.callsignview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.nispok.snackbar.Snackbar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import si.virag.fuzzydateformatter.FuzzyDateTimeFormatter;


interface OnMarkerRequestListener {

    void markerRequestCompleted(String jsonString);

}

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMarkerRequestListener, GoogleMap.OnInfoWindowClickListener {

    public static final String URL_API = "http://api.repeater.my/v1/getposition.php";
    private Location mLastKnownLocation;
    private HashMap<Marker, CallsignModel> hashmap;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3163716613766876~7020251873");

        AdView mAdView = (AdView) findViewById(R.id.adViewMap);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        hashmap = new HashMap<Marker, CallsignModel>();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(3.15,101.72))
                .zoom(7)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = getApplicationContext(); // or
                // getActivity(),
                // YourActivity.this,
                // etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);


    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastKnownLocation != null) {
            new MarkerRequest(mLastKnownLocation, this).execute();
        } else {
            Snackbar.with(this).text(getString(R.string.no_location)).show(this);
        }

    }

    public void markerRequestCompleted(String jsonString) {


        if (jsonString == null) {
            return;
        }
        Gson gson = new Gson();

        CallsignModel[] callsigns = gson.fromJson(jsonString, CallsignModel[].class);


        for (CallsignModel callsign : callsigns) {


            //Log.d("mypapit callsign", callsign.callsign);
            MarkerOptions marking = new MarkerOptions();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date time;


            try {
                time = formatter.parse(callsign.time);
            } catch (ParseException e) {
                time = new java.util.Date();

                e.printStackTrace();
            }

            Date date = new Date();
            String timedistance;

            if (date.after(time)) {

                timedistance = FuzzyDateTimeFormatter.getTimeAgo(getApplicationContext(), time);
            } else {
                timedistance = getString(R.string.unknown);
            }


            marking.position(callsign.getLatLng());
            marking.title(new StringBuilder(callsign.callsign).append(" - ")
                    .append(timedistance).toString());
            marking.snippet(new StringBuilder("@").append(callsign.name).append("\n#").append(callsign.phoneno).append("\n")
                    .append(callsign.client).toString());

            if (callsign.valid == 1) {
                marking.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            } else {
                marking.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

            hashmap.put(mMap.addMarker(marking), callsign);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 10));
            mMap.setOnInfoWindowClickListener(this);


        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        CallsignModel cs = hashmap.get(marker);


        if (cs.phoneno == null) {
            Snackbar.with(this).text("No phone number :(").show(this);
            return;
        }

        if (cs.phoneno.length() > 3 && !cs.phoneno.equals("+60120000")) {
            Uri uri = Uri.parse("smsto:" + cs.phoneno);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", "hello " + cs.callsign + ", ");
            startActivity(intent);


        } else {

            Snackbar.with(this).text("No phone number :(").show(this);
        }

    }
}


class MarkerRequest extends AsyncTask<Void, Void, String> {

    private final Location location;
    private final OnMarkerRequestListener listener;

    public MarkerRequest(Location location, OnMarkerRequestListener listener) {
        this.location = location;
        this.listener = listener;

    }

    @Override
    protected String doInBackground(Void... params) {

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder httpUrl = HttpUrl.parse(MapsActivity.URL_API).newBuilder();

        httpUrl.addQueryParameter("lat", Double.toString(location.getLatitude()));
        httpUrl.addQueryParameter("lng", Double.toString(location.getLongitude()));

        Request request = new Request.Builder()
                .url(httpUrl.build().toString())
                .build();

        try {

            Response response = client.newCall(request).execute();
            return response.body().string();

        } catch (IOException ioex) {
            Log.e("okhttp mypapit", "Error calling callsign API");
            ioex.printStackTrace();
            return null;


        }


    }

    public void onPostExecute(String json) {

        listener.markerRequestCompleted(json);


    }
}
