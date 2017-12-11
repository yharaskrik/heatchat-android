package heatchat.unite.com.heatchat.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
import butterknife.Unbinder;
import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.adapters.ChatMessageAdapter;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.query.MessagesQuery;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private static int maxMessages = 100;
    @BindView(R.id.list_of_messages)
    RecyclerView recyclerView;
    @BindView(R.id.button_chatbox_send)
    Button mSubmitButton;
    @BindView(R.id.edittext_chatbox)
    EditText input;
    @BindView(R.id.empty_view)
    TextView emptyView;
    private DatabaseReference mDatabase;
    private List<ChatMessage> dataset;
    private ChatMessageAdapter messageAdapter;
    private Unbinder unbinder;
    private School selectedSchool;
    private Double latitude;
    private Double longitude;
    private MessagesQuery messagesQuery;
    private ChildEventListener messageListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dataset = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(dataset);
        messagesQuery = new MessagesQuery(getContext());
        messageListener = initializeMessageListener();
        ViewModelProviders.of(getActivity())
                .get(SharedViewModel.class)
                .getSelectedSchool()
                .observe(this, this::changeSchool);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this.messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).setStackFromEnd(true);
        mSubmitButton.setOnClickListener(v -> writeNewPost(
                FirebaseAuth.getInstance().getCurrentUser().getUid(), input.getText().toString()));
        input.setOnClickListener(
                v -> recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()));

        input.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> recyclerView.smoothScrollToPosition(
                        messageAdapter.getItemCount()));
        setEditingEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void writeNewPost(String userId, String body) {
        body = body.trim();
        if (checkSchoolLocation()) {
            if (body != "" && !body.isEmpty()) {
                ChatMessage message = new ChatMessage(userId, body, this.latitude, this.longitude);
                Map<String, Object> postValues = message.toMap();

                String key = mDatabase
                        .child("schoolMessages")
                        .child(this.selectedSchool.getPath())
                        .child("messages").push().getKey();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(
                        "schoolMessages/" + this.selectedSchool.getPath() + "/messages/" + key,
                        postValues);

                mDatabase.updateChildren(childUpdates);

                input.setText("");

//                ArrayList<School> addSchools = new ArrayList<>();
//                key = mDatabase.child("schools").push().getKey();
//                childUpdates = new HashMap<>();
//                childUpdates.put("/schools/" + key, postValues);
//                mDatabase.updateChildren(childUpdates);
            }
        }
    }

    public void setSelectedSchool(School selectedSchool) {
        this.selectedSchool = selectedSchool;
    }

    private boolean checkSchoolLocation() {
        if (selectedSchool != null && longitude != null && latitude != null) {
            Log.d("Changing:", Double.toString(DistanceUtil.distance(selectedSchool.getLat(),
                    latitude,
                    selectedSchool.getLon(),
                    longitude,
                    0.0,
                    0.0)));
            if (DistanceUtil.distance(selectedSchool.getLat(),
                    latitude,
                    selectedSchool.getLon(),
                    longitude,
                    0.0,
                    0.0) > selectedSchool.getRadius() * 1000) {
                setEditingEnabled(false);
                return false;
            } else {
                setEditingEnabled(true);
                return true;
            }
        } else
            return false;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
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

    private void changeSchool(School school) {
        dataset.clear();
        if (school != null) {
            messageAdapter.notifyDataSetChanged();
            if (messageListener != null)
                mDatabase
                        .child("schoolMessages")
                        .child(school.getPath())
                        .child("messages")
                        .removeEventListener(messageListener);
            displayChatMessages(school);
        }
    }

    private void displayChatMessages(School school) {
        this.selectedSchool = school;
        Log.d("PATH", school.getPath());

        dataset.addAll(messagesQuery.getMessages(school));
        Collections.sort(dataset);
        Log.d("Loaded Dataset", dataset.toString());

        if (dataset.size() == 0)
            mDatabase
                    .child("schoolMessages")
                    .child(school.getPath())
                    .child("messages")
                    .addChildEventListener(initializeMessageListener());
        else {
            mDatabase
                    .child("schoolMessages")
                    .child(school.getPath())
                    .child("messages")
                    .orderByChild("time")
                    .startAt(dataset.get(dataset.size() - 1).getTime())
                    .addChildEventListener(initializeMessageListener());

            messageAdapter.notifyDataSetChanged();
        }

        if (dataset.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }


        Log.d("PATH", Integer.toString(dataset.size()));
    }

    private ChildEventListener initializeMessageListener() {
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);

                if (cm != null) {
                    cm.setMessageID(dataSnapshot.getKey());
                    if (!dataset.contains(cm)) {
                        cm.setPath(selectedSchool.getPath());
                        messagesQuery.saveMessage(cm);
                        if (dataset.size() >= maxMessages)
                            dataset = dataset.subList(1, maxMessages);
                        dataset.add(cm);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                    }
                }

                if (recyclerView.getVisibility() == View.GONE) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
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
}
