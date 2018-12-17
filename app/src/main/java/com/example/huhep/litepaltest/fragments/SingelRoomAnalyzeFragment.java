package com.example.huhep.litepaltest.fragments;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SingelRoomAnalyzeFragment extends Fragment {
    @BindView(R.id.singleAnalyze_noRoomTextView)
    TextView noRoomTV;

    @BindView(R.id.singleAnalyze_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.singleAnalyze_swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    private Room room;
    private List<Charge> chargeList;
    private TotalAnalyzeDetailFragment.TotalAutoLoadAdapter adapter;
    private int offset;

    class SingleRoomAnalyzeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SingleRoomViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.recyclerviewofmain_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Charge charge = chargeList.get(position);
            SingleRoomViewHolder roomViewHolder = ((SingleRoomViewHolder) holder);
            roomViewHolder.detailTV.setText(charge.getDescribe());
            roomViewHolder.codeTV.setText(charge.getPassWord());
            if (charge.getChargeType() == Charge.TYPE_LIVE_IN || charge.getChargeType() == Charge.TYPE_MOVE_OUT) {
                roomViewHolder.shareButton.setVisibility(View.INVISIBLE);
                roomViewHolder.timeTV.setText(Util.getDate(charge.getCreateDate(), "yyyy年MM月dd日"));
                if (charge.getChargeType() == Charge.TYPE_LIVE_IN)
                    roomViewHolder.roomNameTV.setText(room.getRoomNum() + " 入住");
                else {
                    roomViewHolder.roomNameTV.setText(room.getRoomNum() + " 退房");
                    roomViewHolder.roomNameTV.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_light));
                }
            } else roomViewHolder.timeTV.setText(charge.getCreateDateToString());
            if (charge.haspainOnWechat())
                roomViewHolder.paidinWechatTV.setVisibility(View.VISIBLE);
            else roomViewHolder.paidinWechatTV.setVisibility(View.INVISIBLE);
            roomViewHolder.shareButton.setEnabled(true);
            roomViewHolder.shareButton.setOnClickListener(v -> {
                Intent intent = new Intent();
                ComponentName comp = new ComponentName("com.tencent.mm",
                        "com.tencent.mm.ui.tools.ShareImgUI");
                intent.setComponent(comp);
                intent.setAction("android.intent.action.SEND");
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                File file = new File(charge.getImage());
                Uri uri = FileProvider.getUriForFile(getContext(), "com.example.huhep.litepaltest", file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                BaseActivity.show("向" + room.getRoomNum() + "发送账单");
                roomViewHolder.shareButton.setEnabled(false);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chargeList.size();
        }

        class SingleRoomViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.item_roomNameTipsTextView)
            TextView roomNameTipTV;
            @BindView(R.id.roomitem_timeTextView)
            TextView timeTV;

            @BindView(R.id.item_roomNameTextView)
            TextView roomNameTV;

            @BindView(R.id.item_occupationTextView)
            TextView occupationTV;

            @BindView(R.id.roomitem_paidinwechatTextView)
            TextView paidinWechatTV;

            @BindView(R.id.item_noteTipsTextView)
            TextView noteTipsTV;

            @BindView(R.id.item_noteTextView)
            TextView noteTV;

            @BindView(R.id.item_detailTextView)
            TextView detailTV;

            @BindView(R.id.item_codeTextView)
            TextView codeTV;

            @BindView(R.id.item_shareBut)
            Button shareButton;

            SingleRoomViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                timeTV.setTextColor(getResources().getColor(R.color.deepDeepDark));
                roomNameTV.setText(room.getRoomNum());
                roomNameTipTV.setText("房间名:");
                paidinWechatTV.setVisibility(View.INVISIBLE);
                occupationTV.setVisibility(View.GONE);
                noteTipsTV.setVisibility(View.GONE);
                noteTV.setVisibility(View.GONE);
                codeTV.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public SingelRoomAnalyzeFragment() {
    }

    @SuppressLint("ValidFragment")
    public SingelRoomAnalyzeFragment(Room room) {
        this.room = room;
        initChargeOfRoom();
        getMoreChargeFromRoom();
    }

    private void getMoreChargeFromRoom() {
        int limit = BaseActivity.getContext().getResources().getInteger(R.integer.MonthOnceShown);
        List<Charge> chargeJustCreate = LitePal.select("createDate,passWord,describe,chargeType,image,roomId,paidOnWechat").where("roomId=?", room.getId() + "").order("createDate desc").offset(offset).limit(limit).find(Charge.class);
        if (adapter != null) {
            if (chargeJustCreate.size() < limit) adapter.setHasMore(false);
            else adapter.setHasMore(true);
        }
        offset += limit;
        chargeList.addAll(chargeJustCreate);
    }

    private void initChargeOfRoom() {
        chargeList = new ArrayList<>();
        offset = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singel_room_analyze, container, false);
        ButterKnife.bind(this, view);
        adapter = new TotalAnalyzeDetailFragment.TotalAutoLoadAdapter(new SingleRoomAnalyzeAdapter());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new TotalAnalyzeDetailFragment.OnNeedToLoadMoreListener() {

            @Override
            void loadMore() {
                if (adapter.isHasMore())
                    getMoreChargeFromRoom();
            }
        });
        refreshLayout.setOnRefreshListener(() -> {
            initChargeOfRoom();
            getMoreChargeFromRoom();
            refreshLayout.setRefreshing(false);
        });
        if (chargeList.size() == 0) {
            noRoomTV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            noRoomTV.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        return view;
    }

}
