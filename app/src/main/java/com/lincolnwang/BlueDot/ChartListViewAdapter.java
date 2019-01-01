package com.lincolnwang.BlueDot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by 11304 on 2018/11/15.
 */

public class ChartListViewAdapter extends BaseAdapter {

    private List<Chart> chartList;
    private Context context;

    public ChartListViewAdapter(Context context,List<Chart> chartList){
        this.context = context;
        this.chartList = chartList;
    }

    @Override
    public int getCount() {
        return chartList == null ? 0:chartList.size();
    }

    @Override
    public Object getItem(int position) {
        return chartList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sensor,null);
        Chart chart = chartList.get(position);
        TextView txtChartTitle = (TextView) convertView.findViewById(R.id.txtChartTitle);
        ImageView imageSensor = (ImageView) convertView.findViewById(R.id.imageSensor);
        LineChartData lineChartData = new LineChartData(chart.getLineList());
        LineChartView lineChartView = (LineChartView) convertView.findViewById(R.id.chart);
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName("Axis X");
        axisY.setName("Axis Y");
        lineChartData.setAxisXBottom(axisX);
        lineChartData.setAxisYLeft(axisY);
        if(lineChartView != null){
            lineChartView.setLineChartData(lineChartData);
        }
        if(txtChartTitle != null){
            txtChartTitle.setText(chart.getSensor().getValue());
        }

        if(chart.getSensor() == Sensor.LIGHT){
            imageSensor.setImageResource(R.drawable.ambient);
        }
        if(chart.getSensor() == Sensor.RECORDER){
            imageSensor.setImageResource(R.drawable.decibel);
        }
        if(chart.getSensor() == Sensor.MAGNETIC){
            imageSensor.setImageResource(R.drawable.magnetometer);
        }
        if(chart.getSensor() == Sensor.ACCEX){
            imageSensor.setImageResource(R.drawable.accx);
        }
        if(chart.getSensor() == Sensor.ACCEY){
            imageSensor.setImageResource(R.drawable.accy);
        }
        if(chart.getSensor() == Sensor.ACCEZ){
            imageSensor.setImageResource(R.drawable.accz);
        }
        if(chart.getSensor() == Sensor.ORIENTATION){
            imageSensor.setImageResource(R.drawable.compass_meter);
        }

        return convertView;
    }
}
