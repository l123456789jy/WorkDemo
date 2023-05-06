package com.camera2demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ArrayList<String> integerArrayListExtra = intent.getStringArrayListExtra("desay.location.callpackageName");
        for (String s : integerArrayListExtra) {
            Log.d(TAG, "onReceive:action "+s);
        }

    }
}
