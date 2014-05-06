package cl.gob.datos.bencinas.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.SyncController;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.junar.searchbenzine.Benzine;

public class Utils {

    private final static String TAG = Utils.class.getSimpleName();
    public static final int DEFAULT_JPG_QUALITY = 70;
    private final static String KEY_DAY = "day";
    private final static String KEY_MONTH = "month";
    private final static String KEY_YEAR = "year";
    public final static int PLAY_SERVICES_DIALOG = 399285;

    public static void openFragment(Fragment srcFrg, Fragment dstFragment,
            Bundle args, int idContainer, boolean addToBack) {
        openFragment(srcFrg, dstFragment, args, idContainer, addToBack, null);
    }

    public static void openFragment(Fragment srcFrg, Fragment dstFragment,
            Bundle args, int idContainer, boolean addToBack, String tag) {
        FragmentManager fragmentManager = srcFrg.getFragmentManager();
        if (args != null) {
            dstFragment.setArguments(args);
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (tag == null) {
            ft.replace(idContainer, dstFragment);
        } else {
            ft.replace(idContainer, dstFragment, tag);
        }
        if (addToBack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    public static Intent createShareIntent(Context context, Benzine benzine) {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("Bencinera: ");
        localStringBuilder.append(benzine.getName());
        localStringBuilder.append("\n");
        localStringBuilder.append("Dirección: ");
        localStringBuilder.append(benzine.getAddress());
        localStringBuilder.append("\n");
        if (benzine.getGasolina93() != null) {
            localStringBuilder.append("Gas 93: ");
            localStringBuilder.append(benzine.getCurrencyPrice(benzine
                    .getGasolina93()));
            localStringBuilder.append("\n");
        }
        if (benzine.getGasolina95() != null) {
            localStringBuilder.append("Gas 95: ");
            localStringBuilder.append(benzine.getCurrencyPrice(benzine
                    .getGasolina95()));
            localStringBuilder.append("\n");
        }
        if (benzine.getGasolina97() != null) {
            localStringBuilder.append("Gas 97: ");
            localStringBuilder.append(benzine.getCurrencyPrice(benzine
                    .getGasolina97()));
            localStringBuilder.append("\n");
        }
        if (benzine.getDiesel() != null) {
            localStringBuilder.append("Disel: ");
            localStringBuilder.append(benzine.getCurrencyPrice(benzine
                    .getDiesel()));
            localStringBuilder.append("\n");
        }
        if (benzine.getKerosene() != null) {
            localStringBuilder.append("Kerosene: ");
            localStringBuilder.append(benzine.getKerosene());
            localStringBuilder.append("\n");
        }
        if (benzine.getLatitude() != 0 && benzine.getLongitude() != 0) {
            localStringBuilder.append("Ubicación: ");
            localStringBuilder.append("http://maps.google.com/?q="
                    + benzine.getLatitude() + "," + benzine.getLongitude());
            localStringBuilder.append("\n");
        }

        localStringBuilder.append(context
                .getString(R.string.more_benzine_station)
                + context.getPackageName());
        Intent localIntent = new Intent("android.intent.action.SEND");
        localIntent.setType("text/plain");
        localIntent.putExtra("android.intent.extra.SUBJECT",
                "Bencinera recomendada");
        localIntent.putExtra("android.intent.extra.TEXT",
                localStringBuilder.toString());
        return localIntent;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isGooglePlayAvailable(Context context) {
        return isGooglePlayAvailable(context, null);
    }

    public static boolean isGooglePlayAvailable(Activity activity) {
        return isGooglePlayAvailable(null, activity);
    }

    private static boolean isGooglePlayAvailable(Context context,
            Activity activity) {
        if (context == null && activity != null)
            context = activity.getApplicationContext();
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.i(TAG, "Google Play services is available.");
            return true;
        }
        Log.e(TAG, "Google Play services is NOT available.");
        if (activity != null) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                    PLAY_SERVICES_DIALOG).show();
        }
        return false;
    }

    public static String getDatePhone(Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                SyncController.PREFS_NAME, 0);
        String formatteDate = pref.getString(KEY_DAY, "") + "/"
                + pref.getString(KEY_MONTH, "") + "/"
                + pref.getString(KEY_YEAR, "");
        return formatteDate;
    }

    public static boolean resizeImageByWidth(String imagePath, int width) {
        return resizeImageByWidth(imagePath, width, DEFAULT_JPG_QUALITY);
    }

    public static boolean resizeImageByWidth(String imagePath, int width,
            int quality) {
        try {
            Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inDither = false;
            options.inPurgeable = true;

            Bitmap image = BitmapFactory.decodeFile(imagePath, options);
            Float imageWidth = new Float(image.getWidth());
            Float imageHeight = new Float(image.getHeight());
            Float ratio = imageHeight / imageWidth;
            compressJpgImage(Bitmap.createScaledBitmap(image, width,
                    (int) (width * ratio), false), quality, imagePath);
            image.recycle();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Compress a JPG image from a file and write it in the output file
     * 
     * This method compress a JPG image from a given path with the given quality
     * and write the compressed image to the output given path
     * 
     * @param imagePath
     *            the image to compress
     * @param quality
     *            the quality used to compress
     * @param output
     *            the output file path
     * @return boolean true if it could write the image
     * @throws FileNotFoundException
     */
    public static boolean compressJpgImage(String imagePath, int quality,
            String output) {
        Options options = new BitmapFactory.Options();
        options.inScaled = false;
        // options.inSampleSize = 8;
        options.inTempStorage = new byte[32 * 1024];
        Bitmap image = BitmapFactory.decodeFile(imagePath, options);
        return compressJpgImage(image, quality, output);
    }

    public static boolean compressJpgImage(Bitmap image, int quality,
            String output) {
        try {
            Boolean result = image.compress(Bitmap.CompressFormat.JPEG,
                    quality, new FileOutputStream(output));
            System.gc();
            image.recycle();
            return result;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Activity activity, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static Boolean isActivityRunning(Class activityClass, Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager
                .getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(
                    task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }
}