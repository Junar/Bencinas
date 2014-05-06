package cl.gob.datos.bencinas.helpers;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cl.gob.datos.bencinas.controller.AppController;

import com.junar.searchbenzine.Benzine;
import com.junar.searchbenzine.dao.BenzineDao;
import com.junar.searchbenzine.dao.DaoMaster;
import com.junar.searchbenzine.dao.DaoMaster.DevOpenHelper;
import com.junar.searchbenzine.dao.DaoSession;

public class LocalDao {
    private DevOpenHelper helper;
    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private BenzineDao benzineDao;
    private Context mContext;

    public LocalDao(Context context) {
        this.initDatabase(context);
    }

    private void initDatabase(Context context) {
        helper = new DaoMaster.DevOpenHelper(context, "searchbenzine-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        benzineDao = getBenzineDao();
        mContext = context;

        Log.i("cache_benzine",
                "total bencineras en cache:" + this.getCachePharmaCount());
    }

    public DaoSession getDaoSession() {
        return this.daoSession;
    }

    protected BenzineDao getBenzineDao() {
        return this.daoSession.getBenzineDao();
    }

    public Boolean isFirstPopulate() {
        return (Settings.getLastSyncDate(mContext).equals("")) ? true : false;
    }

    public void cacheBenzineList(List<Benzine> list) {
        this.benzineDao.insertInTx(list);
    }

    public void cleanCacheBenzineList() {
        this.benzineDao.deleteAll();
    }

    public long getCachePharmaCount() {
        return this.benzineDao.count();
    }

    public List<Benzine> getBenzineList() {
        return this.benzineDao.queryBuilder().list();
    }

    public Benzine getBenzineBestPrice() {
        List<Benzine> benzineList = benzineDao
                .queryBuilder()
                .orderRaw(
                        "(abs(latitude - ("
                                + AppController.getLastLocation().getLatitude()
                                + ")) + abs( longitude - ("
                                + AppController.getLastLocation()
                                        .getLongitude() + "))) LIMIT 3").list();

        Benzine benzineTemp = null;
        for (Benzine benzine : benzineList) {
            if (benzine.getDistanceTo(AppController.getActualLatLng()) <= Settings
                    .getCurrentRadio(mContext)) {
                if (benzineTemp == null) {
                    benzineTemp = benzine;
                    continue;
                }
                if (benzine.getSelectedBenzinePrice(mContext) < benzineTemp
                        .getSelectedBenzinePrice(mContext)) {
                    benzineTemp = benzine;
                }
            }
        }
        if (benzineTemp != null
                && Settings.getBenzineBestPrice(mContext).equals(benzineTemp
                        .getId())) {
            benzineTemp = null;
        }

        return benzineTemp;
    }

    public Benzine getBenzineById(long id) {
        return benzineDao.queryBuilder().where(BenzineDao.Properties.Id.eq(id))
                .unique();
    }
}