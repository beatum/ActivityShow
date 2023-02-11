# Activity show component for OpenCV
<img src="https://img.shields.io/github/package-json/v/beatum/ng-web" alt="Version"> <img src="https://img.shields.io/github/checks-status/beatum/ng-web/master"><img src="https://img.shields.io/github/license/beatum/ng-web"><img alt="GitHub all releases" src="https://img.shields.io/github/downloads/beatum/ng-web/total">

this project build on JDK1.8

## Overviews：

![Main](https://github.com/beatum/ActivityShow/blob/master/imgs/0.jpg)

## Demo:

```java
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame windows = new JFrame();
                    windows.setTitle("Demo....");
                    windows.setBounds(200, 200, 850, 600);
                    GridLayout gridLayout = new GridLayout(2, 3);
                    windows.setLayout(gridLayout);

                    VideoCapture videoCapture1 = new VideoCapture();
                    VideoCapture videoCapture2 = new VideoCapture();
                    VideoCapture videoCapture3 = new VideoCapture();

                    Video viewer1 = new Video(videoCapture1, 0);
                    Video viewer2 = new Video(videoCapture2, 1);
                    Video viewer3 = new Video(videoCapture3, 2);

                    viewer1.setImageProcessingFilter(new IProcessCapture() {
                        public Mat process(Mat mat) {
                            return mat;
                        }
                    });

                    JPanel p1 = new JPanel();
                    p1.setLayout(new GridLayout(1, 1));
                    p1.add(viewer1);

                    JPanel p2 = new JPanel();
                    p2.setLayout(new GridLayout(1, 1));
                    p2.add(viewer2);

                    JPanel p3 = new JPanel();
                    p3.setLayout(new GridLayout(1, 1));
                    p3.add(viewer3);

                    windows.add(p1, 0);
                    windows.add(p2, 1);
                    windows.add(p3, 2);
                    windows.add(new Label("004-Empty"), 3);
                    windows.add(new Label("005-Empty"), 4);
                    windows.add(new Label("006-Empty"), 5);

                    windows.setVisible(true);
                    viewer1.start();
                    viewer2.start();
                    viewer3.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
```

## Development server
Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build

Run `mvn clean package`

## Acknowledgment: 
[OpenCV ](https://docs.opencv.org/)