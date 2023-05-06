package com.camera2demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import static android.content.Context.LOCATION_SERVICE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * @author
 * https://www.jianshu.com/p/87e0dec25071   具体的位置信息
 */
public class GpsManager implements OnNmeaMessageListener {

    private String TAG = getClass().getSimpleName();

    private static GpsManager mInstance = null;
    private LocationManager locationManager;
    private Context mContext;

    public static GpsManager getInstance() {
        if (null == mInstance) {
            synchronized (GpsManager.class) {
                if (null == mInstance) {
                    mInstance = new GpsManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        Log.d(TAG, "init");
        mContext = context;
        //CarLanMsgMgr.getInstance().init(mContext);
        getAddress(30.763, 103.864);
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (!GpsUtil.isOPen(context)) {
            GpsUtil.openGPS(context);
            Toast.makeText(context, "GPS 未打开", Toast.LENGTH_LONG).show();
        }
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG, "no permission.ACCESS_FINE_LOCATION");
                return;
            }
            locationManager.addNmeaListener(this);
            locationManager.registerGnssStatusCallback(mGnssStatusCallback);//添加卫星状态改变监听
            // 最小时间间隔为1秒
            long minTime = 1000;
            // 最小距离间隔为0米,表示忽略距离变化
            float minDistance = 0;
            HandlerThread gps = new HandlerThread("gps");
            gps.start();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTime, minDistance, new MyGPsLocationListener(), gps.getLooper());
            Log.d(TAG, "locationManager.registerGnssStatusCallback(mGnssStatusCallback)");

        }
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        // 开始时间

        String msg = "收到的Nmea 回调 " + message;
        d(TAG, "onNmeaMessage " + msg);
        if (message.contains("$GNGGA")) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 结束时间

    }


    public class MyGPsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            getAddress(location.getLatitude(),location.getLongitude());
            Log.e(TAG, "onLocationChanged=================" + location.getLatitude() + "    Time======" + location.getTime());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    // 获取具体的位置信息
    private void getAddress(double latitude, double longitude) {
        //Geocoder通过经纬度获取具体信息
        Geocoder gc = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> locationList = gc.getFromLocation(latitude, longitude, 1);

            if (locationList != null) {
                Address address = locationList.get(0);
                String countryName = address.getCountryName();//国家
                String countryCode = address.getCountryCode();
                String adminArea = address.getAdminArea();//省
                String locality = address.getLocality();//市
                String subLocality = address.getSubLocality();//区
                String featureName = address.getFeatureName();//街道

                for (int i = 0; address.getAddressLine(i) != null; i++) {
                    String addressLine = address.getAddressLine(i);
                    Log.e(TAG, "addressLine=================" + addressLine);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    private int gpsEffectiveNum;
    private int beiDouEffectiveNum;
    private int galileoEffectiveNum;
    private int glonassEffectiveNum;
    private GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            d(TAG, "--------------------onSatelliteStatusChanged start---------------------------");
            int satelliteCount = status.getSatelliteCount();
            gpsEffectiveNum = 0;//gps卫星有效数量
            beiDouEffectiveNum = 0;//北斗卫星有效数量
            galileoEffectiveNum = 0;//伽利略卫星有效数量
            glonassEffectiveNum = 0;//格洛纳斯卫星有效数量
            for (int index = 0; index < satelliteCount; index++) {
                int type = status.getConstellationType(index);
                float azimuthDegrees = status.getAzimuthDegrees(index);
                float carrierFrequencyHz = status.getCarrierFrequencyHz(index);
                float cn0DbHz = status.getCn0DbHz(index);
                float elevationDegrees = status.getElevationDegrees(index);
                int svId = status.getSvid(index);
                boolean hasAlmanacData = status.hasAlmanacData(index);//是否有历年数据
                boolean hasCarrierFrequencyHz = status.hasCarrierFrequencyHz(index);//是否有载波频率Hz
                boolean hasEphemerisData = status.hasEphemerisData(index);//是否有星历数据
                boolean usedInFix = status.usedInFix(index);//是否在最后一次定位中使用过

                boolean isEffective = hasAlmanacData && hasEphemerisData && usedInFix;//true表示该卫星有效

                String typeName = null;
                switch (type) {
                    case GnssStatus.CONSTELLATION_GPS:
                        typeName = "GPS";
                        if (isEffective) {
                            gpsEffectiveNum++;
                        }
                        break;
                    case GnssStatus.CONSTELLATION_BEIDOU:
                        typeName = "北斗";
                        if (isEffective) {
                            beiDouEffectiveNum++;
                        }
                        break;
                    case GnssStatus.CONSTELLATION_GALILEO:
                        typeName = "GALILEO";
                        if (isEffective) {
                            galileoEffectiveNum++;
                        }
                        break;
                    case GnssStatus.CONSTELLATION_GLONASS:
                        typeName = "GLONASS";
                        if (isEffective) {
                            glonassEffectiveNum++;
                        }
                        break;
                    case GnssStatus.CONSTELLATION_QZSS:
                        typeName = "QZSS";
                        break;
                    case GnssStatus.CONSTELLATION_SBAS:
                        typeName = "SBAS";
                        break;
                    default:
                        typeName = "UNKNOWN";
                        break;
                }

                d(TAG, "onSatelliteStatusChanged  " + typeName
                        + " satelliteCount = " + satelliteCount
                        + " azimuthDegrees = " + azimuthDegrees
                        + " carrierFrequencyHz = " + carrierFrequencyHz
                        + " cn0DbHz = " + cn0DbHz
                        + " elevationDegrees = " + elevationDegrees
                        + " svId = " + svId
                        + " hasAlmanacData = " + hasAlmanacData
                        + " hasCarrierFrequencyHz = " + hasCarrierFrequencyHz
                        + " hasEphemerisData = " + hasEphemerisData
                        + " usedInFix = " + usedInFix);
            }
            String msg = "定位卫星  GPS " + gpsEffectiveNum + " 北斗 " + beiDouEffectiveNum;
            d(TAG, "onSatelliteStatusChanged " + msg);
            d(TAG, "--------------onSatelliteStatusChanged end-------------");


        }
    };

    public int getGpsEffectiveNum() {
        d(TAG, "getGpsEffectiveNum: " + gpsEffectiveNum);
        return gpsEffectiveNum;
    }

    public int getBeiDouEffectiveNum() {
        d(TAG, "getBeiDouEffectiveNum: " + beiDouEffectiveNum);
        return beiDouEffectiveNum;
    }

    public int getGalileoEffectiveNum() {
        d(TAG, "getGalileoEffectiveNum: " + galileoEffectiveNum);
        return galileoEffectiveNum;
    }

    public int getGlonassEffectiveNum() {
        d(TAG, "getGlonassEffectiveNum: " + glonassEffectiveNum);
        return glonassEffectiveNum;
    }

    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }


   /* public void testHal(){
        Location gps = new Location("gps");
        gps.setLatitude(111d);
        gps.setLongitude(23d);
        CarExtGnssManager.get().injectExtLocation(gps);
        CarExtGnssManager.get().injectExtNmea(System.currentTimeMillis(),"$GPGGA");
    }*/
}
