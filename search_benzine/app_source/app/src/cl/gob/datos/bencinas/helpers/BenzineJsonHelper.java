package cl.gob.datos.bencinas.helpers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.junar.api.JunarAPI;
import com.junar.searchbenzine.Benzine;

public class BenzineJsonHelper {
    private static final String TAG = BenzineJsonHelper.class.getSimpleName();
    public static final String DATA_GUID = "PRECI-DE-BENCI-EN-10673";

    public void getDatastreamInfo(String guid) {
        JunarAPI junar = new JunarAPI();
        junar.info(guid);
    }

    public String invokeDatastream(String guid) {
        JunarAPI junar = new JunarAPI();
        return junar.invoke(guid, null, null, -1, -1, -1);
    }

    public String invokeDatastream(String guid, String[] arguments,
            String[] filters, int limit, int page, long timestamp) {
        JunarAPI junar = new JunarAPI();
        return junar.invoke(guid, arguments, filters, limit, page, timestamp);
    }

    public List<Benzine> parseJsonArrayResponse(String response)
            throws JSONException {

        List<Benzine> bencineList = new ArrayList<Benzine>();
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray resultArray = jsonResponse.getJSONArray("result");
        for (int i = 1; i < resultArray.length(); i++) {
            JSONArray columnArray = resultArray.getJSONArray(i);
            Benzine bencine = getBencineFromJson(columnArray);
            bencineList.add(bencine);
        }
        return bencineList;
    }

    public Benzine getBencineFromJson(JSONArray json) {
        Benzine bencine = new Benzine();

        // "result":[
        // ["nombre","lat","long","gasolina_93","gasolina_95","gasolina_97","kerosene",
        // "diesel","autoservicio","dirección","horario"]

        try {
            bencine.setName(json.getString(0));
            try {
                bencine.setLatitude(json.getDouble(1));
                bencine.setLongitude(json.getDouble(2));
            } catch (JSONException e) {
                bencine.setLatitude(0.0d);
                bencine.setLongitude(0.0d);
            }

            if (!json.getString(3).equals("")) {
                bencine.setGasolina93(Double.parseDouble(json.getString(3)));
            }
            if (!json.getString(4).equals("")) {
                bencine.setGasolina95(Double.parseDouble(json.getString(4)));
            }
            if (!json.getString(5).equals("")) {
                bencine.setGasolina97(Double.parseDouble(json.getString(5)));
            }
            if (!json.getString(6).equals("")) {
                bencine.setKerosene(Double.parseDouble(json.getString(6)));
            }
            if (!json.getString(7).equals("")) {
                bencine.setDiesel(Double.parseDouble(json.getString(7)));
            }
            bencine.setAddress(json.getString(9));
            bencine.setSchedule(json.getString(10));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return bencine;
    }
}