package heatchat.unite.com.heatchat;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.ui.chat.ChatFragment;
import heatchat.unite.com.heatchat.ui.schools.SchoolListFragment;
import heatchat.unite.com.heatchat.util.PermissionUtil;
import heatchat.unite.com.heatchat.viewmodel.MainActivityViewModel;
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
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(MainActivityViewModel.class);

        setUpActionBar();
        initializeDrawer();

        checkLocationPermissions();

        viewModel.getUser().observe(this, userResult -> {
            if (userResult == null || (!userResult.isLoggedIn() && userResult.getException() != null)) {
                Timber.e("Could not login!");
                showAuthError();
            } else if (userResult.isLoggedIn()) {
                Timber.d("User is logged in");
                Toast.makeText(this,
                        "Welcome",
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Timber.d("Not logged in");
            }
        });

        if (savedInstanceState == null) {
            setUpFragments();
        }

        viewModel.currentSchool().observe(this, this::changeSchool);

        viewModel.locationUpdates()
                .observe(this, location -> Timber.d("Got a new location %s", location));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocation();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

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
                        (dialog, which) -> viewModel.retryLogin())
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setUpFragments() {
        if (getSupportFragmentManager().findFragmentById(R.id.main_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.school_list_container,
                            SchoolListFragment.newInstance(), "SchoolListFragment")
                    .add(R.id.main_container, ChatFragment.newInstance(), "ChatFragment")
                    .commit();
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
        viewModel.startLocation();
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
