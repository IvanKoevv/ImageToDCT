package imgcompressor.utils;

import imgcompressor.algorithms.FastDct8;
import imgcompressor.algorithms.SlowDct;

public class DctOperator {

    public static short[][] forward(short[][] src) {
        doDctPerRow(src);
        src = transposeMatrix(src);
        doDctPerRow(src);
        return src;
    }

    public static short[][] backward(short[][] src) {
        IDctPerRow(src);
        src = transposeMatrix(src);
        IDctPerRow(src);
        return src;
    }
    
    public static void doDctPerRow(short[][] src) {
        //for (int[] i : src)
        for (int j = 0; j < src.length;j++)
        {
            double[] buff = FastDct8.transform(getshortToDouble(src[j]));
            src[j] = getdoubleToShort(buff);
        }
    }

    public static void IDctPerRow(short[][] src) {
        for (int i =0;i<src.length;i++) {
            double[] buff = FastDct8.inverseTransform(getshortToDouble(src[i]));
            src[i] = getdoubleToShort(buff);
        }
    }

    public static short[][] transposeMatrix(short[][] m) {
        short[][] temp = new short[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    public static short[] getdoubleToShort(double[] src) {
        short[] result = new short[src.length];
        for (int i = 0; i < src.length; i++) {
                result[i] = (short)Math.round(src[i]);
        }
        return result;
    }

    public static double[] getshortToDouble(short[] src) {
        double[] result = new double[src.length];
        for (int i = 0; i < src.length; i++) {
                result[i] = (double) src[i];
            }
        return result;
    }
}