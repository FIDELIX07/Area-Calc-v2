package com.niccher.home.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niccher.home.R;
import com.niccher.home.adapters.Adp_Area;
import com.niccher.home.mod.Mod_Area;

import java.util.ArrayList;
import java.util.List;

public class History_Render extends AppCompatActivity implements OnMapReadyCallback {

    String locFINE = Manifest.permission.ACCESS_FINE_LOCATION;
    String locCOS = Manifest.permission.ACCESS_COARSE_LOCATION;

    GeofencingClient gfeClient;

    MapView mMapView;

    Boolean permAssign = false;

    Polygon polygon;
    PolylineOptions polylineOptions;


    ArrayList<LatLng> locList = new ArrayList<LatLng>();
    ArrayList<LatLng> loc_area = new ArrayList<LatLng>();
    ArrayList<LatLng> loc_init = new ArrayList<LatLng>();

    final int reqcod = 145;
    GoogleMap gMaps;

    float zoomdef = 10f;

    FusedLocationProviderClient floc;

    FloatingActionButton fab_ex;

    TextView txt_peri, txt_area;

    LinearLayout linL, linA, linC, linStatus;

    LatLng new_point;

    Intent getit;
    Bundle getbun;

    String Object_time,Object_uid,Object_latlong,Object_points,Object_perimeter,Object_area;

    public History_Render(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);

        ((AppCompatActivity) this).getSupportActionBar().setTitle("History");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CheckPermissions();

        mMapView = (MapView) findViewById(R.id.mapView);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        getit= this.getIntent();

        Object_time = String.valueOf(getit.getStringExtra("Object_time"));
        Object_uid = String.valueOf(getit.getStringExtra("Object_uid"));
        Object_latlong = String.valueOf(getit.getStringExtra("Object_latlong"));
        Object_points = String.valueOf(getit.getStringExtra("Object_points"));
        Object_perimeter = String.valueOf(getit.getStringExtra("Object_perimeter"));
        Object_area = String.valueOf(getit.getStringExtra("Object_area"));


        Log.e("Passed --- ", "Object_time: "+Object_time);
        Log.e("Passed --- ", "Object_uid: "+Object_uid);
        Log.e("Passed --- ", "Object_latlong: "+Object_latlong);
        Log.e("Passed --- ", "Object_points: "+Object_points);
        Log.e("Passed --- ", "Object_perimeter: "+Object_perimeter);
        Log.e("Passed --- ", "Object_area: "+Object_area);

        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this::onMapReady);
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
        floc = LocationServices.getFusedLocationProviderClient(this);
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
                                Toast.makeText(History_Render.this, "Error\n"+es, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(History_Render.this, "Location Not achieved", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException sec) {
            Toast.makeText(History_Render.this, "Get Device Locate, Sec", Toast.LENGTH_SHORT).show();
            Log.e("Target", "Location SEC " + sec.getMessage());
        }
    }

    private void movCamera(LatLng latlong, float zoom) {
        gMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, zoom));
    }

    private void CheckPermissions() {
        String[] pe = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this,
                locFINE) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this,
                    locCOS) == PackageManager.PERMISSION_GRANTED) {
                permAssign = true;
            } else {
                ActivityCompat.requestPermissions(this, pe, reqcod);
            }
        } else {
            ActivityCompat.requestPermissions(this, pe, reqcod);
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
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

            MarkerOptions markerOptions = new MarkerOptions();
            locList.clear();

            LatLng point_one = null, point_last = null;

            try {
                if(Object_area.endsWith("null")){
                    //Log.e("- Caught As - ", "Perimeter endsWith(\"null\") ");
                    String[] dd = Object_latlong.split("lat/lng:");
                    for ( int sizes = 1; sizes< dd.length; sizes++) {
                        String pointone = dd[sizes].replace("(","").replace(")","").trim();
                        String[] new_latlong =  pointone.split(",");
                        double new_lat = Double.parseDouble(new_latlong[0]);
                        double new_long = Double.parseDouble(new_latlong[1]);
                        new_point = new LatLng(new_lat, new_long);
                        locList.add(new_point);
                        markerOptions.position(new_point);

                        try {
                            googleMap.addPolyline((new PolylineOptions()).addAll(locList )
                                    .width(5)

                                    .color(Color.RED)
                                    .geodesic(false));
                        }catch (Exception mas){
                            //Log.e("- Caught As - ", "/////////////////// "+mas );
                        }

                        gMaps.addMarker(markerOptions);
                    }

                    Log.e("- Area -", Object_perimeter+" Kilometers" );
                    Toast.makeText(this, "- Perimeter -"+Object_perimeter+" Km" , Toast.LENGTH_LONG).show();
                }

                if (isNumeric(Object_area)){
                    String[] dd = Object_latlong.split("lat/lng:");
                    for ( int sizes = 1; sizes< dd.length; sizes++) {
                        String pointone = dd[sizes].replace("(","").replace(")","").trim();
                        Log.e((dd.length-1)+"- Final As - ", "************* are : "+pointone );
                        String[] new_latlong =  pointone.split(",");
                        double new_lat = Double.parseDouble(new_latlong[0]);
                        double new_long = Double.parseDouble(new_latlong[1]);
                        new_point = new LatLng(new_lat, new_long);
                        locList.add(new_point);
                        markerOptions.position(new_point);

                        if(sizes == 1){
                            point_one = new_point;
                        }

                        gMaps.addMarker(markerOptions);
                    }

                    locList.add(point_one);

                    try {
                        polygon = gMaps.addPolygon(new PolygonOptions()
                                .addAll(locList)
                                .strokeWidth(0)
                                .clickable(true)
                                .fillColor(Color.argb(70,140,70,200)));

                        googleMap.addPolyline((new PolylineOptions()).addAll(locList )
                                .width(5)
                                .color(Color.RED)
                                .geodesic(false));
                    }catch (Exception mas){
                        //Log.e("- Caught As - ", "/////////////////// "+mas );
                    }

                    Log.e("- Area -", Object_perimeter+" Kilometers" );
                    Toast.makeText(this, "- Perimeter -"+Object_perimeter+" Km" + "\n- Area -" +String.format("%.3f", Double.parseDouble(Object_area))+" Sq Km", Toast.LENGTH_LONG).show();

                }

            }catch (Exception es){
                String[] dd = Object_latlong.split("lat/lng:");
                for ( int sizes = 1; sizes< dd.length; sizes++) {
                    String pointone = dd[sizes].replace("(","").replace(")","").trim();
                    String[] new_latlong =  pointone.split(",");
                    double new_lat = Double.parseDouble(new_latlong[0]);
                    double new_long = Double.parseDouble(new_latlong[1]);
                    new_point = new LatLng(new_lat, new_long);
                    locList.add(new_point);
                    markerOptions.position(new_point);

                    try {
                        googleMap.addPolyline((new PolylineOptions()).addAll(locList )
                                .width(5)
                                .color(Color.RED)
                                .geodesic(false));
                    }catch (Exception mas){
                        //Log.e("- Caught As - ", "/////////////////// "+mas );
                    }

                    gMaps.addMarker(markerOptions);
                }

                Log.e("- Area -", Object_perimeter+" Kilometers" );
                Toast.makeText(this, "- Perimeter -"+Object_perimeter+" Km" , Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(History_Render.this, "You must allow the application to access location for it to run smoothly", Toast.LENGTH_SHORT).show();
            CheckPermissions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
