
package io.github.beatum.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a group of video capture viewers and their corresponding UI panels.
 *
 * Responsibilities:
 * - Create N capture viewers (device index 0..N-1)
 * - Probe whether each device is available
 * - Add either a viewer panel or an "Empty" placeholder into the provided container
 * - Start/stop/release all devices safely
 *
 * Threading:
 * - UI modifications should be performed on Swing's EDT.
 * - Each viewer runs its own capture thread.
 *
 * Java Compatibility: Java 6+ (also works on Java 8)
 *
 * @author Happy.He
 * @version 2.0-J6
 * @since 2023-02-13
 */
public class DeviceGroup implements Closeable {

    /** OpenCV backend API preference (default: DirectShow on Windows). */
    private int apiPreference = Videoio.CAP_DSHOW;

    /** Number of devices to probe (device indices 0..deviceCount-1). */
    private int deviceCount = 1;

    /** Container that holds all device panels (usually GridLayout). */
    private final JComponent container;

    /** Viewer components for available devices only. */
    private final List<VideoPanel> viewers = new ArrayList<VideoPanel>();

    /** UI panels for each index (available or placeholder). Size = deviceCount. */
    private final List<JPanel> devicePanels = new ArrayList<JPanel>();

    /**
     * Create a new DeviceGroup and initialize UI components.
     *
     * @param container the UI container where panels will be added (e.g. JPanel with GridLayout)
     * @param deviceCount number of device indices to probe (>= 0)
     */
    public DeviceGroup(JComponent container, int deviceCount) {
        this.container = container;
        setDeviceCount(deviceCount);
        init();
    }

    // -------------------- Getters / Setters --------------------

    public int getApiPreference() {
        return apiPreference;
    }

    /**
     * Changes the OpenCV backend preference used for NEW captures.
     * Existing opened viewers are not recreated automatically.
     */
    public void setApiPreference(int apiPreference) {
        this.apiPreference = apiPreference;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    /**
     * Sets how many device indices should be probed (0..count-1).
     * Must be set before init/re-init.
     */
    public void setDeviceCount(int deviceCount) {
        this.deviceCount = Math.max(0, deviceCount);
    }

    /** Unmodifiable list of viewers (available devices only). */
    public List<VideoPanel> getViewers() {
        return Collections.unmodifiableList(viewers);
    }

    /** Unmodifiable list of panels (includes placeholders). */
    public List<JPanel> getDevicePanels() {
        return Collections.unmodifiableList(devicePanels);
    }

    // -------------------- Initialization --------------------

    /**
     * Initialize the device panels and viewers.
     *
     * For each device index:
     * - Try to open + read a test frame
     * - If successful: create a VideoPanel and embed it in a panel
     * - If failed: create a placeholder panel showing "Empty: i"
     */
    private void init() {
        // If you want to be extra safe, ensure this runs on EDT:
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    init();
                }
            });
            return;
        }

        // Clear old state (if re-initialization ever happens)
        viewers.clear();
        devicePanels.clear();
        container.removeAll();

        for (int i = 0; i < deviceCount; i++) {
            JPanel panelForIndex;

            // Probe device availability with a short open/read cycle
            VideoCapture cap = new VideoCapture();
            boolean available = tryOpenAndProbe(cap, i, apiPreference);

            if (available) {
                // Create viewer that will own this capture instance
                VideoPanel viewer = new VideoPanel(cap, apiPreference, i);

                panelForIndex = new JPanel(new GridLayout(1, 1));
                panelForIndex.add(viewer);

                viewers.add(viewer);
            } else {
                // Clean up capture if probe failed
                cap.release();
                panelForIndex = buildPlaceholderPanel(i);
            }

            devicePanels.add(panelForIndex);

            // Add into container at the appropriate index
            container.add(panelForIndex, i);
        }

        // Refresh UI
        container.revalidate();
        container.repaint();
    }

    /**
     * Attempts to open the device and read a single test frame to confirm it works.
     *
     * @param cap capture instance (not opened yet)
     * @param index device index (0..)
     * @param apiPreference backend preference
     * @return true if device was opened and a test frame could be read
     */
    private static boolean tryOpenAndProbe(VideoCapture cap, int index, int apiPreference) {
        try {
            if (!cap.open(index, apiPreference)) {
                return false;
            }
            Mat test = new Mat();
            boolean ok = cap.read(test) && !test.empty();
            test.release();
            return ok;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Builds a Swing placeholder panel for missing/unavailable devices.
     */
    private static JPanel buildPlaceholderPanel(int index) {
        JPanel p = new JPanel(new GridLayout(1, 1));
        JLabel label = new JLabel("Empty: " + index, SwingConstants.CENTER);
        label.setForeground(Color.GRAY);
        p.add(label);
        return p;
    }

    // -------------------- Lifecycle --------------------

    /**
     * Starts capture for all available viewers.
     */
    public void openAll() {
        for (int i = 0; i < viewers.size(); i++) {
            viewers.get(i).start();
        }
    }

    /**
     * Stops all viewers but does not remove UI panels.
     */
    public void stopAll() {
        for (int i = 0; i < viewers.size(); i++) {
            viewers.get(i).stop();
        }
    }

    /**
     * Stops and releases all resources (recommended).
     */
    public void releaseAll() {
        for (int i = 0; i < viewers.size(); i++) {
            viewers.get(i).close(); // your VideoPanel.close() is Java 6 compatible now
        }
    }

    /**
     * Same as releaseAll(). (Java 6 doesn't have try-with-resources, but close() still useful.)
     */
    public void close() {
        releaseAll();
    }
}
