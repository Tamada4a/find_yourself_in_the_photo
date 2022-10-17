package com.example.findyourselfinthephoto;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import generator.RandomUserAgentGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Здесь логика следующая:
 * 1) инициализировать CascadeClassifier
 *  1а) натренировать cascade свой
 * 2) передать в нейронку все фотки для обучения
 * 3) пробегаться по всем спаршеным ссылкам, делая запрос
 * 4) выделить лица с помощью OpenCV
 * 5) с помощью нейронки выделять похожие лица
 * */

public class GetRecognizedFaceUrl {

    private static CascadeClassifier cascadeClassifier;
    private static ArrayList<String> recognized_array = new ArrayList<String>();

    public static void compare_image(String img_1, ArrayList<String> UrlArray, Activity activity) {
        initialize(activity);

        for(int i = 0; i < UrlArray.size(); ++i){
            MyRequests requests = new MyRequests(UrlArray.get(i), img_1);
            requests.execute();
            /*OkHttpClient client = new OkHttpClient();
            String curUrl = UrlArray.get(i);

            Request request = new Request.Builder()
                    .url(curUrl)
                    .header("User-Agent", RandomUserAgentGenerator.getNextMobile())
                    .build();

            int finalI = i;
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println(finalI + " ВСё ХУЕВО");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    byte[] bytes = response.body().bytes();

                    Mat mat_1 = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);

                    MatOfRect faceDetections = new MatOfRect();
                    cascadeClassifier.detectMultiScale(mat_1, faceDetections);
                    System.out.println(finalI + " " + curUrl);

                    System.out.println(String.format("Detected %s faces",
                            faceDetections.toArray().length));

                    Mat mat_2 = Imgcodecs.imread(img_1);
                    Mat hist_1 = new Mat();
                    Mat hist_2 = new Mat();


                    MatOfFloat ranges = new MatOfFloat(0f, 256f);
                    MatOfInt histSize = new MatOfInt(1000);

                    Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
                    Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

                    double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

                    if(res > 0.6)
                        recognized_array.add(curUrl);

                    System.gc();
                }
            });*/
        }


        /*Mat mat_1 = Imgcodecs.imread(img_1);
        Mat mat_2 = Imgcodecs.imread(img_2);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();


        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(1000);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

        System.out.println(res);*/

        //return res > 0.6;
        /*System.out.println("size is: " + recognized_array.size());
        for(int i = 0; i < recognized_array.size(); ++ i)
            System.out.println("Recognized on: " + recognized_array.get(i));
        return recognized_array;*/
    }

    private static void initialize(Activity activity) {
        System.loadLibrary("opencv_java4");

        try {
            InputStream is = activity.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
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
                Log.e("CascadeClassifier", "Failed to load cascade classifier");
                cascadeClassifier = null;
            } else
                Log.i("CascadeClassifier", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CascadeClassifier", "Failed to load cascade. Exception thrown: " + e);
        }
    }



    private static class MyRequests extends AsyncTask {

        private final OkHttpClient client = new OkHttpClient();

        private String curUrl;
        private String img_1;

        public MyRequests(String url, String img_1){
            curUrl = url;
            this.img_1 = img_1;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Request request = new Request.Builder()
                    .url(curUrl)
                    .header("User-Agent", RandomUserAgentGenerator.getNextMobile())
                    .build();

            try {
                Response response = client.newCall(request).execute();

                byte[] bytes = response.body().bytes();

                Mat mat_1 = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);

                ArrayList<Mat> FacesArray = extractFaces(mat_1);

                if(!FacesArray.isEmpty()) {
                    Mat mat_2 = Imgcodecs.imread(img_1);//extractFaces().get(0);

                    for(int i = 0; i < FacesArray.size(); ++i) {

                        Mat src = FacesArray.get(i);

                        Mat hist_1 = new Mat();
                        Mat hist_2 = new Mat();

                        MatOfFloat ranges = new MatOfFloat(0f, 256f);
                        MatOfInt histSize = new MatOfInt(1000);

                        Imgproc.calcHist(Arrays.asList(src), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
                        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

                        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

                        if (res > 0.7) {
                            recognized_array.add(curUrl);
                            System.out.println("You 've been recognized with " + res + " here\n" + curUrl);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            System.out.println("\nYou 've been recognized on 4 photos");
        }

        private ArrayList<Mat> extractFaces(Mat image){
            ArrayList<Mat> detectedFace = new ArrayList<Mat>();

            MatOfRect faceDetections = new MatOfRect();
            cascadeClassifier.detectMultiScale(image, faceDetections);

            /*System.out.println(curUrl);
            System.out.println(String.format("Detected %s faces",
                    faceDetections.toArray().length));*/

            if(!faceDetections.empty())
                for (Rect rect : faceDetections.toArray()) {
                    Mat faceImage = image.submat(rect);
                    detectedFace.add(faceImage);
                }
            return detectedFace;
        }

    }
}
