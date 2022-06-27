package imgcompressor.utils;

import java.awt.Color;
public class ColorController {
    public static short[][] getRedRGBArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src.length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getRed();
            }
        }
        return result;
    }

    public static void transformToYCbCr(short[][][] red, short[][][] green, short[][][] blue) {
        for (int k = 0; k < red.length; k++) {
            for (int i = 0; i < red[0].length; i++) {
                for (int j = 0; j < red[0].length; j++) {
                    double R = red[k][i][j];
                    double G = green[k][i][j];
                    double B = blue[k][i][j];
                    double Y = (0.299d * R) + (0.587d * G) + (0.114d * B);
                    double Cb = 128d - (0.168736d * R) - (0.331264d * G) + (0.5d * B);
                    double Cr = 128d + (0.5d * R) - (0.418688d * G) - (0.081312d * B);
                    red[k][i][j] = (short) Y;
                    green[k][i][j] = (short) Cb;
                    blue[k][i][j] = (short) Cr;
                }
            }
        }
    }

        public static void transformToRGB(short[][][] red, short[][][] green, short[][][] blue) {
        for (int k = 0; k < red.length; k++) {
            for (int i = 0; i < red[0].length; i++) {
                for (int j = 0; j < red[0].length; j++) {
                    double Y = red[k][i][j];
                    double Cb = green[k][i][j];
                    double Cr = blue[k][i][j];
                    double R = Y + 1.402d * (Cr - 128);
                    double G = Y - 0.344136d * (Cb -128) - 0.714136 * (Cr - 128);
                    double B = Y + 1.772 * (Cb - 128);
                    red[k][i][j] = (short) R;
                    green[k][i][j] = (short) G;
                    blue[k][i][j] = (short) B;
                }
            }
        }
    }

    public static short[][] getGreenRGBArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src[0].length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getGreen();
            }
        }
        return result;
    }

    public static short[][] getBlueRGBArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src[0].length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getBlue();

            }
        }
        return result;
    }
    
    public static int[][] getIntRGBArray(short[][] red, short[][] green, short[][] blue) {
        int[][] result = new int[red.length][red.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                if (red[i][j] > 255) {
                    red[i][j] = 255;
                }
                if (green[i][j] > 255) {
                    green[i][j] = 255;
                }
                if (blue[i][j] > 255) {
                    blue[i][j] = 255;
                }
                if (red[i][j] < 0) {
                    red[i][j] = 0;
                }
                if (green[i][j] < 0) {
                    green[i][j] = 0;
                }
                if (blue[i][j] < 0) {
                    blue[i][j] = 0;
                }
                Color buff = new Color((int)red[i][j], (int)green[i][j], (int)blue[i][j]);
                result[i][j] = buff.getRGB();
            }
        }
        return result;
    }
}
