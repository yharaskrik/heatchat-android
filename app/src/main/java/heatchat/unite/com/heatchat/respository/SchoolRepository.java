package heatchat.unite.com.heatchat.respository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import heatchat.unite.com.heatchat.db.HeatChatFirebaseDB;
import heatchat.unite.com.heatchat.db.SchoolDao;
import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Manages functions to get the school list and save it to the local database.
 */
@Singleton
public class SchoolRepository {

    private final SchoolDao schoolDao;
    private final HeatChatFirebaseDB heatChatFirebaseDB;

    @Inject
    public SchoolRepository(SchoolDao schoolDao, HeatChatFirebaseDB heatChatFirebaseDB) {
        this.schoolDao = schoolDao;
        this.heatChatFirebaseDB = heatChatFirebaseDB;
    }

    /**
     * @return A Flowable that emits the list of schools initially and whenever the internal list
     * has changed.
     */
    public Flowable<List<School>> schools() {
        return schoolDao.selectSchools();
    }

    /**
     * Refreshes the school list from the firebase database. This replaces the new school list in
     * the local database with the new list. The local database can be observed with {@link
     * #schools()}.
     * <p>
     * The completable emits an exception if an error occurs getting the list from the firebase
     * database or saving it to the local database.
     * <p>
     * This Completable should not be run on the main thread.
     *
     * @return The Completable that refreshes the local school.
     */
    public Completable refresh() {
        return heatChatFirebaseDB.getSchools()
                .flatMapCompletable(schools ->
                        Completable.fromAction(() -> schoolDao.replaceSchools(schools))
                        .subscribeOn(Schedulers.io())
                );
    }

}
