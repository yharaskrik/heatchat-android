package heatchat.unite.com.heatchat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;

@Database(entities = {ChatMessage.class, School.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ChatMessageDao chatMessageDao();

    public abstract SchoolDao schoolDao();
}
