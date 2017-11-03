package heatchat.unite.com.heatchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jaybell on 02/11/17.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = (ChatMessage) chatMessages.get(position);

        if (message.getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    public ChatMessageAdapter(List<ChatMessage> messageList) {
        this.chatMessages = messageList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message, parent, false);
            return new ChatMessageSendHolder(view);
        }
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message, parent, false);
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
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ChatMessageSendHolder extends RecyclerView.ViewHolder {
        public TextView text, time;

        ChatMessageSendHolder(View view) {
            super(view);
            this.text = (TextView) view.findViewById(R.id.text_message_body);
            this.time = (TextView) view.findViewById(R.id.text_message_time);
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
            this.text = (TextView) view.findViewById(R.id.text_message_body);
            this.time = (TextView) view.findViewById(R.id.text_message_time);
        }
        void bind(ChatMessage message) {
            text.setText(message.getText());
            time.setText(new SimpleDateFormat("HH:mm")
                    .format(new Date(message.getTime() * 1000L)));
        }
    }

}
