package ridelogger.bjt.com.ridelogger_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ridelogger.bjt.com.ridelogger_android.LocationService;

public class StopLocationServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(C.REQUEST_STOP_RIDELOGGER_LOCATION_SERVICE_ACTION)) {
            Log.d(context.getString(R.string.app_name), "GeoRadioStopServiceReceiver.onReceive");
            Intent serviceIntent = new Intent(context, LocationService.class);
            context.stopService(serviceIntent);
        }
    }
}
