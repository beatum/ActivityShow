package io.github.beatum.video;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

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
    private BufferedImage image4Displaying;

    //swap mat
    private Mat resizedMap = new Mat();

    //width of frame.
    private int frameWidth = 1366;
    //height of frame.
    private int frameHeight = 768;

    private byte[] tempByteArray;

    byte[] targetPixels = null;

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

        if (!videoCapture.isOpened()) {
            videoCapture.open(indexOfDevice, apiPreference);
            videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, frameWidth);
            videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, frameHeight);
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

                    int type = 10;
                    if (resizedMap.channels() > 1) {
                        type = 5;
                    }
                    int bufferSize = resizedMap.channels() * resizedMap.cols() * resizedMap.rows();

                    tempByteArray = null;
                    tempByteArray = new byte[bufferSize];
                    resizedMap.get(0, 0, tempByteArray);
                    image4Displaying = null;
                    image4Displaying = new BufferedImage(resizedMap.cols(), resizedMap.rows(), type);
                    targetPixels = ((DataBufferByte) image4Displaying.getRaster().getDataBuffer()).getData();
                    System.arraycopy(tempByteArray, 0, targetPixels, 0, tempByteArray.length);
                    repaint();

                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                }
            } catch (Exception ex) {
                //System.out.println("Error:" + ex.getMessage());
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
    //synchronized
    protected synchronized void paintComponent(Graphics g) {
        //super.paintComponent(g);
        if (null != image4Displaying) {
            g.drawImage(image4Displaying, 0, 0, this);
            //image4Displaying = null;
            tempByteArray = null;
            targetPixels = null;
        }
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

}
