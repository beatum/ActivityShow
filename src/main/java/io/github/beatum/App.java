
package io.github.beatum;

import io.github.beatum.video.DeviceGroup;
import io.github.beatum.video.VideoPanel;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Demo application entry point.
 *
 * <p>UI:
 * <ul>
 *   <li>Path textbox for capture directory</li>
 *   <li>Start button to open camera streams</li>
 *   <li>Capture button to save one snapshot per active camera</li>
 * </ul>
 */
public class App {

    static {
        // ✅ Recommended approach (portable):
        // Ensure OpenCV native binaries are available in java.library.path
        // e.g. -Djava.library.path=... or environment PATH on Windows.
        //
        // If you MUST keep your hardcoded load path, you can keep it,
        // but loadLibrary is cleaner:
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // If you want to keep your original line instead, comment above and use:
        System.load("D:\\Happy\\Software\\Opencv4.6.0\\opencv\\build\\java\\x64\\opencv_java460.dll");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame window = new JFrame("Demo Application");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setBounds(200, 200, 1024, 768);
            window.setLayout(new BorderLayout());

            // -------------------- Top input panel --------------------
            JPanel inputPanel = new JPanel(new SpringLayout());

            JLabel labelOfPath = new JLabel("Path:", JLabel.TRAILING);
            JTextField textFieldOfPath = new JTextField(50);
            textFieldOfPath.setText("D:\\test"); // default
            labelOfPath.setLabelFor(textFieldOfPath);

            JButton btnStart = new JButton("Start");

            inputPanel.add(labelOfPath);
            inputPanel.add(textFieldOfPath);
            inputPanel.add(btnStart);

            SpringUtilities.makeCompactGrid(inputPanel, 1, 3, 6, 6, 6, 6);

            // -------------------- Center camera container --------------------
            JPanel container = new JPanel(new GridLayout(2, 3, 6, 6));
            DeviceGroup deviceGroup = new DeviceGroup(container, 6);

            // -------------------- Bottom capture button --------------------
            JButton btnCapture = new JButton("Capture");
            btnCapture.setEnabled(false);

            // Start streaming
            btnStart.addActionListener(e -> {
                deviceGroup.openAll();
                btnStart.setEnabled(false);
                btnCapture.setEnabled(true);
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
            btnCapture.addActionListener(e -> {
                final String pathText = textFieldOfPath.getText().trim();
                if (pathText.isEmpty()) {
                    JOptionPane.showMessageDialog(window, "Path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final Path dir = Path.of(pathText);

                // Disable capture during saving to avoid re-entrancy
                btnCapture.setEnabled(false);

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        Files.createDirectories(dir);

                        List<VideoPanel> viewers = deviceGroup.getViewers();
                        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

                        for (int i = 0; i < viewers.size(); i++) {
                            VideoPanel viewer = viewers.get(i);

                            // File naming includes index to avoid collisions
                            Path file = dir.resolve("cam" + i + "_" + timestamp + ".png");

                            // ✅ Safe snapshot (does NOT touch VideoCapture directly)
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
                            JOptionPane.showMessageDialog(window, "Capture completed.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(window,
                                    "Capture failed: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
            });

            // -------------------- Assemble UI --------------------
            window.add(inputPanel, BorderLayout.NORTH);
            window.add(container, BorderLayout.CENTER);
            window.add(btnCapture, BorderLayout.SOUTH);

            window.setVisible(true);
        });
    }
}
