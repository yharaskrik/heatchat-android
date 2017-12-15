package heatchat.unite.com.heatchat.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import heatchat.unite.com.heatchat.ui.chat.ChatFragment;
import heatchat.unite.com.heatchat.ui.schools.SchoolListFragment;

/**
 * Module for creating injectors for specific fragments
 */
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract ChatFragment contributeChatFragment();

    @ContributesAndroidInjector
    abstract SchoolListFragment contributeSchoolListFragment();
}
