package com.lincolnwang.BlueDot;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class MonitorActivity extends AppCompatActivity implements MonitorListViewAdapter.ListViewClickListener {

    ListView listView;
    LinearLayout chartLayout;
    View chart;
    MonitorListViewAdapter monitorListViewAdapter;
    Drawable titleBackground;
    ValueAnimator colorFadeAnimation;

    private List<SensorRecord> recordList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        recordList = MotorActivity.recordList;
        listView = (ListView) findViewById(R.id.listView);
        chartLayout = (LinearLayout) findViewById(R.id.chartLayout);
        listView.setDivider(null);
        chart = View.inflate(this,R.layout.item_sensor,null);
        chartLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = chartLayout.getWidth();
                int height = chartLayout.getHeight();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(height,width);
                chart.setLayoutParams(layoutParams);
                chart.setPivotX(0);
                chart.setPivotY(0);
                chart.setRotation(90);
                chart.setTranslationX(width);
                showChart(chart,0);
                chartLayout.addView(chart);
            }
        });

        monitorListViewAdapter = new MonitorListViewAdapter(this,recordList);
        listView.setAdapter(monitorListViewAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(MonitorListViewAdapter.lastPosition >= firstVisibleItem && MonitorListViewAdapter.lastPosition < firstVisibleItem + visibleItemCount)
                    MonitorListViewAdapter.isLastShowed = true;
                else
                    MonitorListViewAdapter.isLastShowed = false;
            }
        });
    }

    private static LineChartData lineChartData = new LineChartData();
    private Axis axisY;
    private AxisValue axisValue;

    private void showChart(View view,int position){


        final SensorRecord record = recordList.get(position);
        ImageView imageSensor = (ImageView) view.findViewById(R.id.imageSensor);
        TextView txtChartTitle = (TextView) view.findViewById(R.id.txtChartTitle);
        LineChartView lineChartView = (LineChartView) view.findViewById(R.id.chart);
        LinearLayout titlebg = (LinearLayout) view.findViewById(R.id.titleBackground);

        titleBackground = titlebg.getBackground();
        titleBackground = DrawableCompat.wrap(titleBackground);
        colorFadeAnimation = ValueAnimator.ofArgb(titleBackground.getAlpha(),record.getTheme());
        titlebg.setBackground(titleBackground);
        colorFadeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DrawableCompat.setTint(titleBackground,(int)valueAnimator.getAnimatedValue());
            }
        });
        colorFadeAnimation.setDuration(300);
        colorFadeAnimation.start();

        if(record != null){
            switch (record.getSensor()){
                case LIGHT:
                    imageSensor.setImageResource(R.drawable.ambient);
                    break;
                case RECORDER:
                    imageSensor.setImageResource(R.drawable.decibel);
                    break;
                case MAGNETIC:
                    imageSensor.setImageResource(R.drawable.magnetometer);
                    break;
                case ACCEX:
                    imageSensor.setImageResource(R.drawable.accx);
                    break;
                case ACCEY:
                    imageSensor.setImageResource(R.drawable.accy);
                    break;
                case ACCEZ:
                    imageSensor.setImageResource(R.drawable.accz);
                    break;
                case ORIENTATION:
                    imageSensor.setImageResource(R.drawable.compass_meter);
                    break;
                default:
                    break;
            }
        }
        txtChartTitle.setText(record.getRecordName());
        final Line line = new Line(record.getPointList());
        line.setColor(record.getTheme());
        line.setCubic(false);
        line.setFilled(true);
        line.setHasLabels(true);
        lineChartData.setLines(new ArrayList<Line>(){{add(line);}});
        if(axisY == null){
            axisY = new Axis();
        }
        axisY.setName(record.getSensor().getValue());
        lineChartData.setAxisYLeft(axisY);
        lineChartView.setLineChartData(lineChartData);
    }

    private List<AxisValue> getAxisYValueList(List<PointValue> pointValueList){
        List<AxisValue> axisValueList = new ArrayList<>();
        for (PointValue value :
                pointValueList) {
            axisValueList.add(new AxisValue(value.getY()));
        }
        return axisValueList;
    }

    @Override
    public void onItemClick(int position) {
        if(chart != null)
        {
            showChart(chart,position);
        }

    }
}
