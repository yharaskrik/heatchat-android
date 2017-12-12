package heatchat.unite.com.heatchat.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import heatchat.unite.com.heatchat.ui.ChatFragment;
import heatchat.unite.com.heatchat.ui.SchoolListFragment;

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
