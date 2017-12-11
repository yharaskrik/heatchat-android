package heatchat.unite.com.heatchat.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import heatchat.unite.com.heatchat.models.ChatMessage;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM chatmessage WHERE path = :path ORDER BY time DESC LIMIT 100")
    Flowable<List<ChatMessage>> loadMessagesByPath(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ChatMessage... messages);

    @Update
    void updateMessage(ChatMessage message);

    @Query("SELECT * FROM chatmessage WHERE path = :path ORDER BY time DESC LIMIT 1")
    ChatMessage getLastMessage(String path);

    @Query("SELECT * FROM chatmessage WHERE path = :path ORDER BY time DESC LIMIT 1")
    Maybe<ChatMessage> getLastMessageMaybe(String path);

    @Delete
    void delete(ChatMessage message);

    @Query("DELETE FROM chatmessage WHERE path = :path and time < :time")
    void deleteOldMessages(String path, long time);
}
