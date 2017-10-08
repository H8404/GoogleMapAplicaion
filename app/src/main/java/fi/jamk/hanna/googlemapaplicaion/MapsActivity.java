package fi.jamk.hanna.googlemapaplicaion;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray otto;
    private List<Otto> ottoMachines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        FetchDataTask task = new FetchDataTask();
        task.execute("http://student.labranet.jamk.fi/~H8404/JSON/otto.json");
        mMap = googleMap;
        LatLng jkl = new LatLng(62.24147, 25.72088);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jkl,10));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                Context context = getApplicationContext();
                CharSequence text = "Otto automaatti";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
        });
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            StringBuffer text = new StringBuffer("");
            try {
                // store highscores
               otto = json.getJSONArray("otto");
                //JsonElement ele = new JsonParser().parse(json);
                for (int i=0;i < otto.length();i++) {
                    JSONObject hs = otto.getJSONObject(i);
                    //text.append(hs.getString("adress")+":"+hs.getString("lat")+"\n");
                    Otto ottoMachine = new Otto(hs.getString("adress"),hs.getDouble("lat"),hs.getDouble("lon"));
                    ottoMachines.add(ottoMachine);
                }
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }finally {
                if (!ottoMachines.isEmpty()) {
                    for (Otto o : ottoMachines) {
                        LatLng position = new LatLng(o.getLat(), o.getLon());
                        mMap.addMarker(new MarkerOptions().position(position).title(o.getAddress()));
                    }
                }
            }
        }
    }

    private class Otto{
        private String address;
        private double lat;
        private double lon;

        public Otto(){

        }

        public Otto(String a, double lat, double lon){
            this.address = a;
            this.lat = lat;
            this.lon = lon;
        }

        public String getAddress(){
            return address;
        }

        public double getLat(){
            return lat;
        }

        public double getLon(){
            return lon;
        }
    }

}

