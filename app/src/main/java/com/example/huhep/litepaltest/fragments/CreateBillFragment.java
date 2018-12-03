package com.example.huhep.litepaltest.fragments;


import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.CurrentMessageCollector;
import com.example.huhep.litepaltest.MessageCollector;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateBillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateBillFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Room room;
    LinearLayout view;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences sharedPreferences;
    private SparseIntArray mapForState;
    public static final int STATE_NOTFINISH = 0;
    public static final int STATE_FINISH = 10;
    public static final int STATE_SAMEAS = 20;
    public static final int STATE_OUTOFBOUND = 30;
    public static final int STATE_LESSTHAN = 40;

    public CreateBillFragment() {
        // Required empty public constructor
    }

    private IStateChangeListener stateChangeListener;

    interface IStateChangeListener {
        void changeToState(int state);
    }

    public void setStateChangeListener(IStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public static CreateBillFragment newInstance(Room room) {
        CreateBillFragment fragment = new CreateBillFragment();
        fragment.room = room;
        if (sharedPreferences == null)
            sharedPreferences = BaseActivity.getSP();
        if (editor == null)
            editor = fragment.sharedPreferences.edit();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        NestedScrollView nestedScrollView = new NestedScrollView(getContext());
        view = new LinearLayout(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        view.setBackgroundResource(R.color.backgroundColor);
        view.setOrientation(LinearLayout.VERTICAL);
        nestedScrollView.addView(view);
        nestedScrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        return nestedScrollView;
    }


    @Override
    public void onResume() {
        super.onResume();
        setupTheView();
    }

    public void setupTheView() {
        view.removeAllViews();
        List<BillType> checkedBillTypesOfCurrentRoom = room.getCheckedBillTypeList();
        Util.sort(checkedBillTypesOfCurrentRoom);
        mapForState = new SparseIntArray();
        int index = 0;
        for (BillType billType : checkedBillTypesOfCurrentRoom) {
            if (billType.isChargeOnDegree()) {
                addChargeOnDegreeCollector(billType, index++);
            } else addChargeOnMonthCollector(billType);
        }
        tellTheManageState();
    }

    private void addChargeOnDegreeCollector(BillType billType, int pos) {
        Bill lastBill = Util.getLastBillOf(room, billType);
        Bill lastBillForNow = null;
        LinearLayout layout = getDefaultLinearLayout();
        int icon = R.drawable.ic_other;
        if (billType.getBillTypeName().contains("电"))
            icon = R.drawable.ic_light;
        if (billType.getBillTypeName().contains("水"))
            icon = R.drawable.ic_water;

        MessageCollector oldBillCollector = new MessageCollector(getContext(), null);
        oldBillCollector.setListener(new MessageCollector.MessageCollectorListener() {
            private String msgBefore;

            @Override
            public void onEditing(MessageCollector messageCollector) {

            }

            @Override
            public void afterEditTextInputChanged(MessageCollector messageCollector, String s) {

            }

            @Override
            public void onFinishEditing(MessageCollector messageCollector) {
                if (BillManageFragment.roomsToShow != null && !BillManageFragment.roomsToShow.contains(room.getId()))
                    BillManageFragment.roomsToShow.add(room.getId());
            }

            @Override
            public void onGetFocus(MessageCollector messageCollector) {
                msgBefore = messageCollector.getEditText().getText().toString();
            }

            @Override
            public void onLoseFocus(MessageCollector messageCollector) {
                String s = messageCollector.getEditText().getText().toString();
                if (TextUtils.isEmpty(s) || s.equals(msgBefore)) {
                    messageCollector.getEditText().setText(msgBefore);
                    messageCollector.setEditTextEnable(false);
                    return;
                }
                double sValue = Double.valueOf(s);
                new AlertDialog.Builder(getContext()).setTitle("这会把上次的读数改变，是否确定？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            if (billType.getLoopThreshold() != 0 && (int) sValue > billType.getLoopThreshold()) {
                                BaseActivity.show("输入有误，比最大读数还大！");
                                messageCollector.getEditText().setText(msgBefore);
                                messageCollector.setEditTextEnable(false);
                            }
                            messageCollector.setEditTextEnable(false);
                            editor.putString(Util.getSPName(room, billType) + "_pre", s).apply();
                        }).setNegativeButton("取消", (dialog, which) -> {
                    messageCollector.getEditText().setText(msgBefore);
                    if (lastBill != null)
                        messageCollector.getEditText().setText(String.valueOf(lastBill.getToDegree()));
                    editor.putString(Util.getSPName(room, billType) + "_pre", msgBefore).apply();
                    messageCollector.setEditTextEnable(false);
                }).setCancelable(false).show();
            }
        });
        oldBillCollector.setTipsDrawable(icon);
        oldBillCollector.setTipsText("上次" + billType.getBillTypeName());
        String tips = billType.getBillTypeName() + "还没有收费记录";
        String preDegree = sharedPreferences.getString(Util.getSPName(room, billType) + "_pre", "");
        Charge charge = null;
        if (lastBill != null) {
            tips = "上次抄表日期是:";
            charge = LitePal.find(Charge.class, lastBill.getCharge_Id());
            if (charge != null && charge.getCreateDateToString().equalsIgnoreCase(Util.getWhen())) {
                lastBillForNow = lastBill;
                tips += Util.getWhenAccurately(lastBill.getFromDate());
                if (lastBill.getFromDate() == 0)
                    tips = "首次抄录日期未记录";
            } else {
                tips += Util.getWhenAccurately(lastBill.getToDate());
            }
        }
        if (preDegree.isEmpty()) {
            if (lastBill != null) {
                if (charge != null && charge.getCreateDateToString().equalsIgnoreCase(Util.getWhen())) {
                    preDegree = String.valueOf(lastBill.getFromDegree());
                    if(charge.getChargeType() == Charge.TYPE_MOVE_OUT)
                        preDegree= String.valueOf(lastBill.getToDegree());
                } else {
                    preDegree = String.valueOf(lastBill.getToDegree());
                }
            }
        }
        if (lastBill != null&&charge != null && charge.getChargeType() == Charge.TYPE_MOVE_OUT) {
            preDegree = String.valueOf(lastBill.getToDegree());
        }
        oldBillCollector.setPreDateTips(tips);
        oldBillCollector.getEditText().setHint("请双击这里填" + billType.getBillTypeName() + "表底");
        oldBillCollector.getEditText().setText(preDegree);
        editor.putString(Util.getSPName(room, billType) + "_pre", preDegree).apply();

        CurrentMessageCollector currentMessageCollector = new CurrentMessageCollector(getContext(), null);
        currentMessageCollector.setTipsDrawable(icon);
        currentMessageCollector.setTipsText("本次" + billType.getBillTypeName());
        String thisDegree = sharedPreferences.getString(Util.getSPName(room, billType), "");
        if (lastBillForNow != null) {
            currentMessageCollector.setHint("修改本月数据");
            if (thisDegree.isEmpty())
                thisDegree = String.valueOf(lastBillForNow.getToDegree());
        }
        if (charge != null && charge.getChargeType() == Charge.TYPE_MOVE_OUT)
            thisDegree = "";

        currentMessageCollector.getEditText().setText(thisDegree);
        editor.putString(Util.getSPName(room, billType), String.valueOf(thisDegree)).apply();
        currentMessageCollector.getEditText().setHint("输入" + room.getRoomNum() + "房" + billType.getBillTypeName() + "读数");
        currentMessageCollector.requestFocus();
        currentMessageCollector.setListener(new MessageCollector.MessageCollectorListener() {
            @Override
            public void onEditing(MessageCollector messageCollector) {
            }

            @Override
            public void afterEditTextInputChanged(MessageCollector messageCollector, String s) {
            }

            @Override
            public void onFinishEditing(MessageCollector messageCollector) {
                if (!BillManageFragment.getRoomsToShow().contains(room.getId()))
                    BillManageFragment.getRoomsToShow().add(room.getId());
            }

            @Override
            public void onGetFocus(MessageCollector messageCollector) {
            }

            @Override
            public void onLoseFocus(MessageCollector messageCollector) {
                String s = currentMessageCollector.getEditText().getText().toString();
                mapForState.put(pos, STATE_NOTFINISH);
                if (s.isEmpty()) {
                    tellTheManageState();
                    return;
                }
                currentMessageCollector.setHint("");
                String oldS = oldBillCollector.getEditText().getText().toString();
                if (oldS.isEmpty()) {
                    mapForState.put(pos, STATE_LESSTHAN);
                    currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(R.color.lightRed));
                    currentMessageCollector.setHint("数据不足，无法计算本月费用");
                } else {
                    double later = Double.valueOf(s);
                    double before = Double.valueOf(oldS);
                    if (mayBeNewLoop(later, before, billType)) {
                        if (billType.getLoopThreshold() == 0) {
                            EditText editText = new EditText(getContext());
                            editText.setMaxLines(1);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                            layoutParams.setMargins(64, 0, 64, 0);
                            editText.setLayoutParams(layoutParams);
                            LinearLayout linearLayout = new LinearLayout(getContext());
                            linearLayout.addView(editText);
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(billType.getBillTypeName() + "是否已经转了一圈？")
                                    .setNegativeButton("不是，重新输入", (dialog, which) -> {
                                        mapForState.put(pos, STATE_NOTFINISH);
                                        tellTheManageState();
                                        currentMessageCollector.getEditText().setText("");
                                        currentMessageCollector.setHint("");
                                        editor.remove(Util.getSPName(room, billType)).apply();
                                    })
                                    .setPositiveButton("是的", (dialog, which) -> {
                                        currentMessageCollector.setHint(billType.getBillTypeName() + "已经转了一圈。\n");
                                        String hintMessage = getHintMessage(billType, later, before);
                                        currentMessageCollector.appendHint(hintMessage);
                                        billType.setLoopThreshold(Integer.valueOf(editText.getText().toString()));
                                        List<BillType> billTypeList = LitePal.where("billTypeName=? and id!=?", billType.getBillTypeName(), String.valueOf(billType.getId())).find(BillType.class);
                                        for (BillType moreBillType : billTypeList) {
                                            moreBillType.setLoopThreshold(Integer.valueOf(editText.getText().toString()));
                                            moreBillType.save();
                                        }
                                        billType.save();
                                    }).setCancelable(false);
                            builder.setMessage("如果是，请输入读数的最大值，然后按确定")
                                    .setView(linearLayout);
                            builder.show();
                        } else {
                            currentMessageCollector.setHint(billType.getBillTypeName() + "已经转了一圈!请注意确认\n");
                        }
                    } else if (later < before) {
                        mapForState.put(pos, STATE_LESSTHAN);
                        currentMessageCollector.setHint("本次读数不能小于上月读数");
                        currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(R.color.lightRed));
                    } else if (later == before) {
                        mapForState.put(pos, STATE_SAMEAS);
                        currentMessageCollector.setHint("本次读数跟上次读数一样，请注意确认");
                        currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                    } else if (tooMuch(lastBill, later, before)) {
                        mapForState.put(pos, STATE_OUTOFBOUND);
                        currentMessageCollector.setHint("本次读数" + (later - before) + " 度跟上次：" + lastBill.howMuchDegree() + " 度差太多，请注意确认\n");
                        currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_light));
                    } else if (billType.getLoopThreshold() != 0 && (int) later > billType.getLoopThreshold()) {
                        mapForState.put(pos, STATE_LESSTHAN);
                        currentMessageCollector.setHint("输入有误，比最大读数还大\n");
                        currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(R.color.lightRed));
                    }
                    if (mapForState.get(pos) == STATE_NOTFINISH) {
                        mapForState.put(pos, STATE_FINISH);
                        String hintMessage = getHintMessage(billType, later, before);
                        currentMessageCollector.appendHint(hintMessage);
                        currentMessageCollector.getEditText().setTextColor(getContext().getResources().getColor(R.color.deepDeepDark));
                    }
                }
                tellTheManageState();
                editor.putString(Util.getSPName(room, billType), s).apply();
            }
        });
        currentMessageCollector.getEditText().setOnLongClickListener(v -> {
            String oldMsg = oldBillCollector.getEditText().getText().toString();
            if (oldMsg.isEmpty()) return true;
            currentMessageCollector.getEditText().setText(oldMsg);
            currentMessageCollector.clearFocus();
            return true;
        });
        layout.addView(oldBillCollector, -1, -2);
        layout.addView(currentMessageCollector, -1, -2);
        view.addView(layout);
    }

    private int getStateOfFragment() {
        int state = STATE_NOTFINISH;
        int count = 0;
        for (int i = 0; i < mapForState.size(); i++) {
            if (mapForState.valueAt(i) > state)
                state = mapForState.valueAt(i);
            if (mapForState.valueAt(i) == STATE_FINISH)
                count++;
        }
        if (state > STATE_FINISH) return state;
        if (mapForState.size() == 0 || count == mapForState.size())
            state = STATE_FINISH;
        else state = STATE_NOTFINISH;
        return state;
    }

    private void tellTheManageState() {
        int state = getStateOfFragment();
        if (stateChangeListener != null)
            stateChangeListener.changeToState(state);
    }

    public static boolean tooMuch(Bill lastBill, double later, double before) {
        if (lastBill == null)
            return false;
        double gap = later - before;
        if (gap > lastBill.howMuchDegree() * 1.5 || gap < lastBill.howMuchDegree() * 0.5)
            return true;
        return false;
    }

    private String getHintMessage(BillType billType, double later, double before) {
        double degree = later - before;
        if (degree < 0) degree = billType.getLoopThreshold() - before + later;
        double price = degree * billType.getPriceEachDegree();
        String priceFormat = String.format("%.2f", price);
        String str = billType.getBillTypeName() + "从上次读数到现在走了" + degree + "度，\n共计" + priceFormat + "元";
        return str;
    }

    private boolean mayBeNewLoop(double later, double before, BillType billType) {
        int loopThreshold = billType.getLoopThreshold();
        if (loopThreshold == 0 && later < before && later < 100) {
            return true;
        }
        if (later < before && later < 0.2 * loopThreshold && before > 0.8 * loopThreshold) {
            return true;
        }
        return false;
    }


    private LinearLayout getDefaultLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 32, 0, 0);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        return linearLayout;
    }

    private void addChargeOnMonthCollector(BillType billType) {
        MessageCollector messageCollector = new MessageCollector(getContext(), null);
        messageCollector.setTipsText(billType.getBillTypeName());
        String rentPrice = String.valueOf(billType.getRentPrice());
        messageCollector.getEditText().setText(rentPrice);
        messageCollector.setListener(new MessageCollector.MessageCollectorListener() {
            @Override
            public void onEditing(MessageCollector messageCollector) {
            }

            @Override
            public void afterEditTextInputChanged(MessageCollector messageCollector, String s) {
            }

            @Override
            public void onFinishEditing(MessageCollector messageCollector) {
                if (!BillManageFragment.getRoomsToShow().contains(room.getId()))
                    BillManageFragment.getRoomsToShow().add(room.getId());
            }

            @Override
            public void onGetFocus(MessageCollector messageCollector) {
            }

            @Override
            public void onLoseFocus(MessageCollector messageCollector) {
                String newMsg = messageCollector.getEditText().getText().toString();
                String savedMsg = String.valueOf(billType.getRentPrice());
                if ("".equals(newMsg)) {
                    messageCollector.getEditText().setText(savedMsg);
                    messageCollector.setEditTextEnable(false);
                    return;
                }
                if (Double.valueOf(newMsg).equals(Double.valueOf(savedMsg))) return;
                if (billType.getBelongTo() == -1) {
                    BillType privateBillType = new BillType(billType);
                    privateBillType.setRentPrice(Double.valueOf(newMsg));
                    privateBillType.setBelongTo(room.getId());
                    privateBillType.save();
                } else {
                    billType.setRentPrice(Double.valueOf(newMsg));
                    billType.save();
                }
                setupTheView();
            }
        });
        messageCollector.setDevider();
        if (!billType.getBillTypeName().contains("租金"))
            messageCollector.setTipsDrawable(R.drawable.ic_other);
        view.addView(messageCollector);
    }
}
