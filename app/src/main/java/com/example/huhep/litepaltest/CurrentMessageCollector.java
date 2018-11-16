package com.example.huhep.litepaltest;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class CurrentMessageCollector extends MessageCollector {
    public CurrentMessageCollector(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEditTextEnable(true);
        editText.setTextColor(getResources().getColor(R.color.deepDeepDark));
        textView.setBackgroundResource(R.color.textViewBackground);
        setDevider();
    }
}
