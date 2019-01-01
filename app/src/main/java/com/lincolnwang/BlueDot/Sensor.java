package com.lincolnwang.BlueDot;

/**
 * Created by 11304 on 2018/11/13.
 */

public enum Sensor {

    DISTANCE(0,"距离"),
    LIGHT(1,"环境光"),
    RECORDER(2,"音强"),
    MAGNETIC(3,"磁力计"),
    ACCEX(4,"加X轴"),
    ACCEY(5,"加Y轴"),
    ACCEZ(6,"加Z轴"),
    ORIENTATION(7,"指南针");


    int sensor;
    String value;
    float currentValue;

    public int getSensor() {
        return sensor;
    }

    public void setSensor(int sensor) {
        this.sensor = sensor;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    private Sensor(int sensor, String value){
        this.sensor = sensor;
        this.value = value;
    }

}
