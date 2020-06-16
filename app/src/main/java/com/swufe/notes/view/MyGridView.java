
package com.swufe.notes.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
//使用GRidView控件实现网格布局来显示备忘录

public class MyGridView extends GridView{

    public MyGridView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, mExpandSpec);
    }
}
