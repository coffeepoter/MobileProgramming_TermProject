package com.cookandroid.term_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class DistanceActivity extends AppCompatActivity {

    private ListView ratingListView;
    private ArrayList<HashMap<String, String>> cafeList;
    private Button backButton;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_distance);

        ratingListView = findViewById(R.id.rating_list);
        backButton = findViewById(R.id.btn_back);
        cafeList = new ArrayList<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 현재 위치 가져오기 및 카페 데이터 요청
        getCurrentLocation();

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MapActivity로 이동
                Intent intent = new Intent(DistanceActivity.this, MapActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // 현재 위치 저장
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    // 현재 위치에서 카페 데이터 요청
                    fetchCafesFromGoogleAPI(currentLatitude, currentLongitude);
                } else {
                    Toast.makeText(DistanceActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCafesFromGoogleAPI(double latitude, double longitude) {
        String apiKey = BuildConfig.PLACES_API_KEY;

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latitude + "," + longitude +
                "&radius=1000" +
                "&type=cafe" +
                "&key=" + apiKey;

        new DistanceActivity.GetCafesTask().execute(url);
    }

    private class GetCafesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse == null) {
                Toast.makeText(DistanceActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonRoot = new JSONObject(jsonResponse);
                JSONArray results = jsonRoot.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject cafe = results.getJSONObject(i);
                    double rating = cafe.optDouble("rating", 0);
                    String name = cafe.optString("name", "Unknown");
                    String address = cafe.optString("vicinity", "No address");
                    String userRatings = cafe.optString("user_ratings_total", "0");

                    // 거리 계산 (현재 위치와 카페 위치를 이용)
                    double cafeLat = cafe.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double cafeLng = cafe.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    float[] resultsDistance = new float[1];
                    Location.distanceBetween(currentLatitude, currentLongitude, cafeLat, cafeLng, resultsDistance);
                    double distance = resultsDistance[0]; // 미터 단위

                    // 거리 정보를 소수점 2자리로 포맷팅
                    String formattedDistance = String.format("%.2f m", distance);

                    HashMap<String, String> cafeInfo = new HashMap<>();
                    cafeInfo.put("name", name);
                    cafeInfo.put("rating", "평점: " + rating);
                    cafeInfo.put("user_ratings", "사용자 평점 수: " + userRatings);
                    cafeInfo.put("address", "주소: " + address);
                    cafeInfo.put("distance", "거리: " + formattedDistance); // 거리 정보 추가

                    cafeList.add(cafeInfo);
                }

                // 거리순 정렬
                Collections.sort(cafeList, new Comparator<HashMap<String, String>>() {
                    @Override
                    public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                        double distance1 = Double.parseDouble(o1.get("distance").replace("거리: ", "").replace("m", ""));
                        double distance2 = Double.parseDouble(o2.get("distance").replace("거리: ", "").replace("m", ""));
                        return Double.compare(distance1, distance2); // 가까운 거리 순
                    }
                });

                // 리스트뷰에 데이터 설정
                SimpleAdapter adapter = new SimpleAdapter(
                        DistanceActivity.this,
                        cafeList,
                        android.R.layout.simple_list_item_2,
                        new String[]{"name", "distance"}, // 거리 정보를 보여줌
                        new int[]{android.R.id.text1, android.R.id.text2}
                );
                ratingListView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
