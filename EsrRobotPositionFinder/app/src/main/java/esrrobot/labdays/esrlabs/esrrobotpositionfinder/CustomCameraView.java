package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view to get access to the camera options.
 */

public class CustomCameraView extends JavaCameraView implements Camera.AutoFocusCallback {
    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void configureFocus(int xDim, int yDim) {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        int minX = xDim / 2 - 20;
        int minY = yDim / 2 - 20;
        int maxX = xDim / 2 + 20;
        int maxY = yDim / 2 + 20;
        Rect focusArea = new Rect(minX, minY, maxX, maxY);
        List<Camera.Area> areas = new ArrayList<>();
        areas.add(new Camera.Area(focusArea, 1000));
        params.setFocusAreas(areas);
        mCamera.setParameters(params);
        mCamera.autoFocus(this);
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {
        Log.d("CustomCameraView", "Focus completed success: " + b);
    }
}
