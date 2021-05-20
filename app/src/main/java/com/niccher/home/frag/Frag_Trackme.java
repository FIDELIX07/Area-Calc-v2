package com.niccher.home.frag;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.niccher.home.R;
import com.niccher.home.Utils.CalcDistance;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Frag_Trackme extends Fragment implements OnMapReadyCallback {

    String locFINE = Manifest.permission.ACCESS_FINE_LOCATION;
    String locCOS = Manifest.permission.ACCESS_COARSE_LOCATION;

    MapView mMapView;

    Boolean permAssign = false;

    int count = 0, area_count = 0;
    double length_ = 0;
    LatLng tapped;
    PolylineOptions polylineOptions;

    double prev,curent;

    ArrayList<LatLng> locList = new ArrayList<LatLng>();
    ArrayList<LatLng> loc_area = new ArrayList<LatLng>();
    Boolean state = false, area = false, length = false, clean = false ;

    final int reqcod = 145;
    GoogleMap gMaps;

    float zoomdef = 22f;

    FusedLocationProviderClient floc;

    FirebaseAuth mAuth;
    FirebaseUser userf;
    DatabaseReference dref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View solv= inflater.inflate(R.layout.frag_trackme, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Track Me");

        mAuth = FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();
        //fdbas= FirebaseDatabase.getInstance();
        dref = FirebaseDatabase.getInstance().getReference("Area_Calc_Saved");

        CheckPermissions();


        mMapView = (MapView) solv.findViewById(R.id.mapView);

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this::onMapReady);

        length = true;

        Toast.makeText(getActivity(), "For every 5 seconds, the app will be tracking you", Toast.LENGTH_SHORT).show();

        rePlay();

        return solv;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void LocateMe() {
        floc = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            if (permAssign) {
                Task tasklocat = floc.getLastLocation();
                tasklocat.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currloc = (Location) task.getResult();

                            try {
                                movCamera(new LatLng(currloc.getLatitude(), currloc.getLongitude()), zoomdef);
                                locList.add(new LatLng(currloc.getLatitude(), currloc.getLongitude()));
                            }catch (Exception es){
                                Toast.makeText(getActivity(), "Error\n"+es, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), "Location Not achieved", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException sec) {
            Toast.makeText(getActivity(), "Get Device Locate, Sec", Toast.LENGTH_SHORT).show();
            Log.e("Target", "Location SEC " + sec.getMessage());
        }
    }

    private void movCamera(LatLng latlong, float zoom) {
        gMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, zoom));
        MarkerOptions my_pos = new MarkerOptions();
        my_pos.position(latlong);
        gMaps.addPolyline((new PolylineOptions()).addAll(locList )
                .width(5)
                .color(Color.RED)
                .geodesic(false));

        gMaps.animateCamera(CameraUpdateFactory.newLatLng(latlong));
        gMaps.addMarker(my_pos);
    }

    private void CheckPermissions() {
        String[] pe = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getContext(),
                locFINE) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    locCOS) == PackageManager.PERMISSION_GRANTED) {
                permAssign = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), pe, reqcod);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), pe, reqcod);
        }
    }

    private void rePlay(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask backtask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.e("check","Check 111" );
                            LocateMe();
                        } catch (Exception e) {
                            Log.e("check","Check 222" );
                        }
                    }
                });
            }
        };
        timer.schedule(backtask , 05, 5000);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (permAssign){
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);

            gMaps = googleMap;
            gMaps.setMyLocationEnabled(true);
            gMaps.getUiSettings().setMyLocationButtonEnabled(true);
            LocateMe();


        }else {
            Toast.makeText(getActivity(), "You must allow the application to access location for it to run smoothly", Toast.LENGTH_SHORT).show();
            CheckPermissions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            locList.clear();
            gMaps.clear();
            return true;
        }

        if (id == R.id.action_save) {
            return true;
        }

        if (id == R.id.action_satelite) {
            gMaps.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        }

        if (id == R.id.action_terrain) {
            gMaps.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            return true;
        }

        if (id == R.id.action_normal) {
            gMaps.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        }

        if (id == R.id.action_hybrid) {
            gMaps.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.mini, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
