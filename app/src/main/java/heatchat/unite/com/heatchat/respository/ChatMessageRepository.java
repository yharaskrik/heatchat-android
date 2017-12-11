package heatchat.unite.com.heatchat.respository;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

import heatchat.unite.com.heatchat.AppDatabase;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Flowable;
import io.reactivex.Notification;

/**
 * Created by Andrew on 12/10/2017.
 */

public class ChatMessageRepository implements ChildEventListener {

    private static AppDatabase db;
    private final DatabaseReference schoolMessagesDB;
    private School currentSchool;

    public ChatMessageRepository(Context context) {
        db = Room.databaseBuilder(context,
                AppDatabase.class, "heatchat-message-db").fallbackToDestructiveMigration().build();
        schoolMessagesDB = FirebaseDatabase.getInstance().getReference().child("schoolMessages");
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d("ChatMessageRepository", "Got a message");
        ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);
        if (cm != null) {
            cm.setPath(currentSchool.getPath());
            cm.setMessageID(dataSnapshot.getKey());
            new SaveMessageTask().execute(cm);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    /**
     * Creates a flowable that will report a changed flowable list of messages for a school.
     * <p>
     * Upon subscription to the flowable, the firebase database is connected in order to keep
     * updated on the new messages.
     *
     * @param school The school to subscribe to.
     * @return A Flowable list that passes the updates list of chat messages when it updates.
     */
    public Flowable<List<ChatMessage>> setSchool(School school) {
        currentSchool = school;
        return db.chatMessageDao().loadMessagesByPath(school.getPath())
                .doOnSubscribe(subscription -> addFirebaseChildUpdates(school))
                .doOnEach(this::checkAndClearOldMessages)
                .doFinally(() -> removeFirebaseChildUpdates(school));
    }

    /**
     * Removes the update subscription from the school.
     *
     * @param school The school to remove the child event listener from.
     */
    private void removeFirebaseChildUpdates(School school) {
        Log.d("ChatMessageRepository", "Clearing listener for " + school.getPath());
        schoolMessagesDB.child(school.getPath())
                .child("messages")
                .removeEventListener(ChatMessageRepository.this);
    }

    /**
     * Checks that the list of chat messages is the maximum size, if so then it removes all chat
     * messages from the school from the database that are older than the oldest message in the
     * list.
     *
     * @param chatMessages The list of chat messages to check.
     */
    @WorkerThread
    private void checkAndClearOldMessages(Notification<List<ChatMessage>> chatMessages) {
        final List<ChatMessage> value = chatMessages.getValue();
        if (value.size() > 10) {
            Log.d("ChatMessageRepository", "List is at the maximum size");
            final ChatMessage chatMessage = value.get(value.size() - 11);
            Log.d("ChatMessageRepository", chatMessage.toString() + " " + chatMessage.getTime());
            db.chatMessageDao()
                    .deleteOldMessages(chatMessage.getPath(), chatMessage.getTime());
        }
    }

    /**
     * Adds a subscription to the firebase for a specific school.
     * <p>
     * This first checks to find the newest message in the database from that school and queries for
     * messages newer than that message.
     *
     * @param school The school to check for messages from.
     */
    @WorkerThread
    private void addFirebaseChildUpdates(School school) {
        if (currentSchool.getPath().equals(school.getPath())) {
            Query query = schoolMessagesDB
                    .child(currentSchool.getPath())
                    .child("messages")
                    .orderByChild("time");
            final ChatMessage lastMessage = db.chatMessageDao().getLastMessage(school.getPath());
            if (lastMessage != null) {
                query = query.startAt(lastMessage.getTime());
            }
            query.addChildEventListener(this);
            Log.d("ChatMessageRepository", "Subscribed to " + school.getPath());
        }
    }

    /**
     * AsyncTask to save a message to the database.
     */
    private static class SaveMessageTask extends AsyncTask<ChatMessage, Void, Integer> {
        @Override
        protected Integer doInBackground(ChatMessage... messages) {
            db.chatMessageDao().insertAll(messages);
            return 1;
        }
    }
}
