package heatchat.unite.com.heatchat.util;

import android.location.Location;

import heatchat.unite.com.heatchat.models.School;

/**
 * Utils for dealing with location and distance calculations
 * <p>
 * Created by Andrew on 12/10/2017.
 */

public class DistanceUtil {

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static double distance(School school, Location location, double el1, double el2) {
        return distance(school.getLat(), location.getLatitude(), school.getLon(),
                location.getLongitude(), el1, el2);
    }
}
