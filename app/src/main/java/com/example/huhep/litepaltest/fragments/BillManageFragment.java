package com.example.huhep.litepaltest.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.ChargeManageActivity;
import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.huhep.litepaltest.BaseActivity.REQUEST_FROM_BILLMANAGE_TO_CHARGEMANAGE;
import static com.example.huhep.litepaltest.BaseActivity.WHEN_KEY_FOR_SHP;
import static com.example.huhep.litepaltest.fragments.CreateBillFragment.STATE_FINISH;
import static com.example.huhep.litepaltest.fragments.CreateBillFragment.STATE_LESSTHAN;
import static com.example.huhep.litepaltest.fragments.CreateBillFragment.STATE_NOTFINISH;
import static com.example.huhep.litepaltest.fragments.CreateBillFragment.STATE_OUTOFBOUND;
import static com.example.huhep.litepaltest.fragments.CreateBillFragment.STATE_SAMEAS;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillManageFragment extends Fragment {
    @BindView(R.id.managebillfragment_toolbar)
    CustomToolbar toolbar;
    @BindView(R.id.managebillfragment_tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.managebillfragment_viewpager)
    ViewPager viewPager;


    private List<Room> roomList;
    private Unbinder bind;
    private long roomIdToShow;
    private FragmentStatePagerAdapter adapter;
    private List<CreateBillFragment> fragmentList;
    private static final String TAG = "PengPeng";

    public BillManageFragment() {
    }

    public static BillManageFragment newInstance(List<Room> roomList,long roomIdToShow) {
        BillManageFragment billManageFragment = new BillManageFragment();
        Util.sort(roomList);
        billManageFragment.roomList = roomList;
        billManageFragment.roomIdToShow = roomIdToShow;
        //更新shareperference
        SharedPreferences.Editor editor = BaseActivity.getSP().edit();
        String whenfromSP = BaseActivity.getSP().getString(WHEN_KEY_FOR_SHP, "");
        if (!Util.getWhen().equals(whenfromSP)){
            editor.clear();
            editor.putString(WHEN_KEY_FOR_SHP, Util.getWhen());
            editor.apply();
        }
        return billManageFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bill_manage, container, false);
        bind = ButterKnife.bind(this, view);
        if (roomList.size()==0) {
            return ifEmpty();
        }
        setupToolBar();
        setupView();
        return view;
    }

    private void setupView() {
        if (roomList.size()==1) tabLayout.setVisibility(View.GONE);
        else tabLayout.setVisibility(View.VISIBLE);
        fragmentList = getFragments();
        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()){
            @Override
            public int getCount() {
                return roomList.size();
            }

            @Override
            public Fragment getItem(int position) {
                fragmentList.get(position).setStateChangeListener(state -> {
                    ImageView imageView = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.tab_colorImageView);
                    switch (state) {
                        case STATE_FINISH:
                            imageView.setImageResource(R.color.deepDeepDark);
                            break;
                        case STATE_NOTFINISH:
                            imageView.setImageResource(R.color.deepDark);
                            break;
                        case STATE_LESSTHAN:
                            imageView.setImageResource(R.color.lightRed);
                            break;
                        case STATE_OUTOFBOUND:
                            imageView.setImageResource(android.R.color.holo_blue_light);
                            break;
                        case STATE_SAMEAS:
                            imageView.setImageResource((android.R.color.holo_green_dark));
                            break;
                    }
                });
                return fragmentList.get(position);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return roomList.get(position).getRoomNum();
            }
        };
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(adapter);
        int tabCount = tabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.billmanagefragment_tablayout);
            TextView textView =  tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_textView);
            textView.setText(adapter.getPageTitle(i));
        }
    }

    public void setRoomIdToShow(long roomIdToShow) {
        this.roomIdToShow = roomIdToShow;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (roomIdToShow!=-1){
            showViewPagerSelectRoom(roomIdToShow);
        }
    }

    public void showViewPagerSelectRoom(long roomIdToShow) {
        if (roomIdToShow==-1) return;
        for (int i = 0; i < roomList.size(); i++) {
            if (roomList.get(i).getId()==roomIdToShow){
                viewPager.setCurrentItem(i);
                return;
            }
        }
    }

    private List<CreateBillFragment> getFragments() {
        List<CreateBillFragment> fragments = new ArrayList<>();
        for (Room room : roomList)
            fragments.add(CreateBillFragment.newInstance(room));
        return fragments;
    }

    public void saveAllMessage() {
        if (fragmentList==null) return;
        for (Fragment fragment : fragmentList) {
            CreateBillFragment fragment1 = (CreateBillFragment) fragment;
            fragment1.saveMessage();
        }
    }

    private void setupToolBar() {
        if (roomList.size()>1)
            toolbar.getToolbar().getMenu().add("查找").setIcon(R.drawable.ic_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toolbar.getToolbar().inflateMenu(R.menu.toobarmenu_createbillfragment);
        String roomNumInToolBar = "";
        if (roomList.size()==1) roomNumInToolBar = roomList.get(0).getRoomNum()+"房";
        toolbar.setTitle(roomNumInToolBar+Util.getWhen().substring(5)+"费用列表");
        toolbar.getToolbar().setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "编辑费用类型":
                    long id = roomList.get(viewPager.getCurrentItem()).getId();
                    Intent intent = new Intent(getContext(), ChargeManageActivity.class);
                    intent.putExtra(BaseActivity.ROOM_ID, id);
                    startActivityForResult(intent,REQUEST_FROM_BILLMANAGE_TO_CHARGEMANAGE);
                    break;
                case "查找":
                    LinearLayout linearLayout = new LinearLayout(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
                    layoutParams.setMargins(64,0,64,0);
                    EditText editText = new EditText(getContext());
                    editText.setHint("请输入房间号");
                    editText.setMaxLines(1);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setLayoutParams(layoutParams);
                    linearLayout.addView(editText);
                    new AlertDialog.Builder(getContext()).setTitle("请输入要查找的房间号:")
                            .setView(linearLayout)
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialog, which) -> {
                                String s = editText.getText().toString();
                                if (s.isEmpty()) return;
                                for (int i=0;i<roomList.size();i++) {
                                    Room room = roomList.get(i);
                                    if (s.equalsIgnoreCase(room.getRoomNum())){
                                        viewPager.setCurrentItem(i);
                                        return;
                                    }
                                }
                                BaseActivity.show("找不到该房间");
                            })
                            .show();
                    break;
            }
            return false;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_FROM_BILLMANAGE_TO_CHARGEMANAGE:
                int currentItem = viewPager.getCurrentItem();
                fragmentList.get(currentItem).setupTheView();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    private View ifEmpty() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setBackgroundResource(R.color.backgroundColor);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(getActivity());
        textView.setText("找不到房间");
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);
        return linearLayout;
    }


    public ViewPager getViewPager() {
        return viewPager;
    }

}
