package com.camera2demo;

import android.app.Activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
//  https://blog.csdn.net/octobershiner/article/details/6641942
public class SensorActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    AppCompatTextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        textView = findViewById(R.id.textView);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            boolean b = sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            Log.d("SensorActivity", "Sensor TYPE_ACCELEROMETER 注册结果"+b);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            boolean b =sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            Log.d("SensorActivity", "Sensor TYPE_MAGNETIC_FIELD 注册结果"+b);
        }
        // 陀螺仪
        Sensor TYPE_GYROSCOPE = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (TYPE_GYROSCOPE != null) {
            boolean b =sensorManager.registerListener(this, TYPE_GYROSCOPE,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            Log.d("SensorActivity", "Sensor TYPE_GYROSCOPE 注册结果"+b);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 磁力传感器
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values.clone();
        }
        // 加速度传感器
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        }
        if (magneticFieldValues != null && accelerometerValues != null) {
            calculateOrientation();
        }

    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        float I[] = new float[9];
        // 坐标轴转换
        boolean success  = SensorManager.getRotationMatrix(R, I, accelerometerValues, magneticFieldValues);
        if (success){
            SensorManager.getOrientation(R, values);
            // 要经过一次数据格式的转换，弧度转换为度
            values[0] = (float) Math.toDegrees(values[0]);
        /*    if (values[0]<0){
                textView.setText(Math.abs(values[0])+180+"");
            }else {*/
                textView.setText(((values[0] + 360.0) % 360.0)+"");
        // }
        }


    }


}