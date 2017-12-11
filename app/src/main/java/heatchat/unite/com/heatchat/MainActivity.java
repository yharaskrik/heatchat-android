package heatchat.unite.com.heatchat;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.ui.ChatFragment;
import heatchat.unite.com.heatchat.ui.SchoolListFragment;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int ACCESS_FINE_LOCATION_CODE = 104;
    private static int maxMessages = 100;

    @BindView(R.id.textViewTitle)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation)
    NavigationView navDrawer;
    private FirebaseAnalytics mFirebaseAnaltyics;
    private int requestCode = 0;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private Disposable subscription;

    private ReactiveLocationProvider locationProvider;
    private Double latitude;
    private Double longitude;

    private boolean isLocation = false;

    private boolean isSorted = false;

    private boolean locationSent = false;

    private DatabaseReference mDatabase;
    private SharedViewModel sharedViewModel;

    private boolean isLoggedIn = false;
    private boolean isLocationFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.locationSent = false;

        initializeViewModels();

        setUpActionBar();

        initializeDrawer();

        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);

        checkAndSetLocationPermissions();

        checkAuth();

        if (savedInstanceState == null) {
            setUpFragmentsIfAllConfigured();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Timber.d("PERMISSION GRANTED %d", requestCode);
        Timber.d("PERMISSION GRANTED %d", ACCESS_FINE_LOCATION_CODE);
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getLocation();
    }

    /**
     * Waits for the view model to get the user
     */
    private void checkAuth() {
        sharedViewModel.getUser().observe(this, result -> {
            if (result != null) {
                if (result.isSuccess() && result.getResult() != null) {
                    Toast.makeText(this,
                            "Welcome " + result.getResult().getDisplayName(),
                            Toast.LENGTH_LONG)
                            .show();
                    isLoggedIn = true;
                    setUpFragmentsIfAllConfigured();
                } else {
                    sharedViewModel.getUser().removeObservers(MainActivity.this);
                    showAuthError();
                }
            }
        });
    }

    private void showAuthError() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this,
                    android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setTitle("Authentication Failed")
                .setMessage(
                        "Authentication to Heatchat servers failed. Press ok to try again.")
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> checkAuth())
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setUpFragmentsIfAllConfigured() {
        if (isLoggedIn) {
            if (getSupportFragmentManager().findFragmentById(R.id.main_container) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.school_list_container,
                                SchoolListFragment.newInstance(), "SchoolListFragment")
                        .add(R.id.main_container, ChatFragment.newInstance(), "ChatFragment")
                        .commit();
            }
        }
    }

    private void setUpActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
    }

    private void initializeViewModels() {
        sharedViewModel = ViewModelProviders.of(this)
                .get(SharedViewModel.class);
        sharedViewModel.getSelectedSchool().observe(this, this::changeSchool);
    }

    private void sendLocation() {
        String key = mDatabase
                .child("/user/locations/").push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lat", this.latitude);
        hashMap.put("lon", this.longitude);
        hashMap.put("uid", sharedViewModel.getUser().getValue().getResult().getUid());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user/locations/" + key, hashMap);

        mDatabase.updateChildren(childUpdates);

        locationSent = true;
        Timber.d("Sent");
    }

    private void initializeDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24px);
        toolbar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(Gravity.START));
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE);
        } else {
            if (locationProvider == null) {
                locationProvider = new ReactiveLocationProvider(this);
            }
            locationProvider.getLastKnownLocation()
                    .subscribe(location -> {
                        Timber.d("Changing location: %s", location.toString());
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    });
        }
    }

    private void setLocationSubscription() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE);
        } else {
            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(5)
                    .setInterval(100);
            subscription = locationProvider.getUpdatedLocation(request)
                    .subscribe(location -> {
                        Log.d("Changing 2:", location.toString());
                        this.longitude = location.getLongitude();
                        this.latitude = location.getLatitude();
//                        checkSchoolLocation();
                        //TODO: Not sure what this is????
//                        if (!isSorted && schools.size() > 0) {
//                            Collections.sort(schools);
//                            mItems.clear();
//                            for (School school : schools)
//                                mItems.add(school.getName());
//                            if (mDrawerAdapter != null)
//                                mDrawerAdapter.notifyDataSetChanged();
//                        }
                        isLocation = true;
                    }, throwable -> {
                        Log.e("RXJAVA", "Throwable " + throwable.getMessage());
                    });
        }
    }

    private void checkAndSetLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE);
            this.requestCode++;
        } else {
            Timber.d("Setting location disposable");

            locationProvider = new ReactiveLocationProvider(this);
            getLocation();
            setLocationSubscription();
        }
    }

    private void changeSchool(School school) {
        mDrawerLayout.closeDrawers();
        toolbar.setTitle(school.getName() + " Heatchat");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
    }
}
