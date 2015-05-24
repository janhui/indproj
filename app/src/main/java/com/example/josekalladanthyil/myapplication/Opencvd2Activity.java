package com.example.josekalladanthyil.myapplication;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Opencvd2Activity extends Activity implements CvCameraViewListener {

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HOUGHCIRCLES = 1;
    public static final int VIEW_MODE_HOUGHLINES = 2;
    public static final int VIEW_MODE_CANNY = 3;
    public static final int VIEW_MODE_COLCONTOUR = 4;
    public static final int VIEW_MODE_OPFLOW = 8;


    public static int viewMode = VIEW_MODE_RGBA;

    private CascadeClassifier mCascade;

    private boolean bDisplayTitle = true;

    private byte[] byteColourTrackCentreHue;

    private double d, dTextScaleFactor, x1, x2, y1, y2;

    private double[] vecHoughLines;

    private Point pt, pt1, pt2;

    private int x, y, radius, iMinRadius, iMaxRadius, iCannyLowerThreshold,
            iCannyUpperThreshold, iAccumulator, iLineThickness = 3,
            iHoughLinesThreshold = 50, iHoughLinesMinLineSize = 20,
            iHoughLinesGap = 20, iNumberOfCameras = 0, iGFFTMax = 40,
            iContourAreaMin = 1000;

    private JavaCameraView mOpenCvCameraView0;
    private JavaCameraView mOpenCvCameraView1;

    private List<Byte> byteStatus;
    private List<Integer> iHueMap, channels;
    private List<Float> ranges;
    private List<Point> cornersThis, cornersPrev;
    private List<MatOfPoint> contours;

    private long lFrameCount = 0, lMilliStart = 0, lMilliNow = 0, lMilliShotTime = 0;

    private Mat mRgba, mGray, mIntermediateMat, mMatRed, mMatGreen, mMatBlue, mROIMat,
            mMatRedInv, mMatGreenInv, mMatBlueInv, mHSVMat, mErodeKernel, mContours,
            lines, matOpFlowPrev, matOpFlowThis;

    private MatOfFloat mMOFerr;
    private MatOfRect faces;
    private MatOfByte mMOBStatus;
    private MatOfPoint2f mMOP2f1, mMOP2f2, mMOP2fptsPrev, mMOP2fptsThis, mMOP2fptsSafe;
    private MatOfPoint2f mApproxContour;
    private MatOfPoint MOPcorners;


    private Scalar colorRed, colorGreen;
    private Size sSize3, sSize5, sMatSize;
    private String string;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView0.enableView();

                    if (iNumberOfCameras > 1)
                        mOpenCvCameraView1.enableView();

                    try {
                        // DO FACE CASCADE SETUP

                        Context context = getApplicationContext();
                        InputStream is3 = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                        File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

                        FileOutputStream os = new FileOutputStream(cascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = is3.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        is3.close();
                        os.close();

                        mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());

                        if (mCascade.empty()) {
                            //Log.d(TAG, "Failed to load cascade classifier");
                            mCascade = null;
                        }

                        cascadeFile.delete();
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        // Log.d(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iNumberOfCameras = Camera.getNumberOfCameras();

        //Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.opencvd2);

        mOpenCvCameraView0 = (JavaCameraView) findViewById(R.id.java_surface_view0);

        if (iNumberOfCameras > 1)
            mOpenCvCameraView1 = (JavaCameraView) findViewById(R.id.java_surface_view1);

        mOpenCvCameraView0.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView0.setCvCameraViewListener(this);

        mOpenCvCameraView0.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (iNumberOfCameras > 1) {
            mOpenCvCameraView1.setVisibility(SurfaceView.GONE);
            mOpenCvCameraView1.setCvCameraViewListener(this);
            mOpenCvCameraView1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView0 != null)
            mOpenCvCameraView0.disableView();
        if (iNumberOfCameras > 1)
            if (mOpenCvCameraView1 != null)
                mOpenCvCameraView1.disableView();
    }


    public void onResume() {
        super.onResume();

        viewMode = VIEW_MODE_RGBA;

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView0 != null)
            mOpenCvCameraView0.disableView();
        if (iNumberOfCameras > 1)
            if (mOpenCvCameraView1 != null)
                mOpenCvCameraView1.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opencvd2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            Intent myIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.barrythomas.co.uk/machinevision.html"));
            startActivity(myIntent1);
        } else if (item.getItemId() == R.id.action_rgbpreview) {
            viewMode = VIEW_MODE_RGBA;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_cannyedges) {
            viewMode = VIEW_MODE_CANNY;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_houghcircles) {
            viewMode = VIEW_MODE_HOUGHCIRCLES;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_houghlines) {
            viewMode = VIEW_MODE_HOUGHLINES;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_colourcontour) {
            viewMode = VIEW_MODE_COLCONTOUR;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_opflow) {
            viewMode = VIEW_MODE_OPFLOW;
            lFrameCount = 0;
            lMilliStart = 0;
            } else
                Toast.makeText(getApplicationContext(), "Sadly, your device does not have a second camera",
                        Toast.LENGTH_LONG).show();
