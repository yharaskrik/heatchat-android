package heatchat.unite.com.heatchat.query;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import heatchat.unite.com.heatchat.AppDatabase;
import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;

public class MessagesQuery {

    private static int maxMessages = 100;
    private static AppDatabase db;

    public MessagesQuery(Context context) {
        db = Room.databaseBuilder(context,
                AppDatabase.class, "heatchat-message-db").fallbackToDestructiveMigration().build();
    }

    public void saveMessage(ChatMessage message) {
        new SaveMessageTask().execute(message);
    }

    public List<ChatMessage> getMessages(School school) {
        try {
            return (new GetMessagesTask().execute(school).get());
        } catch (ExecutionException e) {
            return new ArrayList<>();
        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
    }

    private static class GetMessagesTask extends AsyncTask<School, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(School... schools) {
            List<ChatMessage> messages = db.chatMessageDao().loadMessagesByPath(schools[0].getPath());
            if (messages.size() > 100)
                return messages.subList(messages.size() - maxMessages + 1, messages.size() - 1);
            else
                return messages;
        }
    }

    private static class SaveMessageTask extends AsyncTask<ChatMessage, Void, Integer> {
        @Override
        protected Integer doInBackground(ChatMessage... messages) {
            db.chatMessageDao().insertAll(messages);
            return 1;
        }
    }
}
