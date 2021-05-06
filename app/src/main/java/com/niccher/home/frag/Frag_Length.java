package com.niccher.home.frag;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;
import com.niccher.home.R;
import com.niccher.home.Utils.CalcArea;
import com.niccher.home.Utils.CalcDistance;
import com.niccher.home.mod.Mod_Perimeter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class Frag_Length extends Fragment implements OnMapReadyCallback {

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

    public Frag_Length() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View fraghome = inflater.inflate(R.layout.frag_length, container, false);

        mAuth = FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();
        //fdbas= FirebaseDatabase.getInstance();
        dref = FirebaseDatabase.getInstance().getReference("Area_Calc_Saved");

        CheckPermissions();

        calcDistance = new CalcDistance();

        mMapView = (MapView) fraghome.findViewById(R.id.mapView);

        fab_ex = fraghome.findViewById(R.id.fab_remove_length);

        txt_peri = fraghome.findViewById(R.id.loc_perimeter);

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

        return fraghome;
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

            gMaps.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();

                    count = count + 1;
                    area_count = area_count + 1;

                    if (clean){
                        store_area =""; store_perimeter ="";
                        loc_area.clear();
                        locList.clear();
                        gMaps.clear();
                        clean = false;
                    }
                    if (length){
                        locList.add(latLng);
                        int sizes = locList.size();

                        if (state){
                            store_area =""; store_perimeter ="";
                            locList.clear();
                            gMaps.clear();
                            googleMap.clear();
                            state = false;
                        }

                        if (locList.size() > 1){
                            tapped = locList.get(locList.size()-2);
                            tapped1 = locList.get(locList.size()-1);
                            length_ = length_ + calcDistance.CalculateDistance(tapped, tapped1);
                            String distance  = String.format("%.2f", length_);
                            txt_peri.setText("Distance Approximation: "+distance+" Km");
                            Log.e("Distance is ", "Currently as : " + length_);
                            store_area ="NULLABLE";
                            store_perimeter = distance;
                        }

                        markerOptions.position(latLng);
                        googleMap.addPolyline((new PolylineOptions()).addAll(locList )
                                .width(5)
                                .color(Color.RED)
                                .geodesic(false));

                        gMaps.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        gMaps.addMarker(markerOptions);
                    }else {
                        Toast.makeText(getActivity(), "Please select either Area or Distance so as to proceed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            Toast.makeText(getActivity(), "You must allow the application to access location for it to run smoothly", Toast.LENGTH_SHORT).show();
            CheckPermissions();
        }
    }


    private void SaveList(ArrayList<LatLng> selected, String type) {
        String llong= "";
        for (int i = 0; i < (selected.size()); i++) {
            Log.e("SaveList", i+" SaveList: "+selected.get(i) );
            Log.e("SaveList", i+" Type: "+type);
            llong+=selected.get(i);
        }
        Log.e("SaveList", "SaveList String : "+llong);

        Calendar cal= Calendar.getInstance();
        SimpleDateFormat ctime=new SimpleDateFormat("HH:mm");
        SimpleDateFormat cdate=new SimpleDateFormat("dd-MMMM-yyyy");

        final String ctim=ctime.format(cal.getTime());
        final String cdat=cdate.format(cal.getTime());

        String uploadId = "";

        try {
            uploadId= dref.push().getKey();
            Mod_Perimeter upload = new Mod_Perimeter(uploadId,cdat+" "+ctim,llong,String.valueOf(selected.size()), store_perimeter );
             dref.child(type).child(userf.getUid()).child(uploadId).setValue(upload);
            Toast.makeText(getActivity(), "Length Selection saved", Toast.LENGTH_SHORT).show();

        }catch (Exception s){
            Toast.makeText(getActivity(), "Unable to save your selection", Toast.LENGTH_LONG).show();
            Log.e("SaveList", "SaveList Error : "+s.getMessage());
        }
    }
}