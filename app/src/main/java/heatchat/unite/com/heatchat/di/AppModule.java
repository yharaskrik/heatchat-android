package heatchat.unite.com.heatchat.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import heatchat.unite.com.heatchat.db.AppDatabase;
import heatchat.unite.com.heatchat.db.ChatMessageDao;
import heatchat.unite.com.heatchat.db.SchoolDao;

/**
 * App Module to initialize app wide dependencies that require special handling.
 */
@Module(includes = {ViewModelModule.class})
class AppModule {

    @Singleton
    @Provides
    AppDatabase provideDB(Application app) {
        return Room.databaseBuilder(app, AppDatabase.class, "heatchat-message-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    ChatMessageDao provideChatMessageDao(AppDatabase appDatabase) {
        return appDatabase.chatMessageDao();
    }

    @Singleton
    @Provides
    SchoolDao provideSchoolDao(AppDatabase appDatabase) {
        return appDatabase.schoolDao();
    }


    @Singleton
    @Provides
    FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
