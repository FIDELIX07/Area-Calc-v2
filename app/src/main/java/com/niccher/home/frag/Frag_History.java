package com.niccher.home.frag;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.niccher.home.activities.History_List_Polygon;
import com.niccher.home.activities.History_List_Polyline;
import com.niccher.home.mod.Mod_Perimeter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Frag_History extends Fragment implements OnMapReadyCallback {

    String locFINE = Manifest.permission.ACCESS_FINE_LOCATION;
    String locCOS = Manifest.permission.ACCESS_COARSE_LOCATION;

    MapView mMapView;

    Boolean permAssign = false;

    int count = 0, area_count = 0;
    double length_ = 0;
    LatLng tapped,tapped1;
    PolylineOptions polylineOptions;

    double prev,curent;

    ArrayList<LatLng> locList = new ArrayList<LatLng>();
    ArrayList<LatLng> loc_area = new ArrayList<LatLng>();
    Boolean state = false, area = false, length = false, clean = false ;

    final int reqcod = 145;
    GoogleMap gMaps;

    float zoomdef = 10f;

    FusedLocationProviderClient floc;

    FloatingActionButton fab_ex;

    TextView txt_peri;

    String store_area, store_perimeter;

    CalcDistance calcDistance;

    FirebaseAuth mAuth;
    FirebaseUser userf;
    DatabaseReference dref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View solv= inflater.inflate(R.layout.frag_history, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Track Me");

        mAuth = FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();
        //fdbas= FirebaseDatabase.getInstance();
        dref = FirebaseDatabase.getInstance().getReference("Area_Calc_Saved");

        CheckPermissions();

        calcDistance = new CalcDistance();

        mMapView = (MapView) solv.findViewById(R.id.mapView);

        fab_ex = solv.findViewById(R.id.fab_remove_length);

        txt_peri = solv.findViewById(R.id.loc_perimeter);

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this::onMapReady);

        Animation anim_show = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_show);
        Animation anim_hide = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_hide);

        txt_peri.setText("Perimeter");

        fab_ex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_peri.setText("Perimeter");
                length_ = 0.0;
                store_area =""; store_perimeter ="";
                loc_area.clear();
                locList.clear();
                gMaps.clear();
                clean = false;
            }
        });

        length = true;


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
                            //Log.e("Target", "Location Got ");
                            Location currloc = (Location) task.getResult();

                            try {
                                movCamera(new LatLng(currloc.getLatitude(), currloc.getLongitude()), zoomdef);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

}
