package com.lincolnwang.BlueDot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.serializer.SerializeWriter;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

public class SensorActivity extends AppCompatActivity implements View.OnTouchListener {

    public static ChartListViewAdapter chartListViewAdapter;
    public static List<Chart> chartList = new ArrayList<>();

    private final static long dragResponseMS = 150;
    private View mStartDragItemView = null;
    private Bitmap mDragBitmap;
    private ImageView mDragImageView;
    private boolean isDrag = false;
    private int mDownX,mDownY;
    private int mDownRawX,mDownRawY;
    private int moveX,moveY;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private int mPoint2ItemTop; // 按下的点到所在item的上边缘的距离
    private int mPoint2ItemLeft; // 按下的点到所在item的左边缘的距离
    private int mOffset2Top; // View距离屏幕顶部的偏移量
    private int mOffset2Left; // View距离屏幕左边的偏移量
    private int mStatusHeight=50; // 状态栏的高度

    TextView txtSensor0,txtSensor1,txtSensor2,txtSensor3,txtSensor4,txtSensor5,txtSensor6;
    HorizontalScrollView horizontalScrollView;

    SensorService.SensorBinder sensorBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Intent sensorService = new Intent(SensorActivity.this,SensorService.class);
        bindService(sensorService,sensorConnection,Context.MODE_PRIVATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWindowManager=(WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        chartListViewAdapter = new ChartListViewAdapter(this,chartList);
        ListView ChartListView = (ListView) this.findViewById(R.id.chartList);
        ChartListView.setAdapter(chartListViewAdapter);

        txtSensor0 = (TextView) this.findViewById(R.id.txtSensor0);
        txtSensor1 = (TextView) this.findViewById(R.id.txtSensor1);
        txtSensor2 = (TextView) this.findViewById(R.id.txtSensor2);
        txtSensor3 = (TextView) this.findViewById(R.id.txtSensor3);
        txtSensor4 = (TextView) this.findViewById(R.id.txtSensor4);
        txtSensor5 = (TextView) this.findViewById(R.id.txtSensor5);
        txtSensor6 = (TextView) this.findViewById(R.id.txtSensor6);
        horizontalScrollView = (HorizontalScrollView) this.findViewById(R.id.horizontalScrollView);

        txtSensor0.setOnTouchListener(this);
        txtSensor1.setOnTouchListener(this);
        txtSensor2.setOnTouchListener(this);
        txtSensor3.setOnTouchListener(this);
        txtSensor4.setOnTouchListener(this);
        txtSensor5.setOnTouchListener(this);
        txtSensor6.setOnTouchListener(this);

    }

    @Override
    protected void onDestroy() {
        sensorBinder.stopCollection();
        super.onDestroy();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private Runnable mLongClickRunnable = new Runnable() {

        @Override
        public void run() {
            createDragImage(mDragBitmap, mDownX, mDownY);
            isDrag=true;

            horizontalScrollView.requestDisallowInterceptTouchEvent(true);
        }
    };

    private List<PointValue> toPointValueList(List<Float> values){
        List<PointValue> list = new ArrayList<>();
        for (int i = 0; i < values.size() ;i ++){
            PointValue pointValue = new PointValue(i,values.get(i));
            list.add(pointValue);
        }
        return list;

    }

    private boolean isAlive(Sensor sensor){
        for (Chart chart:
             chartList) {
            if(chart.getSensor() == sensor)
                return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mHandler.postDelayed(mLongClickRunnable,dragResponseMS);
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mDownRawX = (int) event.getRawX();
                mDownRawY = (int) event.getRawY();

                mStartDragItemView=v;
                mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
                mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
                mOffset2Top = mDownRawY - mDownY;
                mOffset2Left = mDownRawX - mDownX;

                //开启mDragItemView绘图缓存
                mStartDragItemView.setDrawingCacheEnabled(true);
                //获取mDragItemView在缓存中的Bitmap对象
                mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
                //释放绘图缓存，避免出现重复的镜像
                mStartDragItemView.destroyDrawingCache();
                break;
            case MotionEvent.ACTION_MOVE:
                if(isDrag && mDragImageView!=null){
                    moveX = (int)event.getX()-horizontalScrollView.getScrollX(); // 滑动后的距离
                    moveY = (int)event.getY();
                    onDragItem(moveX, moveY);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mLongClickRunnable);
                if(isDrag && mDragImageView!=null){
                    onStopDrag();
                    isDrag = false;
                    Chart chart = new Chart();
                    switch (v.getId()){
                        case R.id.txtSensor0:
                            chart.setSensor(Sensor.LIGHT);
                            break;
                        case R.id.txtSensor1:
                            chart.setSensor(Sensor.RECORDER);
                            break;
                        case R.id.txtSensor2:
                            chart.setSensor(Sensor.MAGNETIC);
                            break;
                        case R.id.txtSensor3:
                            chart.setSensor(Sensor.ACCEX);
                            break;
                        case R.id.txtSensor4:
                            chart.setSensor(Sensor.ACCEY);
                            break;
                        case R.id.txtSensor5:
                            chart.setSensor(Sensor.ACCEZ);
                            break;
                        case R.id.txtSensor6:
                            chart.setSensor(Sensor.ORIENTATION);
                            break;
                        default:
                            break;
                    };
                    if(isAlive(chart.getSensor()) == false ){
                        chartList.add(chart);
                        chartListViewAdapter.notifyDataSetChanged();
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mLongClickRunnable);
                if(isDrag && mDragImageView!=null){
                    onStopDrag();
                    isDrag = false;
                }
                break;
            default: break;
        }
        return false;
    }


    private void createDragImage(Bitmap bitmap, int downX , int downY){
        if(mWindowLayoutParams==null)
            mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = downX - mPoint2ItemLeft;
        mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowLayoutParams.alpha = 0.55f; //透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;

        mDragImageView = new ImageView(this);
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    private void onDragItem(int moveX, int moveY){
        mWindowLayoutParams.x = moveX - mPoint2ItemLeft;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置
    }

    private void removeDragImage(){
        if(mDragImageView != null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
    }

    private void onStopDrag(){
        removeDragImage();
    }

    ServiceConnection sensorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorBinder = (SensorService.SensorBinder) iBinder;
            sensorBinder.startCollection();
            sensorBinder.setSensorListener(sensorListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public interface SensorListener{
        public void onRefresh();
    }

    SensorListener sensorListener = new SensorListener() {
        @Override
        public void onRefresh() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Chart chart:
                         chartList) {
                         List<PointValue> pointValues = SensorService.getSensorMap().get(chart.getSensor());
                         chart.setPointValueList(pointValues);
                    }
                    chartListViewAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}
