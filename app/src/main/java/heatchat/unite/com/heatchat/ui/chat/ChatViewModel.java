package heatchat.unite.com.heatchat.ui.chat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.CurrentSchool;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.ChatMessageRepository;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.util.LocationLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A view model class for the chat fragment. This provides LiveData instances for the messages for
 * the currently selected school, and a LiveData that updates if the user can send messages based on
 * the current location and school.
 * <p>
 * This view model uses some nice features of LiveData to update the message list automatically
 * whenever the {@link CurrentSchool} singleton is updated. This saves having to do fragment
 * interaction forwarding in activities.
 */
public class ChatViewModel extends AndroidViewModel {

    private final CurrentSchool currentSchool;
    private final LocationLiveData locationLiveData;

    /**
     * The repository for chat messages that provides the functionality to send and receive
     * messages.
     */
    private ChatMessageRepository repository;

    /**
     * The message list that the ui can observe. This is automatically updated when the selected
     * school changes.
     */
    private LiveData<List<ChatMessage>> messageList;

    /**
     * Keeps track if the school is close enough to send messages to..
     */
    private MediatorLiveData<Boolean> sendingEnabled = new MediatorLiveData<>();

    /**
     * Keeps the disposables that this view model may create. This is cleared when the view model
     * is cleared.
     */
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    ChatViewModel(@NonNull Application application,
                  ChatMessageRepository chatMessageRepository,
                  CurrentSchool currentSchool,
                  LocationLiveData locationLiveData) {
        super(application);
        this.repository = chatMessageRepository;
        this.currentSchool = currentSchool;
        this.locationLiveData = locationLiveData;
        sendingEnabled.setValue(false);
        initChatMessages(currentSchool);
        initEnabled(currentSchool, locationLiveData);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }

    /**
     * Initializes the LiveData from the ChatMessageRepository. This uses a switchMap to
     * automatically switch update the current school in the repository and display the new stream
     * of messages.
     *
     * @param currentSchool The CurrentSchool object to list to changes from.
     */
    private void initChatMessages(CurrentSchool currentSchool) {
        messageList = Transformations.switchMap(currentSchool,
                school -> repository.getSchoolMessages(school));
    }

    /**
     * Initializes the sendingEnabled mediator live data by having it react when the school or
     * location changes.
     *
     * @param currentSchool    The CurrentSchool object to react to.
     * @param locationLiveData The LocationLiveData object to react to.
     */
    private void initEnabled(CurrentSchool currentSchool, LocationLiveData locationLiveData) {
        sendingEnabled.addSource(locationLiveData,
                location -> checkCanSendMessages(currentSchool.getValue(), location));

        sendingEnabled.addSource(currentSchool,
                school -> checkCanSendMessages(school, locationLiveData.getValue()));
    }

    /**
     * Checks if the user can send messages to this school. This is enabled if the current location
     * is within a certain radius of the school.
     * <p>
     * This sets the {@link #sendingEnabled} value to true if the user can post, false if they
     * cannot.
     *
     * @param school   The school to check. If null then the user cannot send messages.
     * @param location The current location. If null then the user cannot post messages.
     */
    private void checkCanSendMessages(@Nullable School school, @Nullable Location location) {
        if (school != null && location != null) {
            Timber.d("School or Location changed to: %s : %s", school.getName(),
                    location.toString());
            sendingEnabled.setValue(DistanceUtil.isLocationCloseToSchool(school, location));
        } else {
            sendingEnabled.setValue(false);
        }
    }

    /**
     * @return The LiveData containing the boolean value if the user is allowed to post messages.
     */
    public LiveData<Boolean> sendingEnabled() {
        return sendingEnabled;
    }

    /**
     * @return The LiveData contains the continually updating list of chat messages for the current
     * school. This will automatically change when the school changes.
     */
    public LiveData<List<ChatMessage>> messageList() {
        return messageList;
    }

    @SuppressLint("MissingPermission")
    public void sendMessage(String message) {
        final Location location = locationLiveData.getValue();
        final Boolean sendingEnabledValue = sendingEnabled.getValue();
        final String uid = FirebaseAuth.getInstance().getUid();
        if (sendingEnabledValue != null && sendingEnabledValue && location != null && uid != null) {
            compositeDisposable.add(
                    repository.postMessage(uid, message, location)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe());
        }
    }
}
