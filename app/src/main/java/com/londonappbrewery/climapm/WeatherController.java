package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    final String LOGCAT_TAG = "Clima";

    // TODO: Set LOCATION_PROVIDER here:
    final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(myIntent);
            }
        });

    }

    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LOGCAT_TAG", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if (city != null) {
            getWeatherForNewCity(city);
        } else {
            getWeatherForCurrentLocation();
        }

    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        Log.d(LOGCAT_TAG, "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(LOGCAT_TAG, "onLocationChanged() callback received");

                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                Log.d(LOGCAT_TAG, "longitude is: " + longitude);
                Log.d(LOGCAT_TAG, "latitude is: " + latitude);

//                RequestParams params = new RequestParams();
//                params.put("lat", latitude);
//                params.put("log", longitude);
//                params.put("appid", APP_ID);
//                getResponse(params);

                // Get current city name and send request
                try {
                    Geocoder geocoder = new Geocoder(WeatherController.this, Locale.ENGLISH);
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String curCity = addresses.get(0).getLocality();
                    Log.d(LOGCAT_TAG, "Current CityName: " + curCity);

                    RequestParams params = new RequestParams();
                    params.put("q", curCity);
                    params.put("appid", APP_ID);

                    getResponse(params);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Log statements to help you debug your app.
                Log.d(LOGCAT_TAG, "onStatusChanged() callback received. Status: " + status);
                Log.d(LOGCAT_TAG, "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(LOGCAT_TAG, "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(LOGCAT_TAG, "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        // Some additional log statements to help you debug
        Log.d(LOGCAT_TAG, "Location Provider used: "
                + mLocationManager.getProvider(LocationManager.GPS_PROVIDER).getName());
        Log.d(LOGCAT_TAG, "Location Provider is enabled: "
                + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        Log.d(LOGCAT_TAG, "Last known location (if any): "
                + mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        Log.d(LOGCAT_TAG, "Requesting location updates");

        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        getResponse(params);
    }

    // To handle the case where the user grants the permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOGCAT_TAG, "onRequestPermissionsResult(): Permission granted!");

                // Getting weather only if we were granted permission.
                getWeatherForCurrentLocation();
            } else {
                Log.d(LOGCAT_TAG, "Permission denied. ");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void getResponse(RequestParams params) {

        // Use Async-http to handle http response in json format
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());
                WeatherDataModel weatherDataModel = WeatherDataModel.fromJson(response);
                updateUI(weatherDataModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.d(LOGCAT_TAG, "Fail: " + e.toString());
                Toast.makeText(WeatherController.this, "Request Failed!", Toast.LENGTH_SHORT).show();

                Log.d(LOGCAT_TAG, "Status code " + statusCode);
                Log.d(LOGCAT_TAG, "Here's what we got instead " + response.toString());
            }
        });
    }

    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {
        mCityLabel.setText(weather.getmCity());
        mTemperatureLabel.setText(weather.getmTemperature());

        // Update the icon based on the resource id of the image in the drawable folder.
        int resourceId = getResources().getIdentifier(weather.getmIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceId);
    }


    // TODO: Add onPause() here:
    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
    }
}
