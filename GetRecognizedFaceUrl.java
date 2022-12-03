package com.example.findyourselfinthephoto;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;


import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import generator.RandomUserAgentGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Здесь логика следующая:
 * 1) инициализировать CascadeClassifier
 *  1а) натренировать cascade свой
 * 2) передать в FaceRecognizer все фотки для обучения
 * 4) выделить по ссылкам лица с помощью OpenCV
 * 5) найти похожие лица
 * */

public class GetRecognizedFaceUrl {

    private static final String TAG = "GetRecognizedFaceUrl";

    public static final double ACCEPT_LEVEL = 4000.0D;

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor save;

    private static String gettedURL;
    public static Set<String> setValues;// сет, состоящий из найденных фоток, где ключ это значение setKey
    //private static FaceRecognizer faceRecognizer;

    private static CascadeClassifier cascadeClassifier;
    private static ArrayList<ArrayList> recognized_array;

    public static void compare_image(ArrayList<String> pathList, ArrayList<ArrayList<String>> UrlArray, Activity activity, String getTedURL) {
        initializeCascadeClassifier(activity);
        //loadDL4J();
        //train(pathList);
        preferences = activity.getSharedPreferences("Link", MODE_PRIVATE);
        save = preferences.edit();
        gettedURL = getTedURL;
        MyRequests requests = new MyRequests(UrlArray, pathList);
        requests.execute();
    }

    private static void initializeCascadeClassifier(Activity activity) {
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

    private static void train(ArrayList<String> pathList) {
        /*int size = pathList.size();

        MatVector images = new MatVector(size);

        Mat labels = new Mat(size, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        for (int i = 0; i < size; ++i){
            Mat img = imread(pathList.get(i), IMREAD_GRAYSCALE);
            org.opencv.core.Mat img2 = Imgcodecs.imread(pathList.get(i));
            System.out.println("TEST " + img + "\n" + img2);
            images.put(i, img);

            labelsBuf.put(i, i);
        }

        String path = "/storage/emulated/0/Android/data/com.example.findyourselfinthephoto/files/Temp/Screenshot_12.png.webp";
        Mat testImage = imread(path, IMREAD_GRAYSCALE);
        System.out.println("TestMat is " + testImage);*/
        /*faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.train(images, labels);
        recognize(pathList);*/
        /*ArrayList<Mat> images = new ArrayList<>();
        int[] labels = new int[size];
        Mat grayImg = new Mat();
        for (int i = 0; i < size; ++i){
            Mat img = Imgcodecs.imread(pathList.get(i));

            int label = Integer.parseInt(pathList.get(i).split("\\-")[0]);

            grayImg.create(img.width(), img.height(),1);
            Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);

            images.add(grayImg);

            labels[i] = i;
        }
        MatOfInt intLabels = new MatOfInt();
        intLabels.fromArray(labels);
        faceRecognizer.train(images, intLabels);
        */


    }

    private static void recognize(ArrayList<String> pathList) {
        /*String path = "/storage/emulated/0/Android/data/com.example.findyourselfinthephoto/files/Temp/Screenshot_12.png.webp";
        Mat testImage = imread(path, IMREAD_GRAYSCALE);
        //int predictedLabel = faceRecognizer.predict_label(testImage);
        int[] label  = new int[1];
        double[] reliability = new double[1];
        faceRecognizer.predict(testImage, label, reliability);
        int prediction = label[0];
        double acceptanceLevel = reliability[0];

        String[] names = {"", "Y Know You"};
        String name;

        if (prediction == 0 *//*-1*//* || acceptanceLevel >= ACCEPT_LEVEL)
            System.out.println("U were recognized: unknown");*/

        //System.out.println("PREDICTED LABEL IS " + pathList.get(prediction) + "   " + acceptanceLevel);
    }

    private static class MyRequests extends AsyncTask {

        private final OkHttpClient client = new OkHttpClient();

        private ArrayList<ArrayList<String>> UrlArray;
        private ArrayList<String> pathList;
        private boolean isRecognized = false;

        public MyRequests(ArrayList<ArrayList<String>> urlArr, ArrayList<String> pathList) {
            recognized_array = new ArrayList<>();
            recognized_array.add(new ArrayList<>());
            recognized_array.add(new ArrayList<>());

            setValues = new HashSet<String>();

            UrlArray = urlArr;
            this.pathList = pathList;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            for (int i = 0; i < UrlArray.get(0).size(); ++i) {
                String curUrl = UrlArray.get(0).get(i);
                String curDonwloadUrl = UrlArray.get(1).get(i);

                Request request = new Request.Builder()
                        .url(curUrl)
                        .header("User-Agent", RandomUserAgentGenerator.getNextMobile())
                        .build();

                try {
                    Response response = client.newCall(request).execute();

                    byte[] bytes = response.body().bytes();

                    Mat mat_1 = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);

                    ArrayList<Mat> FacesArray = extractFaces(mat_1);

                    if (!FacesArray.isEmpty()) {
                        isRecognized = false;
                        for(int a = 0; a < pathList.size() && !isRecognized; ++a) {
                            Mat mat_2 = Imgcodecs.imread(pathList.get(a));//extractFaces().get(0);

                            for (int j = 0; j < FacesArray.size(); ++j) {

                                Mat src = FacesArray.get(j);

                                Mat hist_1 = new Mat();
                                Mat hist_2 = new Mat();

                                MatOfFloat ranges = new MatOfFloat(0f, 256f);
                                MatOfInt histSize = new MatOfInt(1000);

                                Imgproc.calcHist(Arrays.asList(src), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
                                Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

                                double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);

                                if (res > 0.7) {
                                    recognized_array.get(0).add(curUrl);
                                    recognized_array.get(1).add(curDonwloadUrl);
                                    System.out.println("You 've been recognized with " + res + " here\n" + curUrl);
                                    isRecognized = true;
                                    break;
                                }
                            }
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

            ArrayList<String> temp = recognized_array.get(0);
            for(int i = 0; i < recognized_array.get(0).size(); ++i){
                setValues.add(temp.get(i));
            }
            save.putStringSet(gettedURL + '\n' + new Date().toString(), setValues);
            save.apply();

            System.out.println("\nYou 've been recognized on " + recognized_array.get(0).size() + " photos");
        }

        private ArrayList<Mat> extractFaces(Mat image) {
            ArrayList<Mat> detectedFace = new ArrayList<Mat>();

            MatOfRect faceDetections = new MatOfRect();
            cascadeClassifier.detectMultiScale(image, faceDetections);

            if (!faceDetections.empty())
                for (Rect rect : faceDetections.toArray()) {
                    Mat faceImage = image.submat(rect);
                    detectedFace.add(faceImage);
                }
            return detectedFace;
        }


    }
}

