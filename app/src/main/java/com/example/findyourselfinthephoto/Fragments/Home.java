package com.example.findyourselfinthephoto.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.findyourselfinthephoto.CustomListeners.NetworkStateReceiver;
import com.example.findyourselfinthephoto.GetRecognizedFaceUrl;
import com.example.findyourselfinthephoto.Helpers.MyJson;
import com.example.findyourselfinthephoto.Helpers.PhotoHelper;
import com.example.findyourselfinthephoto.R;
import com.sealstudios.multiimageview.MultiImageView;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import br.com.onimur.handlepathoz.HandlePathOz;
import br.com.onimur.handlepathoz.HandlePathOzListener;
import br.com.onimur.handlepathoz.model.PathOz;
import generator.RandomUserAgentGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Home extends Fragment implements HandlePathOzListener.SingleUri, NetworkStateReceiver.NetworkStateReceiverListener {

    private FragmentActivity activity;

    private MultiImageView upload_button;
    private ImageButton start_button;
    private EditText UrlField;

    private ArrayList<String> realPathList = new ArrayList<String>();
    private HandlePathOz handlePathOz;

    private String gettedURL;
    private int totalSize;

    private boolean isOffline = false;

    private PhotoHelper photoHelper;

    private Uri photoURI;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        handlePathOz = new HandlePathOz(getContext(), this);
        activity = getActivity();

        UrlField = view.findViewById(R.id.input_Link);

        upload_button = view.findViewById(R.id.upload_button);

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        start_button = view.findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateData();
            }
        });

        photoHelper = new PhotoHelper(activity);

        return view;
    }

    private void ValidateData(){
        gettedURL = UrlField.getText().toString();

        if(isOffline){
            Toast.makeText(activity,"Отсутствует интернет-соединение!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(realPathList.isEmpty()){
            Toast.makeText(activity,"Добавьте изображение!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(gettedURL)){
            Toast.makeText(activity, "Добавьте ссылку!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!gettedURL.contains("https://disk.yandex.ru/d/")){
            Toast.makeText(activity, "Нужно ввести ссылку на папку!", Toast.LENGTH_SHORT).show();
            return;
        }

        MyRequests request = new MyRequests(gettedURL, activity, "getTotal");
        request.execute();
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            File outputDir = getContext().getCacheDir();

            File photo = new File(outputDir, "photo.jpg");

            photo.delete();
            photoURI = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", photo);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            Intent chooser = Intent.createChooser(galleryIntent,"Select Picture");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent });
            startActivityIntent.launch(chooser);
        }
        catch(Exception e)
        {
            Toast.makeText(activity, "Пожалуйста, проверьте Sd  - карту", Toast.LENGTH_SHORT);
        }
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();

                        if(data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            Toast.makeText(activity, "Проверяем фотографии на наличие лиц", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < clipData.getItemCount(); ++i) {
                                Uri ImageUri = clipData.getItemAt(i).getUri();
                                handlePathOz.getRealPath(ImageUri);
                            }
                        }
                        else if (data.getData() != null) {
                            Uri ImageUri = data.getData();
                            Toast.makeText(activity, "Проверяем фотографию на наличие лиц", Toast.LENGTH_SHORT).show();
                            handlePathOz.getRealPath(ImageUri);
                        }
                        else if(photoURI != null){
                            Toast.makeText(activity, "Проверяем фотографию на наличие лиц", Toast.LENGTH_SHORT).show();
                            handlePathOz.getRealPath(photoURI);
                        }
                    }
                }
            });

    @Override
    public void onRequestHandlePathOz(@NonNull PathOz pathOz, @Nullable Throwable throwable) {
        String path = pathOz.getPath();
        if(!path.isEmpty()){
            if(photoHelper.isContainsFace(path)) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

                upload_button.addImage(bitmap);
                realPathList.add(pathOz.getPath());
            }
            else
                Toast.makeText(activity, "Не получается распознать лицо на фото", Toast.LENGTH_SHORT).show();
        }
        System.out.println("Real Path is " + pathOz.getPath());
    }

    private void HandleGetTotal(String response) throws ParseException{
        totalSize = MyJson.getTotal(response, "YandexDisk");

        if (totalSize != 0) {
            MyRequests request = new MyRequests(gettedURL + "&limit=" + totalSize, activity, "getLinks");
            request.execute();
        } else
            Toast.makeText(activity, "А папка пустая!", Toast.LENGTH_SHORT).show();
    }

    private void HomeHandleResponse(String response) throws ParseException {
        System.out.println("RESPONSE IS " + response);

        ArrayList<ArrayList<String>> result = MyJson.getUrlArray(response, "YandexDisk");

        int size = result.get(0).size();
        for (int i = 0; i < size; ++i)
            System.out.println("Ссылка №" + i + "\n" + result.get(0).get(i) + "\n" + result.get(1).get(i));

        if(result.get(0).size() != 0)
            GetRecognizedFaceUrl.compare_image(realPathList, result, activity, gettedURL);
        else
            Toast.makeText(activity, "В папке не удалось обнаружить фотографии", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkAvailable() {
        isOffline = false;
    }

    @Override
    public void networkUnavailable() {
        isOffline = true;
    }


    private class MyRequests extends AsyncTask {
        private final OkHttpClient client = new OkHttpClient();

        private String url;
        private final String api_url = "https://cloud-api.yandex.net/v1/disk/public/resources?public_key=";

        private String response_string;
        private String callType;
        private FragmentActivity fragmentActivity;


        public MyRequests(String url_for_request, FragmentActivity activity, String callType) {
            this.url = url_for_request;
            this.fragmentActivity = activity;
            this.callType = callType;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Request request = new Request.Builder()
                    .url(api_url + url)
                    .header("User-Agent", RandomUserAgentGenerator.getNextMobile())
                    .build();

            try {
                Response response = client.newCall(request).execute();
                response_string = response.body().string();
                if (response_string.contains("обновите браузер") || response_string.contains("Ресурс не найден") || response_string.isEmpty()
                || response_string.contains("Не удалось найти запрошенный ресурс") || response_string.contains("Internal Server Error") || response_string.contains("InternalServerError"))
                    return -1;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o.equals(-1))
                Toast.makeText(fragmentActivity, "Ошибка доступа по ссылке. Попробуйте ещё раз", Toast.LENGTH_SHORT).show();
            else {
                try {
                    if(response_string != null && response_string != "") {
                        if (Objects.equals(callType, "getTotal"))
                            HandleGetTotal(response_string);
                        else
                            HomeHandleResponse(response_string);
                    }else{
                        Toast.makeText(fragmentActivity, "Невозможно получить доступ к хранилищу", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}