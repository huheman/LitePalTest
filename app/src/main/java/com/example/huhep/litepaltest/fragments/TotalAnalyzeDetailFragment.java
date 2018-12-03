package com.example.huhep.litepaltest.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.utils.BillFormater;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TotalAnalyzeDetailFragment extends Fragment {
    Long maxCreateDate;
    TotalAutoLoadAdapter adapter;
    @BindView(R.id.totalAnalyze_fragment_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.totalAnalyze_fragment_textView)
    TextView textView;
    @BindView(R.id.totalAnalyze_fragment_swipeRefreshLayout)
    SwipeRefreshLayout refreshLayout;

    List<String> keyList;
    Map<String, List<Long>> keyMapChargeList;

    class TotalAnalyzeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerviewofmain_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder commonholder, int position) {
            ViewHolder holder = (ViewHolder) commonholder;
            String key = keyList.get(position);
            List<Long> chargeIdList = keyMapChargeList.get(key);
            holder.timeTV.setText(key);
            List<Bill> billList = new ArrayList<>();
            for (Long chargeId : chargeIdList)
                billList.addAll(LitePal.where("charge_Id=?", chargeId + "").find(Bill.class));
            HashMap<String, List<Bill>> billByName = new BillFormater(billList).getBillByName();
            holder.detailTV.setText("");
            for (String billTypeName : billByName.keySet()) {
                if (holder.detailTV.getText().length() > 0) holder.detailTV.append("\n\n");
                List<Bill> bills = billByName.get(billTypeName);
                holder.detailTV.append(billTypeName + "\n");
                holder.detailTV.append("总收费:  " + String.format("%.2f",Util.getTotalHowMuchOfBillList(bills)) + " 元\n");
                if (bills.get(0).getToDegree() != 0) {
                    holder.detailTV.append("总度数:  " + String.format("%.2f",Util.getHowDegreeOfBillList(bills)) + " 度");
                }
            }
            holder.detailTV.append("\n合计:  " + String.format("%.2f",Util.getTotalHowMuchOfBillList(billList)) + " 元");

        }

        @Override
        public int getItemCount() {
            return keyMapChargeList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
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

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                timeTV.setTextColor(getResources().getColor(R.color.deepDeepDark));
                roomNameTV.setText("费用总计");
                roomNameTipTV.setText("类型:");
                paidinWechatTV.setVisibility(View.INVISIBLE);
                occupationTV.setVisibility(View.INVISIBLE);
                noteTipsTV.setVisibility(View.GONE);
                noteTV.setVisibility(View.GONE);
            }
        }

    }


    static class TotalAutoLoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private RecyclerView.Adapter analyzeAdapter;
        private boolean hasMore ;
        private int typeNormal = 1;
        private int typeMore = 2;

        public TotalAutoLoadAdapter(RecyclerView.Adapter analyzeAdapter) {
            this.analyzeAdapter = analyzeAdapter;
            hasMore = analyzeAdapter.getItemCount() >= BaseActivity.getContext().getResources().getInteger(R.integer.MonthOnceShown);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return typeMore;
            else return typeNormal;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == typeNormal) {
                return analyzeAdapter.onCreateViewHolder(parent, viewType);
            } else {
                return new ViewHolderMore(LayoutInflater.from(BaseActivity.getContext()).inflate(R.layout.totalanalyze_moreitem, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TotalAutoLoadAdapter.ViewHolderMore) {
                ViewHolderMore holderMore = ((ViewHolderMore) holder);
                if (isHasMore()) holderMore.moreTV.setText("正在加载");
                else holderMore.moreTV.setText("已经到底啦");
            } else {
                analyzeAdapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return analyzeAdapter.getItemCount() + 1;
        }

        class ViewHolderMore extends RecyclerView.ViewHolder {
            private final TextView moreTV;

            ViewHolderMore(View view) {
                super(view);
                moreTV = itemView.findViewById(R.id.totalanalyze_moreitem_textView);
            }
        }

        boolean isHasMore() {
            return hasMore;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }
    }

    abstract static class OnNeedToLoadMoreListener extends RecyclerView.OnScrollListener {
        abstract void loadMore();

        private boolean isUp;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_IDLE) {
                int position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                if (position == recyclerView.getAdapter().getItemCount() - 1 && isUp) {
                    loadMore();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            isUp = dy > 0;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_total_analyze_detail, container, false);
        ButterKnife.bind(this, view);
        setupView();
        return view;
    }

    private void setupView() {
        initChargeList();
        adapter = new TotalAutoLoadAdapter(new TotalAnalyzeAdapter());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new OnNeedToLoadMoreListener() {
            @Override
            void loadMore() {
                if (adapter.isHasMore()) {
                    getChargeListInPeriod(maxCreateDate);
                }
            }
        });
        if (keyList.size() == 0) {
            recyclerView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
        }
        refreshLayout.setOnRefreshListener(() -> {
            initChargeList();
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        });
    }

    private void initChargeList() {
        keyList = new ArrayList<>();
        keyMapChargeList = new HashMap<>();
        maxCreateDate = LitePal.max(Charge.class, "createDate", Long.class);
        if (maxCreateDate != 0)
            getChargeListInPeriod(maxCreateDate);
    }

    private void getChargeListInPeriod(long hi) {
        long lo = Util.getMinSearchCreateTime(hi);
        List<Charge> charges = LitePal.select("createDate,id,passWord")
                .where("createDate>? and createDate<=?", lo + "", hi + "")
                .order("createDate desc").find(Charge.class);
        maxCreateDate = lo;
        putChargeInMap(charges);
    }

    private void putChargeInMap(List<Charge> charges) {
        int countOfNew = 0;
        for (Charge charge : charges) {
            String key = charge.getCreateDateToString();
            List<Long> chargesIdForKey = keyMapChargeList.get(key);
            if (chargesIdForKey == null) {
                countOfNew++;
                keyList.add(key);
                chargesIdForKey = new ArrayList<>();
                keyMapChargeList.put(key, chargesIdForKey);
            }
            chargesIdForKey.add(charge.getId());
        }
        if (adapter != null) {
            if (countOfNew < getContext().getResources().getInteger(R.integer.MonthOnceShown)) adapter.setHasMore(false);
            else adapter.setHasMore(true);
        }
    }

}
