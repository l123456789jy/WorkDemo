package com.camera2demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@SuppressLint({"ValidFragment", "NewApi"})
public class MainActivity extends Activity implements View.OnClickListener {
    private String locationStr = "";
    private String message = "";
    private static final int REQUEST_CODE = 10;
    private static final String TAG = MainActivity.class.getSimpleName();

    private CameraDevice cameraDevice;
    TextView textView;
    List<String> requiredPermissions = Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    private String cameraId="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        checkPermissions();

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        getBackCameraId(cameraManager);
        openCamera(cameraManager, cameraId);
        if (Build.VERSION.SDK_INT >= 23) {// android6 执行运行时权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE);
            }
        }
        GpsManager.getInstance().init(this);
         textView = findViewById(R.id.textView);
        textView.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraDevice != null) {
            Log.e(TAG, cameraDevice.getClass().toString());
            cameraDevice.close();
        }
    }

    private void checkPermissions() {
        List<String> unGrantedPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission)) {
                unGrantedPermissions.add(permission);
            }
        }

        if (unGrantedPermissions.size() != 0) {
            requestPermissions(unGrantedPermissions.toArray(new String[unGrantedPermissions.size()]), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getBackCameraId(CameraManager cameraManager) {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (cameraIdList.length==0){
                Log.e(TAG, "没有获取到摄像头列表");
            }
            CameraCharacteristics characteristics;
            for (String cameraId : cameraIdList) {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                float maxZoom = characteristics.get(
                        CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                if (null==ranges){
                    return;
                }
                for (Range<Integer> range : ranges) {
                    Log.e(TAG, "支持的帧率: " + range.toString() + "=======摄像头ID====: " + cameraId);
                  //  textView.setText("支持的帧率: " + range.toString() + "=======摄像头ID====: " + cameraId);
                }
                Log.e(TAG, "最大放大倍数: " + maxZoom);
                if (CameraCharacteristics.LENS_FACING_EXTERNAL == facing || CameraCharacteristics.LENS_FACING_BACK == facing) {
                    this.cameraId=cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(CameraManager cameraManager, String cameraId) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            cameraManager.openCamera(cameraId, callback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            Log.e(TAG, "onOpened: 相机已开启");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "onOpened: 相机已断开");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            Log.e(TAG, "onOpened: 相机出错==" + error);
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            Log.e(TAG, "onOpened: 相机已关闭==");
        }
    };

    private void captureStillPicture(CameraDevice mCameraDevice) {
        try {
            // 1. 先拿到一个 CaptureRequest.Builder 对象
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
           /* captureBuilder.addTarget(mImageReader.getSurface());
            // 2. 通过 CaptureRequest.Builder 对象设置一些捕捉请求的配置
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    // start preview
                }
            };
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            // 3. 通过 CaptureRequest.Builder 对象的 `build()` 方法构建一个 CaptureRequest 对象
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);*/
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {

    }



}




