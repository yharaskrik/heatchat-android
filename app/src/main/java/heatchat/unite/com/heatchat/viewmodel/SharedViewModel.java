package heatchat.unite.com.heatchat.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import heatchat.unite.com.heatchat.models.School;

/**
 * Created by Andrew on 12/10/2017.
 */

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<School> selectedSchool = new MutableLiveData<>();

    public void select(School school) {
        selectedSchool.setValue(school);
    }

    public LiveData<School> getSelectedSchool() {
        return selectedSchool;
    }

}
