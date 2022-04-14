package imgcompressor.compressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import imgcompressor.utils.ColorController;
import imgcompressor.utils.DctOperator;

public class CompressorController {
    private short[][][] mcuRed;
    private short[][][] mcuGreen;
    private short[][][] mcuBlue;
    private ByteArrayOutputStream encodedBytes;


    private short height;
    private short width;
    private int mcuCount;
    private int mcuSize;
    private int qFactor;


    private static final int luminance[][] = {
            { 16, 11, 10, 16, 24, 40, 51, 61 },
            { 12, 12, 14, 19, 26, 58, 60, 55 },
            { 14, 13, 16, 24, 40, 57, 69, 56 },
            { 14, 17, 22, 29, 51, 87, 80, 62 },
            { 18, 22, 37, 56, 68, 109, 103, 77 },
            { 24, 35, 55, 64, 81, 104, 113, 92 },
            { 49, 64, 78, 87, 103, 121, 120, 101 },
            { 72, 92, 95, 98, 112, 100, 103, 99 }
    };
    private static final int chrominance[][] = {
            { 17, 18, 24, 47, 99, 99, 99, 99 },
            { 18, 21, 26, 66, 99, 99, 99, 99 },
            { 24, 26, 56, 99, 99, 99, 99, 99 },
            { 47, 66, 99, 99, 99, 99, 99, 99 },
            { 99, 99, 99, 99, 99, 99, 99, 99 },
            { 99, 99, 99, 99, 99, 99, 99, 99 },
            { 99, 99, 99, 99, 99, 99, 99, 99 },
            { 99, 99, 99, 98, 99, 99, 99, 99 }
    };

    public CompressorController() {
        this.mcuRed = null;
        this.mcuBlue = null;
        this.mcuGreen = null;
        this.height = 0;
        this.width = 0;
        this.mcuCount = 0;
    }

    public CompressorController(int[][][] src, short height, short width) {
        this.mcuRed = new short[src.length][src[0].length][src[0].length];
        this.mcuGreen = new short[src.length][src[0].length][src[0].length];
        this.mcuBlue = new short[src.length][src[0].length][src[0].length];
        this.height = height;
        this.width = width;
        this.mcuCount = src.length;
        this.mcuSize = ((height * width) / mcuCount);
        this.encodedBytes = new ByteArrayOutputStream();
        fill(src);
    }

    public void fill(int[][][] src) {
        for (int i = 0; i < src.length; i++) {
            mcuRed[i] = ColorController.getRedRGBArray(src[i]);
            mcuGreen[i] = ColorController.getGreenRGBArray(src[i]);
            mcuBlue[i] = ColorController.getBlueRGBArray(src[i]);
        }
    }

    public int[][][] getsRGBArray() {
        int[][][] temp = new int[mcuBlue.length][mcuBlue[0].length][mcuBlue[0].length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = ColorController.getIntRGBArray(mcuRed[i], mcuGreen[i], mcuBlue[i]);
        }
        return temp;
    }

    public void compress(int qFactor, int n) {
        this.qFactor = qFactor;
        ColorController.transformToYCbCr(mcuRed, mcuGreen, mcuBlue);
        ShiftAroundZero();
        transformDCT();
        KeepNZigZag(n);
        quantisize(qFactor);
        encode();
        writeBinaryFile();
    }

    public void compressNoWrite(int qFactor, int n) {
        this.qFactor = qFactor;
        ColorController.transformToYCbCr(mcuRed, mcuGreen, mcuBlue);
        ShiftAroundZero();
        transformDCT();
        KeepNZigZag(n);
        quantisize(qFactor);
        encode();
    }


    public void decompress() {
        unquantisize(qFactor);
        transformInverseDCT();
        UnShift();
        ColorController.transformToRGB(mcuRed, mcuGreen, mcuBlue);
    }

