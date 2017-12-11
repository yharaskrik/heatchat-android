package heatchat.unite.com.heatchat.respository;

import android.arch.lifecycle.LiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import heatchat.unite.com.heatchat.models.School;

/**
 * Created by Andrew on 12/10/2017.
 */

public class SchoolRepository {

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();



}
