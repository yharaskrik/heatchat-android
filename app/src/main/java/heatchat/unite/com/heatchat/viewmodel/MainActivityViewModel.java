package heatchat.unite.com.heatchat.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.CurrentSchool;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.UserRepository;
import heatchat.unite.com.heatchat.util.LocationLiveData;

/**
 * ViewModel for the Main activity. This provides a location tracker and the current user.
 */

public class MainActivityViewModel extends ViewModel {

    private final MediatorLiveData<Location> locationMediator = new MediatorLiveData<>();
    private final LocationLiveData locationLiveData;
    private final LiveData<UserRepository.UserResult> user;
    private final UserRepository userRepository;
    private CurrentSchool currentSchool;


    @Inject
    MainActivityViewModel(LocationLiveData locationLiveData, UserRepository userRepository,
                          CurrentSchool currentSchool) {
        this.locationLiveData = locationLiveData;
        user = userRepository.getUser();
        this.userRepository = userRepository;
        this.currentSchool = currentSchool;
        // Update the user's location if the location changes.
        this.locationMediator.addSource(locationLiveData, location -> {
            locationMediator.postValue(location);
            if (location != null) {
                userRepository.sendLocation(location);
            }
        });
    }

    public void retryLogin(){
        userRepository.signInUser();
    }

    public LiveData<Location> locationUpdates() {
        return locationMediator;
    }

    public LiveData<School> currentSchool() {
        return currentSchool;
    }

    /**
     * The LiveData user object. This exposes events
     *
     * @return
     */
    public LiveData<UserRepository.UserResult> getUser() {
        return user;
    }

    /**
     * Initializes the location when the permissions are given and the location already has an
     * active tracker.
     */
    public void startLocation() {
        if (locationLiveData.hasActiveObservers()) {
            locationLiveData.startTrackingUpdates();
        }
    }
}
