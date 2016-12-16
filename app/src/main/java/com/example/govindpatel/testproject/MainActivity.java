package com.example.govindpatel.testproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by govindpatel on 06/12/16.
 */


/*
    todo's
    give ids in xml
    store the data in db,
    dont show the app if no internet connection and there is no previous data
    show the app if there is previous data (optional internet connection)
    internet connection is required when user clicks on refresh button
    class for storing the daily data
 */
public class MainActivity extends Activity implements LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;
    private double latitude;
    private double longitude;

    private TextView mTemperatureLabel;
    private TextView mTimeLabel;
    private TextView mLocationLabel;
    private ImageView mIconImageView;
    private TextView mHumidityValue;
    private TextView mPrecipValue;
    private TextView mSummaryLabel;
    private ImageView mRefreshImageView;
    private ImageView mDegreeImageView;
    private ProgressBar mProgressBar;
    private LinearLayout mHumidityPrecipLinearLayout;

    private Location mLocation;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);
        mTimeLabel = (TextView) findViewById(R.id.timeLabel);
        mLocationLabel = (TextView) findViewById(R.id.locationLabel);
        mIconImageView = (ImageView) findViewById(R.id.iconImageView);
        mHumidityValue = (TextView) findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) findViewById(R.id.summaryLabel);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRefreshImageView = (ImageView) findViewById(R.id.refreshImageView);
        mDegreeImageView = (ImageView) findViewById(R.id.degreeImageView);
        mHumidityPrecipLinearLayout = (LinearLayout) findViewById(R.id.humidity_percip_layout);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Image clicked");
                if (mLocation == null) {
                    mLocation = Utils.getLocation(mContext);
                }
                getForecast();
            }
        });


        if (mLocation == null) {
            mLocation = Utils.getLocation(mContext);
        }

        if (mLocation != null) {
            getForecast();
        } else {
            Drawable refreshDrawable = getResources().getDrawable(R.drawable.refresh);
            mRefreshImageView.setImageDrawable(refreshDrawable);
        }
    }


    private void getForecast() {
        final String apiKey = "bbce3ee83d3fc88c2d074fdee4ad3a2c";

        // location is available and network is available
        if (mLocation != null && Utils.isNetworkAvailable(this)) {

            longitude = mLocation.getLongitude();
            latitude = mLocation.getLatitude();
            final String URL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;
            Log.v(TAG, "URL: " + URL);

            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } finally {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });

                    }
                }
            });
        } else if (mLocation == null) {
            Toast.makeText(this, R.string.location_unavailable_message, Toast.LENGTH_LONG).show();
            Drawable refreshDrawable = getResources().getDrawable(R.drawable.refresh);
            mRefreshImageView.setImageDrawable(refreshDrawable);
            mRefreshImageView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
            Drawable refreshDrawable = getResources().getDrawable(R.drawable.refresh);
            mRefreshImageView.setImageDrawable(refreshDrawable);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(Long.toString(mCurrentWeather.getRoundTemperature()));
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(Double.toString(mCurrentWeather.getmHumidity()));
        mPrecipValue.setText(Long.toString(mCurrentWeather.getPercipPercentage()) + "%");
        mSummaryLabel.setText(mCurrentWeather.getmSummary());
        Drawable iconDrawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(iconDrawable);
        Drawable degreeDrawable = getResources().getDrawable(R.drawable.degree);
        mDegreeImageView.setImageDrawable(degreeDrawable);
        Drawable refreshDrawable = getResources().getDrawable(R.drawable.refresh);
        mRefreshImageView.setImageDrawable(refreshDrawable);
        // mLocationLabel.setText(mCurrentWeather.getmTimezone());
        String address = Utils.getAddress(this, mLocation);
        Log.v(TAG, address);
        mLocationLabel.setText(address);

        mDegreeImageView.setVisibility(View.VISIBLE);
        mTemperatureLabel.setVisibility(View.VISIBLE);
        mTimeLabel.setVisibility(View.VISIBLE);
        mLocationLabel.setVisibility(View.VISIBLE);
        mIconImageView.setVisibility(View.VISIBLE);
        mHumidityPrecipLinearLayout.setVisibility(View.VISIBLE);

    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        String timezone = forecast.getString("timezone");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setmTimezone(timezone);
        currentWeather.setmHumidity(currently.getDouble("humidity"));
        currentWeather.setmTime(currently.getLong("time"));
        currentWeather.setmIcon(currently.getString("icon"));
        currentWeather.setmPercipChance(currently.getDouble("precipProbability"));
        currentWeather.setmSummary(currently.getString("summary"));
        currentWeather.setmTemperature(currently.getDouble("temperature"));

        return currentWeather;
    }


    private void alertUserAboutError() {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.show(getFragmentManager(), "error_dialog");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.v(TAG, "location changed: " + latitude + "," + longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
