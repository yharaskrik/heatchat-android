package heatchat.unite.com.heatchat.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.adapters.ChatMessageAdapter;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.viewmodel.ChatViewModel;
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

    private List<ChatMessage> dataset;

    private ChatMessageAdapter messageAdapter;

    private Unbinder unbinder;

    private Double latitude;
    private Double longitude;
    private ChatViewModel chatViewModel;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataset = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(dataset);

        // Acquire the view models.
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        // Acquire the shared view model to listen to the changing school.
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


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
/*        body = body.trim();
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
        }*/
    }

    private boolean checkSchoolLocation() {
/*        if (selectedSchool != null && longitude != null && latitude != null) {
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
            return false;*/
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
        // Clean up the previous messages
        chatViewModel.getSchoolMessages().removeObservers(this);
        dataset.clear();
        messageAdapter.notifyDataSetChanged();
        // Set the new school and start observing again
        chatViewModel.setSchool(school);
        chatViewModel.getSchoolMessages().observe(this, chatMessages -> {
            dataset.clear();
            if (chatMessages != null) {
                dataset.addAll(chatMessages);
            }
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            showEmptyIfEmpty();
        });
    }

    private void showEmptyIfEmpty() {
        if (dataset.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
