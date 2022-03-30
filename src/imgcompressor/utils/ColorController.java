package imgcompressor.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
public class ColorController {
    public static short[][] GetRedArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src.length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getRed();
            }
        }
        return result;
    }

    public static void RGB2YCbCr(short[][][] red, short[][][] green, short[][][] blue) {
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

        public static void YCbCr2RGB(short[][][] red, short[][][] green, short[][][] blue) {
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

    public static short[][] GetGreenArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src[0].length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getGreen();
            }
        }
        return result;
    }

    public static short[][] GetBlueArray(int[][] src) {
        short[][] result = new short[src.length][src.length];
        for (int i = 0; i < src.length; i++) {
            for (int j = 0; j < src[0].length; j++) {
                Color buff = new Color(src[j][i]);
                result[j][i] = (short)buff.getBlue();

            }
        }
        return result;
    }
    
    public static int[][] GetRGBArray(short[][] red, short[][] green, short[][] blue) {
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
    


        public static int[][][] getMcuRGBArray(int mcuSize, BufferedImage image,int McuCount) {
        int[][][] result = new int[McuCount][mcuSize][mcuSize];
        int blocksRight = image.getWidth() / mcuSize;
        //int blocksDown = image.getHeight() / mcuSize;
        for (int i = 0, offx = 0, offy = 0; i < McuCount; i++, offx++) {
            if (offx == blocksRight) {
                offy++;
                offx = 0;
            }
            for (int y = 0; y < mcuSize; y++) {
                for (int x = 0; x < mcuSize; x++) {
                    result[i][y][x] = image.getRGB(x + (mcuSize * offx), y + (mcuSize * offy));
                }
            }
        }
        return result;
    }
}
