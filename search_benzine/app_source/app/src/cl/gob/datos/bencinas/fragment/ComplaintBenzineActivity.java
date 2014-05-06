package cl.gob.datos.bencinas.fragment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cl.gob.datos.bencinas.R;
import cl.gob.datos.bencinas.controller.AppController;
import cl.gob.datos.bencinas.helpers.Utils;

import com.junar.api.JunarAPI;
import com.junar.searchbenzine.Benzine;

public class ComplaintBenzineActivity extends ActionBarActivity {

    private Benzine benzine;
    private static final int TAKE_PHOTO = 1999;
    private static final int PHOTO_WIDTH = 1024;
    private static final String TAG = ComplaintBenzineActivity.class
            .getSimpleName();

    private EditText name;
    private EditText text;
    private Spinner reasonSpinner;
    private static File mediaFile;
    private TextView benzineName;
    private EditText mail;
    private Button btnPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_complaint);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        long pharId = getIntent().getExtras().getLong("id");

        benzine = AppController.getInstace().getBenzineById(pharId);

        name = (EditText) findViewById(R.id.benzine_complaint_name);
        mail = (EditText) findViewById(R.id.benzine_complaint_email);
        reasonSpinner = (Spinner) findViewById(R.id.benzine_complaint_reason);
        text = (EditText) findViewById(R.id.benzine_complaint_text);

        TextView address = (TextView) findViewById(R.id.benzine_station_address);
        address.setText(benzine.getAddress());

        benzineName = (TextView) findViewById(R.id.benzine_name);
        benzineName.setText(benzine.getName());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.reasons_array, R.layout.spinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reasonSpinner.setAdapter(adapter);
        btnPhoto = (Button) findViewById(R.id.take_photo);
        btnPhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                mediaFile = getOutputMediaFile();

                if (mediaFile != null) {
                    Intent camaraIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(mediaFile));
                    startActivityForResult(camaraIntent, TAKE_PHOTO);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.benzine_complaint_camera_error),
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_send_mail:
            validateFields();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.benzine_complain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO && resultCode != RESULT_OK
                && mediaFile != null && mediaFile.exists()) {
            mediaFile.delete();
            mediaFile = null;
        } else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK
                && mediaFile != null && mediaFile.exists()) {
            btnPhoto.setText(getString(R.string.benzine_complaint_view_photo));
            btnPhoto.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(mediaFile),
                                "image/*");
                        startActivity(intent);
                    } catch (Exception e) {

                    }
                }
            });
        }
    }

    private void validateFields() {
        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError(getString(R.string.benzine_complaint_name_empty)
                    .toString());
            name.requestFocus();
        } else if (TextUtils.isEmpty(mail.getText().toString())) {
            name.setError(null);
            mail.setError(getString(R.string.benzine_complaint_email_empty)
                    .toString());
            mail.requestFocus();
        } else if (reasonSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    getApplicationContext().getString(
                            R.string.benzine_complaint_select_reason),
                    Toast.LENGTH_LONG).show();

        } else if (TextUtils.isEmpty(text.getText().toString())) {
            name.setError(null);
            mail.setError(null);
            text.setError(getString(R.string.benzine_complaint_msg_empty)
                    .toString());
            text.requestFocus();
        } else {
            text.setError(null);
            name.setError(null);
            mail.setError(null);
            sendReport();
        }
    }

    private void sendReport() {
        if (mediaFile != null && mediaFile.exists()) {
            if (!Utils.resizeImageByWidth(mediaFile.getAbsolutePath(),
                    PHOTO_WIDTH)) {
                mediaFile = null;
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.benzine_complaint_image_not_attached),
                        Toast.LENGTH_LONG).show();
            }
        }
        new DoSendComplaint().execute();
    }

    private String getStringFromInputStream(InputStream is) {
        String txt = "";
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                txt += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txt;
    }

    private class DoSendComplaint extends AsyncTask<Void, Void, Boolean> {

        private ProgressDialog progress;
        private HttpPost postImage = new HttpPost(
                "https://api.parse.com/1/files/pic.jpg");
        private HttpPost post = new HttpPost(
                "https://api.parse.com/1/classes/Denuncia");
        private boolean canceled = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postImage
                    .addHeader("X-Parse-Application-Id", JunarAPI.PARSE_APP_ID);
            postImage
                    .addHeader("X-Parse-REST-API-Key", JunarAPI.PARSE_REST_API);
            postImage.setHeader("Content-type", "image/jpeg");

            post.addHeader("X-Parse-Application-Id", JunarAPI.PARSE_APP_ID);
            post.addHeader("X-Parse-REST-API-Key", JunarAPI.PARSE_REST_API);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            progress = new ProgressDialog(ComplaintBenzineActivity.this);
            progress.setMessage(getString(R.string.benzine_complaint_sending));
            progress.setTitle(getString(R.string.benzine_complaint_wait));
            progress.setCancelable(false);
            progress.setButton(ProgressDialog.BUTTON_NEGATIVE,
                    getString(R.string.benzine_complaint_sent_cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                canceled = true;
                                postImage.abort();
                                post.abort();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            JSONObject obj = new JSONObject();
            JSONObject photo = new JSONObject();
            try {
                HttpResponse httpResponse = null;
                if (mediaFile != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(mediaFile
                            .getPath());
                    ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                            imageStream);

                    byte[] content = imageStream.toByteArray();
                    postImage.setEntity(new ByteArrayEntity(content));
                    httpResponse = client.execute(postImage);
                }
                if ((httpResponse != null && httpResponse.getStatusLine()
                        .getStatusCode() == HttpStatus.SC_CREATED)
                        || httpResponse == null) {

                    obj.put("nombre", benzineName.getText().toString());
                    // obj.put("comuna",
                    // AppController.getInstace().getLocalDao()
                    // .getCommuneById(benzine.getCommune()).toString());
                    obj.put("direccion", benzine.getAddress());
                    obj.put("observaciones", text.getText().toString());
                    if (httpResponse != null) {
                        JSONObject imageResponse = new JSONObject(
                                getStringFromInputStream(httpResponse
                                        .getEntity().getContent()));
                        photo.put("__type", "File");
                        photo.put("name", imageResponse.getString("name"));
                        obj.put("foto", photo);
                    }
                    obj.put("nombreDenunciante", name.getText().toString());
                    obj.put("emailDenunciante", mail.getText().toString());
                    obj.put("motivo", reasonSpinner.getSelectedItem()
                            .toString());

                    Location loc = AppController.getLastLocation();
                    if (loc != null) {
                        obj.put("latitud", loc.getLatitude());
                        obj.put("longitud", loc.getLongitude());
                    }

                    StringEntity se = new StringEntity(obj.toString(), "UTF-8");
                    post.setEntity(se);
                    httpResponse = client.execute(post);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            if (result) {
                if (mediaFile != null && mediaFile.exists()) {
                    mediaFile.delete();
                    mediaFile = null;
                }
                ComplaintBenzineActivity.this.finish();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.benzine_complaint_sent_report_ok),
                        Toast.LENGTH_LONG).show();
            } else {
                String message = getString(R.string.benzine_complaint_sent_report_problem);
                if (canceled) {
                    message = getString(R.string.benzine_complaint_sent_report_canceled);
                }
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private File getOutputMediaFile() {
        File dir = getApplicationContext().getExternalFilesDir(null);
        if (!dir.exists())
            dir.mkdirs();

        File mediaFileTmp = null;
        try {
            mediaFileTmp = File.createTempFile("Report_", ".jpg", dir);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return mediaFileTmp;
    }
}
