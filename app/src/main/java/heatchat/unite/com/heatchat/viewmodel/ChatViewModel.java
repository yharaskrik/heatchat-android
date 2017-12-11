package heatchat.unite.com.heatchat.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import heatchat.unite.com.heatchat.models.ChatMessage;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.respository.ChatMessageRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andrew on 12/10/2017.
 */

public class ChatViewModel extends AndroidViewModel {


    ChatMessageRepository repository;
    private LiveData<List<ChatMessage>> schoolMessages = new MutableLiveData<>();


    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatMessageRepository(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void setSchool(School school) {
        schoolMessages = LiveDataReactiveStreams.fromPublisher(repository.setSchool(school)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        );
    }

    public LiveData<List<ChatMessage>> getSchoolMessages() {
        return schoolMessages;
    }
}
