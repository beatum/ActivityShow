package io.github.beatum;

import io.github.beatum.video.DeviceGroup;
import io.github.beatum.video.IProcessCapture;
import io.github.beatum.video.Video;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;

/**
 * Main
 */
public class App {
    static {
        System.load("D:\\software\\OpenCV – 4.6.0\\opencv\\build\\java\\x64\\opencv_java460.dll");

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //System.setProperty("OPENCV_VIDEOIO_MSMF_ENABLE_HW_TRANSFORMS","0");
                    JFrame windows = new JFrame();
                    windows.setTitle("Demo....");
                    windows.setBounds(200, 200, 850, 600);
                    windows.setLayout(new GridLayout(1, 1));
                    JPanel container = new JPanel();
                    container.setLayout(new GridLayout(2, 3));
                    DeviceGroup group = new DeviceGroup(container, 6);

                    windows.add(container, 0);
                    windows.setVisible(true);

                    group.open();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
