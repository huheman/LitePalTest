package com.example.huhep.litepaltest;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.crud.LitePalSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends BaseActivity {
    @BindView(R.id.main_customToolbar)
    CustomToolbar customToolbar;

    @BindView(R.id.main_roomSession)
    MessageCollector roomMessage;

    @BindView(R.id.main_lastElectricSession)
    MessageCollector preElectric;

    @BindView(R.id.main_thisElectricSession)
    CurrentMessageCollector currentElectric;

    @BindView(R.id.main_lastWaterSession)
    MessageCollector preWater;

    @BindView(R.id.main_thisWaterSession)
    CurrentMessageCollector currentWater;

    @BindView(R.id.main_nextButton)
    Button nextButton;

    @BindView(R.id.main_preButton)
    Button preButton;

    @BindView(R.id.main_scrollView)
    ScrollView scrollView;

    @BindView(R.id.main_rentSession)
    MessageCollector rentMessage;

    @BindView(R.id.main_memoEditText)
    EditText memoEditText;

    @BindView(R.id.main_messageButtonsLayout)
    ConstraintLayout messageControllingButtons;

    @BindView(R.id.main_roomButtonsLayout)
    ConstraintLayout roomControllingButtons;
    private static final String TAG = "PengPeng";
    private MessageCollector currentCollector;

    protected List<MessageCollector> messageCollectorList=new ArrayList<>();

    protected MessageCollector.MessageCollectorListener listener = new MessageCollector.MessageCollectorListener() {
        @Override
        public void onEditing(MessageCollector v) {

        }

        @Override
        public void afterEditTextInputChanged(MessageCollector v,String s) {
            for (MessageCollector messageCollector : messageCollectorList) {
                if (messageCollector.getEditText().getText().length() == 0) {
                    preButton.setEnabled(false);
                    nextButton.setEnabled(false);
                    return;
                }
            }
            preButton.setEnabled(true);
            nextButton.setEnabled(true);
        }


        @Override
        public void onFinishEditing(MessageCollector v) {
            //如果输入结束，则显示一些tips内容
        }

        @Override
        public void onGetFocus(MessageCollector v) {
            currentCollector = v;
            //如果当前collector是current的collector，则button变成message类型的
            if (v instanceof CurrentMessageCollector) {
                messageControllingButtons.setVisibility(View.VISIBLE);
                roomControllingButtons.setVisibility(View.GONE);
            } else {
                //否则两个都消失
                roomControllingButtons.setVisibility(View.GONE);
                messageControllingButtons.setVisibility(View.GONE);
            }
            //其他MessageCollector且非CurrentMessageCollector的全部变成不可编辑
            for (MessageCollector messageCollector : messageCollectorList) {
                if (messageCollector != v && messageCollector.getEditText().getText().length() != 0 &&
                        !(messageCollector instanceof CurrentMessageCollector)) {
                    messageCollector.setEditTextEnable(false);
                }
            }
        }

        @Override
        public void onLoseFocus(MessageCollector messageCollector) {
            currentCollector = null;
            messageControllingButtons.setVisibility(View.GONE);
            roomControllingButtons.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Util.setFullScreen(this);
        initMemoEditText();
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: ");
            memoEditText.setText(savedInstanceState.getString("key"));
        }

        customToolbar.setTitle("2018年11月1日房租信息");
        rentMessage.setTipsText("房租");
        rentMessage.setTipsDrawable(R.drawable.ic_money);
        rentMessage.setDevider();
        roomMessage.setHint("房间号一般不用修改");
    }

    private void initMemoEditText() {
        memoEditText.setOnTouchListener((v, event) -> {
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            return false;
        });

        memoEditText.setOnClickListener(new View.OnClickListener() {
            private int count = 0;
            private long firClick;
            private long secClick;
            private long threshold = 500;
            @Override
            public void onClick(View v) {
                count++;
                if (count == 1) {
                    firClick = System.currentTimeMillis();
                } else {
                    secClick = System.currentTimeMillis();
                    if (secClick - firClick <= threshold) {
                        count = 0;
                        memoEditText.setText("");
                    } else {
                        count = 1;
                        firClick = secClick;
                    }
                }
            }
        });
        memoEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                v.setFocusableInTouchMode(false);
                messageControllingButtons.setVisibility(View.GONE);
                roomControllingButtons.setVisibility(View.VISIBLE);
            } else {
                messageControllingButtons.setVisibility(View.GONE);
                roomControllingButtons.setVisibility(View.GONE);
            }
        });
        memoEditText.setOnEditorActionListener((v, actionId, event) -> {
            v.setFocusableInTouchMode(false);
            v.clearFocus();
            return false;
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_preButton:
                break;
            case R.id.main_nextButton:
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: ");
        memoEditText.setText(savedInstanceState.getString("key"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putString("key", memoEditText.getText().toString());
    }
}
