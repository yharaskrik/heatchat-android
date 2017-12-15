package heatchat.unite.com.heatchat.respository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * This class manages tasks related to the user including logging in and reporting the current
 * location.
 * <p>
 * TODO: Not happy with this class.
 */
@Singleton
public class UserRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    MutableLiveData<UserResult> user = new MutableLiveData<>();

    @Inject
    public UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        updateCurrentLoggedInUser();
    }


    private boolean updateCurrentLoggedInUser() {
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Timber.d("Got a user %s", currentUser);
            user.setValue(new UserResult().setUser(currentUser));
            return true;
        } else {
            Timber.d("Didn't find a user");
            user.setValue(new UserResult().setLoggedIn(false));
            return false;
        }
    }

    public LiveData<UserResult> getUser() {
        if (user.getValue() == null || !user.getValue().isLoggedIn()) {
            signInUser();
        }
        return user;
    }

    /**
     * Signs in the user if they are not already logged in. This should only be called if the
     * initial get user fails.
     */
    public void signInUser() {
        if (user.getValue() == null || !user.getValue().isLoggedIn()) {
            Timber.d("Starting login");
            firebaseAuth.signInAnonymously().addOnCompleteListener(runnable -> {
                if (runnable.isSuccessful()) {
                    Timber.d("Performed Login");
                    user.setValue(new UserResult().setUser(runnable.getResult().getUser()));
                } else {
                    Timber.e(runnable.getException(), "Unable to perform login");
                    user.setValue(new UserResult().setException(runnable.getException()));
                }
            });
        } else {
            Timber.d("Did not need to start login");
        }
    }

    /**
     * Sends the user's current location to the firebase database. This function provides no
     * competition listeners as it the user and ui doesn't care if it fails.
     *
     * @param location The Location to send.
     */
    public void sendLocation(Location location) {
        if (user.getValue() == null || !user.getValue().isLoggedIn()) {
            // Can't send location without a user.
            return;
        }
        String key = databaseReference
                .child("/user/locations").push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lat", location.getLatitude());
        hashMap.put("lon", location.getLongitude());
        hashMap.put("uid", user.getValue().getUser().getUid());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user/locations/" + key, hashMap);

        databaseReference.updateChildren(childUpdates);
        Timber.d("Sent Location");
    }

    public class UserResult {

        private boolean isLoggedIn = false;

        private FirebaseUser user;

        private Exception exception;

        public UserResult() {
            isLoggedIn = false;
        }

        public Exception getException() {
            return exception;
        }

        public UserResult setException(Exception exception) {
            this.exception = exception;
            isLoggedIn = false;
            return this;
        }

        public FirebaseUser getUser() {
            return user;
        }

        public UserResult setUser(FirebaseUser user) {
            this.user = user;
            isLoggedIn = true;
            return this;
        }

        public boolean isLoggedIn() {
            return isLoggedIn;
        }

        UserResult setLoggedIn(boolean loggedIn) {
            isLoggedIn = loggedIn;
            return this;
        }
    }
}
