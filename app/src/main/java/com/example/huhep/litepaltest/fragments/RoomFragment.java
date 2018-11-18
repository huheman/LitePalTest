package com.example.huhep.litepaltest.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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

        public int getmPosition() {
            return mPosition;
        }
        class ViewHolder extends RecyclerView.ViewHolder{
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
        }

        public MyRoomFragmentRecyclerAdapter(List<Room> roomList) {
            this.roomList = roomList;
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
                viewHolder.occupcationTextView.setText("已出租");
                viewHolder.occupcationTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                viewHolder.occupcationTextView.setText("空置中");
                viewHolder.occupcationTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
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
            if (room.isPaidOnWechat()) viewHolder.paidInWeChatTextView.setVisibility(View.VISIBLE);
            else viewHolder.paidInWeChatTextView.setVisibility(View.GONE);
            viewHolder.detailTextView.setText(room.getDetail());
            Charge lastCharge = room.getLastCharge();
            if (lastCharge != null) {
                viewHolder.timeTextView.setText(Util.getWhen(lastCharge.getCreateDate()));
            } else {
                viewHolder.timeTextView.setText("未开过单");
            }
            viewHolder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                if (!room.isPaidOnWechat()) menu.add("已经微信支付了");
                else menu.add("取消微信支付状态");
                menu.add("编辑详细信息");
                menu.add("编辑费用类型");
                menu.add("修改费用数额");
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
        View view = inflater.inflate(R.layout.vp_fragment, container,false);
        bind = ButterKnife.bind(this, view);
        adapter = new MyRoomFragmentRecyclerAdapter(roomList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint())  return false;//当前fragment不可见
        BaseActivity.show(roomList.get(adapter.getmPosition()).getRoomNum());
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
