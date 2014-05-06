package cl.gob.datos.bencinas.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.AppController;
import cl.gob.datos.bencinas.helpers.Settings;

import com.google.android.gms.maps.model.LatLng;
import com.junar.searchbenzine.Benzine;

public class AlarmReceiver extends BroadcastReceiver {
    Context mContext = null;
    private static final int NOTIF_ALERTA_ID = 1;
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent in) {
        try {
            mContext = context;
            createNotification(AppController.getActualLatLng());
        } catch (Exception e) {
            Log.d(TAG, "There was an error creating the notification.");
        }
    }

    private void createNotification(LatLng currentLocation) {
        Benzine benzine = AppController.getNearestBenzine(
                AppController.getActualLatLng(),
                Settings.getCurrentRadio(mContext));
        if (benzine != null && Settings.getSoundNotificationStatus(mContext)) {
            NotificationManager mNotificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext)
                    .setSmallIcon(R.drawable.fuelwhitesmall)
                    .setContentTitle(
                            mContext.getString(R.string.notification_title))
                    .setContentText(
                            mContext.getString(R.string.notification_message))
                    .setAutoCancel(true);

            Intent notIntent = new Intent(mContext, BenzineDetailActivity.class);
            notIntent.putExtra("openFromNotification", true);
            notIntent.putExtra("id", benzine.getId());
            PendingIntent contIntent = PendingIntent.getActivity(mContext, 0,
                    notIntent, 0);
            mBuilder.setContentIntent(contIntent);
            Notification notif = mBuilder.build();
            notif.defaults |= Notification.DEFAULT_VIBRATE;
            notif.defaults |= Notification.DEFAULT_SOUND;
            notif.defaults |= Notification.DEFAULT_LIGHTS;
            mNotificationManager.notify(NOTIF_ALERTA_ID, notif);
            Settings.writeSetting(mContext, Settings.PREF_BENZINE_BEST_PRICE,
                    benzine.getId());
        }
    }
}
