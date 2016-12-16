package com.example.govindpatel.testproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by govindpatel on 12/12/16.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    protected static String getAddress(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            // admin area - state (TamilNadu)
            // locality - city (Chennai)
            // countryCode - IN
            // countryName - India
            return address.getLocality() + ", " + address.getCountryCode();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    protected static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    protected static Location getLocation(Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String mBestProvider = mLocationManager.getBestProvider(criteria, true);

        Location mLocation = mLocationManager.getLastKnownLocation(mBestProvider);

        if (mLocation == null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "Location permission not granted");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }

            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        }
        return mLocation;
    }
}
