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

import com.niccher.home.R;

public class Frag_Info extends Fragment {

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv= inflater.inflate(R.layout.frag_info, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("More Actions");


        final String[] mobileArray = {"My Profile","Area History","Perimeter History","Log Out","Exit"};
        final ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_account, mobileArray);
        final ListView listView = (ListView) solv.findViewById(R.id.account_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mobileArray[position] =="My Profile"){
                    Toast.makeText(getActivity(), "My Profile", Toast.LENGTH_SHORT).show();
                }

                if (mobileArray[position] =="Area History"){
                    Toast.makeText(getActivity(), "Area History", Toast.LENGTH_SHORT).show();
                }

                if (mobileArray[position] =="Perimeter History"){
                    Toast.makeText(getActivity(), "Perimeter History", Toast.LENGTH_SHORT).show();
                }

                if (mobileArray[position] =="About Us"){
                    Toast.makeText(getActivity(), "About Us", Toast.LENGTH_SHORT).show();
                }

                if (mobileArray[position] =="Log Out"){
                    Toast.makeText(getActivity(), "Log Out", Toast.LENGTH_SHORT).show();
                }

                if (mobileArray[position] =="Exit"){
                    getActivity().finish();
                    System.gc();
                    System.exit(0);
                }
            }
        });

        return solv;
    }
}
