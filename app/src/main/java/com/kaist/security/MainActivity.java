package com.kaist.security;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "[SM]";
    private static String mPrevMessage = "";
    private static final int SQUARE_SIDE_LENGTH = 32;
    private static final int MESSAGE_LENGTH_IN_BYTE = SQUARE_SIDE_LENGTH * SQUARE_SIDE_LENGTH / 8;

    private static final int       VIEW_MODE_RECOGNITION  = 0;
    private static final int       VIEW_MODE_DEBUG        = 1;
    private int                    mViewMode;

    /*
     * The size of perspective transformed rectangle
     * Increasing this value will slow down the performance significantly.
     */
    private static final int OVERLAY_SIZE = 160;

    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private Mat mOverlay;
    private long mCheckpoint;
    private SecurityUtils su;
    private CameraBridgeViewBase mOpenCvCameraView;

    private MenuItem mItemRecognition;
    private MenuItem mItemDebug;
    private MediaPlayer mMediaPlayer;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        su = new SecurityUtils(this);
        //setGridColor();

        mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemRecognition = menu.add("Recognition");
        mItemDebug = menu.add("Debug");
        return true;
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
        mOverlay = new Mat(OVERLAY_SIZE, OVERLAY_SIZE, CvType.CV_8UC4);
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

    private void startTimer(String name) {
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
        //startTimer("cvtColor");
        Imgproc.cvtColor(mIntermediateMat, mIntermediateMat, Imgproc.COLOR_RGB2HSV);
        //endTimer();

        //detect hot pink color (H: 140 - 160)
        Core.inRange(mIntermediateMat, new Scalar(140, 100, 100), new Scalar(170, 255, 255), mIntermediateMat);

        if(mViewMode == VIEW_MODE_DEBUG) {
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA);
            Imgproc.pyrUp(mRgba, mRgba);
            Imgproc.pyrUp(mRgba, mRgba);
            return mRgba;
        }

        //find shape
        List<MatOfPoint> contours = new ArrayList<>();
        //startTimer("findContours");
        Imgproc.findContours(mIntermediateMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        //endTimer();

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
            //scale up by 4
            Core.multiply(contour, new Scalar(4, 4), contour);
            validContoursList.add(contour);
        }

        //the number of marker has to be 4
        if (validContoursList.size() != 4) return mRgba;

        Log.i(TAG, "The markers are detected.");

        //get the coordinates of center of each marker
        //startTimer("coordinates");
        Point[] markers = new Point[4];
        for (int i = 0; i < validContoursList.size(); i++) {
            //Imgproc.drawContours(mRgba, validContoursList, idx, new Scalar(0, 0, 255), -1);
            MatOfPoint contour = validContoursList.get(i);
            Point[] points = contour.toArray();

            double sumX = 0.0, sumY = 0.0;
            //System.out.println("points.length: " + points.length);
            for (int j = 0; j < points.length; j++) {
                sumX += points[j].x;
                sumY += points[j].y;
            }

            markers[i] = new Point(sumX / points.length, sumY / points.length);
            Imgproc.circle(mRgba, markers[i], 2, new Scalar(204, 255, 204), 2);
        }
        //endTimer();

        //sort the markers in the order of TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT
        Point markersSorted[] = new Point[4];
        double sumMin = 99999.9, sumMax = -99999.9;
        double subMin = 99999.9, subMax = -99999.9;
        for (int i = 0; i < markers.length; i++) {
            double sum = markers[i].x + markers[i].y;
            double sub = markers[i].x - markers[i].y;

            if (sum > sumMax) {
                markersSorted[2] = markers[i]; //BOTTOM_RIGHT
                sumMax = sum;
            }
            if (sum < sumMin) {
                markersSorted[0] = markers[i]; //TOP_LEFT
                sumMin = sum;
            }
            if (sub > subMax) {
                markersSorted[3] = markers[i]; //TOP_RIGHT
                subMax = sub;
            }
            if (sub < subMin) {
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
        dst_pnt.add(new Point(0, OVERLAY_SIZE));
        dst_pnt.add(new Point(OVERLAY_SIZE, OVERLAY_SIZE));
        dst_pnt.add(new Point(OVERLAY_SIZE, 0));
        Mat endM = Converters.vector_Point2f_to_Mat(dst_pnt);

        startTimer("warpPerspective");
        Mat transformMatrix = Imgproc.getPerspectiveTransform(startM, endM);
        Imgproc.warpPerspective(mRgba, mOverlay, transformMatrix, mOverlay.size());
        endTimer();

        double stepx = (double) OVERLAY_SIZE / (SQUARE_SIDE_LENGTH + 2);
        double stepy = (double) OVERLAY_SIZE / (SQUARE_SIDE_LENGTH + 1);
        byte pack = 0;
        int sumEachRow = 0;
        byte[] ciphertext = new byte[MESSAGE_LENGTH_IN_BYTE];
        StringBuilder sb = new StringBuilder();
        for (int y = 1, count = 0; y <= SQUARE_SIDE_LENGTH; y++) {
            for (int x = 1; x <= SQUARE_SIDE_LENGTH + 1; x++) {
                double[] rgb = mOverlay.get((int) (y * stepy), (int) (x * stepx));
                //1. bit-shift 처리 + 16 byte -> AES128 암호화
                //2. setGridPoint
                String ret = (rgb[0] > 127.0) ? "1" : "0";
                int bit = (rgb[0] > 127.0) ? 1 : 0;

                //parity check
                if(x == SQUARE_SIDE_LENGTH + 1) {
                    if((sumEachRow % 2) != bit) {
                        Log.d(TAG, "Parity check fail! (" + sumEachRow + " at " + y + "line)");
                        return mRgba;
                    }
                    sumEachRow = 0;
                    continue;
                }

                pack |= bit << ((x - 1)  % 8);
                sumEachRow += bit;

                if(x % 8 == 0) {
                    ciphertext[count++] = pack;
                    pack = 0;
                }

                sb.append(ret); //read red component only for test purpose
            }
            sb.append("\n");
        }
        //Log.i(TAG, sb.toString());

        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE_IN_STRING", sb.toString());
        bundle.putByteArray("MESSAGE_IN_BYTE", ciphertext);
        Message msg = handler.obtainMessage();
        msg.setData(bundle);
        handler.sendMessage(msg);

        return mRgba;
    }

    //in order to receive string data from separate thread
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("MESSAGE_IN_STRING");
            byte[] ciphertext = bundle.getByteArray("MESSAGE_IN_BYTE");

            //TextView textView = (TextView) findViewById(R.id.main_activity_text_view);
            //textView.setText(message);

            if (!message.equals(mPrevMessage)) {
                mPrevMessage = message;
                try {
                    //zero key is used.
                    byte[] secretKey = su.getPrivateKey();
                    byte[] decrypted = su.decrypt(secretKey, ciphertext);
                    mMediaPlayer.start();
                    setGridColor(decrypted);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    private void setGridColor(byte[] msgArr) {
        Log.i(TAG, "setGridColor()");
        GridLayout layout = (GridLayout) findViewById(R.id.GridView);
        layout.setVisibility(View.VISIBLE);

        startTimer("setGridColor");

        boolean existViewId;
        for (int i = 0; i < SQUARE_SIDE_LENGTH * SQUARE_SIDE_LENGTH; i++) {
            existViewId = false;
            TextView tv = (TextView) findViewById(i);

            if(tv == null) {
                tv = new TextView(this);
                existViewId = true;
            }

            tv.setHeight(10);
            tv.setWidth(10);
            tv.setId(i);

            int bit = 0x1 & (msgArr[i/8] >> (i%8));
            if (bit == 0) {
                tv.setBackgroundColor(Color.BLACK);
            } else if (bit == 1) {
                tv.setBackgroundColor(Color.WHITE);
            }

            if (existViewId) {
                layout.addView(tv);
            }
        }

        endTimer();
    }

    private void DeleteOverlay() {
        Log.i(TAG, "DeleteOverlay()");

        GridLayout layout = (GridLayout) findViewById(R.id.GridView);
        ((ViewManager) layout.getParent()).removeView(layout);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemRecognition) {
            mViewMode = VIEW_MODE_RECOGNITION;
        } else if (item == mItemDebug) {
            mViewMode = VIEW_MODE_DEBUG;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }
}
