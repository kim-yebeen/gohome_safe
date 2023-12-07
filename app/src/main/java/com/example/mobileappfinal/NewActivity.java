package com.example.mobileappfinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.concurrent.TimeUnit;

public class NewActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng currentLocation;
    private LatLng destination;
    private Button startButton;
    private Button destinationButton;
    private Button directionsButton;
    private Button timerButton;

    private GeoApiContext geoApiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        //출발지 버튼
        startButton = findViewById(R.id.startButton);
        //목적지 버튼
        destinationButton = findViewById(R.id.destinationButton);
        //경로표시버튼
        directionsButton = findViewById(R.id.directionsButton);
        //타이머버튼
        timerButton = findViewById(R.id.movetoTimer);

        //clicklistener 설정
        startButton.setOnClickListener(this);
        destinationButton.setOnClickListener(this);
        directionsButton.setOnClickListener(this);
        timerButton.setOnClickListener(this);

        //map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyC_9YGNRXw4prw3-hFpVssB1cVLdm5AcXA")
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 위치 권한 요청
        requestLocationPermission();

        // 현재 위치 가져오기
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                setStartLocation();
                break;
            case R.id.destinationButton:
                setDestination();
                break;
            case R.id.directionsButton:
                calculateDirections();
                break;
            case R.id.movetoTimer:
                moveToTimer();
                break;
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //출발지 설정 버튼 클릭 시
    private void setStartLocation() {
        if (currentLocation != null) {
            showCurrentLocationMarker();
        } else {
            Toast.makeText(this, "현재 위치를 가져오는 것에 실패했습니다. 지도에서 출발지를 설정해주세요.", Toast.LENGTH_SHORT).show();

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    googleMap.clear();
                    currentLocation = latLng;
                    showCurrentLocationMarker();
                }
            });
        }
    }

    //현재 위치 보여주는 marker
    private void showCurrentLocationMarker() {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Start"));
    }

    //목적지 설정 버튼 클릭시
    private void setDestination() {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                destination = latLng;
                googleMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            }
        });
    }

    //경로 표시 버튼 클릭 시
    private void calculateDirections() {
        if (currentLocation == null) {
            Toast.makeText(this, "출발지를 설정해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination == null) {
            Toast.makeText(this, "목적지를 설정해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        DirectionsApiRequest directionsRequest = new DirectionsApiRequest(geoApiContext);
        directionsRequest.origin(new com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude));
        directionsRequest.destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));
        directionsRequest.mode(TravelMode.WALKING);

        directionsRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    PolylineOptions polylineOptions = new PolylineOptions();

                    for (com.google.maps.model.LatLng latLng : route.overviewPolyline.decodePath()) {
                        polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            googleMap.addPolyline(polylineOptions);

                            long durationInSeconds = route.legs[0].duration.inSeconds;
                            int hours = (int) (durationInSeconds / 3600);
                            int minutes = (int) ((durationInSeconds % 3600) / 60);

                            String durationString = String.format("%02d:%02d", hours, minutes);
                            Toast.makeText(NewActivity.this, "예상 소요 시간: " + durationString, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewActivity.this, "경로 계산에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //타이머 이동 버튼 클릭시
    private void moveToTimer() {
        if (currentLocation == null) {
            Toast.makeText(this, "출발지를 설정해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination == null) {
            Toast.makeText(this, "목적지를 설정해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        DirectionsApiRequest directionsRequest = new DirectionsApiRequest(geoApiContext);
        directionsRequest.origin(new com.google.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude));
        directionsRequest.destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));
        directionsRequest.mode(TravelMode.DRIVING);

        directionsRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    PolylineOptions polylineOptions = new PolylineOptions();

                    for (com.google.maps.model.LatLng latLng : route.overviewPolyline.decodePath()) {
                        polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            googleMap.addPolyline(polylineOptions);

                            long durationInSeconds = route.legs[0].duration.inSeconds;
                            int hours = (int) (durationInSeconds / 3600);
                            int minutes = (int) ((durationInSeconds % 3600) / 60);

                            String durationString = String.format("%02d:%02d", hours, minutes);
                            Toast.makeText(NewActivity.this, "예상 소요 시간: " + durationString, Toast.LENGTH_LONG).show();

                            // TimerActivity로 예상 시간 전달
                            Intent intent = new Intent(NewActivity.this, Timer.class);
                            intent.putExtra("duration", durationString);
                            startActivity(intent);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Throwable e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewActivity.this, "경로 계산에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}