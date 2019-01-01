package com.lincolnwang.BlueDot;

import android.app.AlertDialog;
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
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.PointValue;

public class MotorActivity extends AppCompatActivity implements UiRefreshListener,View.OnTouchListener {

    private TTS tts;
    private AlertDialog dialog_AddPath;
    private MotorActionListViewAdapter motorActionListViewAdapter;
    public static List<CarAction> listMotorAction = new ArrayList<>();
    public static List<SensorRecord> recordList = new ArrayList<>();
    private ListView listView;
    BluetoothService.BluetoothBinder bluetoothBinder;
    SensorService.SensorBinder sensorBinder;

    Thread threadRun;
    TextView txtAction0,txtAction1,txtAction2,txtAction3,txtAction4,txtAction5,txtAction6,txtAction7,txtAction8;
    TextView txtCmd0,txtCmd1;
    HorizontalScrollView horizontalScrollView;

    private boolean isDrag = false;
    private final static long dragResponseMS = 150;
    private Bitmap mDragBitmap;
    private int mDownX,mDownY;
    private int mDownRawX,mDownRawY;
    private int moveX,moveY;
    private View mStartDragItemView = null;
    private ImageView mDragImageView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private int mPoint2ItemTop; // 按下的点到所在item的上边缘的距离
    private int mPoint2ItemLeft; // 按下的点到所在item的左边缘的距离
    private int mOffset2Top; // View距离屏幕顶部的偏移量
    private int mOffset2Left; // View距离屏幕左边的偏移量
    private int mStatusHeight=50; // 状态栏的高度
    private Menu mMenu;
    private boolean isMotorRun = false;

