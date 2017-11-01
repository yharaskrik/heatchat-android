package heatchat.unite.com.heatchat;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.reactivestreams.Subscription;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnaltyics;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference mDatabase;
    private EditText input;
    private FloatingActionButton mSubmitButton;
    private int requestCode = 0;

    private double latitude;
    private double longitude;

    private LocationManager lm;

    private static final String REQUIRED = "Required";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        input = findViewById(R.id.input);
        mSubmitButton = findViewById(R.id.fab);

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
        }

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);

                writeNewPost(FirebaseAuth.getInstance().getCurrentUser().getUid(), input.getText().toString());

                // Clear the input
                input.setText("");
            }
        });
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

        ChatMessage message = new ChatMessage(userId, body, this.latitude, this.longitude);
        Map<String, Object> postValues = message.toMap();

        String key = mDatabase.child("messages").push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/messages/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    private void submitPost() {
        final String body = input.getText().toString();

        if (TextUtils.isEmpty(body)) {
            input.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("/messages/").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
//                            Log.e(TAG, "User " + userId + " is unexpectedly null");
//                            Toast.makeText(NewPostActivity.this,
//                                    "Error: could not fetch user.",
//                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // Write new post
                            writeNewPost(mAuth.getCurrentUser().getUid(), body);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void signInAnonymously() {
//        showProgressDialog();
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                             Sign in success, update UI with the signed-in user's information
                            Log.d("AnonymouseAuth", "signInAnonymously:success");
                            mUser = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInAnonymously:failure", task.getException());
//                            Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END signin_anonymously]
    }

    @Override
    public void onStart() {
        super.onStart();

        mUser = mAuth.getCurrentUser();

    }
}
