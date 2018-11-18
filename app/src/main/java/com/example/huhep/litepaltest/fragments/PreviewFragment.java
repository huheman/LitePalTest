package com.example.huhep.litepaltest.fragments;


import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.huhep.litepaltest.bean.Bill.BILL_ERROR;
import static com.example.huhep.litepaltest.bean.Bill.BILL_NOT_DEFINE;
import static com.example.huhep.litepaltest.bean.Bill.BILL_NOT_INIT;
import static com.example.huhep.litepaltest.bean.Bill.BILL_TOO_MUCH;

public class PreviewFragment extends Fragment {
    public static LongSparseArray<Charge> chargeMap;    //roomid对应charge
    public static List<Bill> billList; //所有的bill
    public static HashMap<String, List<Bill>> billOfName;   //总体那里的bill
    private PreviewDetailFragment.onViewHolderClickedListener onViewHolderClickedListener;
    private Unbinder unbinder;

    public void setOnBillsSavedListener(OnBillsSavedListener onBillsSavedListener) {
        this.onBillsSavedListener = onBillsSavedListener;
    }

    private OnBillsSavedListener onBillsSavedListener;

    public interface OnBillsSavedListener{
        void onBillSaved();
    }

    @BindView(R.id.previewmanagefragment_toobar)
    CustomToolbar toolbar;

    @BindView(R.id.previewmanagefragment_noRoomSetTextView)
    TextView noRoomTextView;

    @BindView(R.id.previewmanagefragment_viewPager)
    ViewPager viewPager;

    @BindView(R.id.previewmanagerfragment_tablayout)
    TabLayout tabLayout;

    @BindView(R.id.previewmanagerfragment_coordLayout)
    CoordinatorLayout coordinatorLayout;
    public void setOnViewHolderClickedListener(PreviewDetailFragment.onViewHolderClickedListener onViewHolderClickedListener) {
        this.onViewHolderClickedListener = onViewHolderClickedListener;
    }

    public PreviewFragment() {
        // Required empty public constructor
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

    public void setupBillList() {
        billList = new ArrayList<>();
        billOfName = new HashMap<>();
        chargeMap = new LongSparseArray<>();
        //如果是多种类型，则新建一个Map<"组名",Map<"费用名,List<Bill>>>的map,如果room属于组名里面的，则把组名对应的Map.get("费用名")的bill加上这个。
        List<Room> roomList = LitePal.where("isOccupy=1").find(Room.class);
        for (Room room : roomList) {
            Charge charge;
            Charge lastCharge = room.getLastCharge();
            if (lastCharge!=null && lastCharge.getCreateDateToString().equalsIgnoreCase(Util.getWhen()))
                charge = lastCharge;
            else charge = new Charge(room);
            chargeMap.put(room.getId(),charge);
            List<BillType> checkedBillTypeList = room.getCheckedBillTypeList();
            Util.sort(checkedBillTypeList);
            for (BillType checkBillType : checkedBillTypeList) {
                //所有类型即使出错都放到list里
                Bill bill = new Bill(room, checkBillType);
                charge.addBill(bill);
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

    public void setupView() {
        List<RoomSet> roomSetList = LitePal.findAll(RoomSet.class);
        if (roomSetList.size()==0){
            coordinatorLayout.setVisibility(View.GONE);
            noRoomTextView.setVisibility(View.VISIBLE);
            return;
        }else {
            noRoomTextView.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }
        Util.sort(roomSetList);
        List<Long> list = new ArrayList<>();
        list.add(-1L);
        for (RoomSet roomSet : roomSetList)
            list.add(roomSet.getId());

        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {

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
                        case BILL_NOT_INIT:
                            imageView.setImageResource(R.color.deepDark);
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
            String title = "自定义";
            if (list.get(i)!=-1) title = LitePal.find(RoomSet.class, list.get(i)).getRoomSetName();
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
        if (chargeMap.size()==0){
            BaseActivity.show("没有数据，请先新建房间和收费项目");
            return;
        }
        new AlertDialog.Builder(getContext()).setCancelable(false)
                .setTitle("即将打印")
                .setMessage("请把打印机数据线与手机连接，再进行打印")
                .setPositiveButton("确认", (dialog, which) -> {
                    beginToPrint();
                    saveBills();
                })
                .setNegativeButton("取消", null)
                .show();

    }

    private void saveBills() {
        int count = 0;
        List<Long> roomIds=new ArrayList<>();
        for (Bill bill : billList) {
            int type = bill.getType();
            if (type == Bill.BILL_ALL_OK ||
                    type == Bill.BILL_TOO_MUCH ||
                    type == Bill.BILL_SET_BASEDEGREE) {
                count++;
                roomIds.add(bill.getRoom_id());
                bill.save();
            }
        }

        for (Long roomId : roomIds) {
            chargeMap.get(roomId).save();
        }
        BaseActivity.show("保存了"+count+"条数据");
        //删除缓存
        BaseActivity.getSP().edit().clear().apply();
        if (onBillsSavedListener != null) {
            onBillsSavedListener.onBillSaved();
        }
    }

    private void beginToPrint() {
    }
}
