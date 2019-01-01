package com.lincolnwang.BlueDot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 11304 on 2018/11/5.
 */

public class CarActionListViewAdapter extends BaseAdapter implements UiRefreshListener {
    private final static String TAG = "CarActionListViewAdapter";
    private List<CarAction> listCarAction;
    private Context context;

    static UiRefreshListener uiRefreshListener;

    public CarActionListViewAdapter(Context _context ,List<CarAction> _listCarAction){
        this.context = _context;
        this.listCarAction=_listCarAction;
    }

    public void setUiRefreshListener(UiRefreshListener uiRefreshListener) {
        this.uiRefreshListener = uiRefreshListener;
    }

    public List<CarAction> getListCarAction() {
        return listCarAction;
    }

    public void setListCarAction(List<CarAction> listCarAction) {
        this.listCarAction = listCarAction;
    }

    @Override
    public int getCount() {
        return (listCarAction==null)?0:listCarAction.size();
    }

    @Override
    public Object getItem(int position) {
        return listCarAction.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final CarAction carAction=listCarAction.get(position);
        int cmd=carAction.getCmdType().getCmd();

        if(cmd==CmdType.SEQUEN.getCmd()){

            convertView= LayoutInflater.from(context).inflate(R.layout.item_action, null);

            TextView txtAction=(TextView) convertView.findViewById(R.id.txtAction);
            TextView txtValue=(TextView) convertView.findViewById(R.id.txtValue);
            LinearLayout layout_action=(LinearLayout) convertView.findViewById(R.id.layout_action);

            txtAction.setTag(position);
            txtAction.setText(carAction.getActionType().toString());

            txtValue.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    ((RulePathActivity)context).showDialogAddPath(false, listCarAction, position);
                }
            });


            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加"};

                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // 删除
                                    ((RulePathActivity)context).delCarAction(listCarAction,position);
                                    break;
                                case 1: // 向下添加
                                    ((RulePathActivity)context).showDialogAddPath(true, listCarAction, position);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            });

            switch (carAction.getActionType().getAction()) {
                case 0:
                    layout_action.setBackgroundResource(R.drawable.bg_action_0);
                    txtValue.setTextColor(Color.parseColor("#8F1C6A"));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getLen()));
                    break;
                case 1:
                    layout_action.setBackgroundResource(R.drawable.bg_action_1);
                    txtValue.setTextColor(Color.parseColor("#C22A03"));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getLen()));
                    break;
                case 2:
                    layout_action.setBackgroundResource(R.drawable.bg_action_2);
                    txtValue.setTextColor(Color.parseColor("#DF7708"));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getDegree()));
                    break;
                case 3:
                    layout_action.setBackgroundResource(R.drawable.bg_action_3);
                    txtValue.setTextColor(Color.parseColor("#73B917"));
                    txtValue.setBackgroundResource(R.drawable.bg_value_0);
                    txtValue.setText(String.valueOf(carAction.getDegree()));
                    break;
                case 4: // stop
                    layout_action.setBackgroundResource(R.drawable.bg_action_stop);
                    txtValue.setText("");
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    break;
                case 5: // camera
                    layout_action.setBackgroundResource(R.drawable.bg_action_camera);
                    txtValue.setText("");
                    txtValue.setBackgroundResource(R.drawable.bg_value_4);
                    break;
                default:
                    break;
            }
        }
        else if(cmd==CmdType.LOOP.getCmd()){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, null);
            final LinearLayout layout_action = (LinearLayout) convertView.findViewById(R.id.layout_action);
            TextView txtAction = (TextView) convertView.findViewById(R.id.txtAction);
            TextView txtValue = (TextView) convertView.findViewById(R.id.txtValue);

            txtAction.setText(carAction.getCmdType().getValue());
            txtAction.setTag(position);
            txtValue.setText(String.valueOf(carAction.getNumOfLoop()));

            txtValue.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((RulePathActivity)context).showDialogAddPath(false, listCarAction, position);
                }
            });

            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加","插入动作"};
                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // 删除
                                    ((RulePathActivity)context).delCarAction(listCarAction,position);
                                    break;
                                case 1: // 向下添加
                                    ((RulePathActivity)context).showDialogAddPath(true, listCarAction, position);
                                    break;
                                case 2:
                                    List<CarAction> list=listCarAction.get(position).getLoopCarAction();
                                    ((RulePathActivity)context).showDialogAddPath(true, list, list.size()-1);
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
            CarActionListViewAdapter CarActionListViewAdapter = (CarActionListViewAdapter) listView.getAdapter();
            if(CarActionListViewAdapter == null ) {
                CarActionListViewAdapter = new CarActionListViewAdapter(context, carAction.getLoopCarAction());
            }
            CarActionListViewAdapter.setUiRefreshListener(this.uiRefreshListener);
            listView.setAdapter(CarActionListViewAdapter);

            setListViewHeight(listView);
        }
        else if(cmd == CmdType.CONDITION.getCmd()){
            convertView=LayoutInflater.from(context).inflate(R.layout.item_if, null);
            android.widget.Button btnAddCondition = (Button) convertView.findViewById(R.id.btnAddCondition);
            final LinearLayout layout_action = (LinearLayout) convertView.findViewById(R.id.layout_action);
            final ListView conditionListView = (ListView) convertView.findViewById(R.id.conditionListView);
            final ListView listView=(ListView) convertView.findViewById(R.id.listView);
            ConditionListViewAdapter adapter = new ConditionListViewAdapter(context,carAction.getCondition());
            conditionListView.setAdapter(adapter);
            setListViewHeight(conditionListView);

            btnAddCondition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SingleCondition singleCondition = new SingleCondition();
                    singleCondition.setOperatorType(OperatorType.EQUAL);
                    singleCondition.setSensor(Sensor.DISTANCE);
                    singleCondition.setLogicalOperator(LogicalOperator.AND);
                    singleCondition.setValue(0);
                    ConditionListViewAdapter adapter = new ConditionListViewAdapter(context,carAction.getCondition());
                    conditionListView.setAdapter(adapter);
                    adapter.getListCondition().add(singleCondition);
                    adapter.notifyDataSetChanged();
//                    setListViewHeight(conditionListView);
//                    setListViewHeight(listView);
                    carAction.setCondition(adapter.getListCondition());
                    if(uiRefreshListener != null)
                        uiRefreshListener.onRefresh();
                }
            });

            layout_action.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("操作");
                    CharSequence[] items = {"删除动作", "向下添加","插入动作"};
                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    ((RulePathActivity)context).delCarAction(listCarAction,position);
                                    break;
                                case 1:
                                    ((RulePathActivity)context).showDialogAddPath(true, listCarAction, position);
                                    break;
                                case 2:
                                    List<CarAction> list=listCarAction.get(position).getLoopCarAction();
                                    ((RulePathActivity)context).showDialogAddPath(true, list, list.size()-1);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            });

            CarActionListViewAdapter CarActionListViewAdapter = (CarActionListViewAdapter)listView.getAdapter();
            if(CarActionListViewAdapter == null)
            {
                CarActionListViewAdapter=new CarActionListViewAdapter(context, carAction.getConditionCarAction());
                listView.setAdapter(CarActionListViewAdapter);
            }
            else
                CarActionListViewAdapter.notifyDataSetChanged();
            setListViewHeight(listView);
        }
        return convertView;
    }

    private void setListViewHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    @Override
    public void onRefresh() {

    }
}
