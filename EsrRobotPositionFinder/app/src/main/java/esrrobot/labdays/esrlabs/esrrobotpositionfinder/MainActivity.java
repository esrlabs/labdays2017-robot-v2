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
import org.opencv.core.Scalar;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    private double mCameraOrientation = 0.0;

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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraOrientation = mOpenCvCameraView.getCameraOrientation(getWindowManager().getDefaultDisplay().getRotation());
            }
        });
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
//            Log.d(TAG, "Closest: " + closest);
            mImageProcessing.drawLines(mat, closest);
            Point point = getClosest(closest, code.cornerPoints);
            mImageProcessing.drawPoint(mat, point.x, point.y);
            mImageProcessing.drawLine(mat, new double[]{mat.cols() / 2, mat.rows() / 2}, center, new Scalar(0, 255, 255));
            double angle = getAngle(center, new double[]{point.x, point.y});
            double scale = getScale(center, new double[]{point.x, point.y});
            final double[] fromCenter = getDirectionFromCenter(center, mat.cols(), mat.rows(), scale);
            final double[] pos = calculateGlobalPos(code.rawValue, fromCenter, angle);
            Log.d(TAG, "Angle: " + angle + " Scale: " + scale + " Position: " + Arrays.toString(pos));

            angle += mCameraOrientation;
            if (angle > Math.PI) {
                angle -= 2 * Math.PI;
            }

            publish(code.rawValue, angle, pos, start);
        }
        return mat;
    }

    private static final double QR_DIAGONAL_DISTANCE = 1.85;

    private double getScale(double[] center, double[] corner) {
        double length = Math.pow(center[0] - corner[0], 2);
        length += Math.pow(center[1] - corner[1], 2);
        length = Math.sqrt(length);
        return QR_DIAGONAL_DISTANCE / 2 / length;
    }

    private double[] getDirectionFromCenter(double[] qrCenter, int xDim, int yDim, double scale) {
        double[] res = new double[2];
        res[0] = xDim / 2 - qrCenter[0];
        res[1] = yDim / 2 - qrCenter[1];
        res[0] *= scale;
        res[1] *= scale;
        return res;
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

    private double[] calculateGlobalPos(String code, double[] offset, double angle) {
        double[] rotated = offsetToGlobalCords(offset, angle);
        double[] pos = new double[2];
        pos[0] = Integer.parseInt(code.substring(1, 3));
        pos[1] = Integer.parseInt(code.substring(3, 5));
        pos[0] *= 3;
        pos[1] *= 3;
        pos[0] += rotated[0];
        pos[1] += rotated[1];
        return pos;
    }

    private double[] offsetToGlobalCords(double[] offset, double degrees) {
        degrees = -degrees;
        double x = offset[0];
        double y = offset[1];
        double[] result = new double[2];
        result[0] = x * Math.cos(degrees) - y * Math.sin(degrees);
        result[1] = x * Math.sin(degrees) + y * Math.cos(degrees);
        result[1] = -result[1];
        return result;
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

    private void publish(final String code, final double angle, final double[] pos, long startTime) {
        long stopTime = System.currentTimeMillis();
        String message = "PHONE_1:" + formatTimestamp(startTime) + ":" + formatTimestamp(stopTime)
                + ":QR_1:" + code + "," + convertDouble(angle) + "," + convertDouble(pos[0])
                + "," + convertDouble(pos[1]);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setTitle(String.format(Locale.ENGLISH, "%s,%.3f,%.3f,%.3f",
                        code,
                        angle,
                        pos[0],
                        pos[1]));
            }
        });
        try {
            mqtt.publishMessage(message);
        } catch (Exception e) {
            Log.e(TAG, "Cannot send message:" + message, e);
        }
    }

    private String convertDouble(double value) {
        return String.format(Locale.ENGLISH, "%.3f", value);
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
