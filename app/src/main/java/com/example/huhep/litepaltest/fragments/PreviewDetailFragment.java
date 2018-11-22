package com.example.huhep.litepaltest.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.components.ItemComponents;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewDetailFragment extends Fragment {
    private long roomSetId;
    @BindView(R.id.previewdetail_recyclerView)
    RecyclerView recyclerView;

    private OnCreatedViewFinishedListener onCreatedViewFinishedListener;
    private Unbinder unbinder;
    private int state = 0;

    public void setOnViewHolderClickedListener(PreviewDetailFragment.onViewHolderClickedListener onViewHolderClickedListener) {
        this.onViewHolderClickedListener = onViewHolderClickedListener;
    }

    private onViewHolderClickedListener onViewHolderClickedListener;

    public PreviewDetailFragment() {
        // Required empty public constructor
    }

    interface OnCreatedViewFinishedListener {
        void onCreatedFinished(int state);
    }

    public interface onViewHolderClickedListener {
        void onViewHolderClicked(long roomId);
    }

    @SuppressLint("ValidFragment")
    public PreviewDetailFragment(long roomSetId) {
        this.roomSetId = roomSetId;
    }

    public void setOnCreatedViewFinishedListener(OnCreatedViewFinishedListener listener) {
        this.onCreatedViewFinishedListener = listener;
    }

    class PreviewDetailRecyclerAdapter extends RecyclerView.Adapter<PreviewDetailRecyclerAdapter.ViewHolder> {
        private List<Room> roomList;
        private boolean makeItTogether;

        public PreviewDetailRecyclerAdapter(List<Room> roomList, boolean makeItTogether) {
            this.roomList = roomList;
            this.makeItTogether = makeItTogether;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView roomNumTextView;
            private LinearLayout linearLayout;
            private boolean isCheck;

            public void setCheck(boolean isCheck) {
                this.isCheck = isCheck;
                setTitleColor(isCheck);
            }

            public boolean isCheck() {
                return isCheck;
            }

            public ViewHolder(View itemView) {
                super(itemView);
                roomNumTextView = itemView.findViewById(R.id.previewdetailitem_roomnum);
                linearLayout = itemView.findViewById(R.id.previewdetailitem_linearLayout);
            }

            public void setTitleColor(boolean contains) {
                if (contains) {
                    roomNumTextView.setBackgroundResource(R.color.lightBlue);
                } else {
                    roomNumTextView.setBackgroundResource(R.color.loeckedDark);
                }
            }
        }

        @NonNull
        @Override
        public PreviewDetailRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.previewdetail_recyclerview_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PreviewDetailRecyclerAdapter.ViewHolder holder, int position) {
            if (makeItTogether) {
                holder.roomNumTextView.setText("总体");
                Set<Map.Entry<String, List<Bill>>> entries = PreviewFragment.billOfName.entrySet();
                for (Map.Entry<String, List<Bill>> map : entries) {
                    List<Bill> billList = map.getValue();
                    ItemComponents itemComponents = new ItemComponents(getContext(), null);
                    if (billList.get(0).getToDegree() != 0) {
                        itemComponents.setDuration("共计: " + Util.getTotalHowMuchOfBillList(billList) + " 元");
                        itemComponents.setDetail("共用: " + Util.getHowDegreeOfBillList(billList) + " 度");
                    } else {
                        itemComponents.duration.setVisibility(View.GONE);
                        itemComponents.setDetail("共计：" + Util.getTotalHowMuchOfBillList(billList) + " 元");
                    }
                    itemComponents.setTitle(map.getKey());
                    holder.linearLayout.addView(itemComponents);
                }
                ItemComponents itemComponents = new ItemComponents(getContext(), null);
                itemComponents.setTitle("所有收费");
                itemComponents.duration.setVisibility(View.GONE);
                itemComponents.setDetail(Util.getTotalHowMuchOfBillList(PreviewFragment.billList) + "元");
                holder.linearLayout.addView(itemComponents);
            } else {
                Room room = roomList.get(position);
                holder.roomNumTextView.setText(room.getRoomNum());
                holder.setCheck(BillManageFragment.roomsToShow != null && BillManageFragment.roomsToShow.contains(room.getId()));
                List<BillType> billTypeList = room.getCheckedBillTypeList();
                Util.sort(billTypeList);
                List<Bill> tempBill = new ArrayList<>();
                for (BillType billType : billTypeList) {
                    Bill bill = new Bill(room, billType);
                    if (PreviewFragment.chargeMap.get(room.getId()).containBill(bill)) {
                        bill = PreviewFragment.chargeMap.get(room.getId()).getSameBill(bill);
                    }
                    tempBill.add(bill);
                    if (bill.getType() > state) state = bill.getType();
                    ItemComponents itemComponent = new ItemComponents(getContext(), null);
                    itemComponent.setTitle(billType.getBillTypeName());
                    itemComponent.setDuration(bill.getDuration());
                    itemComponent.setDetail(bill.getDetail());
                    itemComponent.setDetailColor(bill.getType());
                    holder.linearLayout.addView(itemComponent);
                }
                ItemComponents itemComponents = new ItemComponents(getContext(), null);
                itemComponents.setTitle("合计");
                itemComponents.duration.setVisibility(View.GONE);
                double totalHowMuchOfBillList = Util.getTotalHowMuchOfBillList(tempBill);
                itemComponents.setDetail(totalHowMuchOfBillList + " 元");
                holder.linearLayout.addView(itemComponents);
                if (roomList.size() - 1 == position && onCreatedViewFinishedListener != null) {
                    onCreatedViewFinishedListener.onCreatedFinished(state);
                }
                holder.linearLayout.setOnClickListener(v -> {
                    if (onViewHolderClickedListener != null)
                        onViewHolderClickedListener.onViewHolderClicked(room.getId());
                });
                holder.roomNumTextView.setOnClickListener(v -> {
                    if (BillManageFragment.roomsToShow != null) {
                        if (BillManageFragment.roomsToShow.contains(room.getId())) {
                            BillManageFragment.roomsToShow.remove(room.getId());
                            holder.setCheck(false);
                        } else{
                            BillManageFragment.roomsToShow.add(room.getId());
                            holder.setCheck(true);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (makeItTogether) return 1;
            else return roomList.size();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_detail, container, false);
        unbinder = ButterKnife.bind(this, view);

        boolean makeItTogether = false;
        List<Room> roomList = null;
        if (roomSetId != -1) {
            roomList = LitePal.where("roomSetId=? and isOccupy=1", String.valueOf(roomSetId)).find(Room.class);
        } else {
            makeItTogether = true;
        }
        PreviewDetailRecyclerAdapter previewDetailRecyclerAdapter = new PreviewDetailRecyclerAdapter(roomList, makeItTogether);
        recyclerView.setAdapter(previewDetailRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
