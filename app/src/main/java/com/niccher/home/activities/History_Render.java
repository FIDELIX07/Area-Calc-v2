package com.niccher.home.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class History_Render extends AppCompatActivity {

    RecyclerView recyl;
    Adp_Area adp;
    List<Mod_Area> mod_list;

    FirebaseAuth mAuth;
    FirebaseUser userf;

    public History_Render(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_listing);

        getSupportActionBar().setTitle("Saved Polylines");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth= FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();

        recyl=findViewById(R.id.rec_history);
        recyl.setHasFixedSize(true);

        LinearLayoutManager lim = new LinearLayoutManager(this);
        lim.setReverseLayout(true);
        lim.setStackFromEnd(true);
        recyl.setLayoutManager(lim);

        mod_list=new ArrayList<>();

        FetchEm();
    }

    private void FetchEm() {
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Area_Calc_Saved/Length").child(userf.getUid());
        dref.keepSynced(true);

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mod_list.clear();

                for (DataSnapshot ds1: dataSnapshot.getChildren()){
                    Mod_Area ug=ds1.getValue(Mod_Area.class);

                    mod_list.add(ug);

                    adp=new Adp_Area(getApplication(),mod_list);

                    recyl.setAdapter(adp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
