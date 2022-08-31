package com.camera2demo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

//
public class UsbActivity extends Activity implements View.OnClickListener {
    private static UsbManager mUsbManager;


    private static final String ACTION_USB_PERMISSION = "com.android.UsbActivity.USB_PERMISSION";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        ((TextView) findViewById(R.id.textView)).setOnClickListener(this);
        registerReceiver();
    }

    private void tryGetUsbPermission(UsbDevice usbDevice) {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

/*
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
                Log.e("UsbActivity", "USB没权限");
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                Log.e("UsbActivity", "请求权限");
            }*/
        afterGetUsbPermission(usbDevice);

    }


    private void afterGetUsbPermission(UsbDevice usbDevice) {
        //call method to set up device communication
        Toast.makeText(UsbActivity.this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        Toast.makeText(UsbActivity.this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        Log.e("UsbActivity", "doYourOpenUsbDevice: "+connection.getSerial() );
        //add your operation code here
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };


    @Override
    public void onClick(View v) {

    }


    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 这里可以拿到插入的USB设备对象
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备
                    Toast.makeText(UsbActivity.this, "插入USB设备", Toast.LENGTH_LONG).show();
                    tryGetUsbPermission(usbDevice);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备

                    break;
                default:
                    break;
            }
        }
    }


    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        USBReceiver  mUsbReceiver = new USBReceiver();
        registerReceiver(mUsbReceiver, filter);
    }

}