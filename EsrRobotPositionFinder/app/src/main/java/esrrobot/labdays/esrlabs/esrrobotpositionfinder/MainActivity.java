package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.gms.vision.barcode.Barcode;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String TAG = "EsrRobotPositionFinder";

    private CustomCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private Handler mHandler;
    private MQTTConnection mqtt;
    private int mWidth;
    private int mHeight;
    private ImageProcessing mImageProcessing;

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
        mImageProcessing = new ImageProcessing(this);

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

        final Barcode code = mImageProcessing.findBarcode(mat);

        if (code != null) {
            Log.d(TAG, "Code: " + code.rawValue);
            List<double[]> rawLines = mImageProcessing.findLines(mat);
            mImageProcessing.addDebugGraphics(mat, code, rawLines);
            List<Line> lines = Line.fromRawData(rawLines, mat.cols(), mat.rows());
//          Log.d(TAG, "Lines: " + lines);
//          double angle = Line.calculateMostProminantAngleFromGrid(lines);
            double[] center = getCenter(code);
            List<Line> closest = Line.getCloseLines(lines, center, 200);
            if (closest.isEmpty()) {
                return mat;
            }
            Log.d(TAG, "Closest: " + closest);
            mImageProcessing.addClosestDebug(mat, closest);
            Point point = getClosest(closest, code.cornerPoints);
            mImageProcessing.drawPoint(mat, point.x, point.y);
            double angle = getAngle(center, new double[]{point.x, point.y});
            Log.d(TAG, "Angle: " + angle);
            publish(code, angle, start);
        }
        return mat;
    }

    private double getAngle(double[] center, double[] topLeftCorner) {
        double[] dir = new double[2];
        dir[0] = center[0] - topLeftCorner[0];
        dir[1] = center[1] - topLeftCorner[1];
        double angle = Math.atan2(dir[1], dir[0]);
        angle += Math.PI / 4;
        if (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    private double[] getCenter(Barcode code) {
        double[] res = new double[2];
        for (Point p : code.cornerPoints) {
            res[0] += p.x;
            res[1] += p.y;
        }
        res[0] /= 4;
        res[1] /= 4;
        return res;
    }

    private Point getClosest(List<Line> lines, Point[] points) {
        double dist = Double.POSITIVE_INFINITY;
        Point candidate = null;
        for (Point p : points) {
            double[] point = new double[]{p.x, p.y};
            double myDist = Line.getAverageDistance(lines, point);
            if (myDist < dist) {
                dist = myDist;
                candidate = p;
            }
        }
        return candidate;
    }

    private void publish(Barcode code, double angle, long startTime) {
        long stopTime = System.currentTimeMillis();
        String message = "PHONE_1:" + formatTimestamp(startTime) + ":" + formatTimestamp(stopTime)
                + ":QR_1:" + code.rawValue + "," + angle;
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
