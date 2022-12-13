package com.example.findyourselfinthephoto.Helpers;

import static android.content.Context.MODE_PRIVATE;

import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.resize;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.findyourselfinthephoto.R;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/*
* Класс, отвечающий за всякие проверки с фотографиями
* */
public class PhotoHelper {

    private final String TAG = "GetRecognizedFaceUrl";

    private CascadeClassifier _cascadeClassifier;


    public PhotoHelper(Activity homeActivity){
        initializeCascadeClassifier(homeActivity);
    }


    public PhotoHelper(CascadeClassifier cascadeClassifier){
        _cascadeClassifier = cascadeClassifier;
    }


    public boolean isContainsFace(String path){
        Mat matImage = Imgcodecs.imread(path);

        Mat resizedImage = resizeImage(matImage, 2500.0, 2500.0);

        MatOfRect faceDetections = new MatOfRect();
        _cascadeClassifier.detectMultiScale(resizedImage, faceDetections);

        return !faceDetections.empty();
    }


    @NonNull
    public ArrayList<Mat> extractFaces(Mat image) {
        ArrayList<Mat> detectedFace = new ArrayList<Mat>();

        Mat img = resizeImage(image, 2500.0, 2500.0);

        MatOfRect faceDetections = new MatOfRect();
        _cascadeClassifier.detectMultiScale(img, faceDetections);

        if (!faceDetections.empty()) {
            for (Rect rect : faceDetections.toArray()) {
                Mat faceImage = img.submat(rect);
                detectedFace.add(faceImage);
            }
        }
        return detectedFace;
    }


    public Mat resizeImage(@NonNull Mat inputImg, double width, double height){
        Mat newImg = inputImg;

        if(inputImg.size().height > height || inputImg.size().width > width){

            double max = 0;

            double inputHeight = inputImg.size().height;
            double inputWidth = inputImg.size().width;

            if(inputHeight > inputWidth)
                max = inputHeight;
            else
                max = inputWidth;

            double k = max / width;

            double newHeight = inputHeight / k;
            double newWidth = inputWidth / k;

            Size scaleSize = new Size(newWidth, newHeight);

            resize(inputImg, newImg, scaleSize , 0, 0, INTER_AREA);
        }
        return newImg;
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

            _cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (_cascadeClassifier.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                _cascadeClassifier = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }
}
