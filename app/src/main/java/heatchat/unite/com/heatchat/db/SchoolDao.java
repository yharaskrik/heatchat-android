package heatchat.unite.com.heatchat.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import heatchat.unite.com.heatchat.models.School;
import io.reactivex.Flowable;

/**
 * Dao class for adding, deleting and getting schools from the local room database.
 */
@Dao
public abstract class SchoolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSchools(List<School> schools);

    @Query("SELECT * FROM school")
    public abstract Flowable<List<School>> selectSchools();

    @Query("DELETE FROM school")
    public abstract void deleteAllSchools();

    @Transaction
    public void replaceSchools(List<School> schools) {
        deleteAllSchools();
        insertSchools(schools);
    }

}