    public void writeBinaryFile() {
        JFileChooser chose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        if (chose.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File location = new File(chose.getSelectedFile().getAbsolutePath());
            
            try (FileOutputStream out = new FileOutputStream(location, false)) {
                encodedBytes.writeTo(out);

            } catch (FileNotFoundException eF) {
                eF.printStackTrace();

            } catch (IOException eIo) {
                eIo.printStackTrace();
            }
        }
    }

    public void encode() {
        short[][][] zigzag = new short[3][mcuBlue.length][mcuSize];
        zigzag[0] = getZigZagArray(mcuRed);
        zigzag[1] = getZigZagArray(mcuGreen);
        zigzag[2] = getZigZagArray(mcuBlue);

        try {
            encodedBytes.write(new byte[] { (byte) (height >>> 8), (byte) (height & 0xff) });
            encodedBytes.write(new byte[] { (byte) (width >>> 8), (byte) (width & 0xff) });
            encodedBytes.write((int) Math.sqrt(zigzag[0][0].length));
            encodedBytes.write(qFactor);

            for (int n = 0; n < 3; n++) {
                for (int k = 0; k < zigzag[n].length; k++) {
                    short count = 0;
                    short curele;
                    boolean eob = false;
                    for (int i = 0; i < zigzag[n][0].length; i++) {
                        if (eob == true) {
                            break;
                        }
                        curele = zigzag[n][k][i];

                        if (curele == 0) {
                            for (int j = i; j < zigzag[n][0].length; j++) {
                                if ((j == mcuSize - 1) && (zigzag[n][k][j] == 0)) {
                                    encodedBytes.write(0);
                                    eob = true;
                                    break;
                                }
                                if (zigzag[n][k][j] == 0) {
                                    continue;
                                } else {
                                    break;
                                }
                            }
                        }
                        if (eob == true) {
                            break;
                        }
                        if (curele == 0) {
                            count++;
                            continue;
                        }
                        if (count == 15) {
                            encodedBytes.write(0xf0);
                            count = 0;
                            continue;
                        }
                        if (curele != 0) {
                            byte leading = (byte) count;
                            byte[] payload = toByteArray(curele);
                            byte bytes = (byte) payload.length;
                            leading <<= 4;
                            leading = (byte) (leading | bytes);
                            encodedBytes.write(leading);
                            encodedBytes.write(payload);
                            count = 0;
                        }
                        if (i == mcuSize - 1) {
                            encodedBytes.write(0);
                            eob = true;
                        }
                    }
                }
            }
        } catch (IOException eIo) {
            eIo.printStackTrace();
        }
    }
    
        
        

