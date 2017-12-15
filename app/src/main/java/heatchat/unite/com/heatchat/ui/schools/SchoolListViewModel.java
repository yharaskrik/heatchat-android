package heatchat.unite.com.heatchat.ui.schools;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import java.util.List;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.CurrentSchool;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.SchoolRepository;
import heatchat.unite.com.heatchat.util.DistanceUtil;
import heatchat.unite.com.heatchat.util.LocationLiveData;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A view model for the School Listing.
 * <p>
 * This provides methods to get the schools, refresh the school list and set the current school.
 */

public class SchoolListViewModel extends ViewModel {
    private SchoolRepository repository;
    private LocationLiveData locationLiveData;
    private CurrentSchool currentSchool;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SchoolListViewModel(SchoolRepository repository, LocationLiveData locationLiveData,
                               CurrentSchool currentSchool) {
        this.repository = repository;
        this.locationLiveData = locationLiveData;
        this.currentSchool = currentSchool;
    }

    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }

    public LiveData<List<School>> getSchools() {
        return LiveDataReactiveStreams.fromPublisher(repository.schools()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMap(schools1 -> {
                    Flowable<School> schoolFlowable = Flowable.fromIterable(schools1);
                    final Location value = locationLiveData.getValue();
                    if (value != null) {
                        Timber.d("Sorting schools with location %s", value);
                        schoolFlowable = schoolFlowable.map(school -> {
                            school.setDistance(DistanceUtil.distance(school, value, 0, 0));
                            Timber.d(school.toString());
                            return school;
                        });
                    }
                    return schoolFlowable
                            .toSortedList()
                            .toFlowable();
                })
                .observeOn(AndroidSchedulers.mainThread()));
    }

    void refresh() {
        final Disposable subscribe = repository.refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Timber.d("Got a new schools list.");
                }, throwable -> {
                    Timber.e(throwable);
                });
        compositeDisposable.add(subscribe);
    }

    void setCurrentSchool(School school) {
        currentSchool.setValue(school);
    }
}