//        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // TODO Auto-generated method stub
        byteColourTrackCentreHue = new byte[3];
        // green = 60 // mid yellow  27
        byteColourTrackCentreHue[0] = 27;
        byteColourTrackCentreHue[1] = 100;
        byteColourTrackCentreHue[2] = (byte) 255;
        byteStatus = new ArrayList<Byte>();

        channels = new ArrayList<Integer>();
        channels.add(0);
        colorRed = new Scalar(255, 0, 0, 255);
        colorGreen = new Scalar(0, 255, 0, 255);
        contours = new ArrayList<MatOfPoint>();
//        corners = new ArrayList<Point>();
        cornersThis = new ArrayList<Point>();
        cornersPrev = new ArrayList<Point>();

        faces = new MatOfRect();

//        histSize = new MatOfInt(25);

        iHueMap = new ArrayList<Integer>();
        iHueMap.add(0);
        iHueMap.add(0);
        lines = new Mat();

        mApproxContour = new MatOfPoint2f();
        mContours = new Mat();
//        mHist = new Mat();
        mGray = new Mat();
        mHSVMat = new Mat();
        mIntermediateMat = new Mat();
        mMatRed = new Mat();
        mMatGreen = new Mat();
        mMatBlue = new Mat();
        mMatRedInv = new Mat();
        mMatGreenInv = new Mat();
        mMatBlueInv = new Mat();
//        MOIone = new MatOfInt(0);

//        MOFrange = new MatOfFloat(0f, 256f);
        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();
        mMOP2fptsPrev = new MatOfPoint2f();
        mMOP2fptsThis = new MatOfPoint2f();
        mMOP2fptsSafe = new MatOfPoint2f();
        mMOFerr = new MatOfFloat();
        mMOBStatus = new MatOfByte();
        MOPcorners = new MatOfPoint();
        mRgba = new Mat();
        mROIMat = new Mat();
//        mFaceDest = new Mat();
//        mFaceResized = new Mat();
//        matFaceHistogramPrevious = new Mat();
//        matFaceHistogramThis = new Mat();
        matOpFlowThis = new Mat();
        matOpFlowPrev = new Mat();

        pt = new Point(0, 0);
        pt1 = new Point(0, 0);
        pt2 = new Point(0, 0);

//        pts = new ArrayList<Point>();

        ranges = new ArrayList<Float>();
        ranges.add(50.0f);
        ranges.add(256.0f);
//        rect = new Rect();
//        rDest = new Rect();

        sMatSize = new Size();
