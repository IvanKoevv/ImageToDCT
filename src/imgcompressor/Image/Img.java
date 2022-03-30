package imgcompressor.Image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import imgcompressor.compressor.Compressor;

public class Img {
    public BufferedImage image;
    private File imgLocation;
    public BufferedImage result;
    private int mcuSize;
    public Compressor compressor;

    public Img(JFrame main) throws IOException {
        JFileChooser chose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("byte and images", "jpg", "byte", "png");
        chose.setFileFilter(filter);
        int returnVal = chose.showOpenDialog(main);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.mcuSize = 8;
            File location = new File(chose.getSelectedFile().getAbsolutePath());
            String s = chose.getSelectedFile().toString();

            if (s.substring(s.lastIndexOf(".")).equals(".byte")) {
                compressor = new Compressor();
                compressor.decode(new File(s));
                this.image = new BufferedImage(
                        compressor.getWidth(),
                        compressor.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                this.result = new BufferedImage(
                        compressor.getWidth(),
                        compressor.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                compressor.decompress();
                setRGBArray(compressor.getsRGBArray());
                this.image = new BufferedImage(
                        result.getColorModel(),
                        result.copyData(null),
                        result.isAlphaPremultiplied(), null);
            } else {
                this.image = resizeToMcuSize(ImageIO.read(location), mcuSize);
                this.result = resizeToMcuSize(ImageIO.read(location), mcuSize);
            }
            this.imgLocation = location;
        }
    }

    public Img(Img img) {
        this.image = img.image;
        this.mcuSize = img.mcuSize;
        this.result = new BufferedImage(this.image.getWidth(),this.image.getHeight(),this.image.getType());

    }

    public void writeImg(BufferedImage src) throws IOException {
        JFileChooser chose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chose.showSaveDialog(null);
        ImageIO.write(src, "PNG", new File(chose.getSelectedFile().getAbsolutePath()));
    }
    public void writeImg() throws IOException {
        JFileChooser chose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chose.showSaveDialog(null);
        ImageIO.write(result, "PNG", new File(chose.getSelectedFile().getAbsolutePath()));
    }

    private BufferedImage resizeToMcuSize(BufferedImage src, int mcuSize) {
        int newWidth = src.getWidth() - (src.getWidth() % mcuSize);
        int newHeight = src.getHeight() - (src.getHeight() % mcuSize);
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.createGraphics();
        g.drawImage(src, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return newImage;
    }

    public int getMcuCount() {
        return (image.getWidth() * image.getHeight()) / (mcuSize * mcuSize);
    }

    public int[][][] getRGBArray() {
        int[][][] result = new int[this.getMcuCount()][mcuSize][mcuSize];
        int blocksRight = image.getWidth() / mcuSize;
        for (int i = 0, offx = 0, offy = 0; i < getMcuCount(); i++, offx++) {
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

    public void setRGBArray(int[][][] input) {
        int blocksRight = image.getWidth() / mcuSize;
        for (int i = 0, offx = 0, offy = 0; i < getMcuCount(); i++, offx++) {
            if (offx == blocksRight) {
                offy++;
                offx = 0;
            }
            for (int y = 0; y < mcuSize; y++) {
                for (int x = 0; x < mcuSize; x++) {
                    result.setRGB(x + (mcuSize * offx), y + (mcuSize * offy), input[i][y][x]);
                }
            }
        }
    }
    
    public double getSrcSizeKb() {
        double srcSize = (double)(image.getWidth() * image.getHeight()) * 3d / (1024d);
        return srcSize;
    }

    public double getSrcFileSizeKb() {
        double srcSizeFile = imgLocation.length()/1024d;
        return srcSizeFile;
    }

}
    
