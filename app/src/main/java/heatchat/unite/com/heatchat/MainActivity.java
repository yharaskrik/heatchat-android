package heatchat.unite.com.heatchat;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import heatchat.unite.com.heatchat.adapters.ChatMessageAdapter;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.query.MessagesQuery;
import heatchat.unite.com.heatchat.ui.ChatFragment;
import heatchat.unite.com.heatchat.ui.SchoolListFragment;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

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
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.locationSent = false;

        final SharedViewModel sharedViewModel = ViewModelProviders.of(this)
                .get(SharedViewModel.class);
        sharedViewModel.getSelectedSchool().observe(this, this::changeSchool);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeDrawer();

        checkAndSetLocationPermissions();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            signInAnonymously();
            Toast.makeText(this,
                    "Signed in Anonymously",
                    Toast.LENGTH_LONG).show();
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            this.mUser = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.school_list_container,
                    SchoolListFragment.newInstance(), "SchoolListFragment")
                    .add(R.id.main_container, ChatFragment.newInstance("", ""))
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("PERMISSION GRANTED", Integer.toString(requestCode));
        Log.d("PERMISSION GRANTED", Integer.toString(ACCESS_FINE_LOCATION_CODE));
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
//                    setEditingEnabled(false);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onStart() {
        super.onStart();
        getLocation();
    }

    private void sendLocation() {
        String key = mDatabase
                .child("/user/locations/").push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lat", this.latitude);
        hashMap.put("lon", this.longitude);
        hashMap.put("uid", mUser.getUid());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user/locations/" + key, hashMap);

        mDatabase.updateChildren(childUpdates);

        locationSent = true;
        Log.d("Location", "Sent");
    }

    private void initializeDrawer() {
//        mDrawerList.setAdapter(mDrawerAdapter);

//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
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
                    .subscribe(new Consumer<Location>() {
                        @Override
                        public void accept(Location location) {
                            Log.d("Changing 1:", location.toString());
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
//                            checkSchoolLocation();
                        }
                    });
//            checkSchoolLocation();
        }
    }

    private void setLocationSubscription() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE);
//            setEditingEnabled(false);
        } else {
//            setEditingEnabled(true);
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
            Log.d("XYZ", "Setting location disposable");

            locationProvider = new ReactiveLocationProvider(this);
            getLocation();
            setLocationSubscription();
        }
    }

    private void changeSchool(School school) {
        mDrawerLayout.closeDrawers();
        toolbar.setTitle(school.getName() + " Heatchat");
//        toolbarTitle.setText(school.getName() + " Heatchat");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        getLocation();
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AnonymouseAuth", "signInAnonymously:success");
                        mUser = mAuth.getCurrentUser();
                        sendLocation();
                    } else {
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
                                        (dialog, which) -> signInAnonymously())
                                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                    // do nothing
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
    }

}
