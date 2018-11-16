package com.example.huhep.litepaltest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huhep.litepaltest.utils.DoubleClickedListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageCollector extends ConstraintLayout {
    @BindView(R.id.message_editText)
    EditText editText;
    @BindView(R.id.message_preDateTips)
    TextView preDateTips;

    @BindView(R.id.message_tipsText)
    TextView textView;

    @BindView(R.id.message_hint)
    TextView hintTextView;

    @BindView(R.id.message_devide)
    ImageView devider;

    private static final String TAG = "PengPeng";
    private Context mContext;
    private View mView;

    private MessageCollectorListener listener;

    public interface MessageCollectorListener {
        void onEditing(MessageCollector messageCollector);

        void afterEditTextInputChanged(MessageCollector messageCollector,String e);

        void onFinishEditing(MessageCollector messageCollector);

        void onGetFocus(MessageCollector messageCollector);

        void onLoseFocus(MessageCollector messageCollector);
    }

    @SuppressLint("ClickableViewAccessibility")
    public MessageCollector(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.messagecollector, this);
        ButterKnife.bind(this, mView);

        editText.setOnTouchListener(new DoubleClickedListener(new DoubleClickedListener.DoubleClickedCallBack() {
            @Override
            public boolean onClicked() {
                //如果EditText被双击，则设置可以输入
                setEditTextEnable(true);
                if (listener != null) {
                    listener.onEditing(MessageCollector.this);
                }
                return false;
            }

            @Override
            public boolean onDrawableClicked() {
                //右边的小叉叉被点击则判断，如果edittext有内容则清除掉，并消耗这个点击
                if (editText.getText().length() > 0) {
                    editText.setText("");
                    return true;
                }
                return false;
            }
        }));
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (v.getText().length() != 0) {
                setEditTextEnable(false);
            }

            if (listener != null) {
                listener.onFinishEditing(MessageCollector.this);
            }
            return false;
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    editText.setCompoundDrawables(null, null, null, null);
                } else if(hasFocus())
                    setEditTextDrawable(R.drawable.ic_turnoff);

                if (listener != null) {
                    listener.afterEditTextInputChanged(MessageCollector.this,s.toString());
                }
            }
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && editText.getText().length() != 0) {
                //如果有内容则显示一个小叉叉
                setEditTextDrawable(R.drawable.ic_turnoff);
            }else {
                getEditText().setCompoundDrawables(null,null,null,null);

            }
            if (listener != null) {
                if (hasFocus) {
                    listener.onGetFocus(MessageCollector.this);
                } else {
                    listener.onLoseFocus(MessageCollector.this);
                }
            }
        });
    }

    public void clearPreDateTips() {
        preDateTips.setVisibility(GONE);
    }
    public void setPreDateTips(String preMessage) {
        preDateTips.setVisibility(VISIBLE);
        preDateTips.setText(preMessage);
    }

    public void setTipsDrawable(int resource) {
        if (resource == -1) {
            textView.setCompoundDrawables(null, null, null, null);
            return;
        }
        Drawable drawable = getResources().getDrawable(resource);
        int height = (int)getResources().getDimension(R.dimen.iconSize);
        drawable.setBounds(0, 0,height, height);
        textView.setCompoundDrawables(drawable, null, null, null);
    }

    public void setTipsText(String tips) {
        textView.setText(tips);
    }

    protected void setEditTextDrawable(int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        drawable.setBounds(0, 0, drawable.getMinimumWidth() / 2, drawable.getMinimumHeight() / 2);
        editText.setCompoundDrawables(null, null, drawable, null);
        editText.setCompoundDrawablePadding(16);
    }

    public EditText getEditText() {
        return editText;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setListener(MessageCollectorListener listener) {
        this.listener = listener;
    }

    public void setEditTextEnable(boolean enable) {
        if (enable) {
            editText.setFocusableInTouchMode(true);
            editText.setSelection(editText.getText().length());
            if (this instanceof CurrentMessageCollector) return;
            editText.setTextColor(getResources().getColor(R.color.deepDeepDark));
        } else {
            editText.setFocusableInTouchMode(false);
            editText.clearFocus();
            editText.setCompoundDrawables(null, null, null, null);
            if (this instanceof CurrentMessageCollector) return;
            editText.setTextColor(getResources().getColor(R.color.deepDark));
        }

    }

    public void setHint(String string) {
        devider.setVisibility(VISIBLE);
        hintTextView.setVisibility(VISIBLE);
        hintTextView.setText(string);
    }

    public void appendHint(String string){
        devider.setVisibility(VISIBLE);
        hintTextView.setVisibility(VISIBLE);
        hintTextView.append(string);
    }

    public void setDevider() {
        devider.setVisibility(VISIBLE);
    }
}
