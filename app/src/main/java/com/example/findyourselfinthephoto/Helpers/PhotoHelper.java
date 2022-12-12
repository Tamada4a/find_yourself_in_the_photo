package com.example.findyourselfinthephoto.Helpers;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.findyourselfinthephoto.R;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
* Класс, отвечающий за всякие проверки с фотографиями
* */
public class PhotoHelper {

    private final String TAG = "GetRecognizedFaceUrl";

    private CascadeClassifier cascadeClassifier;


    public PhotoHelper(Activity homeActivity){
        initializeCascadeClassifier(homeActivity);
    }


    public boolean isContainsFace(String path){
        Mat matImage = Imgcodecs.imread(path);

        MatOfRect faceDetections = new MatOfRect();
        cascadeClassifier.detectMultiScale(matImage, faceDetections);

        return !faceDetections.empty();
    }


    private void initializeCascadeClassifier(@NonNull Activity activity) {

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
}
