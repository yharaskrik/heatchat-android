package heatchat.unite.com.heatchat.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * A LiveData class that gets the updated location when active.
 * <p>
 * Borrowed from https://github.com/StylingAndroid/ArchitectureComponents/blob/ViewModel/app/src/playServices/java/com/stylingandroid/location/services/livedata/LocationLiveData.java
 * Created by Andrew on 12/10/2017.
 */
@Singleton
public class LocationLiveData extends LiveData<Location> {
    private final Context context;

    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Timber.d("Got Location Result %s", locationResult.getLastLocation().toString());
            Location newLocation = locationResult.getLastLocation();
            postValue(newLocation);
        }
    };

    @Inject
    public LocationLiveData(Application app) {
        this.context = app.getApplicationContext();
//        init();
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Don't have the permissions");
            return;
        }
        Timber.d("Starting location tracking");
        FusedLocationProviderClient locationProviderClient = getFusedLocationProviderClient();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Looper looper = Looper.getMainLooper();
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, looper);
    }

    @Override
    protected void onInactive() {
        if (fusedLocationProviderClient != null) {
            Timber.d("Stopping Location tracking");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @SuppressLint("MissingPermission")
    private void init() {
        if (PermissionUtil.hasLocationPermissions(context)) {
            getFusedLocationProviderClient().getLastLocation().addOnCompleteListener(runnable -> {
                if (runnable.isSuccessful()) {
                    postValue(runnable.getResult());
                }
            });
        }
    }

    @NonNull
    private FusedLocationProviderClient getFusedLocationProviderClient() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
        return fusedLocationProviderClient;
    }
}
