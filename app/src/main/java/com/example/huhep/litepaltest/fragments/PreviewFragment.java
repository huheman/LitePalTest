package com.example.huhep.litepaltest.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.huhep.litepaltest.bean.Bill.BILL_ERROR;
import static com.example.huhep.litepaltest.bean.Bill.BILL_TOO_MUCH;

public class PreviewFragment extends Fragment {
    private List<Long> roomSetId;
    public static List<Bill> billList;
    public static HashMap<String, List<Bill>> billOfName;
    private PreviewDetailFragment.onViewHolderClickedListener onViewHolderClickedListener;
    private Unbinder unbinder;
    private FragmentStatePagerAdapter adapter;

    @BindView(R.id.previewmanagefragment_toobar)
    CustomToolbar toolbar;

    @BindView(R.id.previewmanagefragment_viewPager)
    ViewPager viewPager;

    @BindView(R.id.previewmanagerfragment_tablayout)
    TabLayout tabLayout;

    public void setOnViewHolderClickedListener(PreviewDetailFragment.onViewHolderClickedListener onViewHolderClickedListener) {
        this.onViewHolderClickedListener = onViewHolderClickedListener;
    }

    public PreviewFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public PreviewFragment(List<Long> roomsetId) {
        this.roomSetId = roomsetId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        unbinder = ButterKnife.bind(this, view);

        setupToolbar();
        setupBillList();
        setupView();
        return view;
    }

    private void setupBillList() {
        billList = new ArrayList<>();
        billOfName = new HashMap<>();
        List<Room> roomList = LitePal.where("isOccupy=1").find(Room.class);
        for (Room room : roomList) {
            List<BillType> checkedBillTypeList = room.getCheckedBillTypeList();
            Util.sort(checkedBillTypeList);
            for (BillType checkBillType : checkedBillTypeList) {
                Bill bill = new Bill(room, checkBillType);
                List<Bill> bills = billOfName.get(checkBillType.getBillTypeName());
                if (bills == null) {
                    bills = new ArrayList<>();
                    billOfName.put(checkBillType.getBillTypeName(), bills);
                }
                bills.add(bill);
                billList.add(bill);
            }
        }
    }

    private void setupView() {
        List<Long> list = new ArrayList<>();
        list.add(-1L);
        list.addAll(roomSetId);
        adapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Fragment getItem(int position) {
                PreviewDetailFragment fragment = new PreviewDetailFragment(list.get(position));
                fragment.setOnCreatedViewFinishedListener(state -> {
                    ImageView imageView = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.tab_colorImageView);
                    switch (state) {
                        case BILL_ERROR:
                            imageView.setImageResource(R.color.lightRed);
                            break;
                        case BILL_TOO_MUCH:
                            imageView.setImageResource(android.R.color.holo_blue_light);
                            break;
                        default:
                            imageView.setImageResource(R.color.deepDeepDark);
                            break;
                    }
                });
                if (onViewHolderClickedListener != null) {
                    fragment.setOnViewHolderClickedListener(onViewHolderClickedListener);
                }
                return fragment;
            }
        };
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.billmanagefragment_tablayout);
            TextView textView = tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_textView);
            String title = "总体";
            if (i != 0) title = LitePal.find(RoomSet.class, roomSetId.get(i - 1)).getRoomSetName();
            textView.setText(title);
        }
    }

    private void setupToolbar() {
        toolbar.setTitle("预览详情");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.previewmanagefragment_printButton)
    public void printButtonClicked(View view){
        beginToPrint();
        saveBills();
    }

    private void saveBills() {
        for (Bill bill : billList) {
            if (bill.getType() == Bill.BILL_ALL_OK ||
                    bill.getType() == Bill.BILL_TOO_MUCH ||
                    bill.getType() == Bill.BILL_SET_BASEDEGREE) {
                bill.save();
            }
        }
    }

    private void beginToPrint() {
    }
}
