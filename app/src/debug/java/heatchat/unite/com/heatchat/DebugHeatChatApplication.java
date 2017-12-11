package heatchat.unite.com.heatchat;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * A debug application that enables Stetho and a Timber debugging instance.
 */

public class DebugHeatChatApplication extends HeatChatApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        Timber.plant(new Timber.DebugTree());
    }
}
