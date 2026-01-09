
package io.github.beatum.video;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Path;

/**
 * A Swing {@link JPanel} that continuously grabs frames from an OpenCV {@link VideoCapture}
 * and renders them on-screen.
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Runs capture on a dedicated background thread</li>
 *   <li>Paints on Swing EDT, uses double-buffering to reduce flicker</li>
 *   <li>Supports an optional frame processing filter ({@link IProcessCapture})</li>
 *   <li>Provides thread-safe snapshot APIs (for saving images without touching VideoCapture)</li>
 * </ul>
 *
 * <h2>Threading Model</h2>
 * <ul>
 *   <li>Only the capture thread calls {@link VideoCapture#read(Mat)}.</li>
 *   <li>The EDT calls {@link #paintComponent(Graphics)}.</li>
 *   <li>Snapshots clone the last captured frame under a lock.</li>
 * </ul>
 *
 * <p><b>Note:</b> OpenCV frames are typically BGR (not RGB). This class uses
 * {@link BufferedImage#TYPE_3BYTE_BGR} for display.</p>
 *
 * @author Happy.He
 * @version 2.1
 * @since 2023-02-10
 */
public class VideoPanel extends JPanel implements Runnable, AutoCloseable {

    // -------------------- Capture configuration --------------------

    /** OpenCV capture object (camera/stream). */
    private final VideoCapture videoCapture;

    /** Device index for camera capture (0 = default). */
    private final int deviceIndex;

    /** OpenCV API backend preference (e.g. CAP_DSHOW, CAP_MSMF, CAP_ANY). */
    private final int apiPreference;

    /** Requested capture frame width (driver may adjust). */
    private int frameWidth = 1366;

    /** Requested capture frame height (driver may adjust). */
    private int frameHeight = 768;

    /**
     * Optional capture loop throttle in milliseconds.
     * 0 means no artificial delay.
     * Useful if you want to reduce CPU usage.
     */
    private int captureDelayMs = 0;

    // -------------------- Lifecycle / thread --------------------

    /** Background capture thread. */
    private Thread captureThread;

    /** Capture loop flag (safe stop). */
    private volatile boolean running = false;

    /**
     * Optional frame processing callback.
     * Runs on capture thread, so it should be fast.
     */
    private volatile IProcessCapture imageProcessingFilter;

    // -------------------- Rendering buffers (reused) --------------------

    /** Latest image to paint (read by EDT). */
    private volatile BufferedImage imageForDisplay;

    /** Mat used for resizing to panel size. */
    private final Mat resizedMat = new Mat();

    /** Temporary mat for color conversion if needed (e.g., BGRA->BGR). */
    private final Mat convertedMat = new Mat();

    /** Cached raster byte array for BufferedImage (points to internal buffer). */
    private byte[] imagePixels;

    // -------------------- Snapshot buffers --------------------

    /** Lock protecting access to lastFrame. */
    private final Object frameLock = new Object();

    /**
     * Stores the latest captured/processed frame (NOT resized).
     * Used for snapshot capture without reading VideoCapture from UI thread.
     */
    private final Mat lastFrame = new Mat();

    // -------------------- Constructors --------------------

    /** Disable no-arg constructor. */
    private VideoPanel() {
        throw new IllegalStateException("VideoPanel requires a VideoCapture");
    }

    /**
     * Creates a panel that displays frames from the given {@link VideoCapture}.
     *
     * @param videoCapture capture instance (can be unopened initially)
     * @param apiPreference backend preference (0 = CAP_ANY)
     * @param deviceIndex device index (0..n)
     */
    public VideoPanel(VideoCapture videoCapture, int apiPreference, int deviceIndex) {
        this.videoCapture = videoCapture;
        this.apiPreference = apiPreference;
        this.deviceIndex = deviceIndex;

        setLayout(new GridLayout(1, 1));
        setDoubleBuffered(true);
        setOpaque(true);
        setBackground(Color.BLACK);
    }

    // -------------------- Public API --------------------

    /**
     * Starts the capture thread if not already running.
     * Safe to call multiple times.
     */
    public synchronized void start() {
        if (running) return;

        openCaptureIfNeeded();

        running = true;
        captureThread = new Thread(this, "VideoPanel-CaptureThread-" + deviceIndex);
        captureThread.setDaemon(true);
        captureThread.start();
    }

    /**
     * Stops capture thread safely and releases the capture device.
     * Safe to call multiple times.
     */
    public synchronized void stop() {
        running = false;

        if (captureThread != null) {
            captureThread.interrupt();
            try {
                captureThread.join(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                captureThread = null;
            }
        }

        // Release capture
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
        }
    }

