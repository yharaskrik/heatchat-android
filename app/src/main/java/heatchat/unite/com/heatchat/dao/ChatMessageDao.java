package heatchat.unite.com.heatchat.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import heatchat.unite.com.heatchat.models.ChatMessage;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM chatmessage WHERE path = :path")
    List<ChatMessage> loadMessagesByPath(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ChatMessage... messages);

    @Update
    void updateMessage(ChatMessage message);

    @Delete
    void delete(ChatMessage message);
}
