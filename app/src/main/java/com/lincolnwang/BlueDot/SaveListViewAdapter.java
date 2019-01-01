package com.lincolnwang.BlueDot;

import android.widget.BaseAdapter;

/**
 * Created by 11304 on 2018/11/7.
 */
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SaveListViewAdapter extends BaseAdapter {

    private final static String TAG="SaveListViewAdapter";
    private List<String> listFileName;
    private Context context;

    public SaveListViewAdapter(Context context,List<String> listFileName){
        this.context=context;
        this.listFileName=listFileName;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return (listFileName==null)?0:listFileName.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return listFileName.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if(convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.item_save, null);
        }
        TextView txtFileName=(TextView) convertView.findViewById(R.id.txtFileName);
        txtFileName.setText(listFileName.get(position));

        return convertView;
    }

}
