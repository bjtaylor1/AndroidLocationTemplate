package ridelogger.bjt.com.ridelogger_android;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

public class LocationService extends Service
implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean isDestroyed;

    public LocationService() {
    }

    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        try {
            Log.i(getString(R.string.app_name), "onCreate");

            super.onCreate();
            createNotification();
            registerForLocationUpdates();

            final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }
        catch(final Exception ex) {
            Log.e(getString(R.string.app_name), ex.getClass().getName(), ex);
        }
    }

    private void createNotification() {
        final Intent mainActivityIntent = new Intent(this, MainActivity.class);
        final PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

        final Intent stopServiceIntent = new Intent(this, StopLocationServiceReceiver.class).setAction(C.REQUEST_STOP_RIDELOGGER_LOCATION_SERVICE_ACTION);
        final PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(this, C.REQUEST_STOP_RIDELOGGER_LOCATION_SERVICE, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(Build.VERSION.SDK_INT >= 26) {
            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final NotificationChannel channel = new NotificationChannel(C.NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            final Notification notification = new Notification.Builder(this, C.NOTIFICATION_CHANNEL_ID)
                    .setContentIntent(mainActivityPendingIntent)
                    .setContentText(getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_icon)
                    .setActions(new Notification.Action.Builder(null, getString(R.string.stop), stopServicePendingIntent).build())
                    .build();
            startForeground(1, notification);
        }

        Log.d(getString(R.string.app_name), "done createNotification");
    }

    private void registerForLocationUpdates() {
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int minTime = 5000;
        Log.d(getString(R.string.app_name), String.format("Requesting updates every %d milliseoncds", minTime));
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 10, this);

        final LocationRequest locationRequest = LocationRequest.create()
                .setSmallestDisplacement(10)
                .setFastestInterval(minTime);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult != null) {
                final Location lastLocation = locationResult.getLastLocation();
                onLocationChanged(lastLocation);
            }
        }
    };

    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public void onLocationChanged(final Location location) {
        try {
            if (isDestroyed) return;


            Log.d("onLocationChanged", String.format("Now at %.5f, %.5f", location.getLatitude(), location.getLongitude()));

        } catch (final Exception e) {
            Log.e(getString(R.string.app_name), "Exception in onLocationChanged", e);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
