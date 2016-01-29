package com.company.model;

import com.company.utils.ImageUtils;
import com.company.utils.MapUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.util.Hashtable;

public class BinaryImage extends Image implements Cloneable {

    public Color characterColor = Color.WHITE;
    public Color backgroundColor = Color.BLACK;

    public BinaryImage(BufferedImage bufferedImage, Color characterColor, Color backgroundColor) {
        super(bufferedImage);
        characterColor = characterColor;
        backgroundColor = backgroundColor;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
     * Invert the colors of a binarized image
     * @return
     */
    public void invertImageColors(){
        for (int i=0; i < bufferedImage.getWidth(); i++){
            for (int j=0; j < bufferedImage.getHeight(); j++) {
                if (bufferedImage.getRGB(i, j) == characterColor.getRGB()) {
                    bufferedImage.setRGB(i, j, backgroundColor.getRGB());
                } else {
                    bufferedImage.setRGB(i, j, characterColor.getRGB());
                }
            }
        }
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
     * Apply a mask to an image and return the result
     * @param mask
     * @return
     */
    public void applyMask(final BufferedImage mask){
        int width = getWidth();
        int height = getHeight();
        int imageColor;

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                imageColor = bufferedImage.getRGB(j, i);
                if(!(imageColor == characterColor.getRGB() && mask.getRGB(j, i) == imageColor)){
                    bufferedImage.setRGB(j, i, backgroundColor.getRGB());
                }
            }
        }
    }


    /**
     * Return the repartition of pixel
     */
    public float[] getRepartition (int characterColor){
        float[] pixelCount = new float[4];

        int width = getWidth();
        int height = getHeight();
        float topLeftWhitePixelCount = 0;
        float topRightWhitePixelCount = 0;
        float bottomLeftWhitePixelCount = 0;
        float bottomRightWhitePixelCount = 0;
        float totalPixelCount = 0;

        for (int i=0; i<(width/2); i++){
            for (int j=0; j<(height/2); j++){
                if(bufferedImage.getRGB(i, j) == characterColor){
                    topLeftWhitePixelCount++;
                }
                totalPixelCount++;
            }
            for (int k=(height/2); k<height; k++){
                if(bufferedImage.getRGB(i, k) == characterColor) {
                    bottomLeftWhitePixelCount++;
                }
                totalPixelCount++;
            }
        }

        for (int i=(width/2); i<width; i++){
            for (int j=0; j<(height/2); j++){
                if(bufferedImage.getRGB(i, j) == characterColor) {
                    topRightWhitePixelCount++;
                }
                totalPixelCount++;
            }
            for (int k=(height/2); k<height; k++){
                if(bufferedImage.getRGB(i, k) == characterColor){
                    bottomRightWhitePixelCount++;
                }
                totalPixelCount++;
            }
        }
        /*
        pixelCount[0] = topLeftWhitePixelCount/totalPixelCount;
        pixelCount[1] = topRightWhitePixelCount/totalPixelCount;
        pixelCount[2] = bottomLeftWhitePixelCount/totalPixelCount;
        pixelCount[3] = bottomRightWhitePixelCount/totalPixelCount;
        */

        float totalWhitePixelCount = topLeftWhitePixelCount + topRightWhitePixelCount + bottomLeftWhitePixelCount + bottomRightWhitePixelCount;
        pixelCount[0] = topLeftWhitePixelCount/totalWhitePixelCount;
        pixelCount[1] = topRightWhitePixelCount/totalWhitePixelCount;
        pixelCount[2] = bottomLeftWhitePixelCount/totalWhitePixelCount;
        pixelCount[3] = bottomRightWhitePixelCount/totalWhitePixelCount;

        return pixelCount;
    }

    public int[] countPixelsByColors() {
        int[] result = {0, 0};
        for(int y = 0; y < getHeight(); y++) {
            for(int x = 0; x < getWidth(); x++) {
                if(bufferedImage.getRGB(x, y) == characterColor.getRGB()) {
                    result[0] = result[0] + 1;
                } else {
                    result[1] = result[1] + 1;
                }
            }
        }
        return result;
    }
}
