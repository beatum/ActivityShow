package io.github.beatum;

import io.github.beatum.utils.Commons;
import junit.framework.TestCase;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    static {
        System.load("D:\\software\\OpenCV4.6.0\\opencv\\build\\java\\x64\\opencv_java460.dll");
    }

    public AppTest(String testName) {
        super(testName);
    }

    @Test
    public void test1() {
       // Mat img = Imgcodecs.imread("D:\\Happy\\qrhappy.jpg");
        //BufferedImage bufferedImage = Commons.mat2BufferImage(img);
        //.\System.out.println("Done...");
        assertTrue(true);
    }

}
