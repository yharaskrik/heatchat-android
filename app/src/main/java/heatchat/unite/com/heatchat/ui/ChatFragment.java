package heatchat.unite.com.heatchat.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.adapters.ChatMessageAdapter;
import heatchat.unite.com.heatchat.di.Injectable;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.viewmodel.ChatViewModel;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements Injectable {

    private static int maxMessages = 100;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
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
        messageAdapter = new ChatMessageAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Acquire the view models.
        chatViewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatViewModel.class);

        chatViewModel.sendingEnabled().observe(this, enabled -> {
            Timber.d("Edit enabled changed: %b", enabled);
            setEditingEnabled(enabled);
        });

        chatViewModel.messageList().observe(this, chatMessages -> {
            if (chatMessages == null) {
                messageAdapter.onNewData(new ArrayList<>());
                showEmptyIfEmpty(0);
            } else {
                Timber.d("Got message list size: %s", chatMessages.size());
                messageAdapter.onNewData(new ArrayList<>(chatMessages));
                showEmptyIfEmpty(chatMessages.size());
            }
        });
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

        input.setOnClickListener(
                v -> recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()));

        input.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> recyclerView.smoothScrollToPosition(
                        messageAdapter.getItemCount()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button_chatbox_send)
    void submitPost() {
        final String text = input.getText().toString().trim();
        if (text.length() > 0) {
            chatViewModel.sendMessage(text);
            input.getText().clear();
        }
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

    private void showEmptyIfEmpty(int count) {
        if (count == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
