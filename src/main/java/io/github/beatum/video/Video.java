package io.github.beatum.video;

import io.github.beatum.utils.Commons;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Happy.He
 * @version 1.0
 * @date 2/10/2023 9:09 AM
 */
public class Video extends JPanel implements Runnable {

    //Video capture
    private VideoCapture videoCapture;

    //index of capture device
    private int indexOfDevice = 0;

    private int apiPreference = 0;

    //Thread of capture
    private Thread threadOfCapture = null;

    //image for displaying
    private Image image4Displaying;

    //swap mat
    private Mat resizedMap = new Mat();

    public IProcessCapture getImageProcessingFilter() {
        return imageProcessingFilter;
    }

    public VideoCapture getVideoCapture() {
        return videoCapture;
    }

    public void setImageProcessingFilter(IProcessCapture imageProcessingFilter) {
        this.imageProcessingFilter = imageProcessingFilter;
    }

    //Image processing event
    private IProcessCapture imageProcessingFilter = null;

    private Video() {
    }

    public Video(VideoCapture videoCapture, int apiPreference, int index) {
        this.setLayout(getDefaultLayout());
        this.videoCapture = videoCapture;
        this.indexOfDevice = index;
        this.apiPreference = apiPreference;
        this.init();
    }

    /*mvn
     * initialization
     * */
    private void init() {
        //videoCapture.set(6, VideoWriter.fourcc('M','J','P','G'));
        if (!videoCapture.isOpened()) {
            videoCapture.open(indexOfDevice, apiPreference);
        }
    }

    /*
     * start
     * */
    public void start() {
        if (null == threadOfCapture) {
            threadOfCapture = new Thread(this);
            threadOfCapture.start();
        }
    }

    /*
     * stop
     * */
    public void stop() {
        if (null != threadOfCapture) {
            threadOfCapture.stop();
            threadOfCapture = null;
        }
    }

    public void run() {
        Mat tempMap = new Mat();
        while (true) {
            try {
                if (videoCapture.read(tempMap)) {
                    //Image processing filter
                    if (null != imageProcessingFilter) {
                        tempMap = imageProcessingFilter.process(tempMap);
                    }
                    Number w = getParent().getWidth();
                    Number h = getParent().getHeight();

                    Imgproc.resize(tempMap, resizedMap, new Size(w.doubleValue(), h.doubleValue()));
                    image4Displaying = HighGui.toBufferedImage(resizedMap);
                    repaint();
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        this.stop();
                        threadOfCapture = null;
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error:" + ex.getMessage());
                this.stop();
                threadOfCapture = null;
            }
        }
    }


    /*
     * get default layout
     */
    private LayoutManager getDefaultLayout() {
        GridLayout gridLayout = new GridLayout(1, 1);
        gridLayout.setHgap(0);
        gridLayout.setVgap(0);
        return gridLayout;
    }

    /*
     * paint component
     * */
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image4Displaying, 0, 0, this);
    }
}
