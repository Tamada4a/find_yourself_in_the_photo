package com.example.findyourselfinthephoto.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.findyourselfinthephoto.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* Адаптер для отображения истории поиска в FAQ
* */
public class FAQExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private HashMap<String, List<String>> map;
    private List<String> groupList;

    public FAQExpandableListAdapter(Context context, List<String> groupList,HashMap<String, List<String>> map){
        this.context = context;
        this.groupList=groupList;
        this.map=map;
    }

    @Override
    public int getGroupCount() {
        return map.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return map.get(groupList.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return groupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return map.get(groupList.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String linkName = getGroup(i).toString();
        if(view==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.group_faq,null);
            TextView item = view.findViewById(R.id.lblListHeaderF);
            item.setTypeface(null, Typeface.BOLD);
            item.setText(linkName);
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String model = getChild(i,i1).toString();
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.items_faq,null);
        }
        TextView item = view.findViewById(R.id.lblListItemF);
        item.setText(model);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
