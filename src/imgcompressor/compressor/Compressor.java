package imgcompressor.compressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import imgcompressor.utils.ColorController;
import imgcompressor.utils.dft2D;

public class Compressor {
    private short[][][] MCUred;
    private short[][][] MCUgreen;
    private short[][][] MCUblue;


    private short height;
    private short width;
    private int mcuSize;
    private int qFactor;
    private File encodeLocation;
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

    public Compressor() {
        this.MCUred = null;
        this.MCUblue = null;
        this.MCUgreen = null;
        this.height = 0;
        this.width = 0;
        this.mcuSize = 8;
    }

    public Compressor(int[][][] src, short height, short width) {
        this.MCUred = new short[src.length][src[0].length][src[0].length];
        this.MCUgreen = new short[src.length][src[0].length][src[0].length];
        this.MCUblue = new short[src.length][src[0].length][src[0].length];
        this.height = height;
        this.width = width;
        this.mcuSize = src.length;
        fill(src);
    }

    public void fill(int[][][] src) {
        for (int i = 0; i < src.length; i++) {
            MCUred[i] = ColorController.GetRedArray(src[i]);
            MCUgreen[i] = ColorController.GetGreenArray(src[i]);
            MCUblue[i] = ColorController.GetBlueArray(src[i]);
        }
    }

    public int[][][] GetsRGBArray() {
        int[][][] temp = new int[MCUblue.length][MCUblue[0].length][MCUblue[0].length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = ColorController.GetRGBArray(MCUred[i], MCUgreen[i], MCUblue[i]);
        }
        return temp;
    }

    public void compress(int qFactor, int n) {
        this.qFactor = qFactor;
        ColorController.RGB2YCbCr(MCUred, MCUgreen, MCUblue);
        ShiftAroundZero();
        transform();
        KeepNZigZag(n);
        quantisize(qFactor);
        encode();
    }

    public void decompress() {
        unquantisize(qFactor);
        transformInverse();
        UnShift();
        ColorController.YCbCr2RGB(MCUred, MCUgreen, MCUblue);
    }

