package com.example.findyourselfinthephoto;

import static android.content.Context.MODE_PRIVATE;

import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.resize;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.findyourselfinthephoto.Helpers.PhotoHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.face.EigenFaceRecognizer;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generator.RandomUserAgentGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
* Класс, отвечающий за распознавание лиц с диска
* */
public class GetRecognizedFaceUrl {

    private static final String TAG = "GetRecognizedFaceUrl";

    private static final double ACCEPT_LEVEL = 4000.0D;

    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    private static double min_size;

    private static String gettedURL;
    public static Set<String> setValues;

    private static CascadeClassifier cascadeClassifier;
    private static ArrayList<ArrayList> recognized_array;
    private static FaceRecognizer faceRecognizer;

    private static PhotoHelper photoHelper;


    public static void compare_image(ArrayList<String> pathList, ArrayList<ArrayList<String>> UrlArray, Activity activity, String getTedURL) {
        initializeCascadeClassifier(activity);
        photoHelper = new PhotoHelper(cascadeClassifier);

        train(pathList);

        sharedPref = activity.getPreferences(MODE_PRIVATE);
        editor = sharedPref.edit();

        gettedURL = getTedURL;

        MyRequests requests = new MyRequests(UrlArray, pathList, activity);
        requests.execute();
    }

    private static void initializeCascadeClassifier(@NonNull Activity activity) {
        System.loadLibrary("opencv_java4");

        try {
            InputStream is = activity.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = activity.getDir("cascade", MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (cascadeClassifier.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                cascadeClassifier = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    private static void train(@NonNull ArrayList<String> pathList) {

        int size = pathList.size();

        List<Mat> list = new ArrayList<Mat>(size);
        int[] labels = new int[size];

        ArrayList<Mat> croppedList = new ArrayList<Mat>();


        //Выделяем лицо на фотке
        for(int i = 0; i < size; ++i){
            Mat img = Imgcodecs.imread(pathList.get(i));
            Mat croppedImg = photoHelper.extractFaces(img).get(0);
            croppedList.add(croppedImg);
        }


        //Находим самый минимальный размер, чтоб потом все фотки привести к одному размеру
        min_size = croppedList.get(0).size().width;
        for(int i = 1; i < size; ++i){
            double curWidth = croppedList.get(i).size().width;
            if(curWidth < min_size)
                min_size = curWidth;
        }


        //Приводим к одному размеру и добавляем к нашему списку данных для тренировки
        for(int i = 0; i < size; ++i){
            Mat img = new Mat();

            Size scaleSize = new Size(min_size, min_size);
            resize(croppedList.get(i), img, scaleSize , 0, 0, INTER_AREA);

            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
            list.add(img);
            labels[i] = i;
        }

        MatOfInt labels1 = new MatOfInt();
        labels1.fromArray(labels);
        faceRecognizer = EigenFaceRecognizer.create();
        faceRecognizer.train(list, labels1);
    }


    private static class MyRequests extends AsyncTask {

        private final OkHttpClient client = new OkHttpClient();

        private ArrayList<ArrayList<String>> UrlArray;
        private ArrayList<String> pathList;
        private boolean isRecognized = false;
        private Activity _activity;


        public MyRequests(ArrayList<ArrayList<String>> urlArr, ArrayList<String> pathList, Activity activity) {
            recognized_array = new ArrayList<>();
            recognized_array.add(new ArrayList<String>()); //preview url
            recognized_array.add(new ArrayList<String>()); //download url
            recognized_array.add(new ArrayList<byte[]>()); //bytes preview
            recognized_array.add(new ArrayList<String>()); //file name

            setValues = new HashSet<String>();

            UrlArray = urlArr;
            this.pathList = pathList;
            _activity = activity;
        }


        @NonNull
        @Override
        protected Object doInBackground(Object[] objects) {

            for (int i = 0; i < UrlArray.get(0).size(); ++i) {
                String curUrl = UrlArray.get(0).get(i);
                String curDownloadUrl = UrlArray.get(1).get(i);
                String curName = UrlArray.get(2).get(i);

                Request request = new Request.Builder()
                        .url(curUrl)
                        .header("User-Agent", RandomUserAgentGenerator.getNextMobile())
                        .build();

                try {
                    Response response = client.newCall(request).execute();

                    byte[] bytes = response.body().bytes();

                    Mat inputMat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);

                    ArrayList<Mat> FacesArray = photoHelper.extractFaces(inputMat);

                    if(!FacesArray.isEmpty()){
                        isRecognized = false;

                        for(int j = 0; j < FacesArray.size() && !isRecognized; ++j){
                            Mat temp = FacesArray.get(j);

                            Mat curInputMat = new Mat();

                            Size scaleSize = new Size(min_size, min_size);
                            resize(temp, curInputMat, scaleSize , 0, 0, INTER_AREA);

                            Imgproc.cvtColor(curInputMat, curInputMat, Imgproc.COLOR_BGR2GRAY);

                            int[] label  = new int[1];
                            double[] reliability = new double[1];

                            faceRecognizer.predict(curInputMat, label, reliability);

                            int predictedlabel = label[0];
                            double acceptanceLevel = reliability[0];

                            if(predictedlabel != -1 && (acceptanceLevel > ACCEPT_LEVEL || acceptanceLevel == 0.0)){
                                recognized_array.get(0).add(curUrl);
                                recognized_array.get(1).add(curDownloadUrl);
                                recognized_array.get(2).add(bytes);
                                recognized_array.get(3).add(curName);
                                isRecognized = true;
                                System.out.println("You 've been recognized with " + acceptanceLevel + " here\n" + curUrl);
                            }else
                                System.out.println("\n"+curUrl + "\n" + acceptanceLevel +"\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 1;
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            Type stringType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();

            //ссылки на превью
            ArrayList<ArrayList<String>> previewUrls = new ArrayList<ArrayList<String>>();
            if(sharedPref.contains("previewLinks")){
                String jsonPreviewLinks = sharedPref.getString("previewLinks", "");
                previewUrls = new Gson().fromJson(jsonPreviewLinks, stringType);
            }
            previewUrls.add(recognized_array.get(0));

            //ссылки на скачивание
            ArrayList<ArrayList<String>> download = new ArrayList<ArrayList<String>>();
            if(sharedPref.contains("download")){
                String jsonDownload = sharedPref.getString("download", "");
                download = new Gson().fromJson(jsonDownload, stringType);
            }
            download.add(recognized_array.get(1));

            //картинки для превью
            ArrayList<ArrayList<byte[]>> previewBytes = new ArrayList<ArrayList<byte[]>>();
            if(sharedPref.contains("previewBytes")){
                Type previewItemsType = new TypeToken<ArrayList<ArrayList<byte[]>>>() {}.getType();
                String jsonPreview = sharedPref.getString("previewBytes", "");
                previewBytes = new Gson().fromJson(jsonPreview, previewItemsType);
            }
            previewBytes.add(recognized_array.get(2));

            //имена
            ArrayList<ArrayList<String>> names = new ArrayList<ArrayList<String>>();
            if(sharedPref.contains("names")){
                String jsonNames = sharedPref.getString("names", "");
                names = new Gson().fromJson(jsonNames, stringType);
            }
            names.add(recognized_array.get(3));

            //ключи (текущая ссылка + дата)
            ArrayList<String> keys = new ArrayList<String>();
            if(sharedPref.contains("keys")){
                Type keysItemsType = new TypeToken<ArrayList<String>>() {}.getType();
                String jsonKeys = sharedPref.getString("keys", "");
                keys = new Gson().fromJson(jsonKeys, keysItemsType);
            }
            keys.add(gettedURL + '\n' + new Date().toString());


            String jsonPreviewUrlArray = new Gson().toJson(previewUrls);
            String jsonPreviewArray = new Gson().toJson(previewBytes);
            String jsonDownloadArray = new Gson().toJson(download);
            String jsonNamesArray = new Gson().toJson(names);
            String jsonKeysArray = new Gson().toJson(keys);

            editor.putString("previewLinks", jsonPreviewUrlArray);
            editor.putString("download", jsonDownloadArray);
            editor.putString("previewBytes", jsonPreviewArray);
            editor.putString("names", jsonNamesArray);
            editor.putString("keys", jsonKeysArray);
            editor.apply();

            Toast.makeText(_activity, "Вы были распознаны на " + recognized_array.get(0).size() + " фото!", Toast.LENGTH_SHORT).show();
            System.out.println("\nYou 've been recognized on " + recognized_array.get(0).size() + " photos");
        }
    }
}

