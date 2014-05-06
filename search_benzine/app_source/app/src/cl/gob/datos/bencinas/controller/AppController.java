package cl.gob.datos.bencinas.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import cl.gob.datos.bencinas.helpers.LocalDao;
import cl.gob.datos.bencinas.helpers.Utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.junar.searchbenzine.Benzine;

public class AppController extends Application implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = AppController.class.getSimpleName();
    public static float MAX_RADIO_IN_METERS = 10000;
    private static LocalDao localDao;
    private static AppController mInstance;
    private static LocationClient client;
    private static Location location;

    public static void connectLocationClient() {
        if (Utils.isGooglePlayAvailable(AppController.getInstace()
                .getApplicationContext())) {

            if (client == null) {
                client = new LocationClient(AppController.getInstace(),
                        AppController.getInstace(), AppController.getInstace());
            }
            if (client != null && !client.isConnected()) {
                client.connect();
            }
        }
    }

    public static void disconnectLocationClient() {
        if (client != null && client.isConnected()) {
            client.disconnect();
            client = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localDao = new LocalDao(getApplicationContext());
        mInstance = this;
        AppController.connectLocationClient();
    }

    public static AppController getInstace() {
        return mInstance;
    }

    public LocalDao getLocalDao() {
        return localDao;
    }

    public List<MarkerOptions> getMarkersList() {
        return getMarkersListForBenzineList(getBenzineList());
    }

    public List<MarkerOptions> getMarkersList(List<Benzine> benzine) {
        return getMarkersListForBenzineList(benzine);
    }

    public List<Benzine> getBenzineList() {
        List<Benzine> benzineList = localDao.getBenzineList();
        return benzineList;
    }

    public Benzine getBenzineById(long id) {
        return localDao.getBenzineById(id);
    }

    public List<MarkerOptions> getMarkersListForBenzineList(
            List<Benzine> benzineList) {
        List<MarkerOptions> markerList = new ArrayList<MarkerOptions>();
        Iterator<Benzine> it = benzineList.iterator();

        while (it.hasNext()) {
            Benzine benzine = it.next();

            markerList.add(getMarkerForBenzine(benzine));
        }

        return markerList;
    }

    public MarkerOptions getMarkerForBenzine(Benzine benzine) {
        MarkerOptions marker = new MarkerOptions()
                .position(
                        new LatLng(benzine.getLatitude(), benzine
                                .getLongitude())).title(benzine.getName())
                .snippet(benzine.toString()).icon(benzine.getMarkerIcon());

        return marker;
    }

    public List<Benzine> filterNearestBenzine(Location actualLocation,
            long curRadioInMeters) {
        LatLng location = new LatLng(actualLocation.getLatitude(),
                actualLocation.getLongitude());
        return filterNearestBenzine(location, curRadioInMeters);
    }

    public List<Benzine> filterNearestBenzine(LatLng actualLocation,
            long curRadioInMeters) {
        if (actualLocation == null)
            return null;
        List<Benzine> benzineMarkers = localDao.getBenzineList();
        List<Benzine> benzineInRadio = new ArrayList<Benzine>();
        for (Benzine benzine : benzineMarkers) {
            if (benzine.getDistanceTo(actualLocation) <= curRadioInMeters) {
                benzineInRadio.add(benzine);
            }
        }
        return benzineInRadio;
    }

    public static Benzine getNearestBenzine(LatLng actualLocation,
            long curRadioInMeters) {
        if (actualLocation == null)
            return null;
        return localDao.getBenzineBestPrice();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Log.i(TAG, "Connection failed: " + arg0.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (client != null) {
            Log.i(TAG, "Connection successfully");
            location = client.getLastLocation();
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Disconnected");
    }

    public static Location getLastLocation() {
        connectLocationClient();
        if (client != null && client.isConnected()) {
            Location tmp = client.getLastLocation();
            if (tmp != null) {
                location = tmp;
            }
        }
        return location;

        // For Testing Purposes
        // Location loc = new Location("ccc");
        // loc.setLatitude(-33.468108);
        // loc.setLongitude(-70.620975);
        // return loc;
    }

    public static LatLng getActualLatLng() {
        LatLng hereLatLng = null;
        Location location = getLastLocation();
        if (location != null) {
            hereLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
        }

        return hereLatLng;
    }
}