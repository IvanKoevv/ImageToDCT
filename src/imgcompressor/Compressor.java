package imgcompressor;

import imgcompressor.Image.Img;
import imgcompressor.compressor.CompressorController;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Entry point of lib to compress images based on DCT and run-lenth encoding.
 * 
 */
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
     * Used for opening and loading an image or .byte file
     */
    public void openFile() {
        this.image = new Img(8);
    }

    /** Compresses the image loaded from openFile() and uses a JFileChooser to write the .byte file
     * @param qFactor The quality factor range (1-100)
     * @param coef The number of DCT coeficient to keep (1-64)
     */
    public void compressImage(int qFactor, int coef) {
        compressor = new CompressorController(image.getRGBArray(),
                (short) image.image.getHeight(),
                (short) image.image.getWidth());

        compressor.compress(qFactor, coef);
    }

     /** Compresses the image loaded from openFile() without writing it
     * @param qFactor The quality factor range (1-100)
     * @param coef The number of DCT coeficient to keep (1-64)
     */
     public void compressImageNoWrite(int qFactor, int coef) {
         compressor = new CompressorController(image.getRGBArray(),
                 (short) image.image.getHeight(),
                 (short) image.image.getWidth());

         compressor.compressNoWrite(qFactor, coef);
     }
    
     /** Used after compressing an image. Opens JFileChooser to write image as .png
     */
     public void writeImage() {
         image.writeImg();
     }

      /** Decompresses image.
     */
      public void decompressImage() {
          compressor.decompress();
          image.setRGBArray(compressor.getsRGBArray());
      }

    /** Calculates the mean squared error of the original and compressed image and returns it
     * @return the mean sqared error
     */
    public double getMSE() {
        return image.calculateMSE();
    }


     /** Calculates the PSNR of the original and compressed image and returns it
     * @return PSNR
     */
     public double getPSNR() {
         return image.calculatePSNR();
     }

    /** Returns the Raw input image size. Treated as 24bit per pixel
     * @return Raw image size in Kb
     */
    public double getSrcSizeKb() {
        return image.getSrcSizeKb();
    }

    /** Returns the File size of the input image.
     * @return File size in Kb
     */
    public double getSrcFileSizeKb() {
        return image.getSrcFileSizeKb();
    }

    /** Returns the size of the encoded image.
     * @return size in Kb
     */
    public double getEncodeSizeKb() {
        return compressor.getEncodeSizeKb();
    }

    /** Returns the size of the encoded image.
     * @return size in Kb
     */
    public double getCompresionRatioKB() {
        return (getSrcSizeKb() / getEncodeSizeKb());
    }
    
    /** 
     * Calculates and prints to console the MSE,PSNR and compression ration based on Quality factor.
     * Dataset will start form Quality=1 and go to 100 with steps on every divisible by 5 number.
     * For a total of 21 values.
     * This will compress and decompress for every new step. 
     */
    public void printStatsQf() {
        double[] MSE = new double[21];
        double[] PSNR = new double[21];
        double[] sizeKb = new double[21];
        for (int i = 0; i <= MSE.length - 1; i++) {
            compressor = new CompressorController(image.getRGBArray(),
                    (short) image.image.getHeight(),
                    (short) image.image.getWidth());
            int quality = i * 5;
            if (quality == 0) {
                quality = 1;
            }
            compressor.compressNoWrite(quality, 64);
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
    
    /** 
     * Calculates and prints to console the MSE,PSNR and compression ration based on Coef count.
     * Dataset will start form Coef=1 and go to 64.
     * For a total of 64 values.
     * This will compress and decompress for every new step. 
     */
    public void printStatsCoef() {
        double[] MSE = new double[64];
        double[] PSNR = new double[64];
        double[] sizeKb = new double[64];
        for (int i = 1; i <= MSE.length; i++) {
            compressor = new CompressorController(image.getRGBArray(),
                    (short) image.image.getHeight(),
                    (short) image.image.getWidth());
            compressor.compressNoWrite(100, i);
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
