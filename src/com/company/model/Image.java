package com.company.model;

import com.company.utils.ImageUtils;
import com.company.utils.MapUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.util.Hashtable;

public class Image implements Cloneable {
    public BufferedImage bufferedImage;
    public Color characterColor = Color.WHITE;
    public Color backgroundColor = Color.BLACK;

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BufferedImage copyOfImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        Graphics g = copyOfImage.createGraphics();
        g.drawImage(bufferedImage, 0, 0, null);
        return new Image(copyOfImage);
    }

    public int getSize(){
        return getWidth() * getHeight();
    }

    public int getWidth() {
        return bufferedImage.getWidth();
    }

    public int getHeight() {
        return bufferedImage.getHeight();
    }

    /**
     * Turns the image to a gray scaled one
     * @return
     */
    public void grayScale() {

        int alpha, red, green, blue;
        int newPixel;

        for(int i=0; i < getWidth(); i++) {
            for(int j=0; j < getHeight(); j++) {

                // Get pixels by R, G, B
                alpha = new Color(bufferedImage.getRGB(i, j)).getAlpha();
                red = new Color(bufferedImage.getRGB(i, j)).getRed();
                green = new Color(bufferedImage.getRGB(i, j)).getGreen();
                blue = new Color(bufferedImage.getRGB(i, j)).getBlue();

                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                // Return back to original format
                newPixel = ImageUtils.colorToRGB(alpha, red, red, red);

                // Write pixels into image
                bufferedImage.setRGB(i, j, newPixel);

            }
        }
    }

    /**
     * Return the image to black and white only
     * @return
     */
    public BinaryImage getBynaryImage(Color characterColor, Color backgroundColor) {
        int red;
        int newPixel;
        int threshold = getThreshold();

        BufferedImage binaryImage = new BufferedImage(getWidth(), getHeight(), bufferedImage.getType());

        for(int i=0; i < getWidth(); i++) {
            for(int j=0; j < getHeight(); j++) {

                // Get pixels
                red = new Color(bufferedImage.getRGB(i, j)).getRed();
                int alpha = new Color(bufferedImage.getRGB(i, j)).getAlpha();
                if(red > threshold) {
                    newPixel = 255;
                }
                else {
                    newPixel = 0;
                }
                newPixel = ImageUtils.colorToRGB(alpha, newPixel, newPixel, newPixel);
                binaryImage.setRGB(i, j, newPixel);
            }
        }
        return new BinaryImage(binaryImage, characterColor, backgroundColor);
    }

    /**
     * Get the threshold used to turn the image to black and white
     * @return
     */
    public int getThreshold() {

        int[] histogram = getImageHistogram();
        int total = getHeight() * getWidth();

        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;

            if(wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

    /**
     * Returns the histogram of the image
     */
    public int[] getImageHistogram() {

        int[] histogram = new int[256];

        for(int i=0; i<histogram.length; i++)
            histogram[i] = 0;

        for(int i=0; i< getWidth(); i++) {
            for(int j=0; j< getHeight(); j++) {
                int red = new Color(bufferedImage.getRGB (i, j)).getRed();
                histogram[red]++;
            }
        }
        return histogram;
    }

    /**
     * Return a scaled image
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    public void scale(int width, int height) {
        int imageWidth  = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();

        double scaleX = (double)width/imageWidth;
        double scaleY = (double)height/imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        bufferedImage = bilinearScaleOp.filter(
                bufferedImage,
                null);
    }

    /**
     * Return true if the background is white for the binarized image given as a parameter
     * @return
     */
    public int computeImageBackgroundColor() {

        int imageWidth = getWidth();
        int imageHeight = getHeight();

        Hashtable<Integer, Integer> colors = new Hashtable<>();

        int color = 0;

        for(int i=0; i<imageWidth; i++){
            color = bufferedImage.getRGB(i, 0);
            if(colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 0);
            }

            color = bufferedImage.getRGB(i, imageHeight-1);
            if(colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 0);
            }
        }

        for(int i=0; i<imageHeight; i++){
            color = bufferedImage.getRGB(0, i);
            if(colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 0);
            }

            color = bufferedImage.getRGB(imageWidth-1, i);
            if(colors.containsKey(color)) {
                colors.put(color, colors.get(color) + 1);
            } else {
                colors.put(color, 0);
            }
        }

        return MapUtils.getKeyOfMaximum(colors);
    }

    /**
     * Return an image with an applied Gaussian Blur
     * @return
     */
    public void gaussianBlur(){
        float[] matrix = {
                1/16f, 1/8f, 1/16f,
                1/8f, 1/4f, 1/8f,
                1/16f, 1/8f, 1/16f,
        };

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);
        bufferedImage = op.filter(bufferedImage, null);

    }

    public void crop(Rectangle rect) {
        bufferedImage = bufferedImage.getSubimage(rect.left.x, rect.top.y, rect.getWidth(), rect.getHeight());
    }

    public com.company.model.Rectangle getBoundaries(int characterColor) {
        Rectangle rectangle = new Rectangle();
        rectangle.top = new Pixel(0, getHeight());
        rectangle.left = new Pixel(getWidth(), 0);
        rectangle.bottom = new Pixel(0, 0);
        rectangle.right = new Pixel(0, 0);

        for(int y = 0; y < getHeight(); y++) {
            for(int x = 0; x < getWidth(); x++) {
                if(bufferedImage.getRGB(x, y) == characterColor) {
                    if(y < rectangle.top.y) {
                        rectangle.top = new Pixel(x, y);
                    }
                    if(x < rectangle.left.x) {
                        rectangle.left = new Pixel(x, y);
                    }
                    if(y > rectangle.bottom.y) {
                        rectangle.bottom = new Pixel(x, y);
                    }
                    if(x > rectangle.right.x) {
                        rectangle.right = new Pixel(x, y);
                    }
                }
            }
        }
        if(rectangle.getWidth() == bufferedImage.getWidth()
                || rectangle.getHeight() == bufferedImage.getHeight()
                || rectangle.getHeight() == 0
                || rectangle.getWidth() == 0) {
            rectangle.top = new Pixel(0, 0);
            rectangle.left = new Pixel(0, 0);
            rectangle.bottom = new Pixel(0, bufferedImage.getHeight() - 1);
            rectangle.right = new Pixel(bufferedImage.getWidth() - 1, 0);
        }
        return rectangle;
    }
}