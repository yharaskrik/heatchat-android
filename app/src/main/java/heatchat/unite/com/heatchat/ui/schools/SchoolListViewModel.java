package heatchat.unite.com.heatchat.ui.schools;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import heatchat.unite.com.heatchat.models.School;

/**
 * TODO: Move code to use case class
 * Created by Andrew on 12/10/2017.
 */

public class SchoolListViewModel extends ViewModel {
    MutableLiveData<List<School>> schools = new MutableLiveData<>();

    @Inject
    public SchoolListViewModel() {
    }

    public LiveData<List<School>> getSchools() {
        if (schools.getValue() == null) {
            FirebaseDatabase.getInstance()
                    .getReference().child("schools")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                schools.postValue(toSchools(dataSnapshot));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        return schools;
    }

    private List<School> toSchools(DataSnapshot dataSnapshot) {
        List<School> schools = new ArrayList<>();
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            School school = child.getValue(School.class);
/*            if (isLocation) {
                school.setDistance(distance(latitude,
                        school.getLat(),
                        longitude,
                        school.getLon(),
                        0.0,
                        0.0));
            }*/
            schools.add(school);
        }
        return schools;
    }
}
