package com.example.findyourselfinthephoto.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.findyourselfinthephoto.ExpandableListAdapter;
import com.example.findyourselfinthephoto.FAQExpandableListAdapter;
import com.example.findyourselfinthephoto.R;


public class FAQ extends Fragment {


    private String[] groupHistoryItem = {"Link to folder1","Link to folder2"};

    private String[] linkToFolder1 = {"Link1","Link2","Link3"};
    private String[] linkToFolder2 = {"No photos found :("};

    List<String> groupList;
    List<String> childList;
    HashMap<String,List<String>> map;
    ExpandableListView ELV;
    FAQExpandableListAdapter ELA;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        createGroupList();
        createCollection();
        ELV = (ExpandableListView)view.findViewById(R.id.lExp);
        ELA = new FAQExpandableListAdapter(getActivity(),groupList,map);
        ELV.setAdapter(ELA);
        ELV.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lasExpendPosition = -1;
            @Override
            public void onGroupExpand(int i) {
                if(lasExpendPosition != -1 && i != lasExpendPosition){
                    ELV.collapseGroup(lasExpendPosition);
                }
                lasExpendPosition=i;
            }
        });
        ELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                String selected = ELA.getChild(i,i1).toString();
                Toast.makeText(getActivity().getApplicationContext(), "Selected: "+selected,Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        return view;
    }

    private void createCollection() {
        String[] linkToFolder1 = {"Link1","Link2","Link3"};
        String[] linkToFolder2 = {"No photos found :("};
        map = new HashMap<String,List<String>>();
        for(String group : groupList){
            childList = new ArrayList<>();
            for(String link : linkToFolder1 ){
                childList.add(link);
            }
            map.put("Link to folder1",childList);
            childList = new ArrayList<>();
            for(String link : linkToFolder2 ){
                childList.add(link);
            }
            map.put("Link to folder2",childList);
        }
    }

    private void createGroupList(){
        groupList = new ArrayList<>();
        groupList.add("Link to folder1");
        groupList.add("Link to folder2");
    }

}
