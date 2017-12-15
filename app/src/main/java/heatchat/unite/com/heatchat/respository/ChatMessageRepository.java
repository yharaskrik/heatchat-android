package heatchat.unite.com.heatchat.respository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.location.Location;
import android.support.annotation.WorkerThread;

import com.google.firebase.database.DataSnapshot;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import heatchat.unite.com.heatchat.dao.ChatMessageDao;
import heatchat.unite.com.heatchat.db.ChildEventLiveData.ChildEvent;
import heatchat.unite.com.heatchat.db.HeatChatFirebaseDB;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Completable;
import timber.log.Timber;

/**
 * Manages receiving and sending messages to a school.
 */
@Singleton
public class ChatMessageRepository {

    private static final int MAX_MESSAGES = 30;
    /**
     * The Room ChatMessageDao to save the messages into.
     */
    private final ChatMessageDao chatMessageDao;
    private HeatChatFirebaseDB heatChatFirebaseDB;

    /**
     * The Executor used to update the database.
     */
    private Executor daoExecutor;

    /**
     * Keeps track of the current school.
     */
    private School currentSchool;

    @Inject
    ChatMessageRepository(ChatMessageDao chatMessageDao, HeatChatFirebaseDB heatChatFirebaseDB) {
        this.chatMessageDao = chatMessageDao;
        this.heatChatFirebaseDB = heatChatFirebaseDB;
        this.daoExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Adds a message to the local database.
     * <p>
     * This is posted on the daoExecutor thread as database updates cannot happen on the main
     * thread.
     *
     * @param chatMessage  The ChatMessage to add to the local database.
     * @param school       The school this chat message belongs to.
     * @param dataSnapshot The DataSnapshot this message came from, required to get the key
     */
    private void addMessageToDB(ChatMessage chatMessage, School school, DataSnapshot dataSnapshot) {
        if (chatMessage != null) {
            chatMessage.setPath(school.getPath());
            chatMessage.setMessageID(dataSnapshot.getKey());
            daoExecutor.execute(() -> chatMessageDao.insertAll(chatMessage));
        }
    }

    /**
     * Removes a chat message from the database.
     *
     * @param chatMessage The chat message to remove.
     * @param school      The school that this chat message belongs to.
     */
    private void removeMessageFromDB(ChatMessage chatMessage, School school) {
        if (chatMessage != null) {
            daoExecutor.execute(
                    () -> chatMessageDao.deleteOldMessage(school.getPath(),
                            chatMessage.getTime()));
        }
    }

    /**
     * Returns a LiveData object the will emit the list of ChatMessages (with a capped size) when a
     * new one arrives.
     * <p>
     * This function works by creating a MediatorLiveData that re-emits the LiveData found from the
     * DAO. In addition, a polling update is made to the online database containing the messages.
     * Once a new message is received or removed it is updated in the local database and the changed
     * list is emitted.
     * <p>
     * This function also tracks the size of the list and cuts it down to the maximum allowed sized
     * if it is too big, removing old messages from the database.
     *
     * @param school The school to listen to messages from.
     * @return The LiveData that will emit the messages.
     */
    public LiveData<List<ChatMessage>> getSchoolMessages(final School school) {
        currentSchool = school;
        MediatorLiveData<List<ChatMessage>> liveData = new MediatorLiveData<>();

        liveData.addSource(heatChatFirebaseDB.schoolMessages(school),
                childEvent -> handleNewMessage(childEvent, school));

        liveData.addSource(chatMessageDao.schoolMessages(school.getPath()),
                value -> {
                    checkAndClearOldMessages(value);
                    liveData.postValue(value);
                });
        return liveData;
    }

    /**
     * Handles a new child event by forwarding the chat message to the correct handler method.
     *
     * @param childEvent The ChildEvent that was received.
     * @param school     The current school to use.
     */
    private void handleNewMessage(ChildEvent<ChatMessage> childEvent, School school) {
        switch (childEvent.getEvent()) {
            case ChildEvent.ADDED:
                addMessageToDB(childEvent.getValue(), school, childEvent.getDataSnapshot());
                break;
            case ChildEvent.CHANGED:
                //TODO: Changed
                break;
            case ChildEvent.EXCEPTION:
                //TODO: Exception
                break;
            case ChildEvent.MOVED:
                //TODO: Moved
                break;
            case ChildEvent.REMOVED:
                removeMessageFromDB(childEvent.getValue(), school);
                break;
            default:
                break;
        }
    }

    public Completable postMessage(String uid, String body, Location location) {
        return Completable.fromAction(() -> {
            ChatMessage message = new ChatMessage(uid, body, location.getLatitude(),
                    location.getLongitude());
            heatChatFirebaseDB.postMessage(currentSchool, message);
        });
    }

    /**
     * Checks if the list of chat messages is above maximum size, if so then it removes all chat
     * messages from the school that are older than the allowed amount.
     *
     * @param chatMessages The list of chat messages to check.
     */
    @WorkerThread
    private void checkAndClearOldMessages(List<ChatMessage> chatMessages) {
        if (chatMessages.size() > MAX_MESSAGES) {
            Timber.d("List is at the maximum size, remove old items");
            final ChatMessage chatMessage = chatMessages.get(
                    chatMessages.size() - MAX_MESSAGES - 1);
            Timber.d(chatMessage.toString() + " " + chatMessage.getTime());
            daoExecutor.execute(() ->
                    chatMessageDao.deleteSchoolMessagesOlderThan(chatMessage.getPath(),
                            chatMessage.getTime())
            );
        }
    }
}
