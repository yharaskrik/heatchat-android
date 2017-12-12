package heatchat.unite.com.heatchat.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import heatchat.unite.com.heatchat.MainActivity;

/**
 * Injection for the main activity
 */

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivity();
}
