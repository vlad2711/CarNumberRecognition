package com.kram.vlad.carnumberrecognition;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;


import net.trippedout.cloudvisionlib.CloudVisionApi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vlad on 27.11.2017.
 *
 * Utils class
 * Can Create request to cloud platform and save image from camera intent
 */

public class Utils {

    /**
     * Create request to cloud platform with one feature(Text detection in our case)
     *
     * @param base64Image image that we get from camera. Must be in base64 encoded
     * @param feature feature that you want to use
     *
     * @return request
     */
    public static CloudVisionApi.VisionRequest getRequestOneFeature(String base64Image, String feature) {
        List<CloudVisionApi.Request> list = new ArrayList<>();
        List<CloudVisionApi.Feature> features = new ArrayList<>();
        features.add(new CloudVisionApi.Feature(feature, 1));

        list.add(new CloudVisionApi.Request(new CloudVisionApi.Image(base64Image), features));
        return new CloudVisionApi.VisionRequest(list);
    }

    /**
     *
     * @return URI of our image using file from getOutputMediaFile() function
     */
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a file where our image stored
     * @return file where our image stored
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }
}
