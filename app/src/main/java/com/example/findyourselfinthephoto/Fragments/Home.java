package com.example.findyourselfinthephoto.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.findyourselfinthephoto.GetRecognizedFaceUrl;
import com.example.findyourselfinthephoto.MyJson;
import com.example.findyourselfinthephoto.R;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;

import br.com.onimur.handlepathoz.HandlePathOz;
import br.com.onimur.handlepathoz.HandlePathOzListener;
import br.com.onimur.handlepathoz.model.PathOz;
import generator.RandomUserAgentGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Home extends Fragment implements HandlePathOzListener.SingleUri {

    private FragmentActivity activity;

    private ImageView upload_button;
    private ImageButton start_button;
    private EditText UrlField;

    private String realPath = "";
    private HandlePathOz handlePathOz;

    private String gettedURL;
    private String response = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
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
                gettedURL = UrlField.getText().toString();

                MyRequests request = new MyRequests(gettedURL, activity);
                request.execute();
            }
        });

        return view;
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityIntent.launch(Intent.createChooser(galleryIntent,"Select Picture"));
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();

                        if(data.getClipData() != null){
                            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        }
                        else if (data.getData() != null) {
                            Uri ImageUri = data.getData();
                            upload_button.setImageURI(ImageUri);
                            handlePathOz.getRealPath(ImageUri);
                        }
                    }
                }
            });

    @Override
    public void onRequestHandlePathOz(@NonNull PathOz pathOz, @Nullable Throwable throwable) {
        realPath = pathOz.getPath();
    }

    private void HomeHandleResponse(String response) throws ParseException {
        this.response = response;
        System.out.println("RESPONSE IS " + response);

        ArrayList<ArrayList<String>> result = MyJson.getUrlArray(response, "YandexDisk");

        int size = result.get(0).size();
        for (int i = 0; i < size; ++i)
            System.out.println("Ссылка №" + i + "\n" + result.get(0).get(i) + "\n" + result.get(1).get(i));

        GetRecognizedFaceUrl.compare_image(realPath, result, activity);
    }

    private class MyRequests extends AsyncTask {
        private final OkHttpClient client = new OkHttpClient();

        private String url;
        private final String api_url = "https://cloud-api.yandex.net/v1/disk/public/resources?public_key=";

        private String response_string;

        private FragmentActivity fragmentActivity;


        public MyRequests(String url_for_request, FragmentActivity activity) {
            this.url = url_for_request;
            this.fragmentActivity = activity;
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

                if (response_string.contains("обновите браузер") || response_string.contains("Ресурс не найден") || response_string.isEmpty())
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
                    HomeHandleResponse(response_string);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}