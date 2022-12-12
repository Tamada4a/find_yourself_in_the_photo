package com.example.findyourselfinthephoto.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.findyourselfinthephoto.Adapters.ExpandableListAdapter;
import com.example.findyourselfinthephoto.CustomListeners.NetworkStateReceiver;
import com.example.findyourselfinthephoto.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class History extends Fragment implements NetworkStateReceiver.NetworkStateReceiverListener {

    private SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private ArrayList<ArrayList<byte[]>> previewBytes = new ArrayList<ArrayList<byte[]>>();
    private ArrayList<ArrayList<String>> downloadLinks = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> previewUrls = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> names = new ArrayList<ArrayList<String>>();
    private ArrayList<String> keys = new ArrayList<String>();

    private boolean isOffilne = false;
    private boolean isLongClicked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        expListView = (ExpandableListView)view.findViewById(R.id.lvExp);

        sharedPref = getActivity().getPreferences(MODE_PRIVATE);
        editor = sharedPref.edit();

        loadFromSharedPreference();
        prepareListData();

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild, downloadLinks, previewBytes, previewUrls);

        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(!isLongClicked) {
                    String name = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).toString();
                    if (name != "No photos found :(") {
                        if (!isOffilne) {
                            Intent intent = new Intent();

                            Uri uri = Uri.parse(previewUrls.get(groupPosition).get(childPosition));

                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "image/*");
                            view.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Проблема с подключением к сети", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                isLongClicked = true;
                long packedPosition = expListView.getExpandableListPosition(i);

                int itemType = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    onGroupLongClick(groupPosition);
                }

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    onChildLongClick(groupPosition, childPosition);
                }
                return false;
            }
        });

        return view;
    }


    @Override
    public void networkAvailable() {
        isOffilne = false;
    }


    @Override
    public void networkUnavailable() {
        isOffilne = true;
    }


    private void onGroupLongClick(int groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View alertDialog = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_layout, null);

        TextView labelInfo = (TextView)alertDialog.findViewById(R.id.info_dialog_Label);
        labelInfo.setText("Удаление ссылки из списка");

        TextView dataInfo = (TextView)alertDialog.findViewById(R.id.info_dialog_Text);
        String info = "Вы уверены, что хотите удалить выбранную(№" + (groupPosition + 1) + ") запись?";
        dataInfo.setText(info);

        TextView positiveButton = (TextView)alertDialog.findViewById(R.id.info_dialog_positiveButton);
        positiveButton.setText("Да");

        TextView negativeButton = (TextView)alertDialog.findViewById(R.id.info_dialog_NegativeButton);
        negativeButton.setText("Нет");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isLongClicked = false;
            }
        });

        builder.setView(alertDialog);

        AlertDialog dialog = builder.create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewBytes.remove(groupPosition);
                downloadLinks.remove(groupPosition);
                previewUrls.remove(groupPosition);
                names.remove(groupPosition);
                keys.remove(groupPosition);

                saveToSharedPref();

                prepareListData();

                listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild, downloadLinks, previewBytes, previewUrls);
                expListView.setAdapter(listAdapter);

                dialog.cancel();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    private void onChildLongClick(int groupPosition, int childPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View alertDialog = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_layout, null);

        TextView labelInfo = (TextView)alertDialog.findViewById(R.id.info_dialog_Label);
        labelInfo.setText("Что вы хотите сделать с фотографией?");

        TextView positiveButton = (TextView)alertDialog.findViewById(R.id.info_dialog_positiveButton);
        positiveButton.setText("Скачать");

        TextView negativeButton = (TextView)alertDialog.findViewById(R.id.info_dialog_NegativeButton);
        negativeButton.setText("Поделиться");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isLongClicked = false;
            }
        });

        builder.setView(alertDialog);

        AlertDialog dialog = builder.create();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOffilne)
                    downloadPhoto(groupPosition, childPosition);
                else
                    Toast.makeText(getActivity(), "Проблема с подключением к сети", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String name = names.get(groupPosition).get(childPosition);

                    File outputDir = getContext().getCacheDir();

                    File outputFile = new File(outputDir, name);

                    byte[] dataForWriting = previewBytes.get(groupPosition).get(childPosition);
                    FileUtils.writeByteArrayToFile(outputFile, dataForWriting);

                    Uri photoURI = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", outputFile);

                    Intent sharingIntent = new Intent("android.intent.action.SEND");
                    sharingIntent.setType("image/*");
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    sharingIntent.putExtra("android.intent.extra.STREAM", photoURI);

                    Intent chooser = Intent.createChooser(sharingIntent, "Share using");

                    List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getContext().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    startActivity(chooser);
                    //outputFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.cancel();
            }
        });

        dialog.show();
    }


    private void downloadPhoto(int groupPosition, int childPosition) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(downloadLinks.get(groupPosition).get(childPosition)).build();
                    Response response = client.newCall(request).execute();
                    if(response.code() == 200) {
                        byte[] prePhoto = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(prePhoto, 0, prePhoto.length);

                        String fileName = names.get(groupPosition).get(childPosition);

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, fileName, null);

                        Toast.makeText(getActivity(), "Фотография успешно скачана", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getActivity(), "Что-то пошло не так. Возможно, что ссылка устарела. Необходимо ещё раз провести поиск", Toast.LENGTH_SHORT).show();
                }
                catch (IOException e){

                }
                Looper.loop();
            }
        });

        thread.start();
    }


    private void prepareListData() {
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


    private void saveToSharedPref() {
        String jsonPreviewUrlArray = new Gson().toJson(previewUrls);
        String jsonPreviewArray = new Gson().toJson(previewBytes);
        String jsonDownloadArray = new Gson().toJson(downloadLinks);
        String jsonNamesArray = new Gson().toJson(names);
        String jsonKeysArray = new Gson().toJson(keys);

        editor.putString("previewLinks", jsonPreviewUrlArray);
        editor.putString("download", jsonDownloadArray);
        editor.putString("previewBytes", jsonPreviewArray);
        editor.putString("names", jsonNamesArray);
        editor.putString("keys", jsonKeysArray);
        editor.apply();
    }


    private void loadFromSharedPreference() {
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
    }
}
