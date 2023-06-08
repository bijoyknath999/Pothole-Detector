package com.pdetector.android.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdetector.android.R;
import com.pdetector.android.databinding.ActivityMapsBinding;
import com.pdetector.android.models.Pothole;
import com.pdetector.android.utils.GpsTracker;
import com.pdetector.android.utils.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Pothole> potholeList;
    private DatabaseReference PotholeDatabase = FirebaseDatabase.getInstance().getReference("Pothole");
    private GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        if (!isConnected)
        {
            Tools.ShowNoInternetDialog(this);
        }

        gpsTracker = new GpsTracker(MapsActivity.this);
        gpsTracker.getLocation();

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

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        PotholeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    potholeList = new ArrayList<>();

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        Pothole pothole = snapshot1.getValue(Pothole.class);
                        potholeList.add(pothole);
                    }
                    Collections.reverse(potholeList);

                    if (potholeList.size()<=0)
                    {
                        Toast.makeText(MapsActivity.this, "No Pothole Found.", Toast.LENGTH_SHORT).show();
                    }


                    for (int i = 0; i < potholeList.size(); i++) {

                        LatLng loc = new LatLng(potholeList.get(i).getLat(), potholeList.get(i).getLon());

                        mMap.addMarker(new MarkerOptions().position(loc).title(potholeList.get(i).getStatus()+" Pothole"));

                    }

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            System.out.println("LatLng: " + marker.getPosition().latitude);
                            ShowDialog5(marker.getPosition().latitude,marker.getPosition().longitude,marker);
                            return false;
                        }
                    });
                }
                else {
                    Toast.makeText(MapsActivity.this, "No Pothole Found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "No Pothole Found.", Toast.LENGTH_SHORT).show();
            }
        });



        if (gpsTracker.canGetLocation()) {
            LatLng loc = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            // below line is use to zoom our camera on map.
            // below line is use to move our camera to the specific location.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,15));

            mMap.animateCamera(CameraUpdateFactory.zoomIn());

            mMap.animateCamera(CameraUpdateFactory.zoomTo(15),1000,null);


        }


    }


    private void ShowDialog5(double lat, double lon, Marker marker){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);
        View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog5, null);
        TextView titleText = (TextView) customLayout.findViewById(R.id.custom_dialog5_title);
        titleText.setText(marker.getTitle());
        builder.setView(customLayout);
        builder
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowDeleteDialog(lat,lon,marker);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void ShowDeleteDialog(double lat, double lon, Marker marker) {
        PotholeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    potholeList = new ArrayList<>();

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        Pothole pothole = snapshot1.getValue(Pothole.class);
                        if (pothole.getLat()==lat && pothole.getLon()==lon)
                        {
                            PotholeDatabase.child(pothole.getId()).setValue(null);
                            marker.remove();
                        }
                    }
                }
                else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}