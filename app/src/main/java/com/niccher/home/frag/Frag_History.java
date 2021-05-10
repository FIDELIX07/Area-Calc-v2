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
import com.niccher.home.activities.History_List_Polygon;
import com.niccher.home.activities.History_List_Polyline;

public class Frag_History extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View solv= inflater.inflate(R.layout.frag_info, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Select the History");


        final String[] mobileArray = {"Area History","Perimeter History"};
        final ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_account, mobileArray);
        final ListView listView = (ListView) solv.findViewById(R.id.account_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mobileArray[position] =="Area History"){
                    Intent viw = new Intent(getActivity(), History_List_Polygon.class);
                    startActivity(viw);
                }

                if (mobileArray[position] =="Perimeter History"){
                    Intent viw = new Intent(getActivity(), History_List_Polyline.class);
                    startActivity(viw);
                }
            }
        });

        return solv;
    }
}
