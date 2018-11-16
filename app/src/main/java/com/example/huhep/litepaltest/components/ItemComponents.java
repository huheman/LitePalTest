package com.example.huhep.litepaltest.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.huhep.litepaltest.R;

import static com.example.huhep.litepaltest.bean.Bill.BILL_ERROR;
import static com.example.huhep.litepaltest.bean.Bill.BILL_TOO_MUCH;

public class ItemComponents extends ConstraintLayout {

    private final TextView title;
    private final TextView detail;
    public final TextView duration;

    public ItemComponents(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.itemcompound_layout, this);
        title = findViewById(R.id.itemCompund_titletextView);
        detail = findViewById(R.id.itemCompund_detailtextView);
        duration = findViewById(R.id.itemCompund_durationtextView);
    }

    public void setTitle(String string) {
        title.setText(string);
    }

    public void setDetail(String string) {
        detail.setText(string);
    }

    public void setDuration(String string) {
        duration.setText(string);
    }

    public void setDetailColor(int type) {
        switch (type) {
            case BILL_ERROR:
                detail.setTextColor(getResources().getColor(R.color.lightRed));
                break;
            case BILL_TOO_MUCH:
                detail.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                break;
            default:
                detail.setTextColor(getResources().getColor(R.color.deepDeepDark));
                break;
        }
    }
}
