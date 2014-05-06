package cl.gob.datos.bencinas.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import cl.gob.datos.bencinas.controller.AppController;

public class Settings {

    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    public final static String PREF_CURRENT_RADIO = "pref_current_radio";
    public final static String PREF_ENABLE_SOUND_NOTIFICATION = "pref_sound_notification";
    public final static String PREF_BENZINE_TYPE = "pref_benzine_type";
    public final static String PREF_BENZINE_BEST_PRICE = "pref_benzine_best_price";
    public static final String PREF_OPENED_FROM_NOTIFICATION = "pref_opened_from_notification";
    public final static String PREF_LAST_SYNC_DATE = "pref_last_sync_date";

    private static SharedPreferences getInstance(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("prefs", 0);
        }
        return prefs;
    }

    /**
     * Get the current radio
     * 
     * @param context
     * @return
     */
    public static Integer getCurrentRadio(Context context) {
        return Settings.getInstance(context).getInt(
                Settings.PREF_CURRENT_RADIO,
                (int) AppController.MAX_RADIO_IN_METERS / 2);
    }

    /**
     * Get the best price benzine code
     * 
     * @param context
     * @return
     */
    public static Long getBenzineBestPrice(Context context) {
        return Settings.getInstance(context).getLong(
                Settings.PREF_BENZINE_BEST_PRICE, 0);
    }

    /**
     * Get Sound Notification Status.
     * 
     * @param context
     * @return
     */
    public static boolean getSoundNotificationStatus(Context context) {
        return Settings.getInstance(context).getBoolean(
                Settings.PREF_ENABLE_SOUND_NOTIFICATION, false);
    }

    /**
     * Get Benzine Type
     * 
     * @param context
     * @return
     */
    public static String getBenzineType(Context context) {
        return Settings.getInstance(context).getString(
                Settings.PREF_BENZINE_TYPE, "");
    }

    public static void writeSetting(Context context, String key, Object value) {
        if (editor == null) {
            editor = Settings.getInstance(context).edit();
        }
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.commit();
    }

    public static boolean openedFromNotification(Context context) {
        return Settings.getInstance(context).getBoolean(
                Settings.PREF_OPENED_FROM_NOTIFICATION, false);
    }

    public static void openedFromNotification(Context context, boolean value) {
        Settings.writeSetting(context, Settings.PREF_OPENED_FROM_NOTIFICATION,
                value);
    }

    public static String getLastSyncDate(Context context) {
        return Settings.getInstance(context).getString(
                Settings.PREF_LAST_SYNC_DATE, "");
    }

    public static void saveLastSyncDate(Context context) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Settings.writeSetting(context, Settings.PREF_LAST_SYNC_DATE,
                sdf.format(now.getTime()));
    }
}