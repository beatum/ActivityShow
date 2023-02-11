package io.github.beatum.utils;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Happy.He
 * @version 1.0
 * @date 2/10/2023 9:11 AM
 */
public class Commons {
    /*
     * convert Mat to Buffer Image
     * */
    public static BufferedImage mat2BufferImage(Mat mat) {
        byte[] tempByteArray = null;
        Mat tempMap = new Mat();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            Imgproc.cvtColor(mat, tempMap, Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int lengthOfPointer = mat.channels() * mat.cols() * mat.rows();
        if (tempByteArray == null || tempByteArray.length != lengthOfPointer) {
            tempByteArray = new byte[lengthOfPointer];
        }
        tempMap.get(0, 0, tempByteArray);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), tempByteArray);
        return image;
    }

    /*
     * resize for buffered image
     * */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
