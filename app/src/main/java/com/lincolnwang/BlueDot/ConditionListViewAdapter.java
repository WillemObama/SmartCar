package com.lincolnwang.BlueDot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

public class ConditionListViewAdapter extends BaseAdapter {

    private final static String TAG = "ConditionAdapter";
    private List<SingleCondition> listCondition;
    private Context context;

    public ConditionListViewAdapter(Context context,List<SingleCondition> _listCondition){
        this.context = context;
        this.listCondition = _listCondition;
    }

    @Override
    public int getCount()  {
        return (listCondition==null)?0:listCondition.size();
    }

    @Override
    public Object getItem(int i) {
        return listCondition.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = LayoutInflater.from(context).inflate(R.layout.item_condition,null);
        final SingleCondition singleCondition = listCondition.get(i);
        final EditText txtValue = (EditText) view.findViewById(R.id.txtValue);
        Spinner spinnerSensor = (Spinner) view.findViewById(R.id.spinnerSensor);
        Spinner spinnerOperator = (Spinner) view.findViewById(R.id.spinnerOperator);
        txtValue.setText(String.valueOf(singleCondition.getValue()));
        spinnerSensor.setSelection(singleCondition.getSensor().sensor);
        spinnerOperator.setSelection(singleCondition.getOperatorType().operatorType);

        txtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    singleCondition.setValue(Integer.valueOf(txtValue.getText().toString()));
                }
                catch (Exception e)
                {
                    Log.d(TAG,e.toString());
                }

            }
        });
        spinnerSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == Sensor.DISTANCE.getSensor())
                {
                    singleCondition.setSensor(Sensor.DISTANCE);
                }
                if(position == Sensor.LIGHT.getSensor())
                    singleCondition.setSensor(Sensor.LIGHT);
                if(position == Sensor.RECORDER.getSensor())
                    singleCondition.setSensor(Sensor.RECORDER);
                if(position == Sensor.MAGNETIC.getSensor())
                    singleCondition.setSensor(Sensor.MAGNETIC);
                if(position == Sensor.ACCEX.getSensor())
                    singleCondition.setSensor(Sensor.ACCEX);
                if(position == Sensor.ACCEY.getSensor())
                    singleCondition.setSensor(Sensor.ACCEY);
                if(position == Sensor.ACCEZ.getSensor())
                    singleCondition.setSensor(Sensor.ACCEZ);
                if(position == Sensor.ORIENTATION.getSensor())
                    singleCondition.setSensor(Sensor.ORIENTATION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == OperatorType.EQUAL.getoperatorType()){
                    singleCondition.setOperatorType(OperatorType.EQUAL);
                }
                if(position == OperatorType.GREATER.getoperatorType()){
                    singleCondition.setOperatorType(OperatorType.GREATER);
                }
                if(position == OperatorType.LESS.getoperatorType()){
                    singleCondition.setOperatorType(OperatorType.LESS);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(i != 0)
        {
            final Spinner spinner = (Spinner) view.findViewById(R.id.spinnerLogical);
            spinner.setSelection(singleCondition.getLogicalOperator().getOperator());
            spinner.setVisibility(View.VISIBLE);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position == LogicalOperator.AND.getOperator()){
                        singleCondition.setLogicalOperator(LogicalOperator.AND);
                    }
                    if(position == LogicalOperator.OR.getOperator()){
                        singleCondition.setLogicalOperator(LogicalOperator.OR);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        return view;

    }

    public List<SingleCondition> getListCondition() {
        return listCondition;
    }

    public void setListCondition(List<SingleCondition> listCondition) {
        this.listCondition = listCondition;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
