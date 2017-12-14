package heatchat.unite.com.heatchat.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import heatchat.unite.com.heatchat.viewmodel.ChatViewModel;
import heatchat.unite.com.heatchat.viewmodel.DaggerViewModelFactory;
import heatchat.unite.com.heatchat.viewmodel.MainActivityViewModel;
import heatchat.unite.com.heatchat.viewmodel.SchoolListViewModel;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;

/**
 * A module that enables dependency injection for configured view models.
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel.class)
    abstract ViewModel bindChatViewModel(ChatViewModel chatViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SharedViewModel.class)
    abstract ViewModel bindSharedViewModel(SharedViewModel sharedViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SchoolListViewModel.class)
    abstract ViewModel bindSchoolListViewModel(SchoolListViewModel schoolListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel.class)
    abstract ViewModel bindMainActivityViewModel(MainActivityViewModel mainActivityViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(DaggerViewModelFactory factory);
}
