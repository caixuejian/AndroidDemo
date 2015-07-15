package com.example.dinus.androiddemo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExpandAllGridView extends GridView{
    public ExpandAllGridView(Context context) {
        super(context);
    }

    public ExpandAllGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


}
