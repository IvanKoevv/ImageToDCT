package imgcompressor.algorithms;

import java.util.Objects;

public class SlowDct {
    public static double[] transform(double[] vector) {
        Objects.requireNonNull(vector);
        double[] result = new double[vector.length];
        double scale = Math.sqrt(2.00 / vector.length);
        for (int i = 0; i < vector.length; i++) {
            double sum = 0.00;
            double s = (i == 0) ? Math.sqrt(0.5) : 1.00;
            for (int j = 0; j < vector.length; j++) {
                sum += s * vector[j] * Math.cos(Math.PI * (j + 0.5) * i / vector.length);
            }
            result[i] = sum*scale;
        }
        return result;
    }
    
    public static double[] inverseTransform(double[] vector) {
        Objects.requireNonNull(vector);
        double[] result = new double[vector.length];
        double scale = Math.sqrt(2.00 / vector.length);
        for (int i = 0; i < vector.length; i++) {
            double sum = 0.00;
            for (int j = 0; j < vector.length; j++) {
                double s = (j == 0) ? Math.sqrt(0.5) : 1.00;
                sum += s * vector[j] * Math.cos(Math.PI * (i + 0.5) * j / vector.length);
            }
            result[i] = sum * scale;
        }
        return result;
    }
}