    private static Map<ActionType,Integer> actionTypeIntegerMap = new HashMap<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rulepathactivity_menu,menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_run:
//                if(mBindr == null ||mBindr.getIsBtConnected() == false){
//                    Toast.makeText(this,"设备未连接",Toast.LENGTH_LONG).show();
//                    return false;
//                }
                if(isMotorRun){
                    isMotorRun = false;
                    item.setIcon(R.mipmap.ic_play_arrow_white_48dp);
                    if(threadRun != null){
                        threadRun.interrupt();
                        threadRun = null;
                    }
                }else{
                    isMotorRun = true;
                    item.setIcon(R.mipmap.ic_stop_white_48dp);
                    if(threadRun == null){
                        threadRun = new Thread(runnable);
                    }
                    threadRun.start();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor);
        Utils.initPermission(this);
        tts = new TTS(this);

        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        listView = (ListView) findViewById(R.id.listView);
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.layoutBottom);
        motorActionListViewAdapter=new MotorActionListViewAdapter(this, listMotorAction);
        listView.setAdapter(motorActionListViewAdapter);

        Intent bluetoothService = new Intent(MotorActivity.this,BluetoothService.class);
        bindService(bluetoothService,bluetoothConn,Context.MODE_PRIVATE);
        Intent sensorService = new Intent(MotorActivity.this,SensorService.class);
        bindService(sensorService,sensorConn,Context.MODE_PRIVATE);

        actionTypeIntegerMap.put(ActionType.FORWARD,0);
        actionTypeIntegerMap.put(ActionType.BACK,1);
        actionTypeIntegerMap.put(ActionType.LIGHT,2);
        actionTypeIntegerMap.put(ActionType.RECORDER,3);
        actionTypeIntegerMap.put(ActionType.MAGNETIC,4);
        actionTypeIntegerMap.put(ActionType.ACCEX,5);
        actionTypeIntegerMap.put(ActionType.ACCEY,6);
        actionTypeIntegerMap.put(ActionType.ACCEZ,7);
        actionTypeIntegerMap.put(ActionType.ORIENTATION,8);

        txtAction0 = (TextView) findViewById(R.id.txtAction0);
        txtAction1 = (TextView) findViewById(R.id.txtAction1);
        txtAction2 = (TextView) findViewById(R.id.txtAction2);
        txtAction3 = (TextView) findViewById(R.id.txtAction3);
        txtAction4 = (TextView) findViewById(R.id.txtAction4);
        txtAction5 = (TextView) findViewById(R.id.txtAction5);
        txtAction6 = (TextView) findViewById(R.id.txtAction6);
        txtAction7 = (TextView) findViewById(R.id.txtAction7);
        txtAction8 = (TextView) findViewById(R.id.txtAction8);
        txtCmd0 = (TextView) findViewById(R.id.txtCmd0);
        txtCmd1 = (TextView) findViewById(R.id.txtCmd1);

        txtAction0.setOnTouchListener(this);
        txtAction1.setOnTouchListener(this);
        txtAction2.setOnTouchListener(this);
        txtAction3.setOnTouchListener(this);
        txtAction4.setOnTouchListener(this);
        txtAction5.setOnTouchListener(this);
        txtAction6.setOnTouchListener(this);
        txtAction7.setOnTouchListener(this);
        txtAction8.setOnTouchListener(this);

        txtCmd0.setOnTouchListener(this);
        txtCmd1.setOnTouchListener(this);
    }

    Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {

        }

    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isMotorRun = true;
            initSensorIdentifier();
            runCar(listMotorAction);
            isMotorRun=false;
            Intent monitorActivity = new Intent(MotorActivity.this,MonitorActivity.class);
            startActivity(monitorActivity);
        }
    };

    private Map<Sensor,Integer> sensorIdentifierMap = new HashMap<>();

    private void initSensorIdentifier(){
        sensorIdentifierMap.clear();
        int original = 0;
        sensorIdentifierMap.put(Sensor.LIGHT,original);
        sensorIdentifierMap.put(Sensor.RECORDER,original);
        sensorIdentifierMap.put(Sensor.MAGNETIC,original);
        sensorIdentifierMap.put(Sensor.ACCEX,original);
        sensorIdentifierMap.put(Sensor.ACCEY,original);
        sensorIdentifierMap.put(Sensor.ACCEZ,original);
        sensorIdentifierMap.put(Sensor.ORIENTATION,original);
    }

    private void runCar(List<CarAction> _listCarAction){
        int indexOfAction = 0;
        int pluse = 0;
        while(isMotorRun){
            try {
                CarAction carAction=_listCarAction.get(indexOfAction);
                int cmd=carAction.getCmdType().getCmd();
                int value = 0;
                SensorRecord sensorRecord;
                if(cmd==CmdType.SEQUEN.getCmd()){
                    switch (carAction.getActionType()){
                        case FORWARD:
                            pluse = carAction.getPulse();
                            break;
                        case BACK:
                            pluse = carAction.getPulse();
                            break;
                        case LIGHT:
                        case RECORDER:
                        case MAGNETIC:
                        case ACCEX:
                        case ACCEY:
                        case ACCEZ:
                        case ORIENTATION:
                            value = sensorIdentifierMap.get(carAction.getSensor()) + 1 ;
                            sensorIdentifierMap.put(carAction.getSensor(),value);
                            sensorRecord = new SensorRecord(carAction.getSensor(),carAction.getSensor().getValue() + value);
                            sensorRecord.addPointValue(new PointValue(0,1));
                            sensorRecord.addPointValue(new PointValue(1,4));
                            sensorRecord.addPointValue(new PointValue(2,3));
                            sensorRecord.addPointValue(new PointValue(3,2));
                            sensorRecord.addPointValue(new PointValue(4,3));
                            recordList.add(sensorRecord);
                            break;
                        default:
                            break;
                    }
                }else if(cmd==CmdType.LOOP.getCmd()){
                    for(int i = 0;i < carAction.getNumOfLoop();i++)
                        runCar(carAction.getLoopCarAction());
                }
                else if(cmd == CmdType.CONDITION.getCmd()) {
                    if(carAction.getIsEstablish()) {
                        runCar(carAction.getConditionCarAction());
                    }
                }
                indexOfAction++;
                if(indexOfAction==_listCarAction.size()){
                    break;
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }

    }

    private Runnable mLongClickRunnable = new Runnable() {

        @Override
        public void run() {
            //mVibrator.vibrate(50); //震动一下

            createDragImage(mDragBitmap, mDownX, mDownY);
            isDrag=true;

            horizontalScrollView.requestDisallowInterceptTouchEvent(true);
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mHandler.postDelayed(mLongClickRunnable, dragResponseMS);

                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mDownRawX = (int) event.getRawX();
                mDownRawY = (int) event.getRawY();

                mStartDragItemView = view ;
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
                    moveX = (int)event.getX() - horizontalScrollView.getScrollX(); // 滑动后的距离
                    moveY = (int)event.getY();
                    onDragItem(moveX, moveY);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mLongClickRunnable);
                if(isDrag && mDragImageView != null){
                    onStopDrag();
                    isDrag = false;
                    CarAction carAction = new CarAction();
                    carAction.setModule(Module.Car);
                    carAction.setCmdType(CmdType.SEQUEN);
                    carAction.setLen(0);
                    carAction.setDegree(0);
                    switch (view.getId()) {
                        case R.id.txtAction0:
                            carAction.setActionType(ActionType.FORWARD);
                            break;
                        case R.id.txtAction1:
                            carAction.setActionType(ActionType.BACK);
                            break;
                        case R.id.txtAction2:
                            carAction.setActionType(ActionType.LIGHT);
                            carAction.setSensor(Sensor.LIGHT);
                            break;
                        case R.id.txtAction3:
                            carAction.setActionType(ActionType.RECORDER);
                            carAction.setSensor(Sensor.RECORDER);
                            break;
                        case R.id.txtAction4:
                            carAction.setActionType(ActionType.MAGNETIC);
                            carAction.setSensor(Sensor.MAGNETIC);
                            break;
                        case R.id.txtAction5:
                            carAction.setActionType(ActionType.ACCEX);
                            carAction.setSensor(Sensor.ACCEX);
                            break;
                        case R.id.txtAction6:
                            carAction.setActionType(ActionType.ACCEY);
                            carAction.setSensor(Sensor.ACCEY);
                            break;
                        case R.id.txtAction7:
                            carAction.setActionType(ActionType.ACCEZ);
                            carAction.setSensor(Sensor.ACCEZ);
                            break;
                        case R.id.txtAction8:
                            carAction.setActionType(ActionType.ORIENTATION);
                            carAction.setSensor(Sensor.ORIENTATION);
                            break;
                        case R.id.txtCmd0:
                            carAction.setCmdType(CmdType.LOOP);
                            carAction.setNumOfLoop(0);
                            carAction.setLoopCarAction(new ArrayList<CarAction>());
                            break;
                        case R.id.txtCmd1:
                            carAction.setCmdType(CmdType.CONDITION);
                            carAction.setSensor(null);
                            carAction.setCondition(new ArrayList<SingleCondition>());
                            carAction.setConditionValue(0);
                            carAction.setConditionCarAction(new ArrayList<CarAction>());
                            break;

                        default:
                            break;
                    }

                    int x=mWindowLayoutParams.x;
                    int y=mWindowLayoutParams.y - listView.getScrollY();

                    addActionView(listView,listMotorAction,carAction,x,y);
                    if(motorActionListViewAdapter == null){
                        motorActionListViewAdapter = new MotorActionListViewAdapter(MotorActivity.this, listMotorAction);
                    }
                    listView.setAdapter(motorActionListViewAdapter);
                    motorActionListViewAdapter.notifyDataSetChanged();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mLongClickRunnable);
                if(isDrag && mDragImageView!=null){
                    onStopDrag();
                    isDrag = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void addActionView(ListView _listView,List<CarAction> list,CarAction carAction,int x,int y){

        int insertPosition = _listView.pointToPosition(x,y);
        MotorActionListViewAdapter adapter = (MotorActionListViewAdapter) _listView.getAdapter();
        if(insertPosition==-1){
            list.add(carAction);
        }else{
            if(list.get(insertPosition).getCmdType()==CmdType.LOOP){
                // 判断触摸点与view的相对位置
                View view=_listView.getChildAt(insertPosition);
                if(view!=null){
                    if(y>(int) (view.getY()+50)){
                        // 搜索出子listview
                        ListView v=(ListView) view.findViewById(R.id.listView);

                        addActionView(v,list.get(insertPosition).getLoopCarAction(),carAction,x,y-(int)view.getY()-50);
                        return;
                    }
                }
            }
            if(list.get(insertPosition).getCmdType() == CmdType.CONDITION){
                View view=_listView.getChildAt(insertPosition);
                if(view != null){
                    if(y>(int) (view.getY()+50)){
                        ListView v=(ListView) view.findViewById(R.id.listView);

                        addActionView(v,list.get(insertPosition).getConditionCarAction(),carAction,x,y-(int)view.getY()-50);
                        return;
                    }
                }
            }
            list.add(insertPosition,carAction);
        }
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

        //GridView自动滚动
        //mHandler.post(mScrollRunnable);
    }

    private void onStopDrag(){
        removeDragImage();
    }

    private void removeDragImage(){
        if(mDragImageView != null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
    }

    public void showDialogAddPath(final boolean isAdd, final List<CarAction> _listCarAction, final int position) {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=this.getLayoutInflater().inflate(R.layout.dialog_add_motor, null);
        final EditText txtTrLen,txtTrDegree,txtStep;
        final Spinner spAction,spCmd;
        final LinearLayout layout_len,layout_degree,layout_action,layout_step;
        txtTrLen=(EditText) view.findViewById(R.id.txtTrLen);
        txtTrDegree=(EditText) view.findViewById(R.id.txtTrDegree);
        txtStep=(EditText) view.findViewById(R.id.txtStep);
        spCmd=(Spinner) view.findViewById(R.id.spCmd);
        spAction=(Spinner) view.findViewById(R.id.spAction);
        layout_len=(LinearLayout) view.findViewById(R.id.layout_len);
        layout_degree=(LinearLayout) view.findViewById(R.id.layout_degree);
        layout_action=(LinearLayout) view.findViewById(R.id.layout_action);
        layout_step=(LinearLayout) view.findViewById(R.id.layout_step);

        if(isAdd){
            builder.setTitle("添加动作");
        }else {
            builder.setTitle("修改动作");
            CarAction carAction=_listCarAction.get(position);
            if(carAction.getCmdType()==CmdType.SEQUEN){
                spCmd.setSelection(CmdType.SEQUEN.getCmd());
                layout_action.setVisibility(View.VISIBLE);
                layout_step.setVisibility(View.GONE);
                spAction.setSelection(actionTypeIntegerMap.get(carAction.getActionType()));
                if(carAction.getActionType()==ActionType.FORWARD ||carAction.getActionType()==ActionType.BACK){
                    layout_len.setVisibility(View.VISIBLE);
                    layout_degree.setVisibility(View.GONE);
                    txtTrLen.setText(String.valueOf(carAction.getPulse()));
                }
                else {
                    layout_len.setVisibility(View.GONE);
                    layout_degree.setVisibility(View.GONE);
                    layout_step.setVisibility(View.GONE);
                }
            }else if(carAction.getCmdType()==CmdType.LOOP){
                spCmd.setSelection(CmdType.LOOP.getCmd());
                layout_action.setVisibility(View.GONE);
                layout_len.setVisibility(View.GONE);
                layout_degree.setVisibility(View.GONE);
                layout_step.setVisibility(View.VISIBLE);

                txtStep.setText(String.valueOf(carAction.getNumOfLoop()));
            }
        }

        view.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CarAction carAction;
                if(isAdd){
                    carAction=new CarAction();
                }else{
                    carAction=_listCarAction.get(position);
                }

                int action=spAction.getSelectedItemPosition();
                int cmd=spCmd.getSelectedItemPosition();
                if(cmd==CmdType.SEQUEN.getCmd()){
                    carAction.setCmdType(CmdType.SEQUEN);
                    int pluse = 0;
                    switch (action) {
                        case 0: // 前进
                            pluse = Integer.valueOf(txtTrLen.getText().toString()) ;
                            carAction.setActionType(ActionType.FORWARD);
                            carAction.setPulse(pluse);
                            break;
                        case 1: // 后退
                            pluse = Integer.valueOf(txtTrLen.getText().toString()) ;
                            carAction.setActionType(ActionType.BACK);
                            carAction.setPulse(pluse);
                            break;
                        case 2: // 光照
                            carAction.setActionType(ActionType.LIGHT);
                            break;
                        case 3: // 分贝
                            carAction.setActionType(ActionType.RECORDER);
                            break;
                        case 4: // 磁力计
                            carAction.setActionType(ActionType.MAGNETIC);
                            break;
                        case 5: // 加X轴
                            carAction.setActionType(ActionType.ACCEX);
                            break;
                        case 6: // 加Y轴
                            carAction.setActionType(ActionType.ACCEY);
                            break;
                        case 7: // 加Z轴
                            carAction.setActionType(ActionType.ACCEZ);
                            break;
                        case 8: // 指南针
                            carAction.setActionType(ActionType.ORIENTATION);
                            break;

                        default:
                            break;
                    }
                }else if(cmd==CmdType.LOOP.getCmd()){
                    carAction.setCmdType(CmdType.LOOP);
                    int step=0;
                    if(StringUtils.isNotEmpty(txtStep.getText().toString())){
                        step = Integer.valueOf(txtStep.getText().toString());
                    }
                    carAction.setNumOfLoop(step);
                    if(isAdd){
                        // 实例子对象
                        carAction.setLoopCarAction(new ArrayList<CarAction>());
                    }else{
                        // 不修改子对象
                    }
                }

                if(isAdd){
                    _listCarAction.add(position+1, carAction); // 向后插入一个对象
                }else{
                    _listCarAction.set(position, carAction); // 修改对象
                }

                // 刷新列表
                motorActionListViewAdapter = new MotorActionListViewAdapter(MotorActivity.this,listMotorAction);
                listView.setAdapter(motorActionListViewAdapter);
                dialog_AddPath.cancel();
            }
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_AddPath.cancel();
            }
        });

        spCmd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        layout_action.setVisibility(View.VISIBLE);
                        layout_len.setVisibility(View.VISIBLE);
                        layout_degree.setVisibility(View.GONE);
                        layout_step.setVisibility(View.GONE);
                        break;
                    case 1:
                        layout_action.setVisibility(View.GONE);
                        layout_len.setVisibility(View.GONE);
                        layout_degree.setVisibility(View.GONE);
                        layout_step.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        spAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                //				System.out.println(position);
                switch (position) {
                    case 0:
                    case 1:
                        layout_len.setVisibility(View.VISIBLE);
                        layout_degree.setVisibility(View.GONE);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        layout_len.setVisibility(View.GONE);
                        layout_degree.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(view);
        dialog_AddPath=builder.create();
        dialog_AddPath.show();
    }

    public void delCarAction(List<CarAction> _listCarAction, int position){
        _listCarAction.remove(position);

        motorActionListViewAdapter = new MotorActionListViewAdapter(MotorActivity.this, listMotorAction);
        listView.setAdapter(motorActionListViewAdapter);
    }

    @Override
    public void onRefresh() {
        if(motorActionListViewAdapter != null && listView != null){
            motorActionListViewAdapter = new MotorActionListViewAdapter(MotorActivity.this, listMotorAction);
            listView.setAdapter(motorActionListViewAdapter);
            listView.setSelection(listView.getCount() - 1);
        }
    }

    ServiceConnection bluetoothConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bluetoothBinder = (BluetoothService.BluetoothBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    ServiceConnection sensorConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorBinder = (SensorService.SensorBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
