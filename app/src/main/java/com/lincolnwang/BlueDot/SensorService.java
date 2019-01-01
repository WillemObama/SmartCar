package com.lincolnwang.BlueDot;

import android.app.Service;
import android.content.Intent;
import android.hardware.*;
import android.hardware.Sensor;
import android.media.AudioRecord;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.PointValue;


public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private float light = 0;
    private float volume = 0;
    private float accex = 0;
    private float accey = 0;
    private float accez = 0;
    private float magnetic = 0;
    private float orientation = 0;
    private static Map<com.lincolnwang.BlueDot.Sensor,List<PointValue>> sensorMap;

    float[] gravity = new float[3];//用来保存加速度传感器的值
    float[] r = new float[9];//
    float[] geomagnetic = new float[3];//用来保存地磁传感器的值
    float[] values = new float[3];//用来保存最终的结果

    private SensorBinder sensorBinder;
    AudioRecordDemo audioRecordDemo = new AudioRecordDemo();
    static SensorActivity.SensorListener sensorListener;
    SensorThread sensorThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取系统传感器服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //注册环境光监听
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);
        //注册磁场传感器
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
        //获取加速度传感器
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        audioRecordDemo.getNoiseLevel();
        audioRecordDemo.setVolumeListener(volumeListener);
        return super.onStartCommand(intent, flags, startId);
    }

    public SensorService() {

        sensorBinder = new SensorBinder();
        sensorMap = new android.support.v4.util.ArrayMap<>();
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.LIGHT,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.RECORDER,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.MAGNETIC,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.ACCEX,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.ACCEY,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.ACCEZ,new ArrayList<PointValue>());
        ((android.support.v4.util.ArrayMap<com.lincolnwang.BlueDot.Sensor, List<PointValue>>) sensorMap).
                put(com.lincolnwang.BlueDot.Sensor.ORIENTATION,new ArrayList<PointValue>());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sensorBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LIGHT:
                light = event.values[0];
                com.lincolnwang.BlueDot.Sensor.LIGHT.setCurrentValue(light);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetic = event.values[0];
                geomagnetic = event.values;
                com.lincolnwang.BlueDot.Sensor.MAGNETIC.setCurrentValue(magnetic);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accex = event.values[0];
                accey = event.values[1];
                accez = event.values[2];
                gravity = event.values;
                orientation = getOrientation();
                com.lincolnwang.BlueDot.Sensor.ACCEX.setCurrentValue(accex);
                com.lincolnwang.BlueDot.Sensor.ACCEY.setCurrentValue(accey);
                com.lincolnwang.BlueDot.Sensor.ACCEZ.setCurrentValue(accez);
                com.lincolnwang.BlueDot.Sensor.ORIENTATION.setCurrentValue(orientation);
                break;
            default:
                break;
        }
    }

    public void startCollection() {
        clearSensorData();
        if(sensorThread == null)
        {
            sensorThread = new SensorThread();
            sensorThread.start();
        }

    }

    public void stopCollection(){
        if(sensorThread != null){
            sensorThread.interrupt();
        }
    }

    public static void clearSensorData(){
        for (List<PointValue> data : sensorMap.values()
             ) {
            data.clear();
        }
    }

    public static SensorActivity.SensorListener getSensorListener() {
        return sensorListener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getOrientation(){
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        SensorManager.getOrientation(r, values);
        return (float) Math.toDegrees(values[0]);
    }

    VolumeListener volumeListener = new VolumeListener() {
        @Override
        public void onVolumeChanged(float volume) {
            com.lincolnwang.BlueDot.Sensor.RECORDER.setCurrentValue(volume);
        }
    };

    public static Map<com.lincolnwang.BlueDot.Sensor, List<PointValue>> getSensorMap() {
        return sensorMap;
    }

    public class SensorBinder extends Binder{
        public void startCollection(){
            SensorService.this.startCollection();
        }

        public void stopCollection(){
            SensorService.this.stopCollection();
        }

        public void setSensorListener(SensorActivity.SensorListener listener){
            SensorService.this.sensorListener = listener;
        }
    }

}
interface VolumeListener{
    public void onVolumeChanged(float volume);
}

class SensorThread extends Thread {

    private volatile static boolean on = false;

        @Override
        public void run() {
            int index = 0;
            while (!on){
                try{
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.LIGHT).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.LIGHT.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.RECORDER).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.RECORDER.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.MAGNETIC).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.MAGNETIC.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.ACCEX).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.ACCEX.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.ACCEY).add(new PointValue(index, com.lincolnwang.BlueDot.Sensor.ACCEY.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.ACCEZ).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.ACCEZ.getCurrentValue()));
                    SensorService.getSensorMap().get(com.lincolnwang.BlueDot.Sensor.ORIENTATION).add(new PointValue(index,com.lincolnwang.BlueDot.Sensor.ORIENTATION.getCurrentValue()));
                    if(SensorService.getSensorListener() != null){
                        SensorService.getSensorListener().onRefresh();
                    }
                    index++;
                    if(index > Integer.MAX_VALUE){
                        index = 0;
                        SensorService.clearSensorData();
                    }

                    Thread.sleep(500);
                }
                catch (InterruptedException e){

                }

            }
        }
    };