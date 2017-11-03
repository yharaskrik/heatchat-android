package heatchat.unite.com.heatchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private RecyclerView recyclerView;
    private double latitude;
    private double longitude;

    private List<ChatMessage> dataset;

    private LocationManager lm;

    private static final String REQUIRED = "Required";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        dataset = new ArrayList<>();
        adapter = new ChatMessageAdapter(dataset);
        recyclerView = (RecyclerView) findViewById(R.id.list_of_messages);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(this.adapter);

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
        }

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewPost(FirebaseAuth.getInstance().getCurrentUser().getUid(), input.getText().toString());

                // Clear the input
                input.setText("");
            }
        });

        displayChatMessages();
    }

    private void displayChatMessages() {

        ValueEventListener messageListener =  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot message: dataSnapshot.getChildren()) {
                    ChatMessage cm = message.getValue(ChatMessage.class);
                    dataset.add(cm);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("messages").addValueEventListener(messageListener);

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
                            // If sign in fails, display a message to the user.
                            displayChatMessages();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        mUser = mAuth.getCurrentUser();

    }
}
