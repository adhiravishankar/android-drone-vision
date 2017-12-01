package com.adhiravishankar.dronecomputervision;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Created by adhi on 11/30/17.
 */

public class FaceDetectionTransformation extends BitmapTransformation {

    private float mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize   = 0;
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private CascadeClassifier mJavaDetector;

    public FaceDetectionTransformation(CascadeClassifier mJavaDetector) {
        this.mJavaDetector = mJavaDetector;
    }


    @Override protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                                         @NonNull Bitmap bitmap, int outWidth, int outHeight) {

        Mat inputMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, inputMat);
        Mat grayMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGB2GRAY);


        if (mAbsoluteFaceSize == 0) {
            int height = grayMat.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(grayMat, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (Rect aFacesArray : facesArray)
            Imgproc.rectangle(inputMat, aFacesArray.tl(), aFacesArray.br(), FACE_RECT_COLOR, 3);

        System.out.println("Finished processing.");
        Utils.matToBitmap(inputMat, bitmap);
        return bitmap;
    }

    @Override public String key() {
        return "FaceDetectionTransformation";
    }
}
