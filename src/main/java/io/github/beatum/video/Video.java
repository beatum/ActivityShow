package io.github.beatum.video;

import io.github.beatum.utils.Commons;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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
    VideoCapture videoCapture;

    //index of capture device
    int indexOfDevice = 0;

    //Thread of capture
    private Thread threadOfCapture = null;

    //Buffered image
    private BufferedImage bufferedImage;

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public IProcessCapture getImageProcessingFilter() {
        return imageProcessingFilter;
    }

    public void setImageProcessingFilter(IProcessCapture imageProcessingFilter) {
        this.imageProcessingFilter = imageProcessingFilter;
    }

    //Image processing event
    private IProcessCapture imageProcessingFilter = null;

    private Video() {
    }

    public Video(VideoCapture videoCapture, int index) {
        this.setLayout(getDefaultLayout());
        this.videoCapture = videoCapture;
        this.indexOfDevice = index;
        this.init();
    }

    /*
     * initialization
     * */
    private void init() {
        if (!videoCapture.isOpened()) {
            videoCapture.open(indexOfDevice);
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
            if (videoCapture.read(tempMap)) {

                //Image processing filter
                if (null != imageProcessingFilter) {
                    tempMap = imageProcessingFilter.process(tempMap);
                }

                Number w = getParent().getWidth();
                Number h = getParent().getHeight();

                Mat resizedMap = new Mat();
                Imgproc.resize(tempMap, resizedMap, new Size(w.doubleValue(), h.doubleValue()));
                bufferedImage = Commons.mat2BufferImage(resizedMap);
                repaint();

            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }
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
        g.drawImage(bufferedImage, 0, 0, this);
    }
}
