package com.kram.vlad.carnumberrecognition.activitys;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.kram.vlad.carnumberrecognition.Constants;
import com.kram.vlad.carnumberrecognition.R;
import com.kram.vlad.carnumberrecognition.Utils;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.trippedout.cloudvisionlib.CloudVisionApi;
import net.trippedout.cloudvisionlib.CloudVisionService;
import net.trippedout.cloudvisionlib.ImageUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.kram.vlad.carnumberrecognition.Constants.REQUEST_IMAGE_CAPTURE;
import static com.kram.vlad.carnumberrecognition.Utils.getRequestOneFeature;

/**
 * MainActivity of app. Here we do almost all recognizing.
 */
public class MainActivity extends AppCompatActivity implements Callback<CloudVisionApi.VisionResponse> {

    public static final String TAG = MainActivity.class.getSimpleName();

    //Views
    @BindView(R.id.camera) ImageView mCamera;
    @BindView(R.id.rotate) ImageView mRotate;
    @BindView(R.id.complete) ImageView mComplete;
    @BindView(R.id.cropImageView) CropImageView mCropImageView;
    //Views

    private Uri capturedImageUri;
    private WifiManager mWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        checkPermission();
    }

    /**
     * Check all private permission
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            if (!(checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
            }

            if (!(checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 1);
            }
        }
    }

    /**
     * Create camera intent
     */
    @OnClick(R.id.camera)
    public void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        capturedImageUri = new Utils().getOutputMediaFileUri();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Get image from camera intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), capturedImageUri);
                mCropImageView.setImageBitmap(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.rotate)
    public void rotateImage() {
        mCropImageView.rotateImage(90);
    }

    /**
     * Create request to Cloud vision api and register callback
     */
    @OnClick(R.id.complete)
    public void doRequestToCloudVision() {

        checkWifiState();

        if (mCropImageView.getCroppedImage() != null) {
            CloudVisionService cloudVisionService = CloudVisionApi.getCloudVisionService();
            String encodedData = ImageUtil.getEncodedImageData(mCropImageView.getCroppedImage());

            Call<CloudVisionApi.VisionResponse> call = cloudVisionService.getAnnotations(
                    Constants.GOOGLE_VISION_API_KEY,
                    getRequestOneFeature(encodedData, CloudVisionApi.FEATURE_TYPE_TEXT_DETECTION)
            );

            call.enqueue(this);
        } else {
            Toast.makeText(this, "You must take picture", Toast.LENGTH_LONG).show();
        }

    }

    private void checkWifiState() {
        if (mWifiManager.isWifiEnabled()) {
        } else {
            Toast.makeText(this, "You must have internet connection", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void onResponse(Call<CloudVisionApi.VisionResponse> call, Response<CloudVisionApi.VisionResponse> response) {

        if(response != null) {
            CloudVisionApi.TextResponse textResponse = (CloudVisionApi.TextResponse) response.body().getResponseByType(CloudVisionApi.FEATURE_TYPE_TEXT_DETECTION);
            if (textResponse != null) {
                String responseBody = textResponse.toString();

                //---- Magic Function that get recognizing text>

                String text = "";
                char[] arr = responseBody.toCharArray();
                for (int i = 85; arr[i] != '\''; i++) {
                    text += arr[i];
                }
                text = text.replace("\n", "");

                //---- Magic Function that get recognizing  `text>

                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "App can't recognize text in this photo, take another one", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(Call<CloudVisionApi.VisionResponse> call, Throwable t) {
        Log.d(TAG, t.getMessage());
    }
}
