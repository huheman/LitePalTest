package com.example.huhep.litepaltest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewChargeActivity extends BaseActivity {
    @BindView(R.id.newcharge_toolbar)
    CustomToolbar toolbar;

    @BindView(R.id.newcharge_chargeNameCollector)
    CurrentMessageCollector chargeNameCollector;

    @BindView(R.id.newcharge_chargeTypeRadioGroup)
    RadioGroup radioGroup;

    @BindView(R.id.newcharge_priceCollector)
    CurrentMessageCollector priceCollector;

    @BindView(R.id.newcharge_priceUnit)
    TextView priceUnitTextView;

    @BindView(R.id.newcharge_createButton)
    Button createButton;

    @BindView(R.id.newcharge_maxloopcollector)
    CurrentMessageCollector maxLoopCollector;
    private MessageCollector.MessageCollectorListener listener = new MessageCollector.MessageCollectorListener() {
        @Override
        public void onEditing(MessageCollector messageCollector) {

        }

        @Override
        public void afterEditTextInputChanged(MessageCollector messageCollector, String s2) {
            chargeNameCollector.clearPreDateTips();
            List<BillType> billTypeList = LitePal.where("belongTo=?", String.valueOf(roomId)).find(BillType.class);
            String s = chargeNameCollector.getEditText().getText().toString();
            for (BillType billTypeInList : billTypeList) {
                if (billType != null && s.equals(billType.getBillTypeName())) {
                    break;
                }
                if (s.equals(billTypeInList.getBillTypeName())) {
                    createButton.setEnabled(false);
                    chargeNameCollector.setPreDateTips("有同名费用类型了，请输入另一个名称");
                    return;
                }
            }
            if (chargeNameCollector.getEditText().getText().length() == 0 ||
                    priceCollector.getEditText().getText().length() == 0) {
                createButton.setEnabled(false);
            } else {
                createButton.setEnabled(true);
            }
        }

        @Override
        public void onFinishEditing(MessageCollector messageCollector) {

        }

        @Override
        public void onGetFocus(MessageCollector messageCollector) {

        }

        @Override
        public void onLoseFocus(MessageCollector messageCollector) {

        }
    };
    private BillType billType;
    private String billTypeEditText = "";
    private boolean isChargePerDegree = true;
    private String billTypePrice = "";
    private long roomId;
    private String billTypeMaxLoop = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_charge);
        Util.setFullScreen(this);
        ButterKnife.bind(this);

        setupToolbar();
        getDataFromIntent();
        setupCollector();
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.newcharge_chargePreDegreeRadioButton:
                    maxLoopCollector.setVisibility(View.VISIBLE);
                    priceUnitTextView.setText("元/度");
                    break;
                case R.id.newcharge_chargePreMonthRadioButton:
                    maxLoopCollector.setVisibility(View.GONE);
                    priceUnitTextView.setText("元/月");
                    break;
            }
        });
    }

    private void getDataFromIntent() {
        long chargId = getIntent().getLongExtra(ChargeManageActivity.FLAG_CHARGE_ID, -1);
        billType = LitePal.find(BillType.class, chargId);
        roomId = getIntent().getLongExtra(BaseActivity.ROOM_ID, -1);
        if (billType == null) return;
        else {
            long belongTo = billType.getBelongTo();
            if (belongTo != -1) roomId = belongTo;
        }
        if (billType == null) return;
        createButton.setEnabled(true);
        createButton.setText("修改费用类型");
        toolbar.setTitle("修改费用");
        isChargePerDegree = billType.isChargeOnDegree();
        billTypeEditText = billType.getBillTypeName();
        billTypeMaxLoop = String.valueOf(billType.getLoopThreshold());
        if (billTypeMaxLoop.equalsIgnoreCase("0")) billTypeMaxLoop = "";
        if (isChargePerDegree) {
            billTypePrice = String.valueOf(billType.getPriceEachDegree());
            maxLoopCollector.setVisibility(View.VISIBLE);
        } else {
            billTypePrice = String.valueOf(billType.getRentPrice());
            maxLoopCollector.setVisibility(View.GONE);
        }
    }

    private void setupCollector() {
        String hintForName = "费用名不能重复";
        chargeNameCollector.setHint(hintForName);
        chargeNameCollector.setTipsText("费用名");
        chargeNameCollector.setTipsDrawable(android.R.color.transparent);
        chargeNameCollector.getEditText().setText(billTypeEditText);
        chargeNameCollector.setListener(listener);
        chargeNameCollector.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);

        if (isChargePerDegree) {
            radioGroup.check(R.id.newcharge_chargePreDegreeRadioButton);
            priceUnitTextView.setText("元/度");
            maxLoopCollector.setVisibility(View.VISIBLE);
        } else {
            maxLoopCollector.setVisibility(View.GONE);
            radioGroup.check(R.id.newcharge_chargePreMonthRadioButton);
            priceUnitTextView.setText("元/月");
        }

        String hintForPrice = "该费用是公用的";
        if (roomId != -1)
            hintForPrice = "该费用由" + LitePal.find(Room.class, roomId).getRoomNum() + "专用";
        priceCollector.setTipsDrawable(android.R.color.transparent);
        priceCollector.setTipsText("费用");
        priceCollector.getEditText().setText(billTypePrice);
        priceCollector.setListener(listener);
        priceCollector.setHint(hintForPrice);

        maxLoopCollector.setTipsText("最大量程");
        maxLoopCollector.setTipsDrawable(android.R.color.transparent);
        maxLoopCollector.getEditText().setText(billTypeMaxLoop);
        maxLoopCollector.getEditText().setHint("填写最大读数");
        maxLoopCollector.setHint("未知可以先填0");
    }

    private void setupToolbar() {
        toolbar.getToolbar().setNavigationIcon(R.drawable.ic_back);
        toolbar.getToolbar().setNavigationOnClickListener(v -> finish());
        toolbar.setTitle("创建新的费用");
    }

    @OnClick(R.id.newcharge_createButton)
    public void createBillType(View view) {
        if (billType == null || billType.getBelongTo() == -1 && roomId != -1)
            billType = new BillType();
        billType.setBillTypeName(chargeNameCollector.getEditText().getText().toString());
        Double price = Double.valueOf(priceCollector.getEditText().getText().toString());
        if (radioGroup.getCheckedRadioButtonId() == R.id.newcharge_chargePreDegreeRadioButton) {
            String s = maxLoopCollector.getEditText().getText().toString();
            if (!s.isEmpty() && Integer.valueOf(s) != 0) {
                Integer throead = Integer.valueOf(s);
                billType.setLoopThreshold(throead);
                List<BillType> billTypes = LitePal.where("billTypeName=? and id!=?", billType.getBillTypeName(), String.valueOf(billType.getId())).find(BillType.class);
                for (BillType tempbillType : billTypes) {
                    tempbillType.setLoopThreshold(throead);
                    tempbillType.save();
                }
            }
            billType.setChargeOnDegree(true);
            billType.setPriceEachDegree(price);
        } else {
            billType.setChargeOnDegree(false);
            billType.setRentPrice(price);
        }
        billType.setBelongTo(roomId);
        billType.save();
        finish();
    }
}
