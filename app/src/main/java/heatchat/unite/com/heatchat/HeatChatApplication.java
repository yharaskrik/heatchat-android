package heatchat.unite.com.heatchat;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Andrew on 12/10/2017.
 */

public class HeatChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
