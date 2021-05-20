package com.niccher.home.frag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.niccher.home.HomePage;
import com.niccher.home.R;
import com.niccher.home.activities.History_List_Polygon;
import com.niccher.home.activities.History_List_Polyline;
import com.niccher.home.activities.Profile;
import com.niccher.home.auth.UserLogin;

public class Frag_Info extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser userf;
    DatabaseReference dref;

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv= inflater.inflate(R.layout.frag_info, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("More Actions");

        mAuth = FirebaseAuth.getInstance();
        userf=mAuth.getCurrentUser();
        //fdbas= FirebaseDatabase.getInstance();
        dref = FirebaseDatabase.getInstance().getReference("Area_Calc_Saved");


        final String[] mobileArray = {"My Profile","Area History","Perimeter History","Log Out","Exit"};
        final ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_account, mobileArray);
        final ListView listView = (ListView) solv.findViewById(R.id.account_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mobileArray[position] =="My Profile"){
                    Intent viw = new Intent(getActivity(), Profile.class);
                    startActivity(viw);
                }

                if (mobileArray[position] =="Area History"){
                    Intent viw = new Intent(getActivity(), History_List_Polygon.class);
                    startActivity(viw);
                }

                if (mobileArray[position] =="Perimeter History"){
                    Intent viw = new Intent(getActivity(), History_List_Polyline.class);
                    startActivity(viw);
                }

                if (mobileArray[position] =="Log Out"){
                    mAuth.signOut();

                    FirebaseUser fuse=mAuth.getCurrentUser();
                    if (fuse!=null){
                        //
                    }else {
                        startActivity(new Intent(getActivity(), UserLogin.class));
                        getActivity().finish();
                    }
                }

                if (mobileArray[position] =="Exit"){
                    Intent intt=new Intent(Intent.ACTION_MAIN);
                    intt.addCategory(Intent.CATEGORY_HOME);
                    intt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intt);

                    getActivity().finish();
                    System.gc();
                    System.exit(0);
                }
            }
        });

        return solv;
    }
}
