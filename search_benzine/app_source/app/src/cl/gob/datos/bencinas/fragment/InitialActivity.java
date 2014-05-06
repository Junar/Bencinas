package cl.gob.datos.bencinas.fragment;

import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.SyncController;
import cl.gob.datos.bencinas.helpers.Utils;

public class InitialActivity extends Activity {
    private static final String TAG = InitialActivity.class.getSimpleName();
    private static final String NO_ERROR = "NO_ERROR";
    private static final String CONECTIVITY_ERROR = "CONECTIVITY_ERROR";
    private static final String PARSER_ERROR = "PARSER_ERROR";
    private static final String TOAST_ERROR = "TOAST_ERROR";

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.exit).setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            System.exit(0);
                        } catch (Exception e) {
                            finish();
                        }
                        return true;
                    }
                });
        return true;
    }

    private ProgressBar progress;
    private TextView loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);
        progress = (ProgressBar) findViewById(R.id.progress);
        loading = (TextView) findViewById(R.id.loading);
        checkPlayService();
    }

    private void checkPlayService() {
        if (Utils.isGooglePlayAvailable(this)) {
            new Initialize().execute();
        } else {
            progress.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            loading.setText(getString(R.string.play_service_not_available));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.PLAY_SERVICES_DIALOG) {
            checkPlayService();
        }
    }

    private void goToSearchPharmaActivity() {
        Intent myIntent = new Intent().setClass(this,
                BenzineMainMapActivity.class);
        startActivity(myIntent);
        finish();
    }

    private class Initialize extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void... params) {
            try {
                new SyncController(getApplicationContext());
            } catch (TimeoutException e) {
                Log.e(TAG, e.getMessage());
                return CONECTIVITY_ERROR;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                if (e.getMessage().equalsIgnoreCase("Toast")) {
                    return TOAST_ERROR;
                } else {
                    return PARSER_ERROR;
                }
            }
            return NO_ERROR;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals(NO_ERROR) || result.equals(TOAST_ERROR)) {
                if (result.equals(TOAST_ERROR)) {
                    Toast.makeText(
                            getApplicationContext(),
                            getApplicationContext().getString(
                                    R.string.download_new_information_error),
                            Toast.LENGTH_LONG).show();
                }
                goToSearchPharmaActivity();
            } else {
                loading.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                if (result.equals(CONECTIVITY_ERROR)) {
                    loading.setText(getText(R.string.no_conection_error)
                            .toString());
                } else {
                    loading.setText(getText(R.string.initialization_error)
                            .toString());
                }
            }
        }
    }
}