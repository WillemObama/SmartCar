package com.lincolnwang.BlueDot;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by 11304 on 2018/11/15.
 */

public class Chart {

    private List<PointValue> pointValueList;

    private Sensor sensor;

    private List<Line> lineList;

    private Line line;

    public Chart(){

    }

    public Chart(Sensor sensor,List<PointValue> pointValueList){
        this.sensor = sensor;
        this.pointValueList = pointValueList;
        this.lineList = new ArrayList<>();
        this.line = new Line(this.pointValueList);
        this.lineList.add(line);
    }

    public List<PointValue> getPointValueList() {
        return pointValueList;
    }

    public void setPointValueList(List<PointValue> pointValueList) {
        this.pointValueList = pointValueList;
        this.lineList = new ArrayList<>();
        this.line = new Line(this.pointValueList);
        line.setColor(Color.RED).setCubic(false).setStrokeWidth(1).setHasPoints(false);
        line.setHasLabels(true);
        this.lineList.add(line);
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }
}
