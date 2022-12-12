package com.example.findyourselfinthephoto.Adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.findyourselfinthephoto.R;

/*
* Адаптер для отображения истории поиска
* */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    //Названия заголовков
    private List<String> _listDataHeader;
    //Данные для элементов подпунктов:
    private HashMap<String, List<String>> _listDataChild;
    private ArrayList<ArrayList<String>> _downloadLinks;
    private ArrayList<ArrayList<byte[]>> _previewBytes;
    private ArrayList<ArrayList<String>> _previewUrls;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<String>> listChildData,
                                 ArrayList<ArrayList<String>> downloadLinks,
                                 ArrayList<ArrayList<byte[]>> previewBytes,
                                 ArrayList<ArrayList<String>> previewUrls) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._downloadLinks = downloadLinks;
        this._previewBytes = previewBytes;
        this._previewUrls = previewUrls;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.items_history, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        ImageView imageChild = (ImageView)convertView.findViewById(R.id.childImage);

        if(_previewBytes.get(groupPosition).size() != 0) {
            byte[] previewByte = _previewBytes.get(groupPosition).get(childPosition);
            Bitmap bitmap = BitmapFactory.decodeByteArray(previewByte, 0, previewByte.length);
            imageChild.setImageBitmap(bitmap);
        }
        else{
            imageChild.setImageBitmap(drawNoPhotosFound());
        }
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_history, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private Bitmap drawNoPhotosFound(){
        Drawable drawable = ContextCompat.getDrawable(_context, R.drawable.no_photos_found);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}