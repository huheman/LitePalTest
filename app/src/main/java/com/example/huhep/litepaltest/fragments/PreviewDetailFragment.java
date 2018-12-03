package com.example.huhep.litepaltest.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.components.ItemComponents;
import com.example.huhep.litepaltest.utils.BillFormater;
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
    private List<Room> roomList;

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
            holder.linearLayout.removeAllViews();
            if (makeItTogether) {
                holder.roomNumTextView.setText("总体");
                BillFormater billFormater = new BillFormater(PreviewFragment.billList);
                for (ItemComponents components : billFormater.getComponentsList(getContext()))
                    holder.linearLayout.addView(components);
            } else {
                Room room = roomList.get(position);
                holder.roomNumTextView.setText(room.getRoomNum());
                holder.setCheck(BillManageFragment.getRoomsToShow().contains(room.getId()));
                Charge charge = PreviewFragment.chargeMap.get(room.getId());
                for (ItemComponents itemComponents : charge.getItemComponentsList(getContext())) {
                    holder.linearLayout.addView(itemComponents);
                }
                if (roomList.size() - 1 == position && onCreatedViewFinishedListener != null) {
                    onCreatedViewFinishedListener.onCreatedFinished(charge.getState());
                }
                holder.linearLayout.setOnClickListener(v -> {
                    if (onViewHolderClickedListener != null)
                        onViewHolderClickedListener.onViewHolderClicked(room.getId());
                });
                holder.roomNumTextView.setOnClickListener(v -> {
                    if (BillManageFragment.getRoomsToShow().contains(room.getId())) {
                        BillManageFragment.getRoomsToShow().remove(room.getId());
                        holder.setCheck(false);
                    } else {
                        BillManageFragment.getRoomsToShow().add(room.getId());
                        holder.setCheck(true);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupView();
        return view;
    }

    private void setupView() {
        boolean makeItTogether = false;
        if (roomSetId != -1) {
            roomList = LitePal.select("id,roomNum").where("roomSetId=? and isOccupy=1", String.valueOf(roomSetId)).find(Room.class);
            Util.sort(roomList);
        } else {
            makeItTogether = true;
        }
        PreviewDetailRecyclerAdapter previewDetailRecyclerAdapter = new PreviewDetailRecyclerAdapter(roomList, makeItTogether);
        recyclerView.setAdapter(previewDetailRecyclerAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
