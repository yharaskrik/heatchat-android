package heatchat.unite.com.heatchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by jaybell on 02/11/17.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private List<ChatMessage> chatMessages;

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public ChatMessageViewHolder(View view) {
            super(view);
            this.text = (TextView) view.findViewById(R.id.message_text);
        }
    }

    public ChatMessageAdapter(List<ChatMessage> messageList) {
        this.chatMessages = messageList;
    }

    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message, parent, false);

        return new ChatMessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        holder.text.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
}
