package com.lincolnwang.BlueDot;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by 11304 on 2018/12/29.
 */

public class SensorRecord {

    private String recordName;

    private Sensor sensor;

    private List<PointValue> pointList;

    private int theme;

    private boolean isSelect;

    public SensorRecord(Sensor sensor,String recordName){
        this.sensor = sensor;
        this.recordName = recordName;
        Random colorRandom = new Random();
        this.theme = Color.argb(255,colorRandom.nextInt(228) + 28,colorRandom.nextInt(228) + 28,colorRandom.nextInt(228) + 28);
        pointList = new ArrayList<>();
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public List<PointValue> getPointList() {
        return pointList;
    }

//    public void setPointList(List<PointValue> pointList) {
//        this.pointList = pointList;
//    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public void addPointValue(PointValue pointValue){
        pointList.add(pointValue);
    }
}
