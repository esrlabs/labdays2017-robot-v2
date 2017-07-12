package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mortenmey on 12.07.17.
 */

public class ImageProcessing {

    private BarcodeDetector mBarcodeDetector;

    public ImageProcessing(Context context) {
        mBarcodeDetector = new BarcodeDetector.Builder(context).build();
    }

    public Barcode findBarcode(Mat rgbaImage) {
        Bitmap bitmap = Bitmap.createBitmap(rgbaImage.cols(), rgbaImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaImage, bitmap);
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = mBarcodeDetector.detect(frame);
        return findBarcode(barcodes, rgbaImage.cols(), rgbaImage.rows());
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

    public List<double[]> findLines(Mat rgbaImage) {
        Mat magentaOnly = new Mat();
        Core.inRange(rgbaImage, new Scalar(125, 0, 10, 0), new Scalar(255, 90, 255, 255), magentaOnly);

        // generate gray scale and blur
//        Mat gray = new Mat();
//        Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.blur(gray, gray, new Size(5, 5));

        // detect the edges
//        Mat edges = new Mat();
//        int lowThreshold = 30;
//        int ratio = 3;
//        Imgproc.Canny(gray, edges, lowThreshold, lowThreshold * ratio);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(magentaOnly, lines, 1, Math.PI / 180, 100, 200, 50);

        int nulls = 0;
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < lines.rows(); i++) {
            double[] val = lines.get(i, 0);
            if (val != null) {
                result.add(val);
            } else {
                nulls++;
            }
        }
        Log.d("ImageProcessing", "Lines: " + lines.rows() + " Nulls: " + nulls);
        return result;
    }


    public void addDebugGraphics(Mat rgbaImage, Barcode barcode, List<double[]> lines) {
        for (double[] val : lines) {
            Imgproc.line(rgbaImage, new org.opencv.core.Point(val[0], val[1]), new org.opencv.core.Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
        }

        if (barcode != null) {
            for (int j = 0; j < 4; ++j) {
                drawLine(rgbaImage, barcode.cornerPoints, j, (j + 1) % 4, new Scalar(255, 0, 0));
            }
        }
    }

    public void drawPoint(Mat rgbaImage, double x, double y) {
        Imgproc.circle(rgbaImage, new org.opencv.core.Point(x, y), 5, new Scalar(255, 255, 0), 2);
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

    public void addClosestDebug(Mat rgbaImage, List<Line> closest) {
        for (Line l : closest) {
            Imgproc.line(rgbaImage, new org.opencv.core.Point(l.pointA[0], l.pointA[1]), new org.opencv.core.Point(l.pointB[0], l.pointB[1]), new Scalar(0, 255, 0), 2);
        }
    }
}
