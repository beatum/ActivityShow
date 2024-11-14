package io.github.beatum;

import io.github.beatum.video.DeviceGroup;
import io.github.beatum.video.Video;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

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
 * Main
 */
public class App {
    static {
        System.load("D:\\Happy\\Software\\Opencv4.6.0\\opencv\\build\\java\\x64\\opencv_java460.dll");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame windows = new JFrame();
                    windows.setTitle("Demo Application");
                    windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    windows.setBounds(200, 200, 1024, 768);
                    windows.setLayout(new BorderLayout());

                    //input panel.
                    JPanel inputPanel = new JPanel();
                    inputPanel.setLayout(new SpringLayout());
                    //Path.
                    JLabel labelOfPath = new JLabel("Path:", JLabel.TRAILING);
                    JTextField textFieldOfPath = new JTextField(50);
                    textFieldOfPath.setText("D:\\test");
                    labelOfPath.setLabelFor(textFieldOfPath);
                    inputPanel.add(labelOfPath);
                    inputPanel.add(textFieldOfPath);


                    //start button
                    JButton btnStart = new JButton("Start");
                    inputPanel.add(btnStart);

                    SpringUtilities.makeCompactGrid(inputPanel, 1, 3, 6, 6, 6, 6);

                    //Device group.
                    JPanel container = new JPanel();
                    container.setLayout(new GridLayout(2, 3));
                    DeviceGroup deviceGroup = new DeviceGroup(container, 6);

                    //capture button.
                    JButton btnCapture = new JButton("Capture");
                    btnCapture.setEnabled(false);

                    //start
                    btnStart.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            deviceGroup.open();
                            btnStart.setEnabled(false);
                            btnCapture.setEnabled(true);
                        }
                    });

                    windows.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            deviceGroup.release();
                            System.out.println("window closed.");
                        }
                    });

                    btnCapture.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<Video> devices = deviceGroup.getVideoDevices();
                            for (Video video : devices) {
                                String timestamp = new SimpleDateFormat("yyyyMMddHHmmssS").format(new Date());
                                String path = textFieldOfPath.getText().trim();
                                if (path.equals("")) {
                                    JOptionPane.showMessageDialog(windows, "Path is empty!", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                File dir = new File(path);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                String fileName = textFieldOfPath.getText().trim() + "/" + timestamp + ".png";
                                VideoCapture vc = video.getVideoCapture();
                                Mat frame = new Mat();
                                vc.read(frame);
                                Imgcodecs.imwrite(fileName, frame);
                            }
                        }
                    });

                    windows.add(inputPanel, BorderLayout.NORTH);
                    windows.add(container, BorderLayout.CENTER);
                    windows.add(btnCapture, BorderLayout.SOUTH);

                    windows.setVisible(true);

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }
}
