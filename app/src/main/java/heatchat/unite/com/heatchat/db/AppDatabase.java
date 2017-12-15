package heatchat.unite.com.heatchat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import heatchat.unite.com.heatchat.models.ChatMessage;

@Database(entities = {ChatMessage.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();
}