    /**
     * Same as {@link #stop()}.
     * Enables try-with-resources usage.
     */
    @Override
    public void close() {
        stop();
        // Release Mats
        try {
            resizedMat.release();
        } catch (Exception ignored) {}
        try {
            convertedMat.release();
        } catch (Exception ignored) {}
        try {
            synchronized (frameLock) {
                lastFrame.release();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Sets a per-frame processing filter.
     * If your filter creates a new Mat, prefer returning the same Mat or ensure it manages memory well.
     *
     * @param filter filter implementation or null to disable
     */
    public void setImageProcessingFilter(IProcessCapture filter) {
        this.imageProcessingFilter = filter;
    }

    public IProcessCapture getImageProcessingFilter() {
        return imageProcessingFilter;
    }

    public VideoCapture getVideoCapture() {
        return videoCapture;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    /**
     * Requests new capture frame width. If device is opened, applies immediately.
     */
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, frameWidth);
        }
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    /**
     * Requests new capture frame height. If device is opened, applies immediately.
     */
    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, frameHeight);
        }
    }

    public int getCaptureDelayMs() {
        return captureDelayMs;
    }

    /**
     * Adds optional sleep between frames to reduce CPU usage.
     * @param captureDelayMs delay in ms (0 = no delay)
     */
    public void setCaptureDelayMs(int captureDelayMs) {
        this.captureDelayMs = Math.max(0, captureDelayMs);
    }

    // -------------------- Snapshot APIs --------------------

    /**
     * Returns a clone of the latest captured/processed frame (not resized).
     * <p>Caller owns the returned Mat and must {@link Mat#release()} it when done.</p>
     *
     * @return cloned snapshot Mat; may be empty if no frames yet
     */
    public Mat snapshotFrame() {
        synchronized (frameLock) {
            return lastFrame.empty() ? new Mat() : lastFrame.clone();
        }
    }

    /**
     * Saves a snapshot (latest captured/processed frame, not resized) to a file using OpenCV.
     *
     * @param file file path
     * @return true if saved successfully
     */
    public boolean saveSnapshot(Path file) {
        Mat snap = snapshotFrame();
        try {
            if (snap.empty()) return false;
            return Imgcodecs.imwrite(file.toString(), snap);
        } finally {
            snap.release();
        }
    }

    // -------------------- Capture Loop --------------------

    @Override
    public void run() {
        final Mat frame = new Mat();

        try {
            while (running && !Thread.currentThread().isInterrupted()) {

                // Read a frame (only this thread should read from VideoCapture)
                boolean ok = videoCapture.read(frame);
                if (!ok || frame.empty()) {
                    sleepQuietly(30);
                    continue;
                }

                // Apply optional filter
                Mat processed = frame;
                IProcessCapture filter = this.imageProcessingFilter;
                if (filter != null) {
                    try {
                        processed = filter.process(frame);
                        if (processed == null) processed = frame; // safety fallback
                    } catch (Exception ignored) {
                        processed = frame; // keep running even if filter fails
                    }
                }

                // Keep a copy for snapshot (thread-safe).
                // This stores the "processed" frame (before resizing).
                synchronized (frameLock) {
                    processed.copyTo(lastFrame);
                }

                // Determine panel size (avoid getParent(); panel itself knows its size)
                int w = Math.max(1, getWidth());
                int h = Math.max(1, getHeight());

                // Convert channels if needed:
                // - OpenCV commonly uses BGR (3 channels) or BGRA (4 channels)
                // - BufferedImage supports 1ch gray or 3ch BGR easily
                Mat toDisplay = processed;
                int ch = processed.channels();

                if (ch == 4) {
                    // BGRA -> BGR
                    Imgproc.cvtColor(processed, convertedMat, Imgproc.COLOR_BGRA2BGR);
                    toDisplay = convertedMat;
                    ch = 3;
                } else if (ch != 1 && ch != 3) {
                    // Fallback: convert to BGR for unusual formats
                    try {
                        Imgproc.cvtColor(processed, convertedMat, Imgproc.COLOR_GRAY2BGR);
                        toDisplay = convertedMat;
                        ch = 3;
                    } catch (Exception ignored) {
                        // If conversion fails, try to display original anyway
                        toDisplay = processed;
                        ch = Math.min(3, Math.max(1, processed.channels()));
                    }
                }

                // Resize to panel size
                Imgproc.resize(toDisplay, resizedMat, new Size(w, h));

                // Determine BufferedImage type
                int imageType = (ch == 1)
                        ? BufferedImage.TYPE_BYTE_GRAY
                        : BufferedImage.TYPE_3BYTE_BGR;

                // Ensure image buffer matches size/type (reuse if possible)
                BufferedImage img = ensureImageBuffer(w, h, imageType);

                // Copy bytes from Mat to BufferedImage raster
                int required = resizedMat.channels() * resizedMat.cols() * resizedMat.rows();
                if (imagePixels == null || imagePixels.length < required) {
                    // Rebuild buffer in rare cases
                    img = ensureImageBuffer(w, h, imageType);
                }
                resizedMat.get(0, 0, imagePixels);

                imageForDisplay = img;

                // Repaint safely on EDT
                SwingUtilities.invokeLater(this::repaint);

                // Optional throttle
                if (captureDelayMs > 0) {
                    sleepQuietly(captureDelayMs);
                }
            }
        } catch (Exception ignored) {
            // In production, log this
        } finally {
            frame.release();
        }
    }

    // -------------------- Swing Painting --------------------

    /**
     * Paints the latest frame. Called on Swing EDT.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage img = imageForDisplay;
        if (img != null) {
            g.drawImage(img, 0, 0, this);
        } else {
            // Optional: draw "No Signal"
            g.setColor(Color.DARK_GRAY);
            g.drawString("No Signal", 10, 20);
        }
    }

    // -------------------- Internal helpers --------------------

    /**
     * Opens the capture device if needed and applies width/height requests.
     */
    private void openCaptureIfNeeded() {
        if (videoCapture != null && !videoCapture.isOpened()) {
            videoCapture.open(deviceIndex, apiPreference);
            videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, frameWidth);
            videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, frameHeight);
        }
    }

    /**
     * Ensures a BufferedImage exists with matching size/type; reuses when possible.
     * Also refreshes the cached pixel array reference.
     */
    private BufferedImage ensureImageBuffer(int width, int height, int imageType) {
        BufferedImage img = imageForDisplay;

        boolean mustCreate = (img == null)
                || img.getWidth() != width
                || img.getHeight() != height
                || img.getType() != imageType;

        if (mustCreate) {
            img = new BufferedImage(width, height, imageType);
            imagePixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            imageForDisplay = img;
        } else if (imagePixels == null) {
            imagePixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        }

        return img;
    }

    /**
     * Sleep helper: preserves interrupt status if interrupted.
     */
    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}