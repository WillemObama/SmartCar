package com.lincolnwang.BlueDot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RulePathActivity extends AppCompatActivity implements View.OnTouchListener,BluetoothListener,UiRefreshListener {

    public static final String Tag = "RulePathActivity";

    ListView listView;
    public CarActionListViewAdapter CarActionListViewAdapter;
    public static List<CarAction> listCarAction = new ArrayList<CarAction>();
    AlertDialog dialog_AddPath;
    CommonUtil commonUtil;
    BluetoothService.BluetoothBinder mBindr;

    Thread threadRun;
    TextView txtAction0,txtAction1,txtAction2,txtAction3,txtAction4,txtAction5;
    TextView txtCmd0,txtCmd1;
    HorizontalScrollView horizontalScrollView;
    Menu mMenu;

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
    private boolean isCarRun = false;

    protected String appId = "14724199";

    protected String appKey = "Groae7YyDei6y1gyzl9CZFZw";

    protected String secretKey = "169DxAGTCyhO06kNtMp152PwPKkGiBFo";

    private TtsMode ttsMode = TtsMode.ONLINE;

    protected SpeechSynthesizer mSpeechSynthesizer;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rulepathactivity_menu,menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){

            case R.id.menu_run:
                if(mBindr == null ||mBindr.getIsBtConnected() == false){
                    Toast.makeText(this,"设备未连接",Toast.LENGTH_LONG).show();
                    return false;
                }
                if(isCarRun){
                    isCarRun=false;
                    item.setIcon(R.mipmap.ic_play_arrow_white_48dp);
                    if(threadRun!=null){
                        threadRun.interrupt();
                        threadRun=null;
                    }
                    mBindr.send(StringUtils.buildMessage("0",Vector2.STOP.getX(),Vector2.STOP.getY()));
                }else{
                    isCarRun=true;
                    item.setIcon(R.mipmap.ic_stop_white_48dp);
                    if(threadRun == null){
                        threadRun = new Thread(runnable);
                    }
                    threadRun.start();
                }

                break;

            case R.id.menu_switch:
                intent = new Intent(this,Button.class);
                startActivity(intent);
                this.finish();
                break;

            case R.id.menu_clear:
                initCar();
                CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
                listView.setAdapter(CarActionListViewAdapter);
                break;

            case R.id.menu_save:
                try {
                    // 转换为json文本
                    //JSONSerializer jsonSerializer=new JSONSerializer();
                    //jsonSerializer.config(SerializerFeature.WriteEnumUsingToString,false);
                    //String txt=JStoJSONArrayON.toJSONString(listCarAction);
                    String txt=toJSONArray(listCarAction).toJSONString();
                    // 保存

                    String path = Environment.getExternalStorageDirectory() +"/BlueDot/Save/";
                    if(commonUtil.makeDirs(path)){
                        path+=commonUtil.currentTime("yyyy-MM-dd-HH-mm-ss")+".txt";
                        data2file(txt.getBytes("UTF8"),path);
                        Toast.makeText(RulePathActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RulePathActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(RulePathActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_saveList:
                intent = new Intent(RulePathActivity.this, SaveListActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_home:
                intent = new Intent(RulePathActivity.this,Devices.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_path);
        initPermission();
        initTTs();
        mWindowManager=(WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Intent service = new Intent(RulePathActivity.this,BluetoothService.class);
        RulePathActivity.this.bindService(service,conn,Context.MODE_PRIVATE);
        commonUtil = CommonUtil.getSingleton(this);

        listView=(ListView) this.findViewById(R.id.listView);
        CarActionListViewAdapter=new CarActionListViewAdapter(this, listCarAction);
        listView.setAdapter(CarActionListViewAdapter);

        txtAction0=(TextView) this.findViewById(R.id.txtAction0);
        txtAction1=(TextView) this.findViewById(R.id.txtAction1);
        txtAction2=(TextView) this.findViewById(R.id.txtAction2);
        txtAction3=(TextView) this.findViewById(R.id.txtAction3);
        txtAction4=(TextView) this.findViewById(R.id.txtAction4);
        txtAction5=(TextView) this.findViewById(R.id.txtAction5);

        txtCmd0=(TextView) this.findViewById(R.id.txtCmd0);
        txtCmd1=(TextView) this.findViewById(R.id.txtCmd1);

        txtAction0.setOnTouchListener(this);
        txtAction1.setOnTouchListener(this);
        txtAction2.setOnTouchListener(this);
        txtAction3.setOnTouchListener(this);
        txtAction4.setOnTouchListener(this);
        txtAction5.setOnTouchListener(this);
        txtCmd0.setOnTouchListener(this);
        txtCmd1.setOnTouchListener(this);

        horizontalScrollView=(HorizontalScrollView) this.findViewById(R.id.layoutBottom);

        threadRun=new Thread(runnable);
        initCar();
        speak("初始化结束");
    }

    @Override
    protected void onRestart() {
        if(CarActionListViewAdapter != null && listView != null){
            CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
            listView.setAdapter(CarActionListViewAdapter);
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initTTs() {

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);


        // 3. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "7");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);
        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");

    }

    private void speak(String txt) {

        if (mSpeechSynthesizer == null) {
            return;
        }
        int result = mSpeechSynthesizer.speak(txt);
        checkResult(result, "speak");
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {

            runCar(listCarAction);

            isCarRun=false;
            speak("动作结束");
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);

        }
    };

    private void actionDelay(double time){
        try{
            long ms = new Double(time * 1000).longValue();
            Thread.sleep(ms);
            mBindr.send(StringUtils.buildMessage("0",Vector2.STOP.getX(),Vector2.STOP.getY()));
        }catch (InterruptedException e){
            Log.d(Tag,"ActionErr");
        }
    }

    /**
     * 车辆运行调整算法,递归
     * @param _listCarAction
     */
    private void runCar(List<CarAction> _listCarAction){
        int indexOfAction=0;
        double timeOfAction; // ms,动作总时间
        int degreeOfAction; // 转向角度

        while(isCarRun){
            try {
                CarAction carAction=_listCarAction.get(indexOfAction);
                int cmd=carAction.getCmdType().getCmd();
                if(cmd==CmdType.SEQUEN.getCmd()){
                    switch (carAction.getActionType().getAction()){
                        case 0: // 前进
                            speak(carAction.getActionType().toString()+carAction.getLen()+"秒");
                            timeOfAction=carAction.getLen();
                            if(timeOfAction > 0)
                            {
                                mBindr.send(StringUtils.buildMessage("1",Vector2.UP.getX(),Vector2.UP.getY()));
                                mBindr.send(StringUtils.buildMessage("2",Vector2.UP.getX(),Vector2.UP.getY()));
                                actionDelay(timeOfAction);
                            }
                            break;
                        case 1: // 后退
                            speak(carAction.getActionType().toString()+carAction.getLen()+"秒");
                            timeOfAction=carAction.getLen();
                            if(timeOfAction > 0)
                            {
                                mBindr.send(StringUtils.buildMessage("1",Vector2.DOWN.getX(),Vector2.DOWN.getY()));
                                mBindr.send(StringUtils.buildMessage("2",Vector2.DOWN.getX(),Vector2.DOWN.getY()));
                                actionDelay(timeOfAction);
                            }
                            break;
                        case 2: // 左转
                            speak(carAction.getActionType().toString()+carAction.getDegree()+"秒");
                            timeOfAction=carAction.getDegree();
                            if(timeOfAction > 0)
                            {
                                mBindr.send(StringUtils.buildMessage("1",Vector2.LEFT.getX(),Vector2.LEFT.getY()));
                                mBindr.send(StringUtils.buildMessage("2",Vector2.LEFT.getX(),Vector2.LEFT.getY()));
                                actionDelay(timeOfAction);
                            }
                            break;
                        case 3: // 右转
                            speak(carAction.getActionType().toString()+carAction.getDegree()+"秒");
                            timeOfAction=carAction.getDegree();
                            if(timeOfAction > 0)
                            {
                                mBindr.send(StringUtils.buildMessage("1",Vector2.RIGHT.getX(),Vector2.RIGHT.getY()));
                                mBindr.send(StringUtils.buildMessage("2",Vector2.RIGHT.getX(),Vector2.RIGHT.getY()));
                                actionDelay(timeOfAction);
                            }
                            break;
                        case 4: // 停止
                            speak(carAction.getActionType().toString());
                            mBindr.send(StringUtils.buildMessage("1",Vector2.STOP.getX(),Vector2.STOP.getY()));
                            mBindr.send(StringUtils.buildMessage("2",Vector2.STOP.getX(),Vector2.STOP.getY()));
                            mBindr.send(StringUtils.buildMessage("0",Vector2.STOP.getX(),Vector2.STOP.getY()));
                            break;
                        case 5: // 拍照

                            break;
                        default:
                            break;
                    }
                }else if(cmd==CmdType.LOOP.getCmd()){
                    for(int i=0;i<carAction.getNumOfLoop();i++)
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

    /**
     *
     * @param _listView 父控件视图
     * @param list 父控件动作集合
     * @param carAction 添加动作
     * @param x 滑动位置x
     * @param y 滑动位置y
     */
    private void addActionView(ListView _listView,List<CarAction> list,CarAction carAction,int x,int y){

        int insertPosition = _listView.pointToPosition(x,y);
        CarActionListViewAdapter adapter = (CarActionListViewAdapter) _listView.getAdapter();
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
//                        v.getAdapter().getCount() * ;
                        addActionView(v,list.get(insertPosition).getConditionCarAction(),carAction,x,y-(int)view.getY() - 50 - (v.getAdapter().getCount() - 1) * 52);
                        return;
                    }
                }
            }
            list.add(insertPosition,carAction);
        }
    }

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
                            carAction.setActionType(ActionType.TURN_LEFT);
                            break;
                        case R.id.txtAction3:
                            carAction.setActionType(ActionType.TURN_RIGHT);
                            break;
                        case R.id.txtAction4:
                            carAction.setActionType(ActionType.STOP);
                            break;
                        case R.id.txtAction5:
                            carAction.setActionType(ActionType.CAMERA);
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

                    addActionView(listView,listCarAction,carAction,x,y);
                    if(CarActionListViewAdapter == null){
                        CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
                    }
                    CarActionListViewAdapter.setUiRefreshListener(this);
                    listView.setAdapter(CarActionListViewAdapter);
                    CarActionListViewAdapter.notifyDataSetChanged();
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

    /**
     * 将二进制数据转换为文件的函数
     * @param w
     * @param fileName
     * @throws Exception
     */
    private void data2file(byte[] w, String fileName) throws Exception {
        FileOutputStream out =null;
        try {
            out =new FileOutputStream(fileName);
            out.write(w);
            out.close();
        } catch (Exception e) {
            if (out !=null)
                out.close();
            throw e;
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

    /**
     * 创建拖动的镜像
     * @param bitmap
     * @param downX
     * 			按下的点相对父控件的X坐标
     * @param downY
     * 			按下的点相对父控件的X坐标
     */
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

    /**
     * 从界面上面移动拖动镜像
     */
    private void removeDragImage(){
        if(mDragImageView != null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
    }

    /**
     * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
     * @param moveX
     * @param moveY
     */
    private void onDragItem(int moveX, int moveY){
        mWindowLayoutParams.x = moveX - mPoint2ItemLeft;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置

        //GridView自动滚动
        //mHandler.post(mScrollRunnable);
    }

    /**
     * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
     */
    private void onStopDrag(){
        removeDragImage();
    }

    Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MenuItem item = mMenu.findItem(R.id.menu_run);
                    item.setIcon(R.mipmap.ic_play_arrow_white_48dp);
                    if(threadRun!=null){
                        threadRun.interrupt();
                        threadRun=null;
                    }
                default:
                    break;
            }
        }

    };

    /**
     * 小车初始状态
     */
    private void initCar(){
        if(listCarAction==null)listCarAction=new ArrayList<CarAction>();
        listCarAction.clear();
        CarAction carAction=new CarAction();
        carAction.setActionType(ActionType.STOP); // 停止状态
        carAction.setLen(0);
        carAction.setDegree(0);
        carAction.setX(20);
        carAction.setY(20);
        carAction.setCarDegree(0);
        carAction.setCmdType(CmdType.SEQUEN);
        listCarAction.add(carAction);
    }

    public void delCarAction(List<CarAction> _listCarAction, int position){
        _listCarAction.remove(position);

        CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
        listView.setAdapter(CarActionListViewAdapter);
    }

    /**
     * 在指定集合中向下添加一个指令对象
     * @param isAdd true=添加,false=修改
     * @param _listCarAction 指定的指令对象集合
     * @param position
     */

    public void showDialogAddPath(final boolean isAdd,final List<CarAction> _listCarAction,final int position) {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=this.getLayoutInflater().inflate(R.layout.dialog_add_path, null);
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
                spAction.setSelection(carAction.getActionType().getAction());
                if(carAction.getActionType()==ActionType.FORWARD ||carAction.getActionType()==ActionType.BACK){
                    layout_len.setVisibility(View.VISIBLE);
                    layout_degree.setVisibility(View.GONE);
                    txtTrLen.setText(String.valueOf(carAction.getLen()));
                }else if(carAction.getActionType()==ActionType.TURN_LEFT ||carAction.getActionType()==ActionType.TURN_RIGHT){
                    layout_len.setVisibility(View.GONE);
                    layout_degree.setVisibility(View.VISIBLE);
                    txtTrDegree.setText(String.valueOf(carAction.getDegree()));
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
                    double len=0;
                    double degree=0;
                    if(StringUtils.isNotEmpty(txtTrLen.getText().toString())){
                        len=Double.valueOf(txtTrLen.getText().toString());
                    }
                    if(StringUtils.isNotEmpty(txtTrDegree.getText().toString())){
                        degree=Double.valueOf(txtTrDegree.getText().toString());
                    }
                    switch (action) {
                        case 0: // 前进
                            carAction.setActionType(ActionType.FORWARD);
                            carAction.setLen(len);
                            carAction.setDegree(0); // 偏转
                            // 执行完状态
                            break;
                        case 1: // 后退
                            carAction.setActionType(ActionType.BACK);
                            carAction.setLen(len); // 距离
                            carAction.setDegree(0); // 偏转
                            // 执行完状态
                            break;
                        case 2: // 左转
                            carAction.setActionType(ActionType.TURN_LEFT);
                            carAction.setLen(0);
                            carAction.setDegree(degree); //角度,左偏转
                            break;
                        case 3: // 右转
                            carAction.setActionType(ActionType.TURN_RIGHT);
                            carAction.setLen(0);
                            carAction.setDegree(degree); //角度,右偏转
                            // 执行完状态
                            break;
                        case 4: // 停止
                            carAction.setActionType(ActionType.STOP);
                            carAction.setLen(0);
                            carAction.setDegree(0);
                            break;
                        case 5:
                            carAction.setActionType(ActionType.CAMERA);
                            carAction.setLen(0);
                            carAction.setDegree(0);
                            break;
                        default:
                            break;
                    }
                }else if(cmd==CmdType.LOOP.getCmd()){
                    carAction.setCmdType(CmdType.LOOP);
                    int step=0;
                    if(StringUtils.isNotEmpty(txtStep.getText().toString())){
                        step=Integer.valueOf(txtStep.getText().toString());
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
                CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
                listView.setAdapter(CarActionListViewAdapter);
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
                        layout_len.setVisibility(View.GONE);
                        layout_degree.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                    case 5:
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

    private final SerializerFeature[] CONFIG = new SerializerFeature[]{
            SerializerFeature.WriteNullBooleanAsFalse,//boolean为null时输出false
            SerializerFeature.WriteMapNullValue, //输出空置的字段
            SerializerFeature.WriteNonStringKeyAsString,//如果key不为String 则转换为String 比如Map的key为Integer
            SerializerFeature.WriteNullListAsEmpty,//list为null时输出[]
            SerializerFeature.WriteNullNumberAsZero,//number为null时输出0
            SerializerFeature.WriteNullStringAsEmpty//String为null时输出""
    };

    private JSONArray toJSONArray(Object javaObject) {

        SerializeWriter out = new SerializeWriter();
        String jsonStr;
        try {
            JSONSerializer serializer = new JSONSerializer(out);

            for (com.alibaba.fastjson.serializer.SerializerFeature feature : CONFIG) {
                serializer.config(feature, true);
            }
            serializer.config(SerializerFeature.WriteEnumUsingToString, false);
            serializer.write(javaObject);

            jsonStr =  out.toString();
        } finally {
            out.close();
        }
        JSONArray jsonArray = JSON.parseArray(jsonStr);
        return jsonArray;
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBindr = (BluetoothService.BluetoothBinder) iBinder;
            mBindr.setBluetoothListener(RulePathActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onBluetoothConnectStart() {

    }

    @Override
    public void onBluetoothConnectSuccess() {

    }

    @Override
    public void onBluetoothConnectFailed() {

    }

    @Override
    public void onBluetoothDisconnect() {

    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.d(Tag,"error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRefresh() {
        if(CarActionListViewAdapter != null && listView != null){
            CarActionListViewAdapter=new CarActionListViewAdapter(RulePathActivity.this, listCarAction);
            listView.setAdapter(CarActionListViewAdapter);
            listView.setSelection(listView.getCount() - 1);
        }
    }
}

interface UiRefreshListener{
    public void onRefresh();
}