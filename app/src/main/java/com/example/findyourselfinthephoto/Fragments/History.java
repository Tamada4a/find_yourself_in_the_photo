package com.example.findyourselfinthephoto.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findyourselfinthephoto.ExpandableListAdapter;
import com.example.findyourselfinthephoto.R;

import org.json.simple.ItemList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class History extends Fragment {

    private SharedPreferences preferences;
    private RecyclerView recyclerView;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        expListView = (ExpandableListView)view.findViewById(R.id.lvExp);

        preferences = getActivity().getSharedPreferences("Link", MODE_PRIVATE);

        Map<String, ?> allEntries = preferences.getAll();

        prepareListData(allEntries);

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);

        //SharedPreferences.Editor clear = preferences.edit().clear();
        //clear.commit();


        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).toString() != "No photos found :(") {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listDataChild.get(
                            listDataHeader.get(groupPosition)).get(childPosition)));
                    startActivity(browserIntent);
                }
                return false;
            }
        });

        return view;
    }

    private void prepareListData(Map<String, ?> allEntries) {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        //Добавляем данные о пунктах списка:
        int i = 0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            listDataHeader.add(entry.getKey());
            Set<String> temp = (Set<String>) allEntries.get(entry.getKey());
            List<String> photos = new ArrayList<String>();
            if (temp.isEmpty())
                photos.add("No photos found :(");
            else{
                for (String s : temp){
                    photos.add(s);
                }
            }
            listDataChild.put(listDataHeader.get(i), photos);
            i++;
        }
    }
}