    public void decode(File location) {
        try (FileInputStream fileInputStream = new FileInputStream(location)) {
            byte[] buff = new byte[2];
            buff = fileInputStream.readNBytes(2);
            setHeight(((short) (buff[0] << 8 | buff[1] & 0xff)));
            buff = fileInputStream.readNBytes(2);
            setWidth(((short) (buff[0] << 8 | buff[1] & 0xff)));
            int mcuSizebuff =  fileInputStream.read();
            setMcuSize(mcuSizebuff * mcuSizebuff);
            setqFactor(fileInputStream.read());
            setMcuCount((height * width) / mcuSize);

            buff = fileInputStream.readAllBytes();
            short[][][] zigZagArrays = new short[3][mcuCount][mcuSize];
            int n = 0;
            int index = 0;
            int size = 0;
            int count = 0;
            int mcu = 0;
            for (int i = 0; i < buff.length; i++) {
                if (index > mcuSize-1) {
                    index = 0;
                    continue;
                }
                if (n == 3) {
                    break;
                }
                if (buff[i] == 0xf0) {
                    index += 15;
                    continue;
                }
                count = ((buff[i] & 0xff) >>> 4);
                size = (buff[i] & 0x0f);
                index += count;
                if (buff[i] == 0x00) {
                    mcu += 1;
                    if (mcu >= (height * width / mcuSize)) {
                        n++;
                        mcu = 0;
                    }
                    index = 0;
                    continue;
                }
                if (index > mcuSize-1) {
                    index = 0;
                    continue;
                }
                if (size == 1) {
                    zigZagArrays[n][mcu][index] = toShortArray(new byte[] { buff[i + 1] });
                    i += 1;
                    index += 1;
                    continue;
                }
                if (size == 2) {
                    zigZagArrays[n][mcu][index] = toShortArray(new byte[] { buff[i + 1], buff[i + 2] });
                    i += 2;
                    index += 1;
                }
            }
            mcuRed = new short[mcuCount][(int)Math.sqrt(mcuSize)][(int)Math.sqrt(mcuSize)];
            mcuGreen = new short[mcuCount][(int)Math.sqrt(mcuSize)][(int)Math.sqrt(mcuSize)];
            mcuBlue = new short[mcuCount][(int)Math.sqrt(mcuSize)][(int)Math.sqrt(mcuSize)];
            setZigZag(zigZagArrays[0], mcuRed);
            setZigZag(zigZagArrays[1], mcuGreen);
            setZigZag(zigZagArrays[2], mcuBlue);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void quantisize(int n) {
        double S = ((200d - 2d * n) / 100d);
        for (int i = 0; i < mcuBlue.length; i++) {
            for (int j = 0; j < mcuBlue[0].length; j++) {
                for (int k = 0; k < mcuBlue[0].length; k++) {
                    double scaledLum = Math.ceil(S * luminance[j][k]);
                    double scaledChrom = Math.ceil(S * chrominance[j][k]);
                    if (scaledLum == 0)
                        scaledChrom = 1;
                    if (scaledLum == 0)
                        scaledChrom = 1;

                    mcuRed[i][j][k] = (short) (Math.round(mcuRed[i][j][k] / scaledLum));
                    mcuGreen[i][j][k] = (short) (Math.round(mcuGreen[i][j][k] / scaledChrom));
                    mcuBlue[i][j][k] = (short) (Math.round(mcuBlue[i][j][k] / scaledChrom));
                }
            }
        }
    }

    private void unquantisize(int n) {
        double S = ((200d - 2d * n) / 100d);
        for (int i = 0; i < mcuBlue.length; i++) {
            for (int j = 0; j < mcuBlue[0].length; j++) {
                for (int k = 0; k < mcuBlue[0].length; k++) {
                    double scaledLum = Math.ceil(S * luminance[j][k]);
                    double scaledChrom = Math.ceil(S * chrominance[j][k]);
                    if (scaledLum == 0)
                        scaledChrom = 1;
                    if (scaledLum == 0)
                        scaledChrom = 1;
                        
                    mcuRed[i][j][k] = (short) (Math.round(mcuRed[i][j][k] * scaledLum));
                    mcuGreen[i][j][k] = (short) (Math.round(mcuGreen[i][j][k] * scaledChrom));
                    mcuBlue[i][j][k] = (short) (Math.round(mcuBlue[i][j][k] * scaledChrom));
                }
            }
        }
    }

    private void transformDCT() {
        for (int i = 0; i < mcuBlue.length; i++) {
            mcuRed[i] = DctOperator.forward(mcuRed[i]);
            mcuGreen[i] = DctOperator.forward(mcuGreen[i]);
            mcuBlue[i] = DctOperator.forward(mcuBlue[i]);
        }
    }

    private void transformInverseDCT() {
        for (int i = 0; i < mcuBlue.length; i++) {
            mcuRed[i] = DctOperator.backward(mcuRed[i]);
            mcuGreen[i] = DctOperator.backward(mcuGreen[i]);
            mcuBlue[i] = DctOperator.backward(mcuBlue[i]);
        }
    }

    private void ShiftAroundZero() {
        for (int i = 0; i < mcuBlue.length; i++) {
            for (int j = 0; j < mcuBlue[0].length; j++) {
                for (int k = 0; k < mcuBlue[0][0].length; k++) {
                    mcuRed[i][j][k] -= 128;
                    mcuGreen[i][j][k] -= 128;
                    mcuBlue[i][j][k] -= 128;
                }
            }
        }
    }

    private void UnShift() {
        for (int i = 0; i < mcuBlue.length; i++) {
            for (int j = 0; j < mcuBlue[0].length; j++) {
                for (int k = 0; k < mcuBlue[0][0].length; k++) {
                    mcuRed[i][j][k] += 128;
                    mcuGreen[i][j][k] += 128;
                    mcuBlue[i][j][k] += 128;
                }
            }
        }
    }

    private void KeepNZigZag(int n) {
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < mcuBlue.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            for (int len = 1; len <= mcuBlue[0].length; ++len) {
                for (int i = 0; i < len; ++i) {
                    if (count >= n) {
                        mcuRed[k][row][col] = 0;
                        mcuGreen[k][row][col] = 0;
                        mcuBlue[k][row][col] = 0;
                    }
                    count++;
                    if (i + 1 == len)
                        break;
                    // If row_increment value is true
                    // increment row and decrement col
                    // else decrement row and increment
                    // col
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        --row;
                        ++col;
                    }
                }

                if (len == mcuBlue[0].length)
                    break;
                // Update row or col value according
                // to the last increment
                if (row_inc) {
                    ++row;
                    row_inc = false;
                } else {
                    ++col;
                    row_inc = true;
                }
            }

            if (row == 0) {
                if (col == mcuBlue[0].length - 1)
                    ++row;
                else
                    ++col;
                row_inc = true;
            } else {
                if (row == mcuBlue[0].length - 1)
                    ++col;
                else
                    ++row;
                row_inc = false;
            }

            for (int len, diag = mcuBlue[0].length - 1; diag > 0; --diag) {

                if (diag > mcuBlue[0].length)
                    len = mcuBlue[0].length;
                else
                    len = diag;

                for (int i = 0; i < len; ++i) {
                    if (count >= n) {
                        mcuRed[k][row][col] = 0;
                        mcuGreen[k][row][col] = 0;
                        mcuBlue[k][row][col] = 0;
                    }
                    count++;

                    if (i + 1 == len)
                        break;
                    // Update row or col value according
                    // to the last increment
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        ++col;
                        --row;
                    }
                }

                // Update the indexes of row and col variable
                if (row == 0 || col == (mcuBlue[0].length - 1)) {
                    if (col == (mcuBlue[0].length - 1))
                        ++row;
                    else
                        ++col;

                    row_inc = true;
                }

                else if (col == 0 || row == (mcuBlue[0].length - 1)) {
                    if (row == (mcuBlue[0].length - 1))
                        ++col;
                    else
                        ++row;

                    row_inc = false;
                }
            }
        }
    }

    private void setZigZag(short src[][], short dest[][][]) {
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < dest.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            for (int len = 1; len <= dest[0].length; ++len) {
                for (int i = 0; i < len; ++i) {
                    dest[k][row][col] = src[k][count];
                    count++;
                    if (i + 1 == len)
                        break;
                    // If row_increment value is true
                    // increment row and decrement col
                    // else decrement row and increment
                    // col
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        --row;
                        ++col;
                    }
                }

                if (len == dest[0].length)
                    break;
                // Update row or col value according
                // to the last increment
                if (row_inc) {
                    ++row;
                    row_inc = false;
                } else {
                    ++col;
                    row_inc = true;
                }
            }

            // Update the indexes of row and col variable
            if (row == 0) {
                if (col == dest[0].length - 1)
                    ++row;
                else
                    ++col;
                row_inc = true;
            } else {
                if (row == dest[0].length - 1)
                    ++col;
                else
                    ++row;
                row_inc = false;
            }

            for (int len, diag = dest[0].length - 1; diag > 0; --diag) {

                if (diag > dest[0].length)
                    len = dest[0].length;
                else
                    len = diag;

                for (int i = 0; i < len; ++i) {
                    dest[k][row][col] = src[k][count];
                    count++;

                    if (i + 1 == len)
                        break;
                    // Update row or col value according
                    // to the last increment
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        ++col;
                        --row;
                    }
                }

                // Update the indexes of row and col variable
                if (row == 0 || col == (dest[0].length - 1)) {
                    if (col == (dest[0].length - 1))
                        ++row;
                    else
                        ++col;

                    row_inc = true;
                }

                else if (col == 0 || row == (dest[0].length - 1)) {
                    if (row == (dest[0].length - 1))
                        ++col;
                    else
                        ++row;

                    row_inc = false;
                }
            }
        }
    }

    private short[][] getZigZagArray(short[][][] src) {
        short[][] result = new short[src.length][64];
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < src.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            for (int len = 1; len <= src[0].length; ++len) {
                for (int i = 0; i < len; ++i) {
                    result[k][count] = (short) src[k][row][col];
                    count++;
                    if (i + 1 == len)
                        break;
                    // If row_increment value is true
                    // increment row and decrement col
                    // else decrement row and increment
                    // col
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        --row;
                        ++col;
                    }
                }

                if (len == src[0].length)
                    break;
                // Update row or col value according
                // to the last increment
                if (row_inc) {
                    ++row;
                    row_inc = false;
                } else {
                    ++col;
                    row_inc = true;
                }
            }

            // Update the indexes of row and col variable
            if (row == 0) {
                if (col == src[0].length - 1)
                    ++row;
                else
                    ++col;
                row_inc = true;
            } else {
                if (row == src[0].length - 1)
                    ++col;
                else
                    ++row;
                row_inc = false;
            }

            for (int len, diag = src[0].length - 1; diag > 0; --diag) {

                if (diag > src[0].length)
                    len = src[0].length;
                else
                    len = diag;

                for (int i = 0; i < len; ++i) {
                    result[k][count] = (short) src[k][row][col];
                    count++;

                    if (i + 1 == len)
                        break;
                    // Update row or col value according
                    // to the last increment
                    if (row_inc) {
                        ++row;
                        --col;
                    } else {
                        ++col;
                        --row;
                    }
                }

                // Update the indexes of row and col variable
                if (row == 0 || col == (src[0].length - 1)) {
                    if (col == (src[0].length - 1))
                        ++row;
                    else
                        ++col;

                    row_inc = true;
                }

                else if (col == 0 || row == (src[0].length - 1)) {
                    if (row == (src[0].length - 1))
                        ++col;
                    else
                        ++row;

                    row_inc = false;
                }
            }
        }
        return result;

    }

    public byte[] toByteArray(short src) {
        byte[] result;
        if (src <= 255) {
            result = new byte[1];
            result[0] = (byte) (src & 0xff);
        } else {
            result = new byte[2];
            result[0] = (byte) ((src >> 8) & 0xff);
            result[1] = (byte) (src & 0xff);
        }
        return result;
    }

    public short toShortArray(byte[] src) {
        short result;
        if (src.length == 1) {
            result = src[0];
        } else {
            result = (short) (src[0] << 8 | src[1] & 0xff);
        }
        return result;
    }

    public short getHeight() {
        return height;
    }

    private void setHeight(short height) {
        this.height = height;
    }

    public short getWidth() {
        return width;
    }


    private void setWidth(short width) {
        this.width = width;
    }

    public double getEncodeSizeKb() {
        return encodedBytes.size() / 1024d;
    }
    
    /**
     * @param mcuSize the mcuSize to set
     */
    private void setMcuSize(int mcuSize) {
        this.mcuSize = mcuSize;
    }

    /**
     * @param mcuCount the mcuCount to set
     */
    private void setMcuCount(int mcuCount) {
        this.mcuCount = mcuCount;
    }

    /**
     * @param qFactor the qFactor to set
     */
    private void setqFactor(int qFactor) {
        this.qFactor = qFactor;
    }

}
