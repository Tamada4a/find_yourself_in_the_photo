package com.example.findyourselfinthephoto.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findyourselfinthephoto.Adapters.ExpandableListAdapter;
import com.example.findyourselfinthephoto.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class History extends Fragment {

    private SharedPreferences sharedPref;
    private RecyclerView recyclerView;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private ArrayList<ArrayList<byte[]>> previewBytes = new ArrayList<ArrayList<byte[]>>();
    private ArrayList<ArrayList<String>> downloadLinks = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> previewUrls = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> names = new ArrayList<ArrayList<String>>();
    private ArrayList<String> keys = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        expListView = (ExpandableListView)view.findViewById(R.id.lvExp);

        sharedPref = getActivity().getPreferences(MODE_PRIVATE);


        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild, downloadLinks, previewBytes, previewUrls);

        expListView.setAdapter(listAdapter);


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

    private void prepareListData() {

        //получаем типы для переменных, которые будем вытаскивать из sharedPreference
        Type previewItemsType = new TypeToken<ArrayList<ArrayList<byte[]>>>() {}.getType();
        Type linksType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
        Type keysType = new TypeToken<ArrayList<String>>() {}.getType();

        //получаем ключ, по которому будем вытаскивать
        String jsonPreviewBytes = sharedPref.getString("previewBytes", "");
        String jsonDownload = sharedPref.getString("download", "");
        String jsonPreviewLinks = sharedPref.getString("previewLinks", "");
        String jsonNames = sharedPref.getString("names", "");
        String jsonKeys = sharedPref.getString("keys", "");

        //вытаскиваем сами значения для переменных
        previewBytes = new Gson().fromJson(jsonPreviewBytes, previewItemsType);
        downloadLinks = new Gson().fromJson(jsonDownload, linksType);
        previewUrls = new Gson().fromJson(jsonPreviewLinks, linksType);
        names = new Gson().fromJson(jsonNames, linksType);
        keys = new Gson().fromJson(jsonKeys, keysType);


        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        //Добавляем данные о пунктах списка:
        for(int i = 0; i < keys.size(); ++i){
            listDataHeader.add(keys.get(i));

            ArrayList<String> childItems = new ArrayList<String>();
            if(names.get(i).isEmpty()){
                childItems.add("No photos found :(");
            }
            else{
                for(int j = 0; j < names.get(i).size(); ++j){
                    childItems.add(names.get(i).get(j));
                }
            }
            listDataChild.put(listDataHeader.get(i), childItems);
        }
    }
}
