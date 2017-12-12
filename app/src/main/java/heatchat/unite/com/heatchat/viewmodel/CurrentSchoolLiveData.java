package heatchat.unite.com.heatchat.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;

import heatchat.unite.com.heatchat.models.School;

/**
 * Created by Andrew on 12/11/2017.
 */

public class CurrentSchoolLiveData extends MutableLiveData<School> {
    private static CurrentSchoolLiveData sInstance;

    @MainThread
    public static CurrentSchoolLiveData get() {
        if (sInstance == null) {
            sInstance = new CurrentSchoolLiveData();
        }
        return sInstance;
    }
}
