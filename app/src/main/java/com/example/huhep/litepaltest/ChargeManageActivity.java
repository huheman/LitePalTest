package com.example.huhep.litepaltest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChargeManageActivity extends BaseActivity {
    private static final String TAG = "PengPeng";
    @BindView(R.id.chargemanage_toolbar)
    CustomToolbar toolbar;

    @BindView(R.id.chargemanage_recyclerView)
    RecyclerView recyclerView;

    private long roomIdBelongTo;
    public static final String FLAG_CHARGE_ID = "_charge_id";
    private SparseBooleanArray stateStore;

    class MyRecyclerViewAdapter extends RecyclerSwipeAdapter<MyRecyclerViewAdapter.MyViewHolder> {
        List<BillType> billTypeList;
        MyRecyclerViewAdapter(List<BillType> billTypeList) {
            this.billTypeList = billTypeList;
        }

        @Override
        public MyRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ChargeManageActivity.this).inflate(R.layout.chargemanage_listview_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.MyViewHolder viewHolder, int position) {
            BillType billType = billTypeList.get(position);
            viewHolder.editChargeButton.setOnClickListener(v -> {
                Intent intent = new Intent(ChargeManageActivity.this, NewChargeActivity.class);
                intent.putExtra(ChargeManageActivity.FLAG_CHARGE_ID, billType.getId());
                intent.putExtra(BaseActivity.ROOM_ID, roomIdBelongTo);
                startActivityForResult(intent,BaseActivity.REQUEST_FROM_CHARGEMANAG_TO_NEWCHARGE);
            });
            viewHolder.deleteChargeButton.setOnClickListener(v -> {
                if (roomIdBelongTo != -1 && billType.getBelongTo() == -1) {
                    new AlertDialog.Builder(ChargeManageActivity.this)
                            .setTitle("禁止操作")
                            .setMessage("当前页面不允许删除公共费用类型，请用取消激活代替删除公共费用类型")
                            .setNegativeButton("确定", null)
                            .show();
                    return;
                }
                new AlertDialog.Builder(ChargeManageActivity.this)
                        .setTitle("删除费用")
                        .setMessage("是否删除费用类型：" + billType.getBillTypeName())
                        .setPositiveButton("确定", (dialog, which) -> {
                            LitePal.delete(BillType.class, billType.getId());
                            reflashRecyclerView();
                        })
                        .setNegativeButton("取消", (dialog, which) -> viewHolder.swipeLayout.close())
                        .create()
                        .show();
            });
            viewHolder.detailButton.setOnClickListener(v->{
                View view = LayoutInflater.from(ChargeManageActivity.this).inflate(R.layout.chargeitem_detail, null, false);
                ((TextView) view.findViewById(R.id.chargeitem_detailTextView)).setText(billType.getMemoForBillType());
                new AlertDialog.Builder(ChargeManageActivity.this)
                        .setTitle(billType.getBillTypeName() + "费用详情")
                        .setView(view)
                        .setPositiveButton("确定", null)
                        .show();
            });
            viewHolder.surfaceLayout.setOnClickListener(v -> {
                if (stateStore==null) stateStore=new SparseBooleanArray();
                stateStore.append(position,viewHolder.isOccupyCheckBox.isChecked());
                viewHolder.isOccupyCheckBox.setChecked(!viewHolder.isOccupyCheckBox.isChecked());
                viewHolder.swipeLayout.close();
            });
            viewHolder.isOccupyCheckBox.setChecked(billType.isChecked());
            viewHolder.titleTextView.setText(billType.getBillTypeName());
            viewHolder.subTextView.setText(billType.getSubscrip());
        }

        @Override
        public int getItemCount() {
            return billTypeList.size();
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return 0;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private final SwipeLayout swipeLayout;
            private final TextView titleTextView;
            private final TextView subTextView;
            private final CheckBox isOccupyCheckBox;
            private final ImageButton detailButton;
            private ImageButton editChargeButton;
            private ImageButton deleteChargeButton;
            ConstraintLayout surfaceLayout;

            MyViewHolder(View itemView) {
                super(itemView);
                swipeLayout = itemView.findViewById(R.id.chargemanage_swipelayout);
                editChargeButton = itemView.findViewById(R.id.chargemanage_editCharge_button);
                deleteChargeButton = itemView.findViewById(R.id.chargemanage_deleteCharge_button);
                surfaceLayout = itemView.findViewById(R.id.chargemanage_surfaceLayout);
                titleTextView = itemView.findViewById(R.id.chargemanage_listView_item_titleTextView);
                subTextView = itemView.findViewById(R.id.chargemanage_listView_item_subTextView);
                isOccupyCheckBox = itemView.findViewById(R.id.chargemanage_listView_item_checkbox);
                detailButton = itemView.findViewById(R.id.chargemanage_detailCharge_button);
                surfaceLayout.measure(0, 0);
                int measuredHeight = surfaceLayout.getMeasuredHeight();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(measuredHeight, measuredHeight);
                editChargeButton.setLayoutParams(layoutParams);
                deleteChargeButton.setLayoutParams(layoutParams);
                detailButton.setLayoutParams(layoutParams);
            }

            public boolean isChecked() {
                return isOccupyCheckBox.isChecked();
            }
        }
    }
    private List<BillType> billTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_manage);
        ButterKnife.bind(this);
        Util.setFullScreen(this);
        setupToolbar();

        roomIdBelongTo = getIntent().getLongExtra(BaseActivity.ROOM_ID, -1);
        setupRecyclerView();
        reflashRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void reflashRecyclerView() {
        //根据房间id获得费用类型列表
        billTypeList = Util.getAllBillTypeOf(roomIdBelongTo);
        Util.sort(billTypeList);
        //显示这个列表
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(billTypeList);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        toolbar.setTitle("费用管理");
        toolbar.getToolbar().setNavigationOnClickListener(v->finish());
        toolbar.getToolbar().setNavigationIcon(R.drawable.ic_back);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BaseActivity.REQUEST_FROM_CHARGEMANAG_TO_NEWCHARGE:
                reflashRecyclerView();
                break;
        }
    }

    public void SaveTheCheckedState() {
        for (int i = 0; i < billTypeList.size(); i++) {
            boolean checked = ((MyRecyclerViewAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i)).isChecked();
            if (stateStore==null || stateStore.indexOfKey(i)<0 || checked==stateStore.get(i) ) continue;
            BillType billType = billTypeList.get(i);
            if (roomIdBelongTo!=-1 && billType.getBelongTo()==-1)
                saveAsNewPrivateBillType(checked,billType);
            else updateCurrentBillType(checked,billType);
        }
    }

    private void saveAsNewPrivateBillType(boolean checked, BillType currentBillType) {
        BillType newPrivateBillType = new BillType(currentBillType);
        newPrivateBillType.setBelongTo(roomIdBelongTo);
        newPrivateBillType.setChecked(checked);
        newPrivateBillType.save();
    }

    private void updateCurrentBillType(boolean checked, BillType currentBillType) {
        currentBillType.setChecked(checked);
        currentBillType.save();
        if (currentBillType.isPublic()) {
            List<BillType> privateBillTypeList = LitePal.where("billTypeName=? and id!=?", currentBillType.getBillTypeName(), String.valueOf(currentBillType.getId())).find(BillType.class);
            for (BillType privateBillType : privateBillTypeList) {
                privateBillType.setChecked(checked);
                privateBillType.save();
            }
        }
    }

    @OnClick(R.id.chargemanage_saveButton)
    public void goCreateChargeActivity(View view) {
        Intent intent = new Intent(ChargeManageActivity.this, NewChargeActivity.class);
        intent.putExtra(BaseActivity.ROOM_ID, roomIdBelongTo);
        startActivityForResult(intent,BaseActivity.REQUEST_FROM_CHARGEMANAG_TO_NEWCHARGE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SaveTheCheckedState();
    }
}