//        sSize = new Size();
        sSize3 = new Size(3, 3);
        sSize5 = new Size(5, 5);

        string = "";

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        dTextScaleFactor = ((double) densityDpi / 240.0) * 0.9;

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);


    }

    @Override
    public void onCameraViewStopped() {
        releaseMats();
    }

    public void releaseMats() {
        mRgba.release();
        mIntermediateMat.release();
        mGray.release();
        mMatRed.release();
        mMatGreen.release();
        mMatBlue.release();
        mROIMat.release();
        mMatRedInv.release();
        mMatGreenInv.release();
        mMatBlueInv.release();
        mHSVMat.release();
        mErodeKernel.release();
        mContours.release();
        lines.release();
        faces.release();
        MOPcorners.release();
        mMOP2f1.release();
        mMOP2f2.release();
        mApproxContour.release();

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        iMinRadius = 20;
        iMaxRadius = 400;
        iCannyLowerThreshold = 50;
        iCannyUpperThreshold = 180;
        iAccumulator = 300;
        mErodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, sSize3);

        // start the timing counter to put the framerate on screen
        // and make sure the start time is up to date, do
        // a reset every 10 seconds
        if (lMilliStart == 0)
            lMilliStart = System.currentTimeMillis();

        if ((lMilliNow - lMilliStart) > 10000) {
            lMilliStart = System.currentTimeMillis();
            lFrameCount = 0;
        }

        inputFrame.copyTo(mRgba);
        sMatSize.width = mRgba.width();
        sMatSize.height = mRgba.height();

        switch (viewMode) {

            case VIEW_MODE_RGBA:

                if (bDisplayTitle)
                    ShowTitle("BGR Preview", 1, colorGreen);

                break;


            case VIEW_MODE_CANNY:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);

                iCannyLowerThreshold = 35;
                iCannyUpperThreshold = 75;

                Imgproc.Canny(mGray, mIntermediateMat, iCannyLowerThreshold, iCannyUpperThreshold);

                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);

                if (bDisplayTitle)
                    ShowTitle("Canny Edges", 1, colorGreen);

                break;

            case VIEW_MODE_HOUGHCIRCLES:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);

                // the lower this figure the more spurious circles you get
                // 50 looks good in CANNY, but 100 is better when converting that into Hough circles
                iCannyUpperThreshold = 100;

                Imgproc.HoughCircles(mGray, mIntermediateMat, Imgproc.CV_HOUGH_GRADIENT, 2.0, mGray.rows() / 8,
                        iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);

                if (mIntermediateMat.cols() > 0)
                    for (int x = 0; x < Math.min(mIntermediateMat.cols(), 10); x++) {
                        double vCircle[] = mIntermediateMat.get(0, x);

                        if (vCircle == null)
                            break;

                        pt.x = Math.round(vCircle[0]);
                        pt.y = Math.round(vCircle[1]);
                        radius = (int) Math.round(vCircle[2]);
                        // draw the found circle
                        Imgproc.circle(mRgba, pt, radius, colorRed, iLineThickness);

                        // draw a cross on the centre of the circle
                        DrawCross(mRgba, colorRed, pt);
                    }

                if (bDisplayTitle)
                    ShowTitle("Hough Circles", 1, colorGreen);

                break;

            case VIEW_MODE_HOUGHLINES:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);


                // the lower this figure the more spurious circles you get
                // 50 upper looks good in CANNY, but 75 is better when converting that into Hough circles
                iCannyLowerThreshold = 45;
                iCannyUpperThreshold = 75;

                Imgproc.Canny(mGray, mGray, iCannyLowerThreshold, iCannyUpperThreshold);

                Imgproc.HoughLinesP(mGray, lines, 1, Math.PI / 180, iHoughLinesThreshold, iHoughLinesMinLineSize, iHoughLinesGap);

                for (int x = 0; x < Math.min(lines.cols(), 40); x++) {
                    vecHoughLines = lines.get(0, x);

                    if (vecHoughLines.length == 0)
                        break;

                    x1 = vecHoughLines[0];
                    y1 = vecHoughLines[1];
                    x2 = vecHoughLines[2];
                    y2 = vecHoughLines[3];

                    pt1.x = x1;
                    pt1.y = y1;
                    pt2.x = x2;
                    pt2.y = y2;

                    Imgproc.line(mRgba, pt1, pt2, colorRed, 3);
                }

                if (bDisplayTitle)
                    ShowTitle("Hough Lines", 1, colorGreen);

                break;

            case VIEW_MODE_COLCONTOUR:

                // Convert the image into an HSV image

                Imgproc.cvtColor(mRgba, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

                Core.inRange(mHSVMat, new Scalar(byteColourTrackCentreHue[0] - 10, 100, 100),
                        new Scalar(byteColourTrackCentreHue[0] + 10, 255, 255), mHSVMat);

                // Here i'm only using the external contours and by
                // eroding we make the draw a teeny bit faster and the result a lot smoother
                // on the rough edges where the colour fades out of range by losing a lot
                // of the little spiky corners.

                Imgproc.erode(mHSVMat, mHSVMat, mErodeKernel);
                contours.clear();

                Imgproc.findContours(mHSVMat, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                contours.get(0);

                for (x = 0; x < contours.size(); x++) {
                    d = Imgproc.contourArea(contours.get(x));

                    // get an approximation of the contour (last but one param is the min required
                    // distance between the real points and the new approximation (in pixels)

                    // contours is a List<MatOfPoint>
                    // so contours.get(x) is a single MatOfPoint
                    // but to use approxPolyDP we need to pass a MatOfPoint2f
                    // so we need to do a conversion

                    contours.get(x).convertTo(mMOP2f1, CvType.CV_32FC2);

                    if (d > iContourAreaMin) {

                        Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 2, true);

                        // convert back to MatOfPoint and put it back in the list
                        mMOP2f2.convertTo(contours.get(x), CvType.CV_32S);

                        // draw the contour itself
                        Imgproc.drawContours(mRgba, contours, x, colorRed, iLineThickness);

                    }
                }
//                List<Moments> mu = new ArrayList<Moments>(Contours.size());
//                for (int i = 0; i < Contours.size(); i++) {
//                    mu.add(i, Imgproc.moments(Contours.get(i), false));
//                    Moments p = mu.get(i);
//                    int x = (int) (p.get_m10() / p.get_m00());
//                    int y = (int) (p.get_m01() / p.get_m00());
//                    Core.circle(rgbaImage, new Point(x, y), 4, new Scalar(255,49,0,255));
//                }

                if (bDisplayTitle)
                    ShowTitle("Colour Contours", 1, colorGreen);

                break;

            case VIEW_MODE_OPFLOW:

                if (mMOP2fptsPrev.rows() == 0) {

                    //Log.d("Baz", "First time opflow");
                    // first time through the loop so we need prev and this mats
                    // plus prev points
                    // get this mat
                    Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);

                    // copy that to prev mat
                    matOpFlowThis.copyTo(matOpFlowPrev);

                    // get prev corners
                    Imgproc.goodFeaturesToTrack(matOpFlowPrev, MOPcorners, iGFFTMax, 0.05, 20);
                    mMOP2fptsPrev.fromArray(MOPcorners.toArray());

                    // get safe copy of this corners
                    mMOP2fptsPrev.copyTo(mMOP2fptsSafe);
                } else {
                    //Log.d("Baz", "Opflow");
                    // we've been through before so
                    // this mat is valid. Copy it to prev mat
                    matOpFlowThis.copyTo(matOpFlowPrev);

                    // get this mat
                    Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);

                    // get the corners for this mat
                    Imgproc.goodFeaturesToTrack(matOpFlowThis, MOPcorners, iGFFTMax, 0.05, 20);
                    mMOP2fptsThis.fromArray(MOPcorners.toArray());

                    // retrieve the corners from the prev mat
                    // (saves calculating them again)
                    mMOP2fptsSafe.copyTo(mMOP2fptsPrev);

                    // and save this corners for next time through

                    mMOP2fptsThis.copyTo(mMOP2fptsSafe);
                }

        	
           	/*
           	Parameters:
           		prevImg first 8-bit input image
           		nextImg second input image
           		prevPts vector of 2D points for which the flow needs to be found; point coordinates must be single-precision floating-point numbers.
           		nextPts output vector of 2D points (with single-precision floating-point coordinates) containing the calculated new positions of input features in the second image; when OPTFLOW_USE_INITIAL_FLOW flag is passed, the vector must have the same size as in the input.
           		status output status vector (of unsigned chars); each element of the vector is set to 1 if the flow for the corresponding features has been found, otherwise, it is set to 0.
           		err output vector of errors; each element of the vector is set to an error for the corresponding feature, type of the error measure can be set in flags parameter; if the flow wasn't found then the error is not defined (use the status parameter to find such cases).
            */
                Video.calcOpticalFlowPyrLK(matOpFlowPrev, matOpFlowThis, mMOP2fptsPrev, mMOP2fptsThis, mMOBStatus, mMOFerr);

                cornersPrev = mMOP2fptsPrev.toList();
                cornersThis = mMOP2fptsThis.toList();
                byteStatus = mMOBStatus.toList();

                y = byteStatus.size() - 1;

                for (x = 0; x < y; x++) {
                    if (byteStatus.get(x) == 1) {
                        pt = cornersThis.get(x);
                        pt2 = cornersPrev.get(x);

                        Imgproc.circle(mRgba, pt, 5, colorRed, iLineThickness - 1);

                        Imgproc.line(mRgba, pt, pt2, colorRed, iLineThickness);
                    }
                }

                //Log.d("Baz", "Opflow feature count: "+x);
                if (bDisplayTitle)
                    ShowTitle("Optical Flow", 1, colorGreen);

                break;
        }

        // get the time now in every frame
        lMilliNow = System.currentTimeMillis();

        // update the frame counter
        lFrameCount++;

        if (bDisplayTitle) {
            string = String.format("FPS: %2.1f", (float) (lFrameCount * 1000) / (float) (lMilliNow - lMilliStart));

            ShowTitle(string, 2, colorGreen);
        }


