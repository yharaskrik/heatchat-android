package heatchat.unite.com.heatchat.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Helper methods for checking permissions
 *
 * Created by Andrew on 12/10/2017.
 */

public class PermissionUtil {

    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}
