package com.camera2demo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;
// 直接使用方向传感器 获取度数
public class SensorSActivity extends Activity implements SensorEventListener {
   public String TAG="TYPE_GYROSCOPE";
    long temp=System.currentTimeMillis();;
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
        //   Sensor.TYPE_ORIENTATIO  ==3
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (accelerometer != null) {
            boolean b = sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
            if (b){
                Log.e(TAG, "sensorManager: registerListener  succes");
            }else {
                Log.e(TAG, "sensorManager: registerListener  Faile");
            }
        }else {
            Log.e(TAG, "sensorManager: 没有找到此传感器");
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
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            //图解中已经解释三个值的含义
            float X_lateral = event.values[0];
            float Y_longitudinal = event.values[1];
            float Z_vertical = event.values[2];
            textView.setText(X_lateral+"");
            long l = System.currentTimeMillis();
            long temp = this.temp;
            long l1 =  l-temp;

            Log.e(TAG, "onSensorChanged: "+X_lateral +"  time======"+l1);
            this.temp =l;
        }

    }


}