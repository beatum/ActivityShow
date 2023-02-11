package io.github.beatum.video;

import org.opencv.core.Mat;
import pnuts.lang.Context;
import pnuts.lang.PnutsFunction;

/**
 * @author Happy.He
 * @version 1.0
 * @date 2/10/2023 4:47 PM
 */
public class ProcessCapturePnutsImpl implements IProcessCapture {
    Context context;
    PnutsFunction pnutsFunction;

    public ProcessCapturePnutsImpl(Context context, PnutsFunction pnutsFunction) {
        this.context = context;
        this.pnutsFunction = pnutsFunction;
    }

    public Mat process(Mat mat) {
        Object returnObj = pnutsFunction.call(new Object[]{mat}, context);
        return (Mat) returnObj;
    }
}
