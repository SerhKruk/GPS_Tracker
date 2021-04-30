package com.example.atry;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocListenerInterface{
    private final String EMPTY_TIME = "00 : 00 : 00";
    private final String EMPTY_DISTANCE = "0.0 m";
    private final String EMPTY_SPEED = "0.0 m/s";
    private final float zoomLevel = 19.0f;
    private final int DEFAULT_DISTANCE = 5000;

    private ArrayList<Location> locations = new ArrayList<Location>();
    private double distance, chosen_distance;
    private GoogleMap mMap;
    private Button startButton, stopButton;
    private boolean vibrShowed = false;

    private TimerHelper timerHelper;
    private VibrationHelper vibratorHelper;


    private TextView timerText, progressText;

    //
    private TextView tvDistance, tvVelocity;
    private LocationManager locationManager;
    private MyLocListener myLocListener;
    private Location lastLocation;
    private ProgressBar pb;
    private Marker lastMarker;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initViews();
        initServices();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission() && checkGps()) {
                    startTapped(v);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 15, myLocListener);
                    Toast toast = Toast.makeText(getApplicationContext(), "Waiting for GPS signal", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    if(!checkPermission())
                        askPermissions();
                    if(!checkGps())
                        askGps();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.removeUpdates(myLocListener);
                stopTapped(v);
            }
        });

        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(chosen_distance > 0))
                    showDialog();
            }
        });
    }

    public void initViews()
    {
        chosen_distance = 0;
        tvDistance = findViewById(R.id.idDistanceKm);
        tvVelocity = findViewById(R.id.idSpeed);
        startButton = findViewById(R.id.startButton);

        stopButton = findViewById(R.id.pauseButton);
        timerText = (TextView) findViewById(R.id.idTimer);
        pb = findViewById(R.id.progressBar);

        pb.setMax(DEFAULT_DISTANCE);
        pb.setProgress(0);

        stopButton.setEnabled(false);
    }

    public void initServices()
    {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        myLocListener = new MyLocListener();
        myLocListener.setLocListenerInterface(this);

        timerHelper = new TimerHelper(MapsActivity.this);
        vibratorHelper = new VibrationHelper(MapsActivity.this);
    }

    public void startTapped(View view)
    {
        clearMap();
        stopButton.setEnabled(true);
        startButton.setEnabled(false);
    }


    public void stopTapped(View view)
    {
        timerHelper.stopTimer();

        lastLocation = null;
        if(lastMarker!=null) {
            lastMarker.setIcon(BitmapFromVector(getApplicationContext(), R.drawable.ic_flag));
            lastMarker.setTitle("Finish");
            lastMarker = null;
        }

        stopButton.setEnabled(false);
        startButton.setText("Start");
        distance = 0;
        pb.setProgress(0);
        timerText.setText(EMPTY_TIME);
        tvVelocity.setText(EMPTY_SPEED);
        tvDistance.setText(EMPTY_DISTANCE);

        locations.clear();

        startButton.setEnabled(true);
        vibrShowed = false;
    }

    private void showDialog()
    {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setView(cl);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                androidx.appcompat.app.AlertDialog ad = (androidx.appcompat.app.AlertDialog) dialog;
                EditText ed = ad.findViewById(R.id.edText);

                if(!ed.getText().toString().equals("")) {
                    try
                    {
                        Integer dist =  Integer.parseInt(ed.getText().toString());
                        pb.setMax(dist);
                    }
                    catch (Exception e)
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), "Wrong distance", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
                        toastMessage.setTextColor(Color.RED);
                        toast.show();
                    }
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //
            }
        });
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void clearMap()
    {
        mMap.clear();
    }

    public void paintMarker(Location loc, boolean start)
    {
        LatLng newMarker = new LatLng(loc.getLatitude(), loc.getLongitude());
        if(start) {
            lastMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title("Start").icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_marker)));
        }
        else
            lastMarker = mMap.addMarker(new MarkerOptions().position(newMarker).title(String.valueOf(locations.size())).icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_location_pin)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, zoomLevel));
    }

    public void printLine(Location loc)
    {
        LatLng marker = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), marker)
                    .width(10).color(Color.BLACK));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, zoomLevel));
    }

    @Override
    public void OnLocationChanged(Location loc) {
        if(!timerHelper.timerStarted)
        {
            timerHelper.startTimer();
            vibratorHelper.vibrate();
        }
        updateDistance(loc);
    }

    private void updateDistance(Location loc)
    {
        if(lastLocation==null) // the location is first
        {
            paintMarker(loc, true);
            lastLocation = loc;
            locations.add(loc);
        }
        else if(loc.hasSpeed()) // object moves
        {
            distance += lastLocation.distanceTo(loc);
            pb.setProgress((int)distance);
            checkProgress();

            paintMarker(loc, false);
            locations.add(loc);

            printLine(loc);
            lastLocation = loc;
        }

        tvDistance.setText(String.valueOf(Math.round(distance * 100.0) / 100.0) + " m");
        tvVelocity.setText(String.valueOf(Math.round(Math.round(loc.getSpeed() * 100.0) / 100.0) + " m/s"));
    }

    private void checkProgress()
    {
        if(pb.getProgress() == pb.getMax() && !vibrShowed)
        {
            vibrShowed = true;
            vibratorHelper.vibrate();
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==100 && grantResults[0] == RESULT_OK)
        {
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean checkPermission()
    {
        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    public void askPermissions()
    {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    private boolean checkGps()
    {
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e)
        {
            return false;
        }
        return gps_enabled;
    }

    private void askGps()
    {
        new AlertDialog.Builder(MapsActivity. this)
                .setMessage( "Please turn on your gps" )
                .setPositiveButton( "GPS Settings" , new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) ;
                            }
                        })
                .setNegativeButton( "Cancel" , null )
                .show() ;
    }
}
