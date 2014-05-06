package cl.gob.datos.bencinas.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cl.gob.datos.bencinas.helpers.BenzineJsonHelper;
import cl.gob.datos.bencinas.helpers.LocalDao;
import cl.gob.datos.bencinas.helpers.Settings;
import cl.gob.datos.bencinas.helpers.Utils;

import com.junar.searchbenzine.Benzine;

public class SyncController {
    private static final String TAG = SyncController.class.getSimpleName();
    public static final String PREFS_NAME = "syncPharmaPreference";
    public static final String KEY_DAY = "day";
    public static final String KEY_MONTH = "month";
    public static final String KEY_YEAR = "year";
    public static final String KEY_TIMESTAMP = "timestamp";
    private LocalDao localDao;
    private Context mContext;

    public SyncController(Context context) throws TimeoutException,
            JSONException, IOException {
        mContext = context;
        localDao = AppController.getInstace().getLocalDao();
        initCache();
    }

    private void initCache() throws TimeoutException, JSONException,
            IOException {

        boolean firstTime = false;
        if (Utils.isOnline(mContext)
                && (localDao.isFirstPopulate() || isDiferentSyncDay())) {
            try {
                if (localDao.isFirstPopulate()) {
                    firstTime = true;
                }
                cacheBenzineList();
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                if (firstTime || isDiferentSyncDay()) {
                    throw new JSONException(e.getMessage());
                }
            }
        } else {
            if (localDao.isFirstPopulate() || isDiferentSyncDay()) {
                throw new TimeoutException("There is not internet conection");
            }
        }
    }

    private void cacheBenzineList() throws JSONException, TimeoutException {

        final BenzineJsonHelper benzineDao = new BenzineJsonHelper();

        final String jsonArrayResponse = benzineDao
                .invokeDatastream(BenzineJsonHelper.DATA_GUID);
        if (jsonArrayResponse == null) {
            throw new TimeoutException("There is not internet conection");
        }
        final JSONArray jArray = new JSONObject(jsonArrayResponse)
                .getJSONArray("result");

        if (jArray.length() == 0) {
            throw new JSONException("There is an error parsing benzine list");
        }

        Runnable runner = new Runnable() {
            private String hasFail = "false";

            public String toString() {
                return hasFail;
            }

            @Override
            public void run() {
                try {
                    localDao.cleanCacheBenzineList();
                    List<Benzine> benzineList = benzineDao
                            .parseJsonArrayResponse(jsonArrayResponse);
                    localDao.cacheBenzineList(benzineList);
                    Settings.saveLastSyncDate(mContext);
                } catch (Exception e) {
                    if (localDao.isFirstPopulate()) {
                        hasFail = "true";
                    } else {
                        hasFail = "toast";
                    }
                }
            }

        };

        localDao.getDaoSession().runInTx(runner);
        if (runner.toString().equals("true")) {
            throw new JSONException("There is an error parsing benzine list");
        } else if (runner.toString().equals("toast")) {
            throw new JSONException("Toast");
        }
    }

    private boolean isDiferentSyncDay() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (!Settings.getLastSyncDate(mContext).equals(
                sdf.format(now.getTime()))) {
            return true;
        }
        return false;
    }
}