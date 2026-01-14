
package io.github.beatum;

import io.github.beatum.video.DeviceGroup;
import io.github.beatum.video.VideoPanel;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Demo application entry point.
 *
 * UI:
 * - Path textbox for capture directory
 * - Start button to open camera streams
 * - Capture button to save one snapshot per active camera
 *
 * Java Compatibility: Java 6+ (also works on Java 8)
 */
public class App {

    static {
        // Recommended (portable) approach:
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //
        // If you MUST keep hardcoded load path, keep System.load(...) below:
        System.load("D:\\Happy\\Software\\Opencv4.6.0\\opencv\\build\\java\\x64\\opencv_java460.dll");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                final JFrame window = new JFrame("Demo Application");
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                window.setBounds(200, 200, 1024, 768);
                window.setLayout(new BorderLayout());

                // -------------------- Top input panel --------------------
                JPanel inputPanel = new JPanel(new SpringLayout());

                JLabel labelOfPath = new JLabel("Path:", JLabel.TRAILING);
                final JTextField textFieldOfPath = new JTextField(50);
                textFieldOfPath.setText("D:\\test"); // default
                labelOfPath.setLabelFor(textFieldOfPath);

                final JButton btnStart = new JButton("Start");

                inputPanel.add(labelOfPath);
                inputPanel.add(textFieldOfPath);
                inputPanel.add(btnStart);

                SpringUtilities.makeCompactGrid(inputPanel, 1, 3, 6, 6, 6, 6);

                // -------------------- Center camera container --------------------
                JPanel container = new JPanel(new GridLayout(2, 3, 6, 6));
                final DeviceGroup deviceGroup = new DeviceGroup(container, 6);

                // -------------------- Bottom capture button --------------------
                final JButton btnCapture = new JButton("Capture");
                btnCapture.setEnabled(false);

                // Start streaming
                btnStart.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deviceGroup.openAll();
                        btnStart.setEnabled(false);
                        btnCapture.setEnabled(true);
                    }
                });

                // Release resources on window closing
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        deviceGroup.close();
                        System.out.println("Window closed, devices released.");
                    }
                });

                // Capture snapshots in background so the UI won't freeze
                btnCapture.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final String pathText = textFieldOfPath.getText().trim();
                        if (pathText.length() == 0) {
                            JOptionPane.showMessageDialog(window, "Path is empty!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        final File dir = new File(pathText);

                        // Disable capture during saving to avoid re-entrancy
                        btnCapture.setEnabled(false);

                        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                // Java 6 replacement for Files.createDirectories(...)
                                if (!dir.exists()) {
                                    boolean ok = dir.mkdirs();
                                    if (!ok && !dir.exists()) {
                                        throw new RuntimeException("Cannot create directory: " + dir.getAbsolutePath());
                                    }
                                }
                                if (!dir.isDirectory()) {
                                    throw new RuntimeException("Not a directory: " + dir.getAbsolutePath());
                                }

                                List<VideoPanel> viewers = deviceGroup.getViewers();
                                String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

                                for (int i = 0; i < viewers.size(); i++) {
                                    VideoPanel viewer = viewers.get(i);

                                    // File naming includes index to avoid collisions
                                    File file = new File(dir, "cam" + i + "_" + timestamp + ".png");

                                    // Safe snapshot (does NOT touch VideoCapture directly)
                                    boolean ok = viewer.saveSnapshot(file);

                                    if (!ok) {
                                        System.err.println("Snapshot failed for device index " + i);
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                btnCapture.setEnabled(true);
                                try {
                                    get(); // rethrow background exceptions if any
                                    JOptionPane.showMessageDialog(window, "Capture completed.", "Info",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(window,
                                            "Capture failed: " + ex.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        };

                        worker.execute();
                    }
                });

                // -------------------- Assemble UI --------------------
                window.add(inputPanel, BorderLayout.NORTH);
                window.add(container, BorderLayout.CENTER);
                window.add(btnCapture, BorderLayout.SOUTH);

                window.setVisible(true);
            }
        });
    }
}
