package heatchat.unite.com.heatchat.ui.chat;

import android.support.v7.util.DiffUtil;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
    private Context mContext;
    public ArrayList<Integer> colors;
    public HashMap<String, Integer> userColors;
    private Random randomGenerator;
    private Disposable updateDisposable;
    private RecyclerView recycler;

    public ChatMessageAdapter(List<ChatMessage> messageList, Context context) {
//        this.mContext = context;
        this.chatMessages = messageList;
        this.mContext = context;
        this.colors = new ArrayList<>(Arrays.asList(Color.parseColor("#FF4848"),Color.parseColor("#FF68DD"),Color.parseColor("#FF62B0"),Color.parseColor("#FE67EB"),Color.parseColor("#E469FE"),Color.parseColor("#D568FD"),Color.parseColor("#9669FE"),Color.parseColor("#FF7575"),Color.parseColor("#FF79E1"),Color.parseColor("#FF73B9"),Color.parseColor("#FE67EB"),Color.parseColor("#E77AFE"),Color.parseColor("#D97BFD"),Color.parseColor("#A27AFE"),Color.parseColor("#FF8A8A"),Color.parseColor("#FF86E3"),Color.parseColor("#FF86C2"),Color.parseColor("#FE8BF0"),Color.parseColor("#EA8DFE"),Color.parseColor("#DD88FD"),Color.parseColor("#AD8BFE"),Color.parseColor("#FF9797"),Color.parseColor("#FF97E8"),Color.parseColor("#FF97CB"),Color.parseColor("#FE98F1"),Color.parseColor("#ED9EFE"),Color.parseColor("#E29BFD"),Color.parseColor("#B89AFE"),Color.parseColor("#FFA8A8"),Color.parseColor("#FFACEC"),Color.parseColor("#FFA8D3"),Color.parseColor("#FEA9F3"),Color.parseColor("#EFA9FE"),Color.parseColor("#E7A9FE"),Color.parseColor("#C4ABFE"),Color.parseColor("#FFBBBB"),Color.parseColor("#FFACEC"),Color.parseColor("#FFBBDD"),Color.parseColor("#FFBBF7"),Color.parseColor("#F2BCFE"),Color.parseColor("#EDBEFE"),Color.parseColor("#D0BCFE"),Color.parseColor("#FFCECE"),Color.parseColor("#FFC8F2"),Color.parseColor("#FFC8E3"),Color.parseColor("#FFCAF9"),Color.parseColor("#F5CAFF"),Color.parseColor("#F0CBFE"),Color.parseColor("#DDCEFF"),Color.parseColor("#FFDFDF"),Color.parseColor("#FFDFF8"),Color.parseColor("#FFDFEF"),Color.parseColor("#FFDBFB"),Color.parseColor("#F9D9FF"),Color.parseColor("#F4DCFE"),Color.parseColor("#E6DBFF"),Color.parseColor("#FFECEC"),Color.parseColor("#FFEEFB"),Color.parseColor("#FFECF5"),Color.parseColor("#FFEEFD"),Color.parseColor("#FDF2FF"),Color.parseColor("#FAECFF"),Color.parseColor("#F1ECFF"),Color.parseColor("#FFF2F2"),Color.parseColor("#FFFEFB"),Color.parseColor("#FFF9FC"),Color.parseColor("#FFF9FE"),Color.parseColor("#FFFDFF"),Color.parseColor("#FDF9FF"),Color.parseColor("#FBF9FF"),Color.parseColor("#800080"),Color.parseColor("#872187"),Color.parseColor("#9A03FE"),Color.parseColor("#892EE4"),Color.parseColor("#3923D6"),Color.parseColor("#2966B8"),Color.parseColor("#23819C"),Color.parseColor("#BF00BF"),Color.parseColor("#BC2EBC"),Color.parseColor("#A827FE"),Color.parseColor("#9B4EE9"),Color.parseColor("#6755E3"),Color.parseColor("#2F74D0"),Color.parseColor("#2897B7"),Color.parseColor("#DB00DB"),Color.parseColor("#D54FD5"),Color.parseColor("#B445FE"),Color.parseColor("#A55FEB"),Color.parseColor("#8678E9"),Color.parseColor("#4985D6"),Color.parseColor("#2FAACE"),Color.parseColor("#F900F9"),Color.parseColor("#DD75DD"),Color.parseColor("#BD5CFE"),Color.parseColor("#AE70ED"),Color.parseColor("#9588EC"),Color.parseColor("#6094DB"),Color.parseColor("#44B4D5"),Color.parseColor("#FF4AFF"),Color.parseColor("#DD75DD"),Color.parseColor("#C269FE"),Color.parseColor("#AE70ED"),Color.parseColor("#A095EE"),Color.parseColor("#7BA7E1"),Color.parseColor("#57BCD9"),Color.parseColor("#FF86FF"),Color.parseColor("#E697E6"),Color.parseColor("#CD85FE"),Color.parseColor("#C79BF2"),Color.parseColor("#B0A7F1"),Color.parseColor("#8EB4E6"),Color.parseColor("#7BCAE1"),Color.parseColor("#FFA4FF"),Color.parseColor("#EAA6EA"),Color.parseColor("#D698FE"),Color.parseColor("#CEA8F4"),Color.parseColor("#BCB4F3"),Color.parseColor("#A9C5EB"),Color.parseColor("#8CD1E6"),Color.parseColor("#FFBBFF"),Color.parseColor("#EEBBEE"),Color.parseColor("#DFB0FF"),Color.parseColor("#DBBFF7"),Color.parseColor("#CBC5F5"),Color.parseColor("#BAD0EF"),Color.parseColor("#A5DBEB"),Color.parseColor("#FFCEFF"),Color.parseColor("#F0C4F0"),Color.parseColor("#E8C6FF"),Color.parseColor("#E1CAF9"),Color.parseColor("#D7D1F8"),Color.parseColor("#CEDEF4"),Color.parseColor("#B8E2EF"),Color.parseColor("#FFDFFF"),Color.parseColor("#F4D2F4"),Color.parseColor("#EFD7FF"),Color.parseColor("#EDDFFB"),Color.parseColor("#E3E0FA"),Color.parseColor("#E0EAF8"),Color.parseColor("#C9EAF3"),Color.parseColor("#FFECFF"),Color.parseColor("#F4D2F4"),Color.parseColor("#F9EEFF"),Color.parseColor("#F5EEFD"),Color.parseColor("#EFEDFC"),Color.parseColor("#EAF1FB"),Color.parseColor("#DBF0F7"),Color.parseColor("#FFF9FF"),Color.parseColor("#FDF9FD"),Color.parseColor("#FEFDFF"),Color.parseColor("#FEFDFF"),Color.parseColor("#F7F5FE"),Color.parseColor("#F8FBFE"),Color.parseColor("#EAF7FB"),Color.parseColor("#5757FF"),Color.parseColor("#62A9FF"),Color.parseColor("#62D0FF"),Color.parseColor("#06DCFB"),Color.parseColor("#01FCEF"),Color.parseColor("#03EBA6"),Color.parseColor("#01F33E"),Color.parseColor("#6A6AFF"),Color.parseColor("#75B4FF"),Color.parseColor("#75D6FF"),Color.parseColor("#24E0FB"),Color.parseColor("#1FFEF3"),Color.parseColor("#03F3AB"),Color.parseColor("#0AFE47"),Color.parseColor("#7979FF"),Color.parseColor("#86BCFF"),Color.parseColor("#8ADCFF"),Color.parseColor("#3DE4FC"),Color.parseColor("#5FFEF7"),Color.parseColor("#33FDC0"),Color.parseColor("#4BFE78"),Color.parseColor("#8C8CFF"),Color.parseColor("#99C7FF"),Color.parseColor("#99E0FF"),Color.parseColor("#63E9FC"),Color.parseColor("#74FEF8"),Color.parseColor("#62FDCE"),Color.parseColor("#72FE95"),Color.parseColor("#9999FF"),Color.parseColor("#99C7FF"),Color.parseColor("#A8E4FF"),Color.parseColor("#75ECFD"),Color.parseColor("#92FEF9"),Color.parseColor("#7DFDD7"),Color.parseColor("#8BFEA8"),Color.parseColor("#AAAAFF"),Color.parseColor("#A8CFFF"),Color.parseColor("#BBEBFF"),Color.parseColor("#8CEFFD"),Color.parseColor("#A5FEFA"),Color.parseColor("#8FFEDD"),Color.parseColor("#A3FEBA"),Color.parseColor("#BBBBFF"),Color.parseColor("#BBDAFF"),Color.parseColor("#CEF0FF"),Color.parseColor("#ACF3FD"),Color.parseColor("#B5FFFC"),Color.parseColor("#A5FEE3"),Color.parseColor("#B5FFC8"),Color.parseColor("#CACAFF"),Color.parseColor("#D0E6FF"),Color.parseColor("#D9F3FF"),Color.parseColor("#C0F7FE"),Color.parseColor("#CEFFFD"),Color.parseColor("#BEFEEB"),Color.parseColor("#CAFFD8"),Color.parseColor("#E1E1FF"),Color.parseColor("#DBEBFF"),Color.parseColor("#ECFAFF"),Color.parseColor("#C0F7FE"),Color.parseColor("#E1FFFE"),Color.parseColor("#BDFFEA"),Color.parseColor("#EAFFEF"),Color.parseColor("#EEEEFF"),Color.parseColor("#ECF4FF"),Color.parseColor("#F9FDFF"),Color.parseColor("#E6FCFF"),Color.parseColor("#F2FFFE"),Color.parseColor("#CFFEF0"),Color.parseColor("#EAFFEF"),Color.parseColor("#F9F9FF"),Color.parseColor("#F9FCFF"),Color.parseColor("#FDFEFF"),Color.parseColor("#F9FEFF"),Color.parseColor("#FDFFFF"),Color.parseColor("#F7FFFD"),Color.parseColor("#F9FFFB"),Color.parseColor("#1FCB4A"),Color.parseColor("#59955C"),Color.parseColor("#48FB0D"),Color.parseColor("#2DC800"),Color.parseColor("#59DF00"),Color.parseColor("#9D9D00"),Color.parseColor("#B6BA18"),Color.parseColor("#27DE55"),Color.parseColor("#6CA870"),Color.parseColor("#79FC4E"),Color.parseColor("#32DF00"),Color.parseColor("#61F200"),Color.parseColor("#C8C800"),Color.parseColor("#CDD11B"),Color.parseColor("#4AE371"),Color.parseColor("#80B584"),Color.parseColor("#89FC63"),Color.parseColor("#36F200"),Color.parseColor("#66FF00"),Color.parseColor("#DFDF00"),Color.parseColor("#DFE32D"),Color.parseColor("#7CEB98"),Color.parseColor("#93BF96"),Color.parseColor("#99FD77"),Color.parseColor("#52FF20"),Color.parseColor("#95FF4F"),Color.parseColor("#FFFFAA"),Color.parseColor("#EDEF85"),Color.parseColor("#93EEAA"),Color.parseColor("#A6CAA9"),Color.parseColor("#AAFD8E"),Color.parseColor("#6FFF44"),Color.parseColor("#ABFF73"),Color.parseColor("#FFFF84"),Color.parseColor("#EEF093"),Color.parseColor("#A4F0B7"),Color.parseColor("#B4D1B6"),Color.parseColor("#BAFEA3"),Color.parseColor("#8FFF6F"),Color.parseColor("#C0FF97"),Color.parseColor("#FFFF99"),Color.parseColor("#F2F4B3"),Color.parseColor("#BDF4CB"),Color.parseColor("#C9DECB"),Color.parseColor("#CAFEB8"),Color.parseColor("#A5FF8A"),Color.parseColor("#D1FFB3"),Color.parseColor("#FFFFB5"),Color.parseColor("#F5F7C4"),Color.parseColor("#D6F8DE"),Color.parseColor("#DBEADC"),Color.parseColor("#DDFED1"),Color.parseColor("#B3FF99"),Color.parseColor("#DFFFCA"),Color.parseColor("#FFFFC8"),Color.parseColor("#F7F9D0"),Color.parseColor("#E3FBE9"),Color.parseColor("#E9F1EA"),Color.parseColor("#EAFEE2"),Color.parseColor("#D2FFC4"),Color.parseColor("#E8FFD9"),Color.parseColor("#FFFFD7"),Color.parseColor("#FAFBDF"),Color.parseColor("#E3FBE9"),Color.parseColor("#F3F8F4"),Color.parseColor("#F1FEED"),Color.parseColor("#E7FFDF"),Color.parseColor("#F2FFEA"),Color.parseColor("#FFFFE3"),Color.parseColor("#FCFCE9"),Color.parseColor("#FAFEFB"),Color.parseColor("#FBFDFB"),Color.parseColor("#FDFFFD"),Color.parseColor("#F5FFF2"),Color.parseColor("#FAFFF7"),Color.parseColor("#FFFFFD"),Color.parseColor("#FDFDF0")));
        this.userColors = new HashMap<>();
        this.randomGenerator = new Random();
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
            if (!colors.isEmpty()) {
                Integer color = userColors.get(message.getUid());
                if (color != null) {
                    text.setBackgroundTintList(ColorStateList.valueOf(color));
                }
                else {
                    int index = randomGenerator.nextInt(colors.size());
                    text.setBackgroundTintList(ColorStateList.valueOf(colors.get(index)));
                    userColors.put(message.getUid(), colors.get(index));
                }
            }
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
