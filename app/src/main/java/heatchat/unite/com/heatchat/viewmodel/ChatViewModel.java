package heatchat.unite.com.heatchat.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.ChatMessageRepository;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.util.PermissionUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;
import timber.log.Timber;

/**
 * Created by Andrew on 12/10/2017.
 */

public class ChatViewModel extends AndroidViewModel {

    ChatMessageRepository repository;
    ReactiveLocationProvider reactiveLocationProvider;
    private LiveData<List<ChatMessage>> schoolMessages = new MutableLiveData<>();
    private MutableLiveData<Boolean> editEnabled = new MutableLiveData<>();
    private School selectedSchool;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatMessageRepository(application);
        editEnabled.setValue(false);
        reactiveLocationProvider = new ReactiveLocationProvider(application);
    }

    public LiveData<Boolean> editEnabled() {
        return editEnabled;
    }

    @SuppressLint("MissingPermission")
    public void setSchool(School school) {
        this.selectedSchool = school;
        editEnabled.setValue(false);
        schoolMessages = LiveDataReactiveStreams.fromPublisher(repository.setSchool(school)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        );

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

    public LiveData<List<ChatMessage>> getSchoolMessages() {
        return schoolMessages;
    }

    public void sendMessage(String message){
        



/*        if (checkSchoolLocation()) {
            if (body != "" && !body.isEmpty()) {
                ChatMessage message = new ChatMessage(FirebaseAuth.getInstance().getUid(), body, this.latitude, this.longitude);
                Map<String, Object> postValues = message.toMap();

                String key = mDatabase
                        .child("schoolMessages")
                        .child(this.selectedSchool.getPath())
                        .child("messages").push().getKey();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(
                        "schoolMessages/" + this.selectedSchool.getPath() + "/messages/" + key,
                        postValues);

                mDatabase.updateChildren(childUpdates);

                input.setText("");*//*

//                ArrayList<School> addSchools = new ArrayList<>();
//                key = mDatabase.child("schools").push().getKey();
//                childUpdates = new HashMap<>();
//                childUpdates.put("/schools/" + key, postValues);
//                mDatabase.updateChildren(childUpdates);
            }
        }*/
    }

}
