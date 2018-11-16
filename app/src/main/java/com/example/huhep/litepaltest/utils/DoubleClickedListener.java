package com.example.huhep.litepaltest.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.huhep.litepaltest.CurrentMessageCollector;

public class DoubleClickedListener implements View.OnTouchListener {
    private int count = 0;
    private DoubleClickedCallBack mCallBack;
    private long firClick;
    private long secClick;
    private long thread = 400;
    private static final String TAG = "PengPeng";

    public interface DoubleClickedCallBack{
        public boolean onClicked();
        public boolean onDrawableClicked();
    }

    public DoubleClickedListener(DoubleClickedCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCallBack == null) {
            return false;
        }
        if (v instanceof EditText) {
            EditText editText = (EditText) v;
            Drawable drawableRight = editText.getCompoundDrawables()[2];
            if (drawableRight != null && event.getRawX() >= (v.getRight() - drawableRight.getBounds().width() - v.getPaddingEnd())) {
                return mCallBack.onDrawableClicked();
            }
        }
        if (v.getParent().getParent() instanceof CurrentMessageCollector) {
            //如果是current则可以直接编辑
            v.setFocusableInTouchMode(true);
            return false;
        }
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            count++;
            if (count == 1) {
                firClick = System.currentTimeMillis();
            } else if (count == 2) {
                secClick = System.currentTimeMillis();
                if (secClick - firClick <= thread) {
                    count = 0;
                    return mCallBack.onClicked();
                } else {
                    firClick = secClick;
                    count = 1;
                }
            }
        }
        return false;
    }
}
