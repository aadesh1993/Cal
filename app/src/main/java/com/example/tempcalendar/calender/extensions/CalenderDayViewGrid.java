package com.example.tempcalendar.calender.extensions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class CalenderDayViewGrid extends GridView {

    public CalenderDayViewGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalenderDayViewGrid(Context context) {
        super(context);
    }

    public CalenderDayViewGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //This method is needed to get wrap_content height for GridView
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
