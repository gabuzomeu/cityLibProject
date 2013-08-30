package eu.ttbox.velib.service.download.strasbourg;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import eu.ttbox.velib.VeloContentProvider;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.StationHelper;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.StationDatabase;
import eu.ttbox.velib.service.database.Velo;
import eu.ttbox.velib.service.download.VeloServiceParser;

public class VelhopServiceParser implements VeloServiceParser {

    private static final String TAG = "VelhopServiceParser";

    private Context context;

    public VelhopServiceParser(Context context) {
        this.context = context;
    }

    @Override
    public ArrayList<Station> parseInputStreamForStations(InputStream in, VelibProvider provider) {
        ArrayList<Station> result = null;
        try {
            String json = convertInputStreamAsString(in, "UTF-8");
            JSONObject jObject = new JSONObject(json);
            JSONArray stations = jObject.getJSONArray("s");
            int stationSize = stations.length();
            result = new ArrayList<Station>(stationSize);
            for (int i = 0; i < stationSize; i++) {
                JSONObject src = stations.getJSONObject(i);
                Station station = new Station();
                //id : Identification de la station
                station.setNumber(src.getString("id"));
                //go.x : longitude GPS de la station
                //go.y : latitude GPS de la station
                JSONObject go = src.getJSONObject("go");
                station.setLongitude(go.getDouble("x"));
                station.setLatitude(go.getDouble("y"));
                //ic : "velhop"
                station.setProvider(provider.getProvider());
                //ln : Nom de la station/boutique
                station.setName(src.getString("ln"));
                //ad : emplacement de la station/boutique
                String addr = src.getString("ad");
                station.setAddress(addr);
                station.setFullAddress(addr);
                //ty : type ("station", "boutique", "mixte")
                // TODO src.getString("ty");
                // Add Result
                result.add(station);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in parsing Stations from " + provider + " : " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Station parseInputStreamForStationDispo(InputStream in, Station station) {
//        throw new RuntimeException("Not implemented method by unique station");
        ArrayList<ContentValues> stations = parseInputStreamForStationDispo(in, VelibProvider.FR_STRASBOURG);
        String expectedId = station.getNumber();
        ContentResolver cr = context.getContentResolver();
        for (ContentValues value : stations) {
            String number = value.getAsString(Velo.VeloColumns.COL_NUMBER);
            if (expectedId.equals(number)) {
                StationHelper.updateStationDispoWithContentValues(station, value);
            }
            VelibProvider provider = VelibProvider.FR_STRASBOURG;
            Uri entityUri = VeloContentProvider.Constants.getStationProviderUri(provider, number);
            cr.update(entityUri, value, StationDatabase.SELECT_BY_PROVIDER_NUMBER
                    , new String[]{String.valueOf(provider.getProvider()), number});
        }
        return station;
    }


    //  @Override
    public ArrayList<ContentValues> parseInputStreamForStationDispo(InputStream in, VelibProvider provider) {
        ArrayList<ContentValues> result = null;
        try {
            // Init data
            long veloUpdated = System.currentTimeMillis();
            // Parse
            String json = convertInputStreamAsString(in, "UTF-8");
            JSONObject jObject = new JSONObject(json);
            JSONArray stations = jObject.getJSONArray("s");
            int stationSize = stations.length();
            result = new ArrayList<ContentValues>(stationSize);
            for (int i = 0; i < stationSize; i++) {
                JSONObject src = stations.getJSONObject(i);
                ContentValues value = new ContentValues();
                value.put(Velo.VeloColumns.COL_STATION_UPDATE_TIME, veloUpdated);
                //id : Identification de la station
                value.put(Velo.VeloColumns.COL_NUMBER, src.getInt("id"));
                value.put(Velo.VeloColumns.COL_PROVIDER, provider.getProvider());
                //st : Nombre d'emplacements
                value.put(Velo.VeloColumns.COL_STATION_TOTAL, src.getInt("st"));
                //sa : Nombre de Vélhop disponibles
                value.put(Velo.VeloColumns.COL_STATION_CYCLE, src.getInt("sa"));
                //sf : Nombre d'emplacement disponibles
                value.put(Velo.VeloColumns.COL_STATION_PARKING, src.getInt("sf"));
                //sc : Prise en charge carte bancaire
                // TODO sc

                // Register Result
                result.add(value);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in parsing Stations from " + provider + " : " + e.getMessage(), e);
        }
        return result;
    }


    private String convertInputStreamAsString(InputStream in, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
        StringWriter sw = new StringWriter(1024);
        char[] bufText = new char[1024];
        int charRead = 0;
        while ((charRead = reader.read(bufText)) != -1) {
            sw.write(bufText, 0, charRead);
        }
        return sw.toString();
    }

}
