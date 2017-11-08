package heatchat.unite.com.heatchat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import heatchat.unite.com.heatchat.adapters.ChatMessageAdapter;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.adapters.SchoolListAdapter;
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAnalytics mFirebaseAnaltyics;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ChatMessageAdapter messageAdapter;
    private ChildEventListener messageListener;
    private SchoolListAdapter schoolListAdapter;
    private DatabaseReference mDatabase;
    private int requestCode = 0;
    private LinearLayoutManager llm = new LinearLayoutManager(this);
    private LinearLayoutManager llmSchools = new LinearLayoutManager(this);

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.edittext_chatbox) EditText input;
    @BindView(R.id.button_chatbox_send) Button mSubmitButton;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.list_of_messages) RecyclerView recyclerView;
//    @BindView(R.id.schools_list) RecyclerView schoolsRecyclerView;
    @BindView(R.id.navigation) NavigationView navDrawer;
    @BindView(R.id.nav_options) RelativeLayout rl;
    @BindView(R.id.school_layout) LinearLayout lL;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    private double latitude;
    private double longitude;

    private List<ChatMessage> dataset;
    private List<School> schools;
    private School selectedSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);


        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeDrawer();

        dataset = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(dataset);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.messageAdapter);
        recyclerView.setLayoutManager(llm);
        ((LinearLayoutManager)recyclerView.getLayoutManager()).setStackFromEnd(true);

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

                // Clear the input
                input.setText("");
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
                    });

        }
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
        mDatabase.child(school.getPath()).child("messages")
                .addChildEventListener(initializeMessageListener());
    }

    private void changeSchool(School school) {
        dataset.clear();
        messageAdapter.notifyDataSetChanged();

        mDatabase.removeEventListener(messageListener);
        displayChatMessages(school);
    }

    private void setEditingEnabled(boolean enabled) {
        input.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String body) {

        if (body.trim() != "") {
            ChatMessage message = new ChatMessage(userId, body, this.latitude, this.longitude);
            Map<String, Object> postValues = message.toMap();

            String key = mDatabase.child(this.selectedSchool.getPath()).child("messages").push().getKey();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(this.selectedSchool.getPath() + "/messages/" + key, postValues);

            mDatabase.updateChildren(childUpdates);

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

//        displayChatMessages(selectedSchool);

//        displaySchools();

    }

    private void initializeDrawer() {

//        // Set the list's click listener
//        mDrawerToggle = new ActionBarDrawerToggle(
//                this,
//                mDrawerLayout,
//                toolbar,
//                R.string.drawer_open,
//                R.string.drawer_close);
//        mDrawerLayout.addDrawerListener(mDrawerToggle);
//
//        schools = new ArrayList<>();
//        schoolListAdapter = new SchoolListAdapter(schools);
//        schoolsRecyclerView.setHasFixedSize(true);
//        schoolsRecyclerView.setAdapter(this.schoolListAdapter);
//        schoolsRecyclerView.setLayoutManager(llmSchools);
//
//        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
//
//            @Override public boolean onSingleTapUp(MotionEvent e) {
//                return true;
//            }
//        });
//
//        schoolsRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                View child = recyclerView.findChildViewUnder(e.getX(),e.getY());
//                int position = recyclerView.getChildAdapterPosition(child);
//                if (position < schools.size() && position >= 0) {
//                    Log.d("position", Integer.toString(position));
//                    School school = schools.get(position);
//                    changeSchool(school);
//                    mDrawerLayout.closeDrawers();
//                }
//                return false;
//            }
//
//            @Override
//            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//            }
//
//            @Override
//            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//
//            }
//        });
    }

    public static void onClickedMenu(int id){
        Log.d("Item Clicked", Integer.toString(id));
    }

    private void displaySchools() {

        ChildEventListener schoolListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                School school = dataSnapshot.getValue(School.class);
                schools.add(school);
                if (schools.size() == 1)
                    displayChatMessages(school);
//                schoolListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("schools").addChildEventListener(schoolListener);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private int count = 0;
    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        count = 0;
        schools = new ArrayList<>();
        getMenuInflater().inflate(R.menu.nav_menu, menu);

        Log.d("MENU", "Inflating menu");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                School school = dataSnapshot.getValue(School.class);
                schools.add(school);
                menu.add(0, count, count, school.getName());

                TextView tv = new TextView(MainActivity.this);
                tv.setId(count);
                tv.setText(school.getName());
                tv.setTextColor(Color.BLACK);

                tv.setOnClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedSchool = schools.get(v.getId());
                        changeSchool(selectedSchool);
                    }
                });

                if(count != 0)
                    params1.addRule(RelativeLayout.BELOW, count-1);

                lL.addView(tv);

                if (count == 0) {
                    selectedSchool = school;
                    displayChatMessages(school);
                }

                count++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child("schools").addChildEventListener(childEventListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Indexxyz:", Integer.toString(item.getItemId()));
        selectedSchool = schools.get(item.getItemId());
        changeSchool(selectedSchool);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Log.d("Indexxyz:", Integer.toString(item.getItemId()));
        return true;
    }
}
