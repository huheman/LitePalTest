package com.example.huhep.litepaltest.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.ChargeManageActivity;
import com.example.huhep.litepaltest.MainActivity;
import com.example.huhep.litepaltest.NewRoomActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.huhep.litepaltest.BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_CHARGEMANAG;
import static com.example.huhep.litepaltest.BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_NEWROOM;
import static com.example.huhep.litepaltest.BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_REVERROOM;
import static com.example.huhep.litepaltest.bean.Charge.TYPE_LIVE_IN;
import static com.example.huhep.litepaltest.bean.Charge.TYPE_MOVE_OUT;

public class RoomFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public List<Room> roomList;
    MyRoomFragmentRecyclerAdapter adapter;

    @BindView(R.id.vpfragment_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.vpfragment_textView)
    TextView textView;
    private Unbinder bind;

    public RoomFragment() {
    }

    public static RoomFragment newInstance(String param1, String param2) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    class MyRoomFragmentRecyclerAdapter extends RecyclerSwipeAdapter<MyRoomFragmentRecyclerAdapter.ViewHolder> {
        private List<Room> roomList;
        private int mPosition;
        private SparseArray<Charge> lastChargeList;

        public int getmPosition() {
            return mPosition;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.roomitem_paidinwechatTextView)
            TextView paidInWeChatTextView;

            @BindView(R.id.roomitem_timeTextView)
            TextView timeTextView;

            @BindView(R.id.item_roomNameTextView)
            TextView roomNumTextView;

            @BindView(R.id.item_noteTipsTextView)
            TextView noteTipsTextView;

            @BindView(R.id.item_noteTextView)
            TextView noteTextView;

            @BindView(R.id.main_detailTipsTextView)
            TextView detailTipsTextView;

            @BindView(R.id.item_detailTextView)
            TextView detailTextView;

            @BindView(R.id.item_occupationTextView)
            TextView occupcationTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void makeItOccupy(boolean isOccypy) {
                if (isOccypy) {
                    occupcationTextView.setText("已出租");
                    occupcationTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                } else {
                    occupcationTextView.setText("空置中");
                    occupcationTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }
        }

        public MyRoomFragmentRecyclerAdapter(List<Room> roomList) {
            this.roomList = roomList;
            lastChargeList = new SparseArray<>();
        }

        @Override
        public MyRoomFragmentRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerviewofmain_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyRoomFragmentRecyclerAdapter.ViewHolder viewHolder, int position) {
            Room room = roomList.get(position);
            if (room.isOccupy()) {
                viewHolder.makeItOccupy(true);
            } else {
                viewHolder.makeItOccupy(false);
            }
            viewHolder.roomNumTextView.setText(room.getRoomNum());
            if (TextUtils.isEmpty(room.getMemoForRoom())) {
                viewHolder.noteTipsTextView.setVisibility(View.GONE);
                viewHolder.noteTextView.setVisibility(View.GONE);
            } else {
                viewHolder.noteTextView.setVisibility(View.VISIBLE);
                viewHolder.noteTipsTextView.setVisibility(View.VISIBLE);
                viewHolder.noteTextView.setText(room.getMemoForRoom());
            }

            viewHolder.detailTextView.setText(room.getDetail());
            Charge lastCharge = room.getLastCharge();
            if (lastCharge != null) {
                lastChargeList.append(position, lastCharge);
                viewHolder.timeTextView.setText(Util.getWhen(lastCharge.getCreateDate()));
                if (lastCharge.haspainOnWechat())
                    viewHolder.paidInWeChatTextView.setVisibility(View.VISIBLE);
                else viewHolder.paidInWeChatTextView.setVisibility(View.GONE);
            } else {
                viewHolder.timeTextView.setText("未开过单");
                viewHolder.paidInWeChatTextView.setVisibility(View.GONE);
            }
            viewHolder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                if (lastCharge == null || !lastCharge.haspainOnWechat()) menu.add("已经微信支付了");
                else menu.add("取消微信支付状态");
                menu.add("编辑房间信息");
                menu.add("编辑费用类型");
                menu.add("修改账单");
                if (room.isOccupy())
                    menu.add("退房");
                else menu.add("出租");
                menu.add("删除房间");
            });
            viewHolder.itemView.setOnLongClickListener(v -> {
                mPosition = position;
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return roomList.size();
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return 0;
        }
    }

    public static RoomFragment newInstance(List<Room> roomList) {
        RoomFragment fragment = new RoomFragment();
        Util.sort(roomList);
        fragment.roomList = roomList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vp_fragment, container, false);
        bind = ButterKnife.bind(this, view);
        adapter = new MyRoomFragmentRecyclerAdapter(roomList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;//当前fragment不可见
        MyRoomFragmentRecyclerAdapter.ViewHolder viewHolder = (MyRoomFragmentRecyclerAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(adapter.getmPosition());
        Charge charge = adapter.lastChargeList.get(adapter.getmPosition());
        Room room = adapter.roomList.get(adapter.getmPosition());
        if (room == null) return false;
        switch (item.getTitle().toString()) {
            case "已经微信支付了":
                if (charge == null) {
                    BaseActivity.show("请先生成账单");
                    break;
                }

                viewHolder.paidInWeChatTextView.setVisibility(View.VISIBLE);
                charge.setPaidOnWechat(true).save();
                break;
            case "取消微信支付状态":
                if (charge == null) {
                    BaseActivity.show("请先生成账单");
                    break;
                }
                viewHolder.paidInWeChatTextView.setVisibility(View.GONE);
                charge.setPaidOnWechat(false).save();
                break;

            case "退房":
                moveOut(room);
                break;
            case "出租":
                liveIn(room);
                break;
            case "删除房间":
                deleteRoom(room);
                break;
            case "编辑房间信息":
                toOpenNewRoomActivity(room);
                break;
            case "编辑费用类型":
                toOpenChargeManageActivity(room);
                break;
            case "修改账单":
                toGotoBillManageFragment(room);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void toGotoBillManageFragment(Room room) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && room.isOccupy()) {
            activity.reflashBillManageFragment(room.getId());
        } else {
            BaseActivity.show("房间处于空置状态，无法修改账单");
        }
    }

    private void toOpenChargeManageActivity(Room room) {
        Intent intent = new Intent(getContext(), ChargeManageActivity.class);
        intent.putExtra(BaseActivity.ROOM_ID, room.getId());
        startActivityForResult(intent, REQUEST_FROM_MAINFRAGMENT_TO_CHARGEMANAG);
    }

    private void toOpenNewRoomActivity(Room room) {
        Intent intent = new Intent(getContext(), NewRoomActivity.class);
        intent.putExtra(BaseActivity.ROOM_ID, room.getId());
        startActivityForResult(intent, BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_REVERROOM);
    }

    private void liveIn(Room room) {
        Intent intent = new Intent(getContext(), NewRoomActivity.class);
        intent.putExtra(BaseActivity.ROOM_ID, room.getId());
        intent.putExtra(BaseActivity.FORCE_TO_TRUE, true);
        startActivityForResult(intent, REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_REVERROOM);
    }

    private void moveOut(Room room) {
        Charge lastCharge = adapter.lastChargeList.get(adapter.mPosition);
        Charge moveOutCharge = new Charge();
        moveOutCharge.setChargeType(TYPE_MOVE_OUT);
        moveOutCharge.setRoomId(room.getId());
        if (lastCharge == null) {
            makeItMoveOut(room,moveOutCharge);
            return;
        }
        SparseArray<Bill> usedDegreeBill = new SparseArray<>();
        SparseArray<EditText> usedDegreeEditText = new SparseArray<>();
        int index = 0;
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        List<Bill> usedBillList = lastCharge.getUsedBillList();
        for (Bill usedBill : usedBillList) {
            if (usedBill.getbillType().isChargeOnDegree()) {
                usedDegreeBill.append(index++, usedBill);
                EditText editText = new EditText(getContext());
                editText.setHint("请输入" + usedBill.getbillType().getBillTypeName() + "最后读数");
                editText.setMaxLines(1);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                layoutParams.leftMargin = 20;
                layoutParams.rightMargin = 20;
                editText.setLayoutParams(layoutParams);
                linearLayout.addView(editText);
                usedDegreeEditText.append(index - 1, editText);
            }
        }
        if (usedDegreeBill.size() == 0) {
            makeItMoveOut(room,moveOutCharge);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("正在退房")
                .setMessage("要提供表底读数才能退房:")
                .setPositiveButton("确定", (dialog, which) -> {
                    for (int i = 0; i < usedDegreeBill.size(); i++) {
                        Editable editable = usedDegreeEditText.get(i).getText();
                        Bill bill = usedDegreeBill.get(i);
                        if (TextUtils.isEmpty(editable)) {
                            BaseActivity.show("表底不能为空");
                            return;
                        } else {
                            Bill billCreated = moveOutCharge.createBill(bill, Double.valueOf(editable.toString()));
                            billCreated.save();
                        }
                    }
                    makeItMoveOut(room,moveOutCharge);
                }).setNegativeButton("取消", (dialog, which) -> BaseActivity.show("取消了退房"));
        if (linearLayout.getChildCount() > 0) {
            builder.setView(linearLayout);
        }
        builder.show();
    }

    private void makeItMoveOut(Room room,Charge moveOutCharge) {
        room.setOccupy(false);
        room.setTimeToMoveOut(System.currentTimeMillis());
        room.save();
        moveOutCharge.createDescrib();
        moveOutCharge.save();
        adapter.notifyDataSetChanged();
        MainActivity activity = (MainActivity) getActivity();
        if (activity.getBillManageFragment()!=null)
            activity.getBillManageFragment().setupView();
    }

    private void deleteRoom(Room room) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.leftMargin = 30;
        layoutParams.rightMargin = 30;
        EditText editText = new EditText(getContext());
        editText.setLayoutParams(layoutParams);
        linearLayout.addView(editText);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint("请输入房间号:" + room.getRoomNum());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("请输入房间号以确认删除")
                .setView(linearLayout)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (!editText.getText().toString().equalsIgnoreCase(room.getRoomNum())) return;
                    LitePal.delete(Room.class, room.getId());
                    LitePal.deleteAll(Charge.class, "roomId=?", String.valueOf(room.getId()));
                    LitePal.deleteAll(Bill.class, "roomId=?", String.valueOf(room.getId()));
                    roomList.remove(room);
                    adapter.notifyItemRemoved(adapter.mPosition);
                    BaseActivity.show(room.getRoomNum() + "删除成功");
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity.getBillManageFragment()!=null)
                        activity.getBillManageFragment().setupView();
                })
                .setNegativeButton("取消", (dialog, which) -> BaseActivity.show("取消删除房间:" + room.getRoomNum()))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_REVERROOM:
                if (resultCode == BaseActivity.RESULT_FROM_NEWROOM_TO_MAINFRAGMENT) {
                    Room room = LitePal.find(Room.class, adapter.roomList.get(adapter.getmPosition()).getId());
                    adapter.roomList.set(adapter.getmPosition(), room);
                    adapter.notifyDataSetChanged();
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity.getBillManageFragment()!=null)
                        activity.getBillManageFragment().setupView();
                }
        }
        getActivity().getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
    }
}
