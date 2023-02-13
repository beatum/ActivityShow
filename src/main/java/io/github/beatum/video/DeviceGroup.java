package io.github.beatum.video;

import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Happy.He
 * @version 1.0
 * @date 2/13/2023 8:51 AM
 */
public class DeviceGroup {
    private int apiPreference = 700;
    private Integer quantityOfVideoDevice = 1;
    private List<Video> videoDevices = new LinkedList<Video>();
    private List<JPanel> devicePanels = new LinkedList<JPanel>();
    private JComponent devicePanel;

    public int getApiPreference() {
        return apiPreference;
    }

    public void setApiPreference(int apiPreference) {
        this.apiPreference = apiPreference;
    }

    public Integer getQuantityOfVideoDevice() {
        return quantityOfVideoDevice;
    }

    public void setQuantityOfVideoDevice(Integer quantityOfVideoDevice) {
        this.quantityOfVideoDevice = quantityOfVideoDevice;
    }

    public List<Video> getVideoDevices() {
        return videoDevices;
    }

    public List<JPanel> getDevicePanels() {
        return devicePanels;
    }

    private DeviceGroup() {
    }

    public DeviceGroup(JComponent grid, Integer qty) {
        this.devicePanel = grid;
        this.quantityOfVideoDevice = qty;
        init();
    }

    /*
     * initialize
     * */
    private void init() {
        for (int i = 0; i < quantityOfVideoDevice; i++) {
            Video viewer = new Video(new VideoCapture(), this.apiPreference, i);
            JPanel _devicePanel = null;
            if (viewer.getVideoCapture().grab()) {
                _devicePanel = new JPanel(new GridLayout(1, 1));
                _devicePanel.add(viewer);
                videoDevices.add(viewer);
            } else {
                _devicePanel = new JPanel(new CardLayout());
                _devicePanel.add(new Label("Empty:" + i));
            }
            devicePanels.add(_devicePanel);
            devicePanel.add(_devicePanel, i);
        }
    }

    /*
     * open device
     * */
    public void open() {
        for (int i = 0; i < videoDevices.size(); i++) {
            Video device = videoDevices.get(i);
            device.start();
        }
    }

    /*
     * release
     * */
    public void release() {
        for (int i = 0; i < videoDevices.size(); i++) {
            Video device = videoDevices.get(i);
            device.stop();
            device.getVideoCapture().release();
        }
    }
}
