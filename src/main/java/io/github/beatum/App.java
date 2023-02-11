package io.github.beatum;

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
                    JFrame windows = new JFrame();
                    windows.setTitle("Demo....");
                    windows.setBounds(200, 200, 850, 600);
                    GridLayout gridLayout = new GridLayout(2, 3);
                    windows.setLayout(gridLayout);

                    VideoCapture videoCapture1 = new VideoCapture();
                    VideoCapture videoCapture2 = new VideoCapture();
                    VideoCapture videoCapture3 = new VideoCapture();

                    Video viewer1 = new Video(videoCapture1, 0);
                    Video viewer2 = new Video(videoCapture2, 1);
                    Video viewer3 = new Video(videoCapture3, 2);

                    viewer1.setImageProcessingFilter(new IProcessCapture() {
                        public Mat process(Mat mat) {
                            return mat;
                        }
                    });

                    JPanel p1 = new JPanel();
                    p1.setLayout(new GridLayout(1, 1));
                    p1.add(viewer1);

                    JPanel p2 = new JPanel();
                    p2.setLayout(new GridLayout(1, 1));
                    p2.add(viewer2);

                    JPanel p3 = new JPanel();
                    p3.setLayout(new GridLayout(1, 1));
                    p3.add(viewer3);

                    windows.add(p1, 0);
                    windows.add(p2, 1);
                    windows.add(p3, 2);
                    windows.add(new Label("004-Empty"), 3);
                    windows.add(new Label("005-Empty"), 4);
                    windows.add(new Label("006-Empty"), 5);

                    windows.setVisible(true);
                    viewer1.start();
                    viewer2.start();
                    viewer3.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
