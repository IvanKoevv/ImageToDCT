package imgcompressor;

import imgcompressor.Image.Img;
import imgcompressor.compressor.CompressorController;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Compressor {
    private Img image;
    private CompressorController compressor;

    /**
     * @return the source image
     */
    public BufferedImage getSourceImage() {
        return image.image;
    }

    /**
     * @return the result image
     */
    public BufferedImage getResultImage() {
        return image.result;
    }

    /**
     * @param image the image to set
     */
    public void setImage(Img image) {
        this.image = image;
    }


    public void openFile() {
        this.image = new Img(8);
    }

    public void compressImage(int qFactor, int n) {
        compressor = new CompressorController(image.getRGBArray(),
                (short) image.image.getHeight(),
                (short) image.image.getWidth());

        compressor.compress(qFactor, n);
    }

    public void compressImageNoWrite(int qFactor, int n) {
        compressor = new CompressorController(image.getRGBArray(),
                (short) image.image.getHeight(),
                (short) image.image.getWidth());

        compressor.compressNoWrite(qFactor, n);
    }
    
    public void writeImage() {
        image.writeImg();
    }

    public void decompressImage() {
        compressor.decompress();
        image.setRGBArray(compressor.getsRGBArray());
    }

    public double getMSE() {
        return image.calculateMSE();
    }

    public double getPSNR() {
        return image.calculatePSNR();
    }

    public double getSrcSizeKb() {
        return image.getSrcSizeKb();
    }

    public double getSrcFileSizeKb() {
        return image.getSrcFileSizeKb();
    }

    public double getEncodeSizeKb() {
        return compressor.getEncodeSizeKb();
    }

    public double getCompresionRatioKB() {
        return (getSrcSizeKb() / getEncodeSizeKb());
    }
    
    public void printStatsQf() {
        double[] MSE = new double[20];
        double[] PSNR = new double[20];
        double[] sizeKb = new double[20];
        for (int i = 0; i < MSE.length; i++) {
            compressor = new CompressorController(image.getRGBArray(), (short) image.image.getHeight(),
                    (short) image.image.getWidth());
            compressor.compressNoWrite((i + 1) * 5, 64);
            compressor.decompress();
            image.setRGBArray(compressor.getsRGBArray());
            MSE[i] = image.calculateMSE();
            PSNR[i] = image.calculatePSNR();
            sizeKb[i] = (image.getSrcSizeKb() / compressor.getEncodeSizeKb());
        }

        System.out.println("MSE range: ");
        System.out.println(Arrays.toString(MSE));
        System.out.println("PSNR range: ");
        System.out.println(Arrays.toString(PSNR));
        System.out.println("File size range: ");
        System.out.println(Arrays.toString(sizeKb));
    }
    
    public void printStatsCoef() {
        double[] MSE = new double[64];
        double[] PSNR = new double[64];
        double[] sizeKb = new double[64];
        for (int i = 1; i <= MSE.length; i++) {
            compressor = new CompressorController(image.getRGBArray(), (short) image.image.getHeight(),
                    (short) image.image.getWidth());
            compressor.compressNoWrite(1, i);
            compressor.decompress();
            image.setRGBArray(compressor.getsRGBArray());
            MSE[i-1] = image.calculateMSE();
            PSNR[i-1] = image.calculatePSNR();
            sizeKb[i-1] = (image.getSrcSizeKb() / compressor.getEncodeSizeKb());
        }
        
        System.out.println("MSE range: ");
        System.out.println(Arrays.toString(MSE));
        System.out.println("PSNR range: ");
        System.out.println(Arrays.toString(PSNR));
        System.out.println("File size range: ");
        System.out.println(Arrays.toString(sizeKb));
    }
}