    public void encode() {
        short[][][] zigzag = new short[3][MCUblue.length][64];
        zigzag[0] = GetZigZagArray(MCUred);
        zigzag[1] = GetZigZagArray(MCUgreen);
        zigzag[2] = GetZigZagArray(MCUblue);
        JFileChooser chose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        if (chose.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File location = new File(chose.getSelectedFile().getAbsolutePath());
            encodeLocation = location;
            try (FileOutputStream fileOutputStream = new FileOutputStream(location, false)) {
                fileOutputStream.write(new byte[] { (byte) (height >>> 8), (byte) (height & 0xff) });
                fileOutputStream.write(new byte[] { (byte) (width >>> 8), (byte) (width & 0xff) });
                fileOutputStream.write((int) Math.sqrt(zigzag[0][0].length));
                fileOutputStream.write(qFactor);

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
                                    if (j == 63 && zigzag[n][k][j] == 0) {
                                        fileOutputStream.write(0);
                                        eob = true;
                                    }
                                    if (zigzag[n][k][j] == 0) {
                                        continue;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (curele == 0) {
                                count++;
                                continue;
                            }
                            if (count == 15) {
                                fileOutputStream.write(0xf0);
                                count = 0;
                                continue;
                            }
                            if (curele != 0) {
                                byte leading = (byte) count;
                                byte[] payload = Short2ByteArray(curele);
                                byte bytes = (byte) payload.length;
                                leading <<= 4;
                                leading = (byte) (leading | bytes);
                                fileOutputStream.write(leading);
                                fileOutputStream.write(payload);
                                count = 0;
                            }
                            if (i == 63) {
                                fileOutputStream.write(0);
                                eob = true;
                            }
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void decode(File location) {
        try (FileInputStream fileInputStream = new FileInputStream(location)) {
            byte[] buff = new byte[2];
            buff = fileInputStream.readNBytes(2);
            this.height = ((short) (buff[0] << 8 | buff[1] & 0xff));
            System.out.println((short) (buff[0] << 8 | buff[1] & 0xff));
            buff = fileInputStream.readNBytes(2);
            System.out.println((short) (buff[0] << 8 | buff[1] & 0xff));
            this.width = ((short) (buff[0] << 8 | buff[1] & 0xff));
            this.mcuSize = fileInputStream.read();
            this.qFactor = fileInputStream.read();
            buff = fileInputStream.readAllBytes();
            short[][][] zigzagarrays = new short[3][height * width / 64][64];
            int n = 0;
            int index = 0;
            int size = 0;
            int count = 0;
            int mcu = 0;
            for (int i = 0; i < buff.length; i++) {
                if (index > 63) {
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
                    if (mcu >= (height * width / 64)) {
                        n++;
                        mcu = 0;
                    }
                    index = 0;
                    continue;
                }
                if (index > 63) {
                    index = 0;
                    continue;
                }
                if (size == 1) {
                    zigzagarrays[n][mcu][index] = ByteArray2Short(new byte[] { buff[i + 1] });
                    i += 1;
                    index += 1;
                    continue;
                }
                if (size == 2) {
                    zigzagarrays[n][mcu][index] = ByteArray2Short(new byte[] { buff[i + 1], buff[i + 2] });
                    i += 2;
                    index += 1;
                }
            }
            MCUred = new short[(height * width) / (mcuSize * mcuSize)][mcuSize][mcuSize];
            MCUgreen = new short[(height * width) / (mcuSize * mcuSize)][mcuSize][mcuSize];
            MCUblue = new short[(height * width) / (mcuSize * mcuSize)][mcuSize][mcuSize];
            SetZigZag(zigzagarrays[0], MCUred);
            SetZigZag(zigzagarrays[1], MCUgreen);
            SetZigZag(zigzagarrays[2], MCUblue);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void quantisize() {
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0].length; k++) {
                    MCUred[i][j][k] = (short)Math.round((float) MCUred[i][j][k] / (float) luminance[j][k]);
                    MCUgreen[i][j][k] = (short)Math.round((float) MCUgreen[i][j][k] / (float) chrominance[j][k]);
                    MCUblue[i][j][k] = (short)Math.round((float) MCUblue[i][j][k] / (float) chrominance[j][k]);
                }
            }
        }
    }

    private void quantisize(int n) {
        int S = (n >= 50) ? (5000 / n) : (200 - (2 * n));
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0].length; k++) {
                    MCUred[i][j][k] /= (Math.floor((S * luminance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                    MCUgreen[i][j][k] /= (Math.floor((S * chrominance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                    MCUblue[i][j][k] /= (Math.floor((S * chrominance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                }
            }
        }
    }

    public void unquantisize() {
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0].length; k++) {
                    MCUred[i][j][k] = (short)Math.round((float) MCUred[i][j][k] * (float) luminance[j][k]);
                    MCUgreen[i][j][k] = (short)Math.round((float) MCUgreen[i][j][k] * (float) chrominance[j][k]);
                    MCUblue[i][j][k] = (short)Math.round((float) MCUblue[i][j][k] * (float) chrominance[j][k]);
                }
            }
        }
    }

    private void unquantisize(int n) {
        int S = (n >= 50) ? (5000 / n) : (200 - (2 * n));
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0].length; k++) {
                    MCUred[i][j][k] *= (Math.floor((S * luminance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                    MCUgreen[i][j][k] *= (Math.floor((S * chrominance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                    MCUblue[i][j][k] *= (Math.floor((S * chrominance[j][k]) / 100) == 0) ? (1)
                            : (short)(Math.floor(S * luminance[j][k] / 100));
                }
            }
        }
    }

    private void transform() {
        for (int i = 0; i < MCUblue.length; i++) {
            MCUred[i] = dft2D.forward(MCUred[i]);
            MCUgreen[i] = dft2D.forward(MCUgreen[i]);
            MCUblue[i] = dft2D.forward(MCUblue[i]);
        }
    }

    private void transformInverse() {
        for (int i = 0; i < MCUblue.length; i++) {
            MCUred[i] = dft2D.backward(MCUred[i]);
            MCUgreen[i] = dft2D.backward(MCUgreen[i]);
            MCUblue[i] = dft2D.backward(MCUblue[i]);
        }
    }

    private void ShiftAroundZero() {
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0][0].length; k++) {
                    MCUred[i][j][k] -= 128;
                    MCUgreen[i][j][k] -= 128;
                    MCUblue[i][j][k] -= 128;
                }
            }
        }
    }

    private void UnShift() {
        for (int i = 0; i < MCUblue.length; i++) {
            for (int j = 0; j < MCUblue[0].length; j++) {
                for (int k = 0; k < MCUblue[0][0].length; k++) {
                    MCUred[i][j][k] += 128;
                    MCUgreen[i][j][k] += 128;
                    MCUblue[i][j][k] += 128;
                }
            }
        }
    }

    private void KeepNZigZag(int n) {
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < MCUblue.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            // Print matrix of lower half zig-zag pattern
            for (int len = 1; len <= MCUblue[0].length; ++len) {
                for (int i = 0; i < len; ++i) {
                    if (count > n) {
                        MCUred[k][row][col] = 0;
                        MCUgreen[k][row][col] = 0;
                        MCUblue[k][row][col] = 0;
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

                if (len == MCUblue[0].length)
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
                if (col == MCUblue[0].length - 1)
                    ++row;
                else
                    ++col;
                row_inc = true;
            } else {
                if (row == MCUblue[0].length - 1)
                    ++col;
                else
                    ++row;
                row_inc = false;
            }

            // Print the next half zig-zag pattern
            for (int len, diag = MCUblue[0].length - 1; diag > 0; --diag) {

                if (diag > MCUblue[0].length)
                    len = MCUblue[0].length;
                else
                    len = diag;

                for (int i = 0; i < len; ++i) {
                    if (count > n) {
                        MCUred[k][row][col] = 0;
                        MCUgreen[k][row][col] = 0;
                        MCUblue[k][row][col] = 0;
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
                if (row == 0 || col == (MCUblue[0].length - 1)) {
                    if (col == (MCUblue[0].length - 1))
                        ++row;
                    else
                        ++col;

                    row_inc = true;
                }

                else if (col == 0 || row == (MCUblue[0].length - 1)) {
                    if (row == (MCUblue[0].length - 1))
                        ++col;
                    else
                        ++row;

                    row_inc = false;
                }
            }
        }
    }

    private void SetZigZag(short src[][], short dest[][][]) {
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < dest.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            // Print matrix of lower half zig-zag pattern
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

            // Print the next half zig-zag pattern
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

    private short[][] GetZigZagArray(short[][][] src) {
        short[][] result = new short[src.length][64];
        int row, col, count;
        boolean row_inc = false;
        for (int k = 0; k < src.length; k++) {
            row = 0;
            col = 0;
            count = 0;
            // Print matrix of lower half zig-zag pattern
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

            // Print the next half zig-zag pattern
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

    public byte[] Short2ByteArray(short src) {
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

    public short ByteArray2Short(byte[] src) {
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

    public void setHeight(short height) {
        this.height = height;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public double getEncodeSizeKb() {
        return encodeLocation.length()/1024d;
    }

}
