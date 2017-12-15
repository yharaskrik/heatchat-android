package heatchat.unite.com.heatchat.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.CurrentSchool;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.models.LiveDataResult;
import heatchat.unite.com.heatchat.util.LocationLiveData;
import timber.log.Timber;

/**
 * Created by Andrew on 12/10/2017.
 */

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<LiveDataResult<FirebaseUser>> user = new MutableLiveData<>();
    private MutableLiveData<School> selectedSchool;
    private LocationLiveData locationLiveData;

    @Inject
    public SharedViewModel(CurrentSchool currentSchool, LocationLiveData locationLiveData) {
        selectedSchool = currentSchool;
        this.locationLiveData = locationLiveData;
    }

    public LocationLiveData locationUpdates() {
        return locationLiveData;
    }

    public void select(School school) {
        selectedSchool.setValue(school);
    }

    public LiveData<School> getSelectedSchool() {
        return selectedSchool;
    }


    public LiveData<LiveDataResult<FirebaseUser>> getUser() {
        if (user.getValue() == null || user.getValue().getResult() == null) {
            final FirebaseAuth auth = FirebaseAuth.getInstance();
            final FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                auth.signInAnonymously()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Timber.d("signInAnonymously:success");
                                user.setValue(new LiveDataResult<>(
                                        auth.getCurrentUser()));
                            } else {
                                user.setValue(new LiveDataResult<>(task.getException()));
                            }
                        });
            } else {
                user.setValue(new LiveDataResult<>(currentUser));
            }
        }
        return user;
    }

    /**
     * Starts the LocationTracking if there are already active observers.
     */
    public void setLocationPermissionsEnabled() {
        if (locationLiveData.hasActiveObservers()) {
            locationLiveData.startTrackingUpdates();
        }
    }
}
