package com.lincolnwang.BlueDot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 11304 on 2018/12/2.
 */

public class MotorActionListViewAdapter  extends BaseAdapter{

    private final static String TAG = "MotorActionListViewAdapter";
    private List<CarAction> motorActionList;
    private Context mContext;

    static UiRefreshListener uiRefreshListener;

    public MotorActionListViewAdapter(Context context, List<CarAction> motorActionList){
        this.motorActionList = motorActionList;
        this.mContext = context;
    }

    public void setUiRefreshListener(UiRefreshListener uiRefreshListener) {
        this.uiRefreshListener = uiRefreshListener;
    }

    @Override
    public int getCount() {
        return motorActionList == null ? 0 : motorActionList.size();
    }

    @Override
    public Object getItem(int position) {
        return motorActionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final CarAction carAction = motorActionList.get(position);
        int cmd=carAction.getCmdType().getCmd();

        if(cmd==CmdType.SEQUEN.getCmd()){

            convertView= LayoutInflater.from(mContext).inflate(R.layout.item_motor, null);

            TextView txtAction=(TextView) convertView.findViewById(R.id.txtAction);
            TextView txtValue=(TextView) convertView.findViewById(R.id.txtValue);
            TextView txtUnit = (TextView) convertView.findViewById(R.id.txtUnit);
            LinearLayout layout_action=(LinearLayout) convertView.findViewById(R.id.layout_action);

            txtAction.setTag(position);
            txtAction.setText(carAction.getActionType().toString());

            txtValue.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    ((MotorActivity)mContext).showDialogAddPath(false, motorActionList, position);
                }
            });


            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加"};

                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // 删除
                                    ((MotorActivity)mContext).delCarAction(motorActionList,position);
                                    break;
                                case 1: // 向下添加
                                    ((MotorActivity)mContext).showDialogAddPath(true, motorActionList, position);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            });

            Resources res = mContext.getResources();
            switch (carAction.getActionType().getAction()) {
                case 0:
                    layout_action.setBackgroundResource(R.drawable.bg_action_0);
                    txtValue.setTextColor(res.getColor(R.color.motorAction0));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getPulse()));
                    break;
                case 1:
                    layout_action.setBackgroundResource(R.drawable.bg_action_1);
                    txtValue.setTextColor(res.getColor(R.color.motorAction1));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getPulse()));
                    break;
                case 6:
                    layout_action.setBackgroundResource(R.drawable.bg_action_2);
                    txtValue.setTextColor(res.getColor(R.color.motorAction2));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 7:
                    layout_action.setBackgroundResource(R.drawable.bg_action_3);
                    txtValue.setTextColor(res.getColor(R.color.motorAction3));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 8:
                    layout_action.setBackgroundResource(R.drawable.bg_action_6);
                    txtValue.setTextColor(res.getColor(R.color.motorAction4));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 9:
                    layout_action.setBackgroundResource(R.drawable.bg_action_7);
                    txtValue.setTextColor(res.getColor(R.color.motorAction5));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 10:
                    layout_action.setBackgroundResource(R.drawable.bg_action_8);
                    txtValue.setTextColor(res.getColor(R.color.motorAction6));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 11:
                    layout_action.setBackgroundResource(R.drawable.bg_action_9);
                    txtValue.setTextColor(res.getColor(R.color.motorAction7));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                case 12:
                    layout_action.setBackgroundResource(R.drawable.bg_action_10);
                    txtValue.setTextColor(res.getColor(R.color.motorAction8));
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    txtValue.setText("");
                    txtUnit.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
        else if(cmd==CmdType.LOOP.getCmd()){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list, null);
            final LinearLayout layout_action = (LinearLayout) convertView.findViewById(R.id.layout_action);
            TextView txtAction = (TextView) convertView.findViewById(R.id.txtAction);
            TextView txtValue = (TextView) convertView.findViewById(R.id.txtValue);

            txtAction.setText(carAction.getCmdType().getValue());
            txtAction.setTag(position);
            txtValue.setText(String.valueOf(carAction.getNumOfLoop()));

            txtValue.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((MotorActivity)mContext).showDialogAddPath(false, motorActionList, position);
                }
            });

            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加","插入动作"};
                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // 删除
                                    ((MotorActivity)mContext).delCarAction(motorActionList,position);
                                    break;
                                case 1: // 向下添加
                                    ((MotorActivity)mContext).showDialogAddPath(true, motorActionList, position);
                                    break;
                                case 2:
                                    List<CarAction> list=motorActionList.get(position).getLoopCarAction();
                                    ((MotorActivity)mContext).showDialogAddPath(true, list, list.size()-1);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            });
            ListView listView=(ListView) convertView.findViewById(R.id.listView);
            MotorActionListViewAdapter motorActionListViewAdapter = (MotorActionListViewAdapter) listView.getAdapter();
            if(motorActionListViewAdapter == null ) {
                motorActionListViewAdapter = new MotorActionListViewAdapter(mContext, carAction.getLoopCarAction());
            }
            listView.setAdapter(motorActionListViewAdapter);

            CommonUtil.setListViewHeight(listView);
        }
        else if(cmd == CmdType.CONDITION.getCmd()){
            convertView=LayoutInflater.from(mContext).inflate(R.layout.item_if, null);
            Button btnAddCondition = (Button) convertView.findViewById(R.id.btnAddCondition);
            final LinearLayout layout_action = (LinearLayout) convertView.findViewById(R.id.layout_action);
            final ListView conditionListView = (ListView) convertView.findViewById(R.id.conditionListView);
            final ListView listView=(ListView) convertView.findViewById(R.id.listView);
            ConditionListViewAdapter adapter = new ConditionListViewAdapter(mContext,carAction.getCondition());
            conditionListView.setAdapter(adapter);
            CommonUtil.setListViewHeight(listView);

            btnAddCondition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SingleCondition singleCondition = new SingleCondition();
                    singleCondition.setOperatorType(OperatorType.EQUAL);
                    singleCondition.setSensor(Sensor.DISTANCE);
                    singleCondition.setLogicalOperator(LogicalOperator.AND);
                    singleCondition.setValue(0);

                    ConditionListViewAdapter adapter = new ConditionListViewAdapter(mContext,carAction.getCondition());
                    conditionListView.setAdapter(adapter);
                    adapter.getListCondition().add(singleCondition);
                    adapter.notifyDataSetChanged();

                    carAction.setCondition(adapter.getListCondition());
                    if(uiRefreshListener != null)
                        uiRefreshListener.onRefresh();
                }
            });

            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加","插入动作"};
                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    ((MotorActivity)mContext).delCarAction(motorActionList,position);
                                    break;
                                case 1:
                                    ((MotorActivity)mContext).showDialogAddPath(true, motorActionList, position);
                                    break;
                                case 2:
                                    List<CarAction> list=motorActionList.get(position).getLoopCarAction();
                                    ((MotorActivity)mContext).showDialogAddPath(true, list, list.size()-1);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            });

            MotorActionListViewAdapter motorActionListViewAdapter = (MotorActionListViewAdapter)listView.getAdapter();
            if(motorActionListViewAdapter == null)
            {
                motorActionListViewAdapter=new MotorActionListViewAdapter(mContext, carAction.getConditionCarAction());
                listView.setAdapter(motorActionListViewAdapter);
            }
            else
                motorActionListViewAdapter.notifyDataSetChanged();
            CommonUtil.setListViewHeight(listView);
        }
        return convertView;
    }
}
