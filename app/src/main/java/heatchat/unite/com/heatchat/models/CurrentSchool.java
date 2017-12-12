package heatchat.unite.com.heatchat.models;

import android.arch.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A class that represents the currently selected school. This loads the last viewed school from the
 * user preferences when it first initializes. Interested components can observe changes as this is
 * a LiveData subclass. This can also be changed by any component that is in charge of updating the
 * current school.
 * <p>
 * This class is a singleton that is managed by dagger.
 * <p>
 * Created by Andrew on 12/11/2017.
 */
@Singleton
public class CurrentSchool extends MutableLiveData<School> {

    @Inject
    public CurrentSchool() {
        super();
        //TODO Initialize default value
    }

    @Override
    public void setValue(School value) {
        super.setValue(value);
        // TODO save selection
    }
}
