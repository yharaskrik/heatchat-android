package heatchat.unite.com.heatchat;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.ui.ChatFragment;
import heatchat.unite.com.heatchat.ui.SchoolListFragment;
import heatchat.unite.com.heatchat.util.PermissionUtil;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    private static final int ACCESS_FINE_LOCATION_CODE = 104;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @BindView(R.id.textViewTitle)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation)
    NavigationView navDrawer;

    private FirebaseAnalytics mFirebaseAnaltyics;
    private ActionBarDrawerToggle mDrawerToggle;
    private DatabaseReference mDatabase;
    private SharedViewModel sharedViewModel;

    private boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);

        sharedViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SharedViewModel.class);
        sharedViewModel.getSelectedSchool().observe(this, this::changeSchool);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setUpActionBar();

        initializeDrawer();


        checkLocationPermissions();

        checkAuth();

        if (savedInstanceState == null) {
            setUpFragmentsIfAllConfigured();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedViewModel.setLocationPermissionsEnabled();
                initializeLocation();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onStart() {
        super.onStart();
        initializeLocation();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Waits for the view model to get the user
     * <p>
     * TODO: Move to repository
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
        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Sends the user's location to the Firebase Database.
     * //TODO: Change to a repository and move to view model.
     *
     * @param location The new location.
     */
    private void sendLocation(Location location) {
        String key = mDatabase
                .child("/user/locations/").push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lat", location.getLatitude());
        hashMap.put("lon", location.getLongitude());
        hashMap.put("uid", sharedViewModel.getUser().getValue().getResult().getUid());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user/locations/" + key, hashMap);

        mDatabase.updateChildren(childUpdates);
        Timber.d("Sent Location");
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void initializeLocation() {
        sharedViewModel.locationUpdates().removeObservers(this);
        sharedViewModel.locationUpdates().observe(this, this::sendLocation);
    }

    /**
     * Checks the location permissions and requests them if required. If enabled than {@link
     * #initializeLocation()} is called.
     */
    private void checkLocationPermissions() {
        if (!PermissionUtil.hasLocationPermissions(this)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(android.R.string.ok,
                                (dialogInterface, i) -> ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        ACCESS_FINE_LOCATION_CODE))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_FINE_LOCATION_CODE);
            }
        } else {
            initializeLocation();
        }
    }

    private void changeSchool(School school) {
        mDrawerLayout.closeDrawers();
        toolbar.setTitle(school.getName() + " Heatchat");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
    }
}
