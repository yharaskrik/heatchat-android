package heatchat.unite.com.heatchat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnaltyics;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ChatMessageAdapter adapter;
    private DatabaseReference mDatabase;
    private EditText input;
    private Button mSubmitButton;
    private int requestCode = 0;
    private Toolbar toolbar;
    LinearLayoutManager llm;

    private RecyclerView recyclerView;
    private double latitude;
    private double longitude;

    private List<ChatMessage> dataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        llm = new LinearLayoutManager(this);
        dataset = new ArrayList<>();
        adapter = new ChatMessageAdapter(dataset);
        recyclerView = (RecyclerView) findViewById(R.id.list_of_messages);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.adapter);
        recyclerView.setLayoutManager(llm);

        this.mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, this.requestCode);
            this.requestCode++;
        }
        else {

            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(5)
                    .setInterval(100);

            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
            Disposable subscription = locationProvider.getUpdatedLocation(request)
                .subscribe(location -> {
                    this.longitude = location.getLongitude();
                    this.latitude = location.getLatitude();
                });


        }

        mFirebaseAnaltyics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        input = findViewById(R.id.edittext_chatbox);
        mSubmitButton = findViewById(R.id.button_chatbox_send);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
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
            displayChatMessages();
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
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        input.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    getSupportActionBar().show();
//                }
//                else if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    getSupportActionBar().hide();
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });
    }

    private void displayChatMessages() {

        ChildEventListener messageListener =  new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);

                dataset.add(cm);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        mDatabase.child("messages").addChildEventListener(messageListener);
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

            String key = mDatabase.child("messages").push().getKey();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/messages/" + key, postValues);

            mDatabase.updateChildren(childUpdates);
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

                            displayChatMessages();
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


        recyclerView.smoothScrollToPosition(adapter.getItemCount());

    }
}
