![Alt text](readmeImage.jpg?raw=true "Title")

## About

This project is the practical part of my batchelors work. It is a self implementation of lossy image compression using the Descreet Cosine Transfrom(DCT) based on the methodology of JPEG. There are no external dependancies. The project uses only standart Java libs. Curently there are two implemented dct's the slow direct way and the faster AAN algorith.

The app has a gui that allows the user to:

- open standart image files (jpg,png,bmp.....) 
- compress and encode them to a binary file based on the users quality settings of 1-100 and original coefficients to keep 1-64, 
- decode the binary files back to the now altered but lossly compressed image
- save the currently loaded image

## Compression steps

1. Load image as sRGB representation.
2. Transform into YCbCr colorspace (0-255).
3. Chop image into blocks of 8x8(MCU), if the width and height are not devisible by 8 resize the image to fit.
4. Shift the values around 0 (Xn = x - 128).
5. Apply the DCT transformation first on rows of MCU matrix then on columns by effect of tranpose.
6. Quntise the resulting matrix based on the standart JPEG qunatization tables for luminance and chrominance.
7. (Optional) Keep N matrix elements in zigzag fashion starting from top left to bottom right.
8. Run lenth encode the zigzag matrix starting from top left to bottom right based on the amount of zeroes preceding each element in little endian format. With first 1 byte for length of 0 and amount of bytes to code the Value. The first 4 bits are for 0 and the last 4 bits are for amount. Bytes afterward are the Value 
(0 length,amount of bytes to represent X)(Value of x)
Example {10, 0, 1} -> {(0,1)(10); (1,1)(1)  }


## TODO

- Add PSNR and mean statistical error metric
- debug boundary cases of decode algorithm
- clean up code
- unify repeating zigzag methods
- add loading message to UI
- write proper comments
- make compression save dialog force .byte extension
- set max and min open image sizes
- maybe rename methods to accurately represent what they do
- continue developing NxN matrix sizes and allow them
- maybe decouple image logic thread from UI