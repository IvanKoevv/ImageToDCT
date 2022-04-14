package imgcompressor.utils;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class StatisticsController {
    public static double calculateMSE(BufferedImage original, BufferedImage changed) {
        double sumR = 0, sumG = 0, sumB = 0;
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                Color a = new Color(original.getRGB(x, y));
                Color b = new Color(changed.getRGB(x, y));
                sumR += Math.pow(a.getRed() - b.getRed(), 2);
                sumG += Math.pow(a.getGreen() - b.getGreen(), 2);
                sumB += Math.pow(a.getBlue() - b.getBlue(), 2);
            }
        }

        double mse = ((sumR + sumG + sumB) / 3) / (original.getWidth() * original.getHeight());
        return mse;
    }

    public static double calculatePSNR(BufferedImage original, BufferedImage changed) {
        return 10 * Math.log10(255 * 255 / calculateMSE(original,changed));
    }
}