//        if (System.currentTimeMillis() - lMilliShotTime < 1500)
//            ShowTitle(sShotText, 3, colorRed);

        return mRgba;
    }

    public boolean onTouchEvent(final MotionEvent event) {

//        bShootNow = true;
        return false; // don't need more than one touch event

    }


    public void DrawCross(Mat mat, Scalar color, Point pt) {
        int iCentreCrossWidth = 24;

        pt1.x = pt.x - (iCentreCrossWidth >> 1);
        pt1.y = pt.y;
        pt2.x = pt.x + (iCentreCrossWidth >> 1);
        pt2.y = pt.y;

        Imgproc.line(mat, pt1, pt2, color, iLineThickness - 1);

        pt1.x = pt.x;
        pt1.y = pt.y + (iCentreCrossWidth >> 1);
        pt2.x = pt.x;
        pt2.y = pt.y - (iCentreCrossWidth >> 1);

        Imgproc.line(mat, pt1, pt2, color, iLineThickness - 1);

    }

    private void ShowTitle(String s, int iLineNum, Scalar color) {
        Imgproc.putText(mRgba, s, new Point(10, (int) (dTextScaleFactor * 60 * iLineNum)),
                Core.FONT_HERSHEY_SIMPLEX, dTextScaleFactor, color, 2);
    }

}
