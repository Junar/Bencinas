package cl.gob.datos.bencinas.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.AppController;
import cl.gob.datos.bencinas.helpers.Utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.junar.searchbenzine.Benzine;
import com.junar.searchbenzine.Benzine.Marca;

public class BenzineDetailActivity extends ActionBarActivity {

    private Benzine benzine;
    private boolean openFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_benzine_detail);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        openBenzineDetail();
    }

    private void openBenzineDetail() {

        long benId = getIntent().getExtras().getLong("id");

        benzine = AppController.getInstace().getBenzineById(benId);

        TextView schedule = (TextView) findViewById(R.id.benzine_station_schedule);
        schedule.setText(benzine.getSchedule());

        TextView priceGasoline93 = (TextView) findViewById(R.id.benzine_gas93_price);
        if (benzine.getGasolina93() != null) {
            priceGasoline93.setText(benzine.getCurrencyPrice(benzine
                    .getGasolina93()));
        } else {
            priceGasoline93.setText(getString(R.string.detail_not_available));
        }

        TextView priceGasoline95 = (TextView) findViewById(R.id.benzine_gas95_price);
        if (benzine.getGasolina95() != null) {
            priceGasoline95.setText(benzine.getCurrencyPrice(benzine
                    .getGasolina95()));
        } else {
            priceGasoline95.setText(getString(R.string.detail_not_available));
        }

        TextView priceGasoline97 = (TextView) findViewById(R.id.benzine_gas97_price);
        if (benzine.getGasolina97() != null) {
            priceGasoline97.setText(benzine.getCurrencyPrice(benzine
                    .getGasolina97()));
        } else {
            priceGasoline97.setText(getString(R.string.detail_not_available));
        }

        TextView priceDisel = (TextView) findViewById(R.id.disel_price);
        if (benzine.getDiesel() != null) {
            priceDisel.setText(benzine.getCurrencyPrice(benzine.getDiesel()));
        } else {
            priceDisel.setText(getString(R.string.detail_not_available));
        }

        TextView priceKerosene = (TextView) findViewById(R.id.kerosene_price);
        if (benzine.getKerosene() != null) {
            priceKerosene.setText(benzine.getCurrencyPrice(benzine
                    .getKerosene()));
        } else {
            priceKerosene.setText(getString(R.string.detail_not_available));
        }

        TextView address = (TextView) findViewById(R.id.benzine_station_address);
        address.setText(benzine.getAddress());

        TextView benzineName = (TextView) findViewById(R.id.benzine_name);
        benzineName.setText(benzine.getName());

        Button btnRoute = (Button) findViewById(R.id.btn_benzine_rout);
        btnRoute.setEnabled(benzine.getLatitude() != 0
                && benzine.getLongitude() != 0);
        btnRoute.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = "http://maps.google.com/maps?daddr="
                        + benzine.getLatitude() + "," + benzine.getLongitude();
                BenzineDetailActivity.this.startActivity(new Intent(
                        "android.intent.action.VIEW", Uri.parse(str)));
            }
        });

        Button btnReport = (Button) findViewById(R.id.btn_benzine_report);
        btnReport.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent benzineComplain = new Intent(getApplicationContext(),
                        ComplaintBenzineActivity.class);
                benzineComplain.putExtra("id", benzine.getId());
                startActivity(benzineComplain);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm
                .findFragmentById(R.id.map);
        GoogleMap mapa = fragment.getMap();

        final View mImageView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_marker, null);

        TextView numTxt = (TextView) mImageView.findViewById(R.id.num_txt);
        numTxt.setText(benzine.getDistanceStringTo(AppController
                .getActualLatLng()));

        ImageView benzineImage = (ImageView) mImageView
                .findViewById(R.id.benzineImage);
        benzineImage.setImageResource(Marca.findIdByName(benzine.getName()));
        benzineName.setCompoundDrawablesWithIntrinsicBounds(
                Marca.findIdByName(benzine.getName()), 0, 0, 0);

        MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(
                benzine.getLatitude(), benzine.getLongitude()));
        markerOpt
                .icon(BitmapDescriptorFactory.fromBitmap(Utils
                        .createDrawableFromView(BenzineDetailActivity.this,
                                mImageView)));

        Marker marker = mapa.addMarker(markerOpt);
        marker.showInfoWindow();
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),
                16));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case R.id.action_route:
            String str = "http://maps.google.com/maps?daddr="
                    + benzine.getLatitude() + "," + benzine.getLongitude();
            BenzineDetailActivity.this.startActivity(new Intent(
                    "android.intent.action.VIEW", Uri.parse(str)));
            return true;

        case android.R.id.home:
            finish();
            if (openFromNotification) {
                Intent mainActivity = new Intent(this,
                        BenzineMainMapActivity.class);
                startActivity(mainActivity);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (openFromNotification
                && !Utils.isActivityRunning(BenzineMainMapActivity.class,
                        getApplicationContext())) {
            Intent mainActivity = new Intent(this, BenzineMainMapActivity.class);
            startActivity(mainActivity);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.benzine_detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat
                .getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(Utils.createShareIntent(
                getApplicationContext(), benzine));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openFromNotification = getIntent().getExtras().containsKey(
                "openFromNotification");
        if (openFromNotification) {
            openBenzineDetail();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }
}
