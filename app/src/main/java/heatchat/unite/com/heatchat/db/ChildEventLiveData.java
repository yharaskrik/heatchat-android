package heatchat.unite.com.heatchat.db;

import android.arch.lifecycle.LiveData;
import android.support.annotation.IntDef;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

/**
 * A LiveData implementation that listens for events on a FireBase ChildEventListener.
 * <p>
 * The LiveData listens on the query when there are active observer and stops when there is no
 * longer any active observers.
 * <p>
 * Each event is posted to the LiveData feed with a wrapper type of {@link ChildEvent}. This
 * contains a property with the type of event it was.
 */

public class ChildEventLiveData<T> extends LiveData<ChildEventLiveData.ChildEvent<T>>
        implements ChildEventListener {

    private final Class<T> clazz;

    private Query query;

    ChildEventLiveData(Query query, Class<T> clazz) {
        this.query = query;
        this.clazz = clazz;
    }

    @Override
    protected void onActive() {
        super.onActive();
        query.addChildEventListener(this);
        Timber.d("Subscribed to %s", query.getRef().toString());
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        query.removeEventListener(this);
        Timber.d("Un-subscribed from %s", query.getRef().toString());
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Timber.d("Get datasnapshot %s", dataSnapshot);
        postEvent(dataSnapshot, ChildEvent.ADDED);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Timber.d("Datasnapshot changed %s", dataSnapshot);
        postEvent(dataSnapshot, ChildEvent.CHANGED);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Timber.d("Datasnapshot removed %s", dataSnapshot);
        postEvent(dataSnapshot, ChildEvent.REMOVED);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Timber.d("Datasnapshot moved %s", dataSnapshot);
        postEvent(dataSnapshot, ChildEvent.MOVED);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Timber.e(databaseError.toException());
    }

    private void postEvent(DataSnapshot dataSnapshot, int event) {
        final T value = dataSnapshot.getValue(clazz);
        if (value != null) {
            setValue(new ChildEvent<>(value, event, dataSnapshot));
        }
    }

    public static class ChildEvent<V> {

        public static final int ADDED = 1;
        public static final int REMOVED = 2;
        public static final int CHANGED = 3;
        public static final int MOVED = 4;
        public static final int EXCEPTION = 5;
        private V value;
        private int event = ADDED;
        private DataSnapshot dataSnapshot;

        ChildEvent(V value, @EventType int event, DataSnapshot dataSnapshot) {
            this.value = value;
            this.event = event;
            this.dataSnapshot = dataSnapshot;
        }

        public DataSnapshot getDataSnapshot() {
            return dataSnapshot;
        }

        public V getValue() {
            return value;
        }

        @EventType
        public int getEvent() {
            return event;
        }

        ChildEvent<V> added() {
            this.event = ADDED;
            return this;
        }

        ChildEvent<V> removed() {
            this.event = REMOVED;
            return this;
        }

        ChildEvent<V> changed() {
            this.event = CHANGED;
            return this;
        }

        ChildEvent<V> moved() {
            this.event = MOVED;
            return this;
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({ADDED, REMOVED, CHANGED, MOVED, EXCEPTION})
        @interface EventType {
        }
    }
}
