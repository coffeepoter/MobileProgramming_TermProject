package com.cookandroid.term_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cookandroid.term_project.utils.PermissionUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;
    private boolean permissionDenied = false;
    private boolean mLocationPermissionDenied = false;

    private FusedLocationProviderClient fusedLocationClient;


    private GoogleMap map;
    private UiSettings Uisettings;
    private CheckBox mMyLocationButtonCheckbox;
    private CheckBox mMyLocationLayerCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);

        mMyLocationButtonCheckbox = (CheckBox) findViewById(R.id.mylocationbutton_toggle);
        mMyLocationLayerCheckbox = (CheckBox) findViewById(R.id.mylocationlayer_toggle);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        Uisettings = map.getUiSettings();

        // 현위치 표시
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        // UI 표시
        Uisettings.setZoomControlsEnabled(isChecked(R.id.zoom_buttons_toggle));
        Uisettings.setCompassEnabled(isChecked(R.id.compass_toggle));
        Uisettings.setMyLocationButtonEnabled(isChecked(R.id.mylocationbutton_toggle));
        Uisettings.setScrollGesturesEnabled(isChecked(R.id.scroll_toggle));
        Uisettings.setZoomGesturesEnabled(isChecked(R.id.zoom_gestures_toggle));
        Uisettings.setTiltGesturesEnabled(isChecked(R.id.tilt_toggle));
        Uisettings.setRotateGesturesEnabled(isChecked(R.id.rotate_toggle));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(isChecked(R.id.mylocationlayer_toggle));

        enableMyLocation();
    }

    // UI 표시
    private boolean checkReady() {
        if (map == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setCompassEnabled(((CheckBox) v).isChecked());
    }

    public void setMyLocationButtonEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Uisettings.setMyLocationButtonEnabled(mMyLocationButtonCheckbox.isChecked());
        } else {
            mMyLocationButtonCheckbox.setChecked(false);
            requestLocationPermission(MY_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(mMyLocationLayerCheckbox.isChecked());
        } else {
            mMyLocationLayerCheckbox.setChecked(false);
            PermissionUtils.requestPermission(this, LOCATION_LAYER_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }

    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        Uisettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void requestLocationPermission(int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionUtils.RationaleDialog
                    .newInstance(requestCode, false).show(
                            getSupportFragmentManager(), "dialog");
        } else {
            PermissionUtils.requestPermission(this, requestCode,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }


    // 현위치 표시 관련
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        // 현재 위치를 가져와서 findNearbyCafes 호출
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // 현재 위치가 null이 아닐 경우 findNearbyCafes 호출
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // 줌 레벨 15
                            findNearbyCafes(location);
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return true;
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableMyLocation();
        } else {
            Toast.makeText(this, "Error!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    // Place API
    private void findNearbyCafes(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String apiKey = BuildConfig.PLACES_API_KEY;

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latitude + "," + longitude +
                "&radius=1000" +
                "&type=cafe" +
                "&key=" + apiKey;

        new GetNearbyCafesTask().execute(url);
    }

    private class GetNearbyCafesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // 응답 코드 확인
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                } else {
                    Log.e("API_ERROR", "Response Code: " + responseCode);
                    return null; // 오류 발생 시 null 반환
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            Log.d("API_RESPONSE", result.toString());
            return result.toString();
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            Log.d("JSON_RESPONSE", jsonResponse);

            // JSON 파싱 및 카페 마커 추가
            try {
                JSONObject jsonRoot = new JSONObject(jsonResponse);
                JSONArray results = jsonRoot.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject cafe = results.getJSONObject(i);
                    double lat = cafe.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = cafe.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    double rating = cafe.optDouble("rating", 0);

                    if (rating >= 4.0) {
                        LatLng cafeLocation = new LatLng(lat, lng);
                        map.addMarker(new MarkerOptions().position(cafeLocation).title(cafe.getString("name")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}

      