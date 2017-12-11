package heatchat.unite.com.heatchat.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.util.LiveDataResult;
import timber.log.Timber;

/**
 * Created by Andrew on 12/10/2017.
 */

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<School> selectedSchool = new MutableLiveData<>();
    private final MutableLiveData<LiveDataResult<FirebaseUser>> user = new MutableLiveData<>();

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
}
