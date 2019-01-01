package com.lincolnwang.BlueDot;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.*;

import java.util.List;

/**
 * Created by 11304 on 2018/12/30.
 */

public class MonitorListViewAdapter extends BaseAdapter implements View.OnClickListener {

    private List<SensorRecord> recordList;
    private Context mContext;
    private static View currentView,lastView;
    Animation enlargeAnimation,narrowAnimation;
    ListViewClickListener listViewClickListener;
    private View selectView;

    public MonitorListViewAdapter(Context mContext,List<SensorRecord> recordList){
        this.mContext = mContext;
        this.recordList = recordList;
        listViewClickListener = (MonitorActivity) mContext;
        enlargeAnimation = AnimationUtils.loadAnimation(mContext,R.anim.enlargeanimation);
        narrowAnimation = AnimationUtils.loadAnimation(mContext,R.anim.narrowanimation);
        enlargeAnimation.setDuration(300);
        narrowAnimation.setDuration(300);
        //enlargeAnimation.setFillAfter(true);
        //narrowAnimation.setFillAfter(true);
        recordList.get(0).setSelect(true);
//        enlargeAnimation.setAnimationListener(this);
//        narrowAnimation.setAnimationListener(this);

    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = View.inflate(mContext,R.layout.item_monitor,null);

        SensorRecord record = recordList.get(i);
        ImageView monitorImg = (ImageView) view.findViewById(R.id.monitorImg);
        TextView monitorTxt = (TextView) view.findViewById(R.id.monitorTxt);
        monitorImg.setTag(i);
        if(lastView == null && i == 0){
            lastView = monitorImg;
        }
        monitorImg.setOnClickListener(this);

        if(monitorImg != null){
            monitorTxt.setText(record.getRecordName());
            ViewGroup.LayoutParams layoutParams = monitorImg.getLayoutParams();
            if(record.isSelect()){
                layoutParams.width = (int)(CommonUtil.getSingleton(mContext).dp2px(110));
            }
            else {
                layoutParams.width = (int)(CommonUtil.getSingleton(mContext).dp2px(80));
            }
            monitorImg.setLayoutParams(layoutParams);
            monitorImg.setBackgroundColor(record.getTheme());
        }
        return view;
    }


    public static int lastPosition = 0,currentPosition = 0;
    public static boolean isLastShowed = false;
    @Override
    public void onClick(View view) {

        if(lastView != view){
            view.startAnimation(enlargeAnimation);
            if(lastView != null ){
                if(isLastShowed )
                    lastView.startAnimation(narrowAnimation);

                recordList.get(lastPosition).setSelect(false);
            }
            currentPosition = (int)view.getTag();
            lastPosition = currentPosition;
            listViewClickListener.onItemClick(currentPosition);
            lastView = view;
            recordList.get(currentPosition).setSelect(true);
            MonitorListViewAdapter.this.notifyDataSetChanged();
        }
    }

    public interface ListViewClickListener{
        public void onItemClick(int position);

    }

}


