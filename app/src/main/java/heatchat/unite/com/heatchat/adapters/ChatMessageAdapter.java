package heatchat.unite.com.heatchat.adapters;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.models.ChatMessage;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChatMessageAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private List<ChatMessage> chatMessages;
    private Disposable updateDisposable;
    private RecyclerView recycler;

    public ChatMessageAdapter() {
        this.chatMessages = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.getUid() == null) {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
        if (message.getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message, parent, false);
            return new ChatMessageSendHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_message, parent, false);
            return new ChatMessageReceivedHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);


        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((ChatMessageAdapter.ChatMessageSendHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ChatMessageAdapter.ChatMessageReceivedHolder) holder).bind(message);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recycler = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recycler = null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void onNewData(List<ChatMessage> newMessageList) {
        if (updateDisposable != null) {
            updateDisposable.dispose();
        }
        updateDisposable = Single.fromCallable(
                () -> DiffUtil.calculateDiff(new ChatMessageDiff(newMessageList)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(diffResult -> {
                    chatMessages = newMessageList;
                    diffResult.dispatchUpdatesTo(this);
                    if (recycler != null) {
                        recycler.scrollToPosition(chatMessages.size() - 1);
                    }
                });
    }

    public class ChatMessageSendHolder extends RecyclerView.ViewHolder {
        public TextView text, time;

        ChatMessageSendHolder(View view) {
            super(view);
            Log.d("Sent", "");
            view.setElevation(4);
            this.text = view.findViewById(R.id.text_message_body);
            this.time = view.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            text.setText(message.getText());
            time.setText(new SimpleDateFormat("HH:mm")
                    .format(new Date(message.getTime() * 1000L)));
        }
    }

    public class ChatMessageReceivedHolder extends RecyclerView.ViewHolder {
        public TextView text, time;

        ChatMessageReceivedHolder(View view) {
            super(view);
            Log.d("Received", "");
            view.setElevation(4);
            this.text = view.findViewById(R.id.text_message_body);
            this.time = view.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            text.setText(message.getText());
            time.setText(new SimpleDateFormat("HH:mm")
                    .format(new Date(message.getTime() * 1000L)));
        }
    }

    private class ChatMessageDiff extends DiffUtil.Callback {
        private final List<ChatMessage> newMessageList;

        public ChatMessageDiff(List<ChatMessage> newMessageList) {
            this.newMessageList = newMessageList;
        }

        @Override
        public int getOldListSize() {
            return chatMessages.size();
        }

        @Override
        public int getNewListSize() {
            return newMessageList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return chatMessages.get(oldItemPosition)
                    .getMessageID()
                    .equals(newMessageList.get(newItemPosition).getMessageID());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return chatMessages.get(oldItemPosition)
                    .getText()
                    .equals(newMessageList.get(newItemPosition).getText());
        }
    }
}
