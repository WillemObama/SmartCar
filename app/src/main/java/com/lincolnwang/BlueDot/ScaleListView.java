package com.lincolnwang.BlueDot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by 11304 on 2018/12/30.
 */

public class ScaleListView extends ListView implements View.OnClickListener {

    public ScaleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        
    }
}
