package heatchat.unite.com.heatchat.respository;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.location.Location;
import android.support.annotation.WorkerThread;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import heatchat.unite.com.heatchat.AppDatabase;
import heatchat.unite.com.heatchat.dao.ChatMessageDao;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Notification;
import timber.log.Timber;

/**
 * Manages receiving and sending messages to a school.
 */

public class ChatMessageRepository {

    private static AppDatabase db;
    private final DatabaseReference schoolMessagesDB;
    private final ChatMessageDao chatMessageDao;
    private School currentSchool;
    private Executor daoExecutor;

    private ChildEventListener chatMessageListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Timber.d("Got a message");
            ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);
            if (cm != null) {
                cm.setPath(currentSchool.getPath());
                cm.setMessageID(dataSnapshot.getKey());
                daoExecutor.execute(() -> chatMessageDao.insertAll(cm));
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
    };

    public ChatMessageRepository(Context context) {
        db = Room.databaseBuilder(context,
                AppDatabase.class, "heatchat-message-db").fallbackToDestructiveMigration().build();
        schoolMessagesDB = FirebaseDatabase.getInstance().getReference().child("schoolMessages");
        daoExecutor = Executors.newSingleThreadExecutor();
        chatMessageDao = db.chatMessageDao();
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
    public Flowable<List<ChatMessage>> schoolMessages(School school) {
        return db.chatMessageDao().loadMessagesByPath(school.getPath())
                .doOnSubscribe(subscription -> {
                    if (currentSchool != null) {
                        clearSchool(currentSchool);
                    }
                    currentSchool = school;
                    addFirebaseChildUpdates(school);
                })
                .doOnEach(this::checkAndClearOldMessages)
                .doFinally(() -> clearSchool(school));
    }


    public Completable postMessage(String uid, String body, Location location) {
        return Completable.fromAction(() -> {
            ChatMessage message = new ChatMessage(uid, body, location.getLatitude(),
                    location.getLongitude());
            Map<String, Object> postValues = message.toMap();

            String key = schoolMessagesDB
                    .child(currentSchool.getPath())
                    .child("messages").push().getKey();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(
                    currentSchool.getPath() + "/messages/" + key,
                    postValues);

            schoolMessagesDB.updateChildren(childUpdates);
        });
    }


    /**
     * Removes the update subscription from the school.
     *
     * @param school The school to remove the child event listener from.
     */
    private void clearSchool(School school) {
        Timber.d("Clearing listener for %s", school.getPath());
        schoolMessagesDB.child(school.getPath())
                .child("messages")
                .removeEventListener(chatMessageListener);
        currentSchool = null;
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
            Timber.d("List is at the maximum size");
            final ChatMessage chatMessage = value.get(value.size() - 11);
            Timber.d(chatMessage.toString() + " " + chatMessage.getTime());
            daoExecutor.execute(() ->
                    chatMessageDao.deleteOldMessages(chatMessage.getPath(), chatMessage.getTime())
            );
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
        Query query = schoolMessagesDB
                .child(school.getPath())
                .child("messages")
                .orderByChild("time");
        final ChatMessage lastMessage = chatMessageDao.getLastMessage(school.getPath());
        if (lastMessage != null) {
            query = query.startAt(lastMessage.getTime());
        }
        query.addChildEventListener(chatMessageListener);
        Timber.d("Subscribed to %s", school.getPath());
    }

}
