package io.github.beatum.video;

import org.opencv.core.Mat;

/**
 * @author Happy.He
 * @version 1.0
 * @date 2/10/2023 11:33 AM
 */
public interface IProcessCapture {
    //Process
    public Mat process(Mat mat);
}
