package com.oidiotlin.StyleTransfer;


public class Transfer {
    private static float[] floatValues;
    private static int[] intValues;
    public Bitmap run(int[] intValues) {
        TimingLogger timings = new TimingLogger(TAG, "stylizeImage");
        Bitmap scaledBitmap = scaleBitmap(bitmap, 480, 640); // desiredSize
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) * 1.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) * 1.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) * 1.0f;
        }
        timings.addSplit("Rebuild input tensor");

        inferenceInterface.feed(INPUT_NODE, floatValues, 640, 480, 3);
        inferenceInterface.run(new String[]{OUTPUT_NODE});
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);
        timings.addSplit("Inference");

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3])) << 16)
                            | (((int) (floatValues[i * 3 + 1])) << 8)
                            | ((int) (floatValues[i * 3 + 2]));
        }
        scaledBitmap.setPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());
        timings.addSplit("Rebuild output image");
        timings.dumpToLog();
        return scaledBitmap;
    }


}
