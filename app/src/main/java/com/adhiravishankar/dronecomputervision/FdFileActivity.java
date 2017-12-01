package com.adhiravishankar.dronecomputervision;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * Created by adhi on 11/30/17.
 */

public class FdFileActivity extends Activity {

    private static final String    TAG                 = "OCVSample::Activity";
    private CascadeClassifier mJavaDetector;
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private float mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize   = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");


                    try {
                        load_xml(R.raw.lbpcascade_frontalface, "lbpcascade_frontalface.xml");



                        final Handler handler = new Handler();
                        final int delay = 3000; //milliseconds

                        handler.postDelayed(new Runnable(){
                            public void run(){
                                //do something
                                try {
                                    FdFileActivity.this.run();
                                } catch (IOException | ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                handler.postDelayed(this, delay);
                            }
                        }, delay);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void load_xml(int resource, String file) throws IOException {
        // load cascade file from application resources
        InputStream is = getResources().openRawResource(resource);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, file);
        FileOutputStream os = new FileOutputStream(mCascadeFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();

        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        if (mJavaDetector.empty()) {
            Log.e(TAG, "Failed to load cascade classifier");
            mJavaDetector = null;
        } else
            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

        cascadeDir.delete();
    }


    void run() throws IOException, ExecutionException, InterruptedException {
        // FutureTarget<Bitmap> futureTarget =
        //        Glide.with(this)
        //                .load("http://192.168.29.203:8000/latest/")
        //                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

        //Bitmap bitmap = onReceiveFrame(futureTarget.get());

        ImageView imageView = findViewById(R.id.imageView);
        //Glide.with(this).load(bitmap).into(imageView);

        Glide.with(this).load("http://192.168.29.203:8000/latest/")
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.bitmapTransform(new FaceDetectionTransformation(mJavaDetector)))
                .into(imageView);

    }

     Bitmap onReceiveFrame(Bitmap bitmap) {
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
}
