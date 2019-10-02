package com.setblue.googlemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int REQ_PERMISSION = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    PolylineOptions lineOptions = new PolylineOptions();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setMyLocation();
        setMarker(21.180849,72.818977,"I want to be Here");
        LatLng endLatLng= new LatLng(21.180849,72.818977);

        points.add(endLatLng);
        points.add(endLatLng);
    }



    private void setMyLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            setMarker(location.getLatitude(),location.getLongitude(),"I m Here");
                            LatLng curLatLng= new LatLng(location.getLatitude(),location.getLongitude());
                            points.set(1,curLatLng);
                            lineOptions.addAll(points);
                            lineOptions.width(12);
                            lineOptions.color(Color.BLUE);
                            if(points.size()!=0)mMap.addPolyline(lineOptions);//to avoid crash

                        }
                    }
                });


    }

    private void setMarker(double latitude, double longitude, String mytitle) {
        LatLng myloc= new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions()
                .position(myloc)
                .title(mytitle));



        CameraPosition cameraPosition= new CameraPosition.Builder()
                .target(myloc)
                .zoom(15)
                .bearing(90)
                .tilt(30)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQ_PERMISSION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                setMyLocation();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng endLatLng= new LatLng(21.180849,72.818977);
        //Place current location marker
        LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        points.set(1,curLatLng);
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.BLUE);
        if(points.size()!=0)mMap.addPolyline(lineOptions);//to avoid crash

        double abc = CalculationByDistance(curLatLng,endLatLng);
      //  Toast.makeText(this, abc+"", Toast.LENGTH_SHORT).show();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(curLatLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        Toast.makeText(this, " Result Radius*c :   " + abc, Toast.LENGTH_SHORT).show();
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        //setMyLocation();


        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }


    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Toast.makeText(this, " Meter   " + meterInDec, Toast.LENGTH_SHORT).show();
//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                + " Meter   " + meterInDec);

        return Radius * c;
    }


}
