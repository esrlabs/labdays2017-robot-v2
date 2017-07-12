package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "EsrRobotPositionFinder";

    private CustomCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BarcodeDetector mBarcodeDetector = null;
    private Handler mHandler;
    private MQTTConnection mqtt;
    private int mWidth;
    private int mHeight;

    private Runnable mCamFocus = new Runnable() {
        @Override
        public void run() {
            mOpenCvCameraView.configureFocus(mWidth, mHeight);
            mHandler.postDelayed(this, 1000);
        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
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
        mHandler = new Handler();
        mBarcodeDetector = new BarcodeDetector.Builder(this).build();


        try {
            mqtt = new MQTTConnection(this);
        } catch (MqttException e) {
            Log.e(TAG, "cannot create MQTT connection", e);
        }

        mqtt.connect(new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "message arrived 2.0:" + message);
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CustomCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "Internal OpenCV library not found.");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            System.loadLibrary("native-lib");
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(final int width, final int height) {
        mHandler.postDelayed(mCamFocus, 1000);
    }

    public void onCameraViewStopped() {
        mHandler.removeCallbacks(mCamFocus);
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();
        long start = System.currentTimeMillis();
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = mBarcodeDetector.detect(frame);

        final Barcode code = findBarcode(barcodes, mat.cols(), mat.rows());
        if (code == null) {
            return mat;
        }
        Log.d(TAG, "Code: " + code.rawValue);
        for (int j = 0; j < 4; ++j) {
            drawLine(mat, code.cornerPoints, j, (j + 1) % 4, new Scalar(255, 0, 0));
        }
        publish(code, start);
        return mat;
    }

    private void publish(Barcode code, long startTime) {
        long stopTime = System.currentTimeMillis();
        String message = "PHONE_1:" + formatTimestamp(startTime) + ":" + formatTimestamp(stopTime)
                + ":QR_1:" + code.rawValue;
        try {
            mqtt.publishMessage(message);
        } catch (Exception e) {
            Log.e(TAG, "Cannot send message:" + message, e);
        }

    }

    private String formatTimestamp(long time) {
        String fraction = Long.toString(time % 1000);
        while (fraction.length() < 3) {
            fraction = "0" + fraction;
        }
        return time / 1000 + "." + fraction;
    }

    private Barcode findBarcode(SparseArray<Barcode> barcodes, int xDim, int yDim) {
        double closestDist = Double.POSITIVE_INFINITY;
        Barcode closest = null;
        for (int i = 0; i < barcodes.size(); ++i) {
            Barcode current = barcodes.valueAt(i);
            double dist = squaredDistanceFromCenter(current.cornerPoints, xDim, yDim);
            if (dist < closestDist) {
                closest = current;
                closestDist = dist;
            }
        }
        return closest;
    }

    private double squaredDistanceFromCenter(Point[] data, int xDim, int yDim) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < 4; ++i) {
            x += data[i].x;
            y += data[i].y;
        }
        x /= 4;
        y /= 4;

        int centerX = xDim / 2;
        int centerY = yDim / 2;

        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2);
    }

    private void drawLine(Mat mat, Point[] points, int from, int to, Scalar color) {
        int cols = mat.cols();
        int rows = mat.rows();
        org.opencv.core.Point p1 = convert(points[from]);
        org.opencv.core.Point p2 = convert(points[to]);
        Imgproc.line(mat, p1, p2, color, 10);
    }

    private org.opencv.core.Point convert(Point p) {
        return new org.opencv.core.Point(p.x, p.y);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
