package org.uniroma2.sdcc.Utils;

import com.github.fedy2.weather.data.Astronomy;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.unit.Time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;

/**
 * defines methods for checking right light intensity
 * comparing to given weather conditions
 */
public class WeatherHelper {


    public static final Float CLOUDY_SKY_INTENSITY_MINIMUM = 70.0f;
    public static final Float SUNNY_SKY_INTENSITY_MAXIMUM = 10.0f;
    public static final Float VISIBILITY_INTENSITY_MINIMUM = 80.0f;
    public static final Float VISIBILITY_INTENSITY_MAXIMUM = 10.0f;
    public static final Float DARK_SKY_INTENSITY_MINIMUM = 90.0f;
    public static final Float GLARE_SKY_INTENSITY_MAXIMUM = 50.0f;
    public static final Integer[] darkSkyCodes = {1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 19, 20, 21, 26, 27, 28, 29, 30, 37, 38, 39, 40, 44, 45, 47};


    public WeatherHelper() {
    }

    /**
     * checks if it's day now
     *
     * @param weather channel
     * @return true if it's day, false otherwise
     */
    public static boolean isDay(Channel weather) {
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime now = currentDate.toLocalDateTime();
        Integer nowHour = now.getHour();

        Astronomy astronomy = weather.getAstronomy();
        Time sunrise = astronomy.getSunrise();
        Time sunset = astronomy.getSunset();

        if (nowHour > sunrise.getHours() && nowHour < sunset.getHours()) {
            return true;
        }

        return false;

    }

    /**
     * check if it's cloudy
     *
     * @param weather channel
     * @return true if it's cloudy, else otherwise
     */
    public static boolean isCloudy(Channel weather) {

        return darkSkyFromCode(weather.getItem().getCondition().getCode());
    }

    /**
     * checks if it's dark sky from yahoo condition code
     *
     * @param code yahoo condition code
     * @return true if it's dark, false otherwise
     */
    private static boolean darkSkyFromCode(Integer code) {
        return Arrays.stream(darkSkyCodes).
                filter(e -> e.equals(code)).count() > 0;
    }

    /**
     * given certain visibility thresholds,
     * determines the difference between
     * the actual intensity and the interval
     * in which it should be
     *
     * @param intensity  value from lamp
     * @param visibility
     * @return hashmap with different values
     */
    public static HashMap<Float, Float> rightIntensityByVisibility(Float intensity, Float visibility) {
        HashMap<Float, Float> i = new HashMap<>();

        Float underGap = intensity - VISIBILITY_INTENSITY_MINIMUM;
        Float overGap = intensity - VISIBILITY_INTENSITY_MAXIMUM;
        if (!(visibility > .5) && underGap < 0) {
            i.put(VISIBILITY_INTENSITY_MINIMUM, underGap);
            i.put(VISIBILITY_INTENSITY_MAXIMUM, 0f);
            return i;
        } else if (visibility > .5 && overGap > 0) {
            i.put(VISIBILITY_INTENSITY_MINIMUM, 0f);
            i.put(VISIBILITY_INTENSITY_MAXIMUM, overGap);
            return i;
        }
        i.put(VISIBILITY_INTENSITY_MINIMUM, 0f);
        i.put(VISIBILITY_INTENSITY_MAXIMUM, 0f);
        return i;
    }


    /**
     * given certain weather thresholds,
     * determines the difference between
     * the actual intensity and the interval
     * in which it should be
     *
     * @param code      yahoo condition code
     * @param intensity value from lamp
     * @return hashmap with different values
     */
    public static HashMap<Float, Float> rightIntensityOnWeatherByCode(Integer code, Float intensity) {

        HashMap<Float, Float> i = new HashMap<>();
        Float underGap = intensity - CLOUDY_SKY_INTENSITY_MINIMUM;
        Float overGap = intensity - SUNNY_SKY_INTENSITY_MAXIMUM;
        // if cloudy sky
        if (darkSkyFromCode(code)) {
            if (underGap < 0) {
                i.put(CLOUDY_SKY_INTENSITY_MINIMUM, underGap);
                i.put(SUNNY_SKY_INTENSITY_MAXIMUM, 0f);
                return i;
            }
        } else if (overGap > 0) {
            i.put(SUNNY_SKY_INTENSITY_MAXIMUM, overGap);
            i.put(CLOUDY_SKY_INTENSITY_MINIMUM, 0f);
            return i;
        }
        i.put(CLOUDY_SKY_INTENSITY_MINIMUM, 0f);
        i.put(SUNNY_SKY_INTENSITY_MAXIMUM, 0f);
        return i;

    }

    /**
     * given certain astronomy (sunset,sunrise) thresholds,
     * determines the difference between
     * the actual intensity and the interval
     * in which it should be
     *
     * @param intensity lamp value
     * @param astronomy sunset,sunrise,etc
     * @return hashmap with different values
     */
    public static HashMap<Float, Float> rightIntesityByAstronomy(Float intensity, Astronomy astronomy) {
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime now = currentDate.toLocalDateTime();
        Integer nowHour = now.getHour();
        Integer nowMin = now.getMinute();

        Time sunrise = astronomy.getSunrise();
        Time sunset = astronomy.getSunset();

        HashMap<Float, Float> i = new HashMap<>();
        Float underGap = intensity - DARK_SKY_INTENSITY_MINIMUM;
        Float overGap = intensity - GLARE_SKY_INTENSITY_MAXIMUM;
            /* if it's dark */
        if (nowHour < sunrise.getHours() || nowHour > sunset.getHours()) {
            if (underGap < 0) {
                i.put(DARK_SKY_INTENSITY_MINIMUM, underGap);
                i.put(GLARE_SKY_INTENSITY_MAXIMUM, 0f);
                return i;
            }
        } else if (overGap > 0) {
            i.put(DARK_SKY_INTENSITY_MINIMUM, 0f);
            i.put(GLARE_SKY_INTENSITY_MAXIMUM, overGap);
            return i;
        }
        i.put(DARK_SKY_INTENSITY_MINIMUM, 0f);
        i.put(GLARE_SKY_INTENSITY_MAXIMUM, 0f);
        return i;
    }
}
