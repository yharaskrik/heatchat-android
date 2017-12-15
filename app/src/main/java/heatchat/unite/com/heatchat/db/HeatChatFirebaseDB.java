package heatchat.unite.com.heatchat.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Maybe;
import timber.log.Timber;

/**
 * A class for common functions in the Firebase Live Database.
 * <p>
 * This class provides functions for getting the messages for a school with {@link
 * #schoolMessages(School)}, posting a message with {@link #postMessage(School, ChatMessage)}.
 */
@Singleton
public class HeatChatFirebaseDB {

    private static final int MAX_MESSAGES = 30;

    private static final String SCHOOL_MESSAGES_DB = "schoolMessages/%s/messages";
    private static final String SCHOOLS_DB = "schools";
    private FirebaseDatabase database;


    @Inject
    HeatChatFirebaseDB(FirebaseDatabase database) {
        this.database = database;
    }

    /**
     * Returns a LiveData object that will provide change events when the firebase database of
     * messages for that school changes. This initially will load the first {@link #MAX_MESSAGES}
     * from the firebase database emitting a new ChatMessage event to the live data for each
     * message. When a new message comes in once the {@link #MAX_MESSAGES} limit has been reached a
     * remove ChildEvent is emitted for old messages.
     *
     * @param school The school to get the messages for.
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

    /**
     * Queries the firebase database for the list of schools.
     *
     * @return A Maybe that emits the list of schools, nothing if there are no schools or the
     * exception if an error occurs.
     */
    public Maybe<List<School>> getSchools() {
        return Maybe.<DataSnapshot>create(e -> database.getReference(SCHOOLS_DB)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            e.onSuccess(dataSnapshot);
                        } else {
                            e.onComplete();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                })).map(dataSnapshot -> {
            List<School> schools = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                School school = snapshot.getValue(School.class);
                if (school != null) {
                    schools.add(school);
                }
            }
            return schools;
        });


    }

}
