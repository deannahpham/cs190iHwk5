package edu.ucsb.cs190i.deannahpham.deannahphamgeofencing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// HELP FROM : https://developer.android.com/guide/topics/location/strategies.html
//https://stackoverflow.com/questions/14478179/background-service-with-location-listener-in-android
// https://examples.javacodegeeks.com/android/android-google-places-api-example/
// https://developers.google.com/places/web-service/
// https://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;

    final int LOCATION_PERMISSION_REQUEST_CODE = 1252;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    public static Double currentLat;
    public static Double currentLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        new GetPlacesTask().execute();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("LOOKHERE", "start");

//        LatLng UCSB = new LatLng(34.4140, -119.8489);
//        mMap.addMarker(new MarkerOptions().position(UCSB).title("Marker in UCSB"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(UCSB));

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOOKHERE", "permissions");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            // fall back to network if GPS is not available
            Log.d("LOOKHERE", "null");
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }
        if (loc != null) {
            Log.d("LOOKHERE", " ! null");
            addListenerLocation();
//            currentLat = loc.getLatitude();
//            currentLon = loc.getLongitude();
//            Log.d("LOOKHERE", " my lat " + currentLat + " my lon " + currentLon);
//            LatLng current = new LatLng(currentLat, currentLon);
//            mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        }
//        new GetPlacesTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addListenerLocation();
            } else {
                // if permission is not granted
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                Log.d("LOOKHERE", "no permission");
            }
        }
    }

    private void addListenerLocation() {
        Log.d("LOOKHERE", "in add listener location ");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("LOOKHERE", "in location changed");

                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                Log.d("LOOKHERE", "on location changed: current lat: " + currentLat + "current Lon: " + currentLon);
                LatLng current = new LatLng(currentLat, currentLon);
                mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                new GetPlacesTask().execute();
                addMarkers(GetPlacesTask.listPOI);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("LOOKHERE", "in status changed ");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LOOKHERE", "in prov enabled ");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("LOOKHERE", "in prov disabled ");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //   Maybe:  ActivityCompat#requestPermissions and onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListener);
    }


    public static class GetPlacesTask extends AsyncTask {
        //private OnPlacesRetrievedListener Listener;

        //public GetPostsTask(OnPlacesRetrievedListener listener) {
            //Listener = listener;
        //}


        public static List<PointsOfInterestDetails> listPOI = new ArrayList<>();

        @Override
        protected Object doInBackground(Object[] params) {
            Log.d("LOOKHERE", "do in background ");
            String location = String.valueOf(currentLat) + "," + String.valueOf(currentLon);
            String radius = String.valueOf(500);
            String key = "AIzaSyBcCsSUmfTpGVOqRajgadPVGjQ_QyYW72w";

            String url = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%s&key=%s", location, radius, key);

            try {
                URLConnection connection = new URL(url).openConnection();
                Log.d("LOOKHERE", "after openConnection ");

                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                BufferedReader r = new BufferedReader(inputStreamReader);
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    jsonString.append(line).append('\n');
                }
                jsonString.toString();

                Log.d("WHY", "jsonString: " + jsonString);


                JSONObject jsonObject = new JSONObject(String.valueOf(jsonString));
                JSONArray results = jsonObject.getJSONArray("results");


                for (int i = 0; i < results.length() - 1; i++) {
                    PointsOfInterestDetails POI = new PointsOfInterestDetails();
                    JSONObject result = (JSONObject) results.get(i);
                    //Log.d("WHY", "result: " + result.toString());

                    JSONObject geometry = result.getJSONObject("geometry");
                    JSONObject poiLocation = geometry.getJSONObject("location");
                    //Log.d("WHY", "geometry: " + geometry.toString());
                    Log.d("check", "location: " + poiLocation.toString());

                    double lat = poiLocation.getDouble("lat");
                    double lng = poiLocation.getDouble("lng");

                    //Log.d("check", "lat1: " + lat + "lng1: " + lng);

                    POI.setLatitude(lat);
                    POI.setLongitude(lng);

                    //Is this right?
                    String placeId = result.getString("place_id");
                    Log.d("check", "placeId: " + placeId.toString());

                    POI.setPlaceId(placeId);

                    String name = result.getString("name");
                    POI.setName(name);

                    listPOI.add(POI);
                }
            }
            catch (Exception e) {
                return new ArrayList<>();
            }

            return null;
        }

    }

    public static void addMarkers(List<PointsOfInterestDetails> POI) {
        Log.d("WHY", "in add markers");
        double lat;
        double lng;
        String name;

        for (PointsOfInterestDetails poi : POI){
            lat = poi.getLatitude();
            lng = poi.getLongitude();
            name = poi.getName();

            LatLng current = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(current).title(name));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        }





        //double lat = POI.getLatitude();
        //double lng = POI.getLongitude();

        //Log.d("check", "lat: " + lat + "lng: " + lng);

//        LatLng current = new LatLng(lat, lng);
//        mMap.addMarker(new MarkerOptions().position(current).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));

    }

    //    public static void GetPosts(OnPlacesRetrievedListener listener) {
//        new GetPostsTask(listener).execute();
//    }
//
//    public interface OnPlacesRetrievedListener {
//        void OnPlacesRetrieved();
//    }

//    @Override
//    protected void onPostExecute() {
//        Listener.OnPostListRetrieved();
//    }

}
