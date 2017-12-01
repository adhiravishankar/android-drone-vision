package com.adhiravishankar.dronecomputervision;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by adhi on 11/30/17.
 */

public class FdFileActivity2 extends Activity {

    FaceDetector detector;
    FaceView faceView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_view);

        faceView = findViewById(R.id.faceView);

        detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                long start = System.currentTimeMillis();
                FdFileActivity2.this.run(start);
               handler.postDelayed(this, delay);
            }
        }, delay);
    }


    void run(final long start) {
        // FutureTarget<Bitmap> futureTarget =
        //        Glide.with(this)
        //                .load("http://192.168.29.203:8000/latest/")
        //                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

        //Bitmap bitmap = onReceiveFrame(futureTarget.get());

        //Glide.with(this).load(bitmap).into(imageView);

        Glide.with(this).asBitmap()
                .load("http://192.168.29.203:8000/latest/")
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(new SimpleTarget<Bitmap>() {
                    /**
                     * The method that will be called when the resource load has finished.
                     *
                     * @param resource   the loaded resource.
                     * @param transition
                     */
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (detector.isOperational()) {
                            // Create a frame from the bitmap and run face detection on the frame.
                            Frame frame = new Frame.Builder().setBitmap(resource).build();
                            SparseArray<Face> faces = detector.detect(frame);
                            faceView.setContent(resource, faces);
                            long stop = System.currentTimeMillis();
                            System.out.print(faces.size());
                            System.out.print(",");
                            System.out.print(start);
                            System.out.print(",");
                            System.out.print(stop);
                            System.out.print(",");
                            System.out.println(stop - start);
                        }
                    }
                });

    }
}
