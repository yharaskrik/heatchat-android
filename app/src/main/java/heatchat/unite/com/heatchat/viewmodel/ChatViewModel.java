package heatchat.unite.com.heatchat.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.CurrentSchool;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.ChatMessageRepository;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.util.PermissionUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

/**
 * Created by Andrew on 12/10/2017.
 */

public class ChatViewModel extends AndroidViewModel {

    /**
     * The repository for chat messages that provides the functionality to send and receive messages.
     */
    private ChatMessageRepository repository;

    /**
     * The location provider
     */
    private ReactiveLocationProvider reactiveLocationProvider;

    /**
     * The message list that the ui can observe. This is automatically updated when the selected
     * school changes.
     */
    private LiveData<List<ChatMessage>> messageList;

    /**
     * Keeps track if the school is close enough to edit.
     */
    private MutableLiveData<Boolean> editEnabled = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    ChatViewModel(@NonNull Application application,
                  ChatMessageRepository chatMessageRepository, CurrentSchool currentSchool) {
        super(application);
        this.repository = chatMessageRepository;
        editEnabled.setValue(false);
        reactiveLocationProvider = new ReactiveLocationProvider(application);
        messageList = Transformations.switchMap(currentSchool, school -> {
            setSchool(school);
            return LiveDataReactiveStreams.fromPublisher(
                    repository.setSchool(school)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
            );
        });

    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }

    public LiveData<Boolean> editEnabled() {
        return editEnabled;
    }

    @SuppressLint("MissingPermission")
    public void setSchool(School school) {
        editEnabled.setValue(false);
        if (PermissionUtil.hasLocationPermissions(getApplication())) {
            reactiveLocationProvider.getLastKnownLocation()
                    .firstOrError()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((location, throwable) -> {
                        if (throwable != null) {
                            Timber.e(throwable);
                            editEnabled.setValue(false);
                        } else {
                            if (DistanceUtil.distance(school, location,
                                    0.0,
                                    0.0) < school.getRadius() * 1000) {
                                editEnabled.setValue(true);
                            } else {
                                editEnabled.setValue(false);
                            }
                        }
                    });
        }
    }

    public LiveData<List<ChatMessage>> messageList() {
        return messageList;
    }

    @SuppressLint("MissingPermission")
    public void sendMessage(String message) {
        final Disposable subscribe = reactiveLocationProvider.getLastKnownLocation()
                .firstOrError()
                .flatMapCompletable(location -> {
                    final String uid = FirebaseAuth.getInstance().getUid();
                    return repository.postMessage(uid, message, location);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        compositeDisposable.add(subscribe);
    }
}
