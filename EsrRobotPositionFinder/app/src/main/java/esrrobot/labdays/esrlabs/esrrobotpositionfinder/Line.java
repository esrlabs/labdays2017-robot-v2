package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mortenmey on 12.07.17.
 */

public class Line {

    public final double angle;
    public final double distanceFromCenter;
    public final double[] pointA;
    public final double[] pointB;

    public Line(double[] startEnd, int dimX, int dimY) {
        double dirX = startEnd[0] - startEnd[2];
        double dirY = startEnd[1] - startEnd[3];
        double rawAngle = Math.atan2(dirY, dirX);
        if (rawAngle < 0) {
            rawAngle += Math.PI;
        }
        angle = rawAngle;

        pointA = Arrays.copyOf(startEnd, 2);
        pointB = Arrays.copyOfRange(startEnd, 2, 4);
        double[] center = new double[]{dimX / 2, dimY / 2};
        distanceFromCenter = lineToPointDistance2D(pointA, pointB, center);
    }

    //Compute the dot product AB . AC
    private static double dotProduct(double[] pointA, double[] pointB, double[] pointC) {
        double[] AB = new double[2];
        double[] BC = new double[2];
        AB[0] = pointB[0] - pointA[0];
        AB[1] = pointB[1] - pointA[1];
        BC[0] = pointC[0] - pointB[0];
        BC[1] = pointC[1] - pointB[1];
        double dot = AB[0] * BC[0] + AB[1] * BC[1];

        return dot;
    }

    //Compute the cross product AB x AC
    private static double crossProduct(double[] pointA, double[] pointB, double[] pointC) {
        double[] AB = new double[2];
        double[] AC = new double[2];
        AB[0] = pointB[0] - pointA[0];
        AB[1] = pointB[1] - pointA[1];
        AC[0] = pointC[0] - pointA[0];
        AC[1] = pointC[1] - pointA[1];
        double cross = AB[0] * AC[1] - AB[1] * AC[0];

        return cross;
    }

    //Compute the distance from A to B
    private static double distance(double[] pointA, double[] pointB) {
        double d1 = pointA[0] - pointB[0];
        double d2 = pointA[1] - pointB[1];

        return Math.sqrt(d1 * d1 + d2 * d2);
    }

    /**
     * Compute the distance from AB to C
     */
    public static double lineToPointDistance2D(double[] pointA, double[] pointB, double[] pointC) {
        double dist = crossProduct(pointA, pointB, pointC) / distance(pointA, pointB);
        return Math.abs(dist);
    }

    public static List<Line> fromRawData(List<double[]> raw, int xDim, int yDim) {
        List<Line> lines = new ArrayList<>(raw.size());
        for (double[] val : raw) {
            lines.add(new Line(val, xDim, yDim));
        }
        return lines;
    }

    @Override
    public String toString() {
        return "Line{" +
                "angle=" + angle +
                ", distanceFromCenter=" + distanceFromCenter +
                '}';
    }

    public static double calculateMostProminantAngleFromGrid(List<Line> lines) {
        double res = 0;
        for (Line l : lines) {
            if (l.angle > Math.PI / 2) {
                res += l.angle - Math.PI / 2;
            } else {
                res += l.angle;
            }
        }

        if (!lines.isEmpty()) {
            res /= lines.size();
        }
        return res;
    }

    public static List<Line> getCloseLines(final List<Line> lines, final double[] point, double minDistance) {
        List<Line> res = new ArrayList<>();
        for (Line l : lines) {
            double dist = lineToPointDistance2D(l.pointA, l.pointB, point);
            if (dist < minDistance) {
                res.add(l);
            }
        }
        return res;
    }

    public static double getAverageDistance(List<Line> lines, double[] point) {
        double dist = 0;
        for (Line l : lines) {
            dist += lineToPointDistance2D(l.pointA, l.pointB, point);
        }
        return dist / lines.size();
    }

}
