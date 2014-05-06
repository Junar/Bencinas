package cl.gob.datos.bencinas.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent;
    private static AlarmManager alarmMgr;
    private long mInterval = 5000;

    @Override
    public void onReceive(Context context, Intent intent) {
            if (cl.gob.datos.bencinas.helpers.Settings
                    .getSoundNotificationStatus(context)) {
                startAlarmManager(context);
            } else {
                stopAlarmManager();
            }
    }

    private void startAlarmManager(Context context) {
        if (alarmMgr == null) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            alarmMgr = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + mInterval, mInterval,
                    pendingIntent);
        }
    }

    private void stopAlarmManager() {
        if (alarmMgr != null) {
            alarmMgr.cancel(pendingIntent);
        }
    }
}
