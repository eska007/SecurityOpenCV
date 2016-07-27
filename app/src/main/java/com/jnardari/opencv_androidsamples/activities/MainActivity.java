package com.jnardari.opencv_androidsamples.activities;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jnardari.opencv_androidsamples.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private int                    overlaySize = 100;

    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    private Mat                    mOverlay;
    private long                   mCheckpoint;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //in order to receive string data from separate thread
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("MESSAGE");
            TextView textView = (TextView) findViewById(R.id.main_activity_text_view);
            textView.setText(message);
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mOverlay = new Mat(overlaySize, overlaySize, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        mOverlay.release();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Bitmap outputBitmap = Bitmap.createBitmap(mOverlay.width(), mOverlay.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mOverlay, outputBitmap);
        ImageView imageView1 = (ImageView) findViewById(R.id.main_activity_image_view);
        imageView1.setImageBitmap(outputBitmap);

        return super.onTouchEvent(event);
    }

    long mStartTime;
    long mEndTime;
    String mTimerName;

    private void startTimer(String name){
        mTimerName = name;
        mStartTime = System.nanoTime();
    }

    private void endTimer() {
        mEndTime = System.nanoTime();
        Log.i(TAG, mTimerName + " duration:" + ((mEndTime - mStartTime) / 1000000) + "ms");
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //calculate time for each frame to measure performance
        long currentTime = System.nanoTime();
        Log.i(TAG, "Camera frame period:" + ((currentTime - mCheckpoint) / 1000000) + "ms");
        mCheckpoint = currentTime;

        mRgba = inputFrame.rgba();

        //scale down by 1/4 for better performance
        Imgproc.pyrDown(mRgba, mIntermediateMat);
        Imgproc.pyrDown(mIntermediateMat, mIntermediateMat);

        //convert image from RGB to HSV
        startTimer("cvtColor");
        Imgproc.cvtColor(mIntermediateMat, mIntermediateMat, Imgproc.COLOR_RGB2HSV);
        endTimer();

        //detect hot pink color (H: 140 - 160)
        Core.inRange(mIntermediateMat, new Scalar(140, 100, 100), new Scalar(160, 255, 255), mIntermediateMat);

        //find shape
        List<MatOfPoint> contours = new ArrayList<>();
        startTimer("findContours");
        Imgproc.findContours(mIntermediateMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        endTimer();

        //pick the largest contour in the list of contours
        double maxArea = -1;
        for (int idx = 0; idx < contours.size(); idx++) {
            double area = Imgproc.contourArea(contours.get(idx));
            if (area > maxArea)
                maxArea = area;
        }

        //select only valid contours by comparing its area
        List<MatOfPoint> validContoursList = new ArrayList<>();
        for (int idx = 0; idx < contours.size(); idx++) {
            MatOfPoint contour = contours.get(idx);
            if (Imgproc.contourArea(contour) > 0.2 * maxArea) {
                //scale up by 4
                Core.multiply(contour, new Scalar(4,4), contour);
                validContoursList.add(contour);
            }
        }

        //the number of marker has to be 4
        if(validContoursList.size() != 4) return mRgba;

        Log.i(TAG, "The markers are detected.");

        //get the coordinates of center of each marker
        startTimer("coordinates");
        Point[] markers = new Point[4];
        for (int i = 0; i < validContoursList.size(); i++) {
            //Imgproc.drawContours(mRgba, validContoursList, idx, new Scalar(0, 0, 255), -1);
            MatOfPoint contour = validContoursList.get(i);
            Point[] points = contour.toArray();

            double sumX = 0.0, sumY = 0.0;
            //System.out.println("points.length: " + points.length);
            for( int j = 0 ; j < points.length ; j++ ) {
                sumX += points[j].x;
                sumY += points[j].y;
            }

            markers[i] = new Point(sumX / points.length, sumY / points.length);
            Imgproc.circle(mRgba, markers[i], 10, new Scalar(204, 255, 204), 6);
        }
        endTimer();

        //sort the markers in the order of TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT
        Point markersSorted[]  = new Point[4];
        double sumMin = 99999.9, sumMax = -99999.9;
        double subMin = 99999.9, subMax = -99999.9;
        for(int i = 0; i < markers.length; i++) {
            double sum = markers[i].x + markers[i].y;
            double sub = markers[i].x - markers[i].y;

            if(sum > sumMax) {
                markersSorted[2] = markers[i]; //BOTTOM_RIGHT
                sumMax = sum;
            }
            if(sum < sumMin) {
                markersSorted[0] = markers[i]; //TOP_LEFT
                sumMin = sum;
            }
            if(sub > subMax) {
                markersSorted[3] = markers[i]; //TOP_RIGHT
                subMax = sub;
            }
            if(sub < subMin) {
                markersSorted[1] = markers[i]; //BOTTOM_LEFT
                subMin = sub;
            }
        }

        //apply perspective transform for pixel recognition
        List<Point> src_pnt = new ArrayList<>();
        src_pnt.add(markersSorted[0]);
        src_pnt.add(markersSorted[1]);
        src_pnt.add(markersSorted[2]);
        src_pnt.add(markersSorted[3]);
        Mat startM = Converters.vector_Point2f_to_Mat(src_pnt);

        List<Point> dst_pnt = new ArrayList<>();
        dst_pnt.add(new Point(0, 0));
        dst_pnt.add(new Point(0, overlaySize));
        dst_pnt.add(new Point(overlaySize, overlaySize));
        dst_pnt.add(new Point(overlaySize, 0));
        Mat endM = Converters.vector_Point2f_to_Mat(dst_pnt);

        startTimer("warpPerspective");
        Mat transformMatrix = Imgproc.getPerspectiveTransform(startM, endM);
        Imgproc.warpPerspective(mRgba, mOverlay, transformMatrix, mOverlay.size());
        endTimer();

        double step = (double)overlaySize / 17;

        StringBuilder sb = new StringBuilder();
        for(int row = 1; row <= 16; row++) {
            for(int col = 1; col <= 16; col++) {
                double[] rgb = mOverlay.get((int) (row * step), (int) (col * step));
                sb.append((rgb[0] > 127.0) ? "1" : "0"); //read red component only for test purpose
            }
            sb.append("\n");
        }
        Log.i(TAG, sb.toString());

        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE", sb.toString());
        Message msg = handler.obtainMessage();
        msg.setData(bundle);
        handler.sendMessage(msg);

        return mRgba;
    }
}
