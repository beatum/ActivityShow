# ActivityShow — Swing Component for OpenCV Multi-Camera Display

<img src="https://img.shields.io/badge/version-1.0.6-green" />

A lightweight **Java Swing** component to display **multiple OpenCV camera streams** in a grid, with optional **frame processing** and **snapshot capture**.

- Built with **JDK 1.8**
- Uses **OpenCV Java bindings**
- Packaged with **Maven**

---

## Overview

Display multiple camera feeds in a Swing UI.

![Main](/imgs/0.jpg)

---

## Features

- ✅ Display **N** camera devices in a grid layout
- ✅ Optional per-frame processing via `IProcessCapture`
- ✅ Safe start/stop capture threads
- ✅ Snapshot capture without interfering with the streaming thread *(recommended)*

---

## Requirements

- **JDK 1.8**
- **OpenCV Java** (example: OpenCV 4.6.0)
- OS-supported camera backend (e.g., DirectShow on Windows)

> Note: If you use `System.load(...)`, the DLL must match your OS architecture (x64 vs x86) and your JDK architecture.

---

## Build

```bash
mvn clean package
```

---

## Demo (Recommended)

This demo uses the safer approach:

- `DeviceGroup.openAll()` starts streaming
- `VideoPanel.saveSnapshot(...)` saves images **without calling `VideoCapture.read()` from the UI thread**

```java
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
 * Demo Application
 */
public class App {

    static {
        // Recommended: use loadLibrary if java.library.path is configured
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // If you prefer a hard-coded DLL path, you can use:
        // System.load("D:\Happy\Software\Opencv4.6.0\opencv\build\java\x64\opencv_java460.dll");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame window = new JFrame("Demo Application");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setBounds(200, 200, 1024, 768);
            window.setLayout(new BorderLayout());

            // ---------- Input panel ----------
            JPanel inputPanel = new JPanel(new SpringLayout());

            JLabel labelOfPath = new JLabel("Path:", JLabel.TRAILING);
            JTextField textFieldOfPath = new JTextField(50);
            textFieldOfPath.setText("D:\test");
            labelOfPath.setLabelFor(textFieldOfPath);

            JButton btnStart = new JButton("Start");

            inputPanel.add(labelOfPath);
            inputPanel.add(textFieldOfPath);
            inputPanel.add(btnStart);

            SpringUtilities.makeCompactGrid(inputPanel, 1, 3, 6, 6, 6, 6);

            // ---------- Device grid ----------
            JPanel container = new JPanel(new GridLayout(2, 3, 6, 6));
            DeviceGroup deviceGroup = new DeviceGroup(container, 6);

            // ---------- Capture button ----------
            JButton btnCapture = new JButton("Capture");
            btnCapture.setEnabled(false);

            btnStart.addActionListener(e -> {
                deviceGroup.openAll();
                btnStart.setEnabled(false);
                btnCapture.setEnabled(true);
            });

            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    deviceGroup.close(); // stop + release
                    System.out.println("Window closed, devices released.");
                }
            });

            // Capture snapshots in background (avoid freezing UI)
            btnCapture.addActionListener(e -> {
                String pathText = textFieldOfPath.getText().trim();
                if (pathText.isEmpty()) {
                    JOptionPane.showMessageDialog(window, "Path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Path dir = Path.of(pathText);
                btnCapture.setEnabled(false);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        Files.createDirectories(dir);

                        List<VideoPanel> viewers = deviceGroup.getViewers();
                        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

                        for (int i = 0; i < viewers.size(); i++) {
                            VideoPanel viewer = viewers.get(i);
                            Path file = dir.resolve("cam" + i + "_" + timestamp + ".png");

                            boolean ok = viewer.saveSnapshot(file);
                            if (!ok) {
                                System.err.println("Snapshot failed: device " + i);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        btnCapture.setEnabled(true);
                        JOptionPane.showMessageDialog(window, "Capture completed.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                };

                worker.execute();
            });

            // ---------- Layout ----------
            window.add(inputPanel, BorderLayout.NORTH);
            window.add(container, BorderLayout.CENTER);
            window.add(btnCapture, BorderLayout.SOUTH);

            window.setVisible(true);
        });
    }
}
```

---

## Why `saveSnapshot()` is recommended

Avoid reading frames from `VideoCapture` on the UI thread **when your viewer is already streaming**:

```java
vc.read(frame); // Not recommended if VideoPanel already reads in another thread
```

Reading from the same `VideoCapture` in multiple threads can cause race conditions and unstable behavior.

Use the viewer API instead:

```java
viewer.saveSnapshot(file); // Safe
```

---

## Frame Processing (Optional)

You can add a per-frame filter:

```java
// Example: attach filter to the first viewer (if available)
if (!deviceGroup.getViewers().isEmpty()) {
    deviceGroup.getViewers().get(0).setImageProcessingFilter(input -> {
        // Do OpenCV processing here (e.g. blur, edges, etc.)
        return input; // return processed Mat
    });
}
```

Tip: Prefer **in-place processing** to reduce allocations and improve performance.

---

## Acknowledgment

- [OpenCV](https://docs.opencv.org/)

---