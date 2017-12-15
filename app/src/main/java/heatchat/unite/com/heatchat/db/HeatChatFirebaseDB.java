package heatchat.unite.com.heatchat.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import timber.log.Timber;

/**
 * Created by Andrew on 12/14/2017.
 */
@Singleton
public class HeatChatFirebaseDB {

    private static final int MAX_MESSAGES = 30;

    private static final String SCHOOL_MESSAGES_DB = "schoolMessages/%s/messages";
    private FirebaseDatabase database;


    @Inject
    HeatChatFirebaseDB(FirebaseDatabase database) {
        this.database = database;
    }

    /**
     * Starts listening to a school messages using a LiveData object.
     *
     * @param school The school to listen to.
     * @return The ChildEventLiveData that posts ChildEvent<ChatMessage> objects.
     */
    public ChildEventLiveData<ChatMessage> schoolMessages(School school) {
        return new ChildEventLiveData<>(
                database.getReference(String.format(SCHOOL_MESSAGES_DB, school.getPath()))
                        .orderByChild("time")
                        .limitToLast(MAX_MESSAGES), ChatMessage.class);
    }

    /**
     * Posts a message to the school.
     *
     * @param school  The school to post the message to.
     * @param message The message to post to the school.
     */
    public void postMessage(School school, ChatMessage message) {
        final DatabaseReference databaseReference = database.getReference(
                String.format(SCHOOL_MESSAGES_DB, school.getPath()));
        String key = databaseReference
                .push()
                .getKey();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, message.toMap());
        Timber.d("Sending %s", childUpdates.toString());
        databaseReference.updateChildren(childUpdates);
    }

}
