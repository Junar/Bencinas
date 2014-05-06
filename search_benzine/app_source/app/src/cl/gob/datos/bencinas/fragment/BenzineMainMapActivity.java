package cl.gob.datos.bencinas.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.AppController;
import cl.gob.datos.bencinas.helpers.MultipleOrientationSlidingDrawer;
import cl.gob.datos.bencinas.helpers.Settings;
import cl.gob.datos.bencinas.helpers.Utils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.junar.searchbenzine.Benzine;
import com.junar.searchbenzine.Benzine.Marca;

public class BenzineMainMapActivity extends ActionBarActivity implements
        ClusterManager.OnClusterClickListener<Benzine>,
        ClusterManager.OnClusterInfoWindowClickListener<Benzine>,
        ClusterManager.OnClusterItemClickListener<Benzine>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Benzine> {

    private GoogleMap googleMap;
    private Context context;
    private HashMap<String, Benzine> benzineList = new HashMap<String, Benzine>();
    private LatLngBounds.Builder builder;
    private AlertDialog settingDialog;
    private TextView rangeText;
    private LatLng currentLocation;
    private ClusterManager<Benzine> mClusterManager;
    private List<Benzine> markersList = new ArrayList<Benzine>();
    private GoogleMap mapa;
    SupportMapFragment fragment;
    private static final int MENU_ITEM_CONFIGURATION = 1;
    private static final int MENU_ITEM_REFRESH = 2;
    private static final int LOCATION_SETTINGS = 1234;
    private MultipleOrientationSlidingDrawer drawer;
    private Button buttonGas93;
    private Button buttonGas95;
    private Button buttonGas97;
    private Button buttonDisel;
    private Button buttonKerosene;
    private final static String RECEIVER_ACTION = "cl.gob.datos.bencinas.ALARM_CONFIGURATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyLocationStatus();
        setContentView(R.layout.fragment_benzine_closets);
        context = getApplicationContext();
        if (Settings.getSoundNotificationStatus(context)) {
            sendAlarmBrodcastMessage();
        }
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        buttonGas93 = (Button) findViewById(R.id.btn_gasoline_93);
        buttonGas95 = (Button) findViewById(R.id.btn_gasoline_95);
        buttonGas97 = (Button) findViewById(R.id.btn_gasoline_97);
        buttonDisel = (Button) findViewById(R.id.btn_diesel);
        buttonKerosene = (Button) findViewById(R.id.btn_kerosene);
        drawer = (MultipleOrientationSlidingDrawer) findViewById(R.id.drawer);
        if (Settings.getBenzineType(context).equals("")) {
            drawer.open();
            Settings.writeSetting(context, Settings.PREF_BENZINE_TYPE,
                    Benzine.BENZINE_GAS_93);
            clearButtons(buttonGas93);
        } else {
            clearButtons(null);
            selectCorrectButton();
        }

        android.view.View.OnClickListener buttonsListener = new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.writeSetting(context, Settings.PREF_BENZINE_TYPE,
                        v.getTag());
                drawer.close();
                clearButtons(v);
                refreshMap();
            }
        };

        buttonGas93.setOnClickListener(buttonsListener);
        buttonGas95.setOnClickListener(buttonsListener);
        buttonGas97.setOnClickListener(buttonsListener);
        buttonDisel.setOnClickListener(buttonsListener);
        buttonKerosene.setOnClickListener(buttonsListener);

        final ImageView handler = (ImageView) findViewById(R.id.handle_c_image);
        handler.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.setImageResource(R.drawable.bencinera_viva);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_benzine);
        mapa = fragment.getMap();
        currentLocation = null;

        if (googleMap == null) {
            googleMap = mapa;
            googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View localView = ((LayoutInflater) context
                            .getSystemService("layout_inflater")).inflate(
                            R.layout.info_marker_layout, null);
                    TextView name = (TextView) localView
                            .findViewById(R.id.benzine_marker);
                    TextView address = (TextView) localView
                            .findViewById(R.id.address_marker);
                    Benzine phar = benzineList.get(marker.getSnippet());
                    if (phar != null) {
                        name.setText(phar.getName());
                        address.setText(phar.getAddress());
                    } else {
                        return null;
                    }
                    return localView;
                }
            });
            addMarkers();
            makeSettingsDialog();
        }
    }

    private void selectCorrectButton() {
        if (Settings.getBenzineType(context).equalsIgnoreCase(
                (String) buttonGas93.getTag())) {
            buttonGas93
                    .setBackgroundResource(R.drawable.background_card_selected);
        } else if (Settings.getBenzineType(context).equalsIgnoreCase(
                (String) buttonGas95.getTag())) {
            buttonGas95
                    .setBackgroundResource(R.drawable.background_card_selected);
        } else if (Settings.getBenzineType(context).equalsIgnoreCase(
                (String) buttonGas97.getTag())) {
            buttonGas97
                    .setBackgroundResource(R.drawable.background_card_selected);
        } else if (Settings.getBenzineType(context).equalsIgnoreCase(
                (String) buttonDisel.getTag())) {
            buttonDisel
                    .setBackgroundResource(R.drawable.background_card_selected);
        } else if (Settings.getBenzineType(context).equalsIgnoreCase(
                (String) buttonKerosene.getTag())) {
            buttonKerosene
                    .setBackgroundResource(R.drawable.background_card_selected);
        }
    }

    @Override
    public void onClusterItemInfoWindowClick(Benzine phar) {
        if (phar != null) {
            Long pharId = phar.getId();
            Intent benzineDetail = new Intent(context,
                    BenzineDetailActivity.class);
            benzineDetail.putExtra("id", pharId);
            startActivity(benzineDetail);
        }
    }

    @Override
    public boolean onClusterItemClick(Benzine phar) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Benzine> cluster) {
    }

    @Override
    public boolean onClusterClick(Cluster<Benzine> cluster) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                cluster.getPosition(), googleMap.getCameraPosition().zoom + 2));
        return true;
    }

    private void makeSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                BenzineMainMapActivity.this);

        // builder.setInverseBackgroundForced(true);

        builder.setTitle(getString(R.string.map_config_title));

        LinearLayout mainLayout = new LinearLayout(BenzineMainMapActivity.this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Notification Layout
        LinearLayout notificationLayout = new LinearLayout(
                BenzineMainMapActivity.this);
        notificationLayout.setPadding(20, 2, 20, 2);

        // add text view
        TextView tv = new TextView(this);
        tv.setText(getString(R.string.map_config_notification_message));
        tv.setTextSize(16);

        notificationLayout.addView(tv);

        // add Toggle button
        final ToggleButton tb = new ToggleButton(this);
        tb.setTextOn("SI");
        tb.setTextOff("NO");
        tb.setChecked(Settings.getSoundNotificationStatus(context));
        tb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        notificationLayout.addView(tb);
        mainLayout.addView(notificationLayout);

        // add text view
        TextView infoText = new TextView(this);
        infoText.setText(getString(R.string.map_config_notification_message_information));
        infoText.setTextSize(13);
        infoText.setPadding(20, 2, 20, 2);
        mainLayout.addView(infoText);

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setImageResource(android.R.color.darker_gray);
        iv.setPadding(20, 2, 20, 2);
        mainLayout.addView(iv);

        String msg = getString(R.string.map_config_message).replaceAll("#",
                String.valueOf(Settings.getCurrentRadio(context)));
        rangeText = new TextView(this);
        rangeText.setText(msg);
        rangeText.setPadding(20, 5, 20, 5);
        rangeText.setTextSize(16);

        mainLayout.addView(rangeText);

        final SeekBar seekBar = new SeekBar(BenzineMainMapActivity.this);
        seekBar.setMax((int) AppController.MAX_RADIO_IN_METERS);
        seekBar.incrementProgressBy(1000);
        seekBar.setProgress(Settings.getCurrentRadio(context));
        mainLayout.addView(seekBar);
        builder.setView(mainLayout);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            rangeText.setTextColor(getResources()
                    .getColor(R.color.blanco_total));
            tv.setTextColor(getResources().getColor(R.color.blanco_total));
            infoText.setTextColor(getResources().getColor(R.color.blanco_total));
            iv.setImageResource(android.R.color.white);
        }

        builder.setPositiveButton(getString(R.string.btn_accept),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Settings.writeSetting(getApplicationContext(),
                                Settings.PREF_CURRENT_RADIO,
                                seekBar.getProgress());
                        Settings.writeSetting(getApplicationContext(),
                                Settings.PREF_ENABLE_SOUND_NOTIFICATION,
                                tb.isChecked());
                        sendAlarmBrodcastMessage();
                        refreshMap();
                    }
                });
        builder.setNegativeButton(getString(R.string.btn_cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        settingDialog = builder.create();
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                progressChanged = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                String message = getString(R.string.map_config_message)
                        .replaceAll("#", String.valueOf(progressChanged));
                rangeText.setText(message);
            }
        });

    }

    public void showSettingDialog() {
        settingDialog.show();
    }

    protected void addMarkers() {
        googleMap.clear();
        builder = new LatLngBounds.Builder();
        addActualLocationMarker();
        addBenzineMarkers();
    }

    protected void addActualLocationMarker() {
        LatLng hereLatLng = AppController.getActualLatLng();
        if (hereLatLng != null) {
            currentLocation = hereLatLng;
            googleMap.addMarker(new MarkerOptions().position(hereLatLng).title(
                    getString(R.string.actual_location)));
            builder.include(hereLatLng);
            googleMap.addCircle(new CircleOptions().center(hereLatLng)
                    .radius(Settings.getCurrentRadio(context))
                    .strokeColor(Color.BLACK).strokeWidth(5)
                    .fillColor(0x4000ff00));
        }
    }

    protected void addBenzineMarkers() {

        mClusterManager = new ClusterManager<Benzine>(context, mapa);
        markersList.clear();
        benzineList.clear();
        mClusterManager.setRenderer(new BenzineRenderer());
        mapa.setOnCameraChangeListener(mClusterManager);
        mapa.setOnMarkerClickListener(mClusterManager);
        mapa.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        List<Benzine> allBenzineMarkersList;
        allBenzineMarkersList = getNearestBenzineList();

        if ((allBenzineMarkersList != null && allBenzineMarkersList.size() > 0)) {

            for (Benzine benzine : allBenzineMarkersList) {
                if (benzine.hasCurrentBenzineType(context)) {
                    benzineList.put(benzine.toString(), benzine);
                }
            }
            LatLng latLong = null;
            for (Map.Entry<String, Benzine> element : benzineList.entrySet()) {
                latLong = element.getValue().getPosition();
                if (latLong != null) {
                    markersList.add(element.getValue());
                    builder.include(element.getValue().getPosition());
                }
            }

            if (markersList.size() > 0) {
                mClusterManager.addItems(markersList);
                mClusterManager.cluster();
            }

        } else {
            Toast.makeText(context, getString(R.string.no_benzine_in_range),
                    Toast.LENGTH_LONG).show();
        }

        if (fragment.getView().getViewTreeObserver().isAlive()) {
            fragment.getView().getViewTreeObserver()
                    .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (fragment.getView() != null) {
                                ViewTreeObserver treeObs = fragment.getView()
                                        .getViewTreeObserver();
                                if (treeObs != null) {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                        treeObs.removeGlobalOnLayoutListener(this);
                                    } else {
                                        treeObs.removeOnGlobalLayoutListener(this);
                                    }
                                }
                                centerMap();
                            }
                        }
                    });
        }
    }

    private void centerMap() {
        if (builder != null && markersList.size() > 0) {
            if (benzineList.size() > 1) {
                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                        padding);
                googleMap.animateCamera(cu);
            } else if (benzineList.size() == 1) {
                Benzine f = (Benzine) benzineList.values().toArray()[0];
                LatLng loc = new LatLng(f.getLatitude(), f.getLongitude());
                googleMap
                        .moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
            } else if (benzineList.size() == 0 && currentLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLocation, 11));
            }
        } else {
            if (currentLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLocation, 11));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(-36.102376, -71.117249), 5));
            }
        }
    }

    protected List<Benzine> getNearestBenzineList() {
        if (currentLocation != null) {
            return AppController.getInstace().filterNearestBenzine(
                    currentLocation, Settings.getCurrentRadio(context));
        } else {
            return null;
        }
    }

    protected List<Benzine> getTodayBenzine() {
        return AppController.getInstace().getBenzineList();
    }

    /**
     * Draws profile photos inside markers (using IconGenerator). When there are
     * multiple people in the cluster, draw multiple photos (using
     * MultiDrawable).
     */
    private class BenzineRenderer extends DefaultClusterRenderer<Benzine> {
        private final IconGenerator mIconGenerator = new IconGenerator(context);
        private final View mImageView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_marker, null);

        public BenzineRenderer() {
            super(context, mapa, mClusterManager);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Benzine benzine,
                MarkerOptions markerOptions) {
            if (benzine.getPosition() != null) {
                TextView numTxt = (TextView) mImageView
                        .findViewById(R.id.num_txt);
                numTxt.setText(benzine.getCurrencyPrice(benzine
                        .getSelectedBenzinePrice(context)));
                ImageView benzineImage = (ImageView) mImageView
                        .findViewById(R.id.benzineImage);

                benzineImage.setImageResource(Marca.findIdByName(benzine
                        .getName()));

                markerOptions
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils
                                .createDrawableFromView(
                                        BenzineMainMapActivity.this, mImageView)))
                        .snippet(benzine.toString()).title(benzine.getName())
                        .position(benzine.getPosition());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem refresh = menu.add(Menu.FIRST, MENU_ITEM_REFRESH, Menu.NONE,
                getText(R.string.ic_refresh));
        refresh.setIcon(R.drawable.ic_refresh);
        MenuItemCompat.setShowAsAction(refresh,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        MenuItem item = menu.add(Menu.FIRST, MENU_ITEM_CONFIGURATION,
                Menu.NONE, getText(R.string.ic_action_settings));
        item.setIcon(R.drawable.ic_action_settings);
        MenuItemCompat.setShowAsAction(item,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case MENU_ITEM_CONFIGURATION:
            showSettingDialog();
            break;
        case MENU_ITEM_REFRESH:
            refreshMap();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ResourceAsColor")
    private void clearButtons(View button) {
        buttonGas93.setBackgroundResource(R.drawable.background_card);
        buttonGas95.setBackgroundResource(R.drawable.background_card);
        buttonGas97.setBackgroundResource(R.drawable.background_card);
        buttonDisel.setBackgroundResource(R.drawable.background_card);
        buttonKerosene.setBackgroundResource(R.drawable.background_card);
        if (button != null) {
            button.setBackgroundResource(R.drawable.background_card_selected);
        }
    }

    public void verifyLocationStatus() {
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            createLocationDisabledAlert();
        }
    }

    private void createLocationDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // builder.setInverseBackgroundForced(true);
        builder.setTitle(getString(R.string.map_config_location_title));
        builder.setMessage(getString(R.string.map_config_location))
                .setCancelable(false)
                .setPositiveButton(R.string.btn_accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showLocationOptions();
                            }
                        });
        builder.setNegativeButton(R.string.btn_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showLocationOptions() {
        Intent locationOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(locationOptionsIntent, LOCATION_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS) {
            refreshMap();
        }
    }

    private void refreshMap() {
        addMarkers();
        centerMap();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshMap();
    }

    private void sendAlarmBrodcastMessage() {
        Intent intent = new Intent(RECEIVER_ACTION);
        sendBroadcast(intent);
    }
}
