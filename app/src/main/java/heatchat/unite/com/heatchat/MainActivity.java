package heatchat.unite.com.heatchat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnaltyics;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ChatMessageAdapter messageAdapter;
    private ChildEventListener messageListener;
    private DatabaseReference mDatabase;
    private int requestCode = 0;
    private LinearLayoutManager llm = new LinearLayoutManager(this);
    private LinearLayoutManager llmSchools = new LinearLayoutManager(this);

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edittext_chatbox)
    EditText input;
    @BindView(R.id.button_chatbox_send)
    Button mSubmitButton;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.left_drawer)
    ListView mDrawerList;
    @BindView(R.id.list_of_messages)
    RecyclerView recyclerView;
    @BindView(R.id.navigation)
    NavigationView navDrawer;

    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter mDrawerAdapter;

    private Double latitude;
    private Double longitude;
    private boolean isLocation = false;
    private boolean isSorted = false;

    private List<ChatMessage> dataset;
    private List<School> schools;
    private School selectedSchool;

    private ArrayList<String> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mItems = new ArrayList<String>();
        schools = new ArrayList<School>();

        initializeDrawer();

        dataset = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(dataset);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.messageAdapter);
        recyclerView.setLayoutManager(llm);
        ((LinearLayoutManager) recyclerView.getLayoutManager()).setStackFromEnd(true);

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

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewPost(FirebaseAuth.getInstance().getCurrentUser().getUid(), input.getText().toString());
            }
        });

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

        input.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    private void initializeDrawer() {
        mDatabase.child("schools").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                schools.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    School school = child.getValue(School.class);
                    if(isLocation) {
                        school.setDistance(distance(latitude,
                                school.getLat(),
                                longitude,
                                school.getLon(),
                                0.0,
                                0.0));
                    }
                    schools.add(school);
                }

                if (isLocation) {
                    isSorted = true;
                    Collections.sort(schools);
                }

                for (School school : schools) {
                    mItems.add(school.getName());
                }

                mDrawerAdapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        R.layout.drawer_list_item,
                        mItems);
                mDrawerList.setAdapter(mDrawerAdapter);

                mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

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
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawerLayout.openDrawer(Gravity.START);
                    }
                });
                mDrawerLayout.addDrawerListener(mDrawerToggle);

                changeSchool(schools.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (schools.get(position).getPath() != selectedSchool.getPath()) {
                mDatabase.removeEventListener(messageListener);
                changeSchool(schools.get(position));
            }
            mDrawerLayout.closeDrawers();
        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private void checkAndSetLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.requestCode);
            this.requestCode++;
        } else {
            Log.d("XYZ", "Setting location disposable");
            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(5)
                    .setInterval(100);

            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
            Disposable subscription = locationProvider.getUpdatedLocation(request)
                    .subscribe(location -> {
                        Log.d("LOCATION", location.toString());
                        this.longitude = location.getLongitude();
                        this.latitude = location.getLatitude();
                        checkSchoolLocation();
                        if (!isSorted && schools.size() > 0) {
                            Collections.sort(schools);
                            mItems.clear();
                            for (School school: schools)
                                mItems.add(school.getName());
                            if (mDrawerAdapter != null)
                                mDrawerAdapter.notifyDataSetChanged();
                        }
                        isLocation = true;

                    });
        }
    }

    private boolean checkSchoolLocation() {
        if (selectedSchool != null && longitude != null && latitude != null) {
            if (distance(selectedSchool.getLat(),
                    latitude,
                    selectedSchool.getLon(),
                    longitude,
                    0.0,
                    0.0) > 30000) {
                setEditingEnabled(false);
                return false;
            }
            else {
                setEditingEnabled(true);
                return true;
            }
        }
        else
            return false;
    }

    private ChildEventListener initializeMessageListener() {
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);

                dataset.add(cm);
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        return messageListener;
    }

    private void displayChatMessages(School school) {
        this.selectedSchool = school;
        Log.d("PATH", school.getPath());
        mDatabase.child(school.getPath()).child("messages")
                .addChildEventListener(initializeMessageListener());
    }

    private void changeSchool(School school) {
        dataset.clear();
        messageAdapter.notifyDataSetChanged();
        if (messageListener != null)
            mDatabase.child(selectedSchool.getPath()).child("messages")
                    .removeEventListener(messageListener);
        displayChatMessages(school);
        checkSchoolLocation();
    }

    private void setEditingEnabled(boolean enabled) {
        input.setEnabled(enabled);
        if (enabled) {
            input.setHint(R.string.close_hint);
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            input.setHint(R.string.not_close_hint);
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String body) {
        body = body.trim();
        if (checkSchoolLocation()) {
            if (body != "" && !body.isEmpty()) {
                ChatMessage message = new ChatMessage(userId, body, this.latitude, this.longitude);
                Map<String, Object> postValues = message.toMap();

                String key = mDatabase.child(this.selectedSchool.getPath()).child("messages").push().getKey();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(this.selectedSchool.getPath() + "/messages/" + key, postValues);

                mDatabase.updateChildren(childUpdates);

                input.setText("");
            }

//            School school = new School("UBC Vancouver", 49.261725, -123.241273, "ubcv");
//            postValues = school.toMap();
//            key = mDatabase.child("schools").push().getKey();
//            childUpdates = new HashMap<>();
//            childUpdates.put("/schools/" + key, postValues);
//            mDatabase.updateChildren(childUpdates);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("AnonymouseAuth", "signInAnonymously:success");
                            mUser = mAuth.getCurrentUser();
                        } else {
                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(MainActivity.this);
                            }
                            builder.setTitle("Authentication Failed")
                                    .setMessage("Authentication to Heatchat servers failed. Press ok to try again.")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            signInAnonymously();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        mUser = mAuth.getCurrentUser();

        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
    }
}
