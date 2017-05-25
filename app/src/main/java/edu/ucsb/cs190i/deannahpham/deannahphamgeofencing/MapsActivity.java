package edu.ucsb.cs190i.deannahpham.deannahphamgeofencing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.android.gms.maps.model.Marker;
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
// https://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
// https://stackoverflow.com/questions/29302927/assign-a-click-listener-to-the-info-window-cannot-navigate-to-the-webpage-googl

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
        }

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MapsActivity.this, "CLICKED", Toast.LENGTH_SHORT).show();

                Log.d("tag", "tag url: " + marker.getTag().toString());

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marker.getTag().toString()));
                startActivity(browserIntent);

            }
        });

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

                Log.d("check", "jsonString: " + jsonString);


                JSONObject jsonObject = new JSONObject(String.valueOf(jsonString));
                JSONArray results = jsonObject.getJSONArray("results");


                for (int i = 0; i < results.length() - 1; i++) {
                    PointsOfInterestDetails POI = new PointsOfInterestDetails();
                    JSONObject result = (JSONObject) results.get(i);
                    //Log.d("WHY", "result: " + result.toString());

                    JSONObject geometry = result.getJSONObject("geometry");
                    JSONObject poiLocation = geometry.getJSONObject("location");
                    //Log.d("WHY", "geometry: " + geometry.toString());
                    //Log.d("check", "location: " + poiLocation.toString());

                    double lat = poiLocation.getDouble("lat");
                    double lng = poiLocation.getDouble("lng");

                    //Log.d("check", "lat1: " + lat + "lng1: " + lng);

                    POI.setLatitude(lat);
                    POI.setLongitude(lng);

                    String name = result.getString("name");
                    POI.setName(name);

                    //Is this right?
                    String placeId = result.getString("place_id");
                    Log.d("check", "placeId: " + placeId.toString());

                    POI.setPlaceId(placeId);

                    String detailsUrl = String.format("https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&key=%s", placeId, key);

                    try {
                        URLConnection detailsConnection = new URL(detailsUrl).openConnection();
                        Log.d("LOOKHERE", "after openConnection ");

                        InputStreamReader detailsInputStreamReader = new InputStreamReader(detailsConnection.getInputStream(), "UTF-8");
                        BufferedReader detailsR = new BufferedReader(detailsInputStreamReader);
                        StringBuilder details_jsonString = new StringBuilder();
                        String detailsLine;
                        while ((detailsLine = detailsR.readLine()) != null) {
                            details_jsonString.append(detailsLine).append('\n');
                        }
                        details_jsonString.toString();

                        Log.d("details", "details_jsonString: " + details_jsonString);

                        JSONObject details_jsonObject = new JSONObject(String.valueOf(details_jsonString));
                        JSONObject details_results = details_jsonObject.getJSONObject("result");
                        String details_url = details_results.getString("url");

                        Log.d("details_url", "details_url: " + details_url);

                        POI.setUrl(details_url);

                    }
                    catch (Exception e) {
                    }

                    listPOI.add(POI);
                    //Log.d("detail", "listPOI: " + listPOI.get(0).getUrl());
                    //Log.d("detail", "listPOI: " + listPOI.get(1).getUrl());

                }
            }
            catch (Exception e) {
                return new ArrayList<>();
            }

            return null;
        }

    }

    public void addMarkers(List<PointsOfInterestDetails> POI) {
        Log.d("check", "in add markers");
        double lat;
        double lng;
        String name;
        String placeId;
        String url;

        for (PointsOfInterestDetails poi : POI){
            lat = poi.getLatitude();
            lng = poi.getLongitude();
            name = poi.getName();
            url = poi.getUrl();
            //Log.d("url", "url: "+ url);

            LatLng current = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(current).title(name)).setTag(url);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));

        }

//        for(int i = 0 ; i < 1000000; i++) {
//            //wasting time so i'm not adding to my list while i'm iterating over it and causing a crash
//        }

//        final String finalUrl = url;
//        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(Marker marker) {
//                Toast.makeText(MapsActivity.this, "CLICKED", Toast.LENGTH_SHORT).show();
//
//                //marker.ge
//
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
//                startActivity(browserIntent);
//
//            }
//        });


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
