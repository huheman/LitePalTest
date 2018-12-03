package com.example.huhep.litepaltest.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.MainActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AnalyzeManagmentFragment extends Fragment {
    @BindView(R.id.analyzemanagment_toolbar)
    CustomToolbar customToolbar;

    @BindView(R.id.analyzemanagment_tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.analyzemanagment_viewPager)
    ViewPager viewPager;

    private Unbinder bind;
    private int roomPos = -1;
    private List<String> titleList;
    List<Fragment> fragmentList;

    class MyFragmentStateAdatpter extends FragmentStatePagerAdapter {
        public MyFragmentStateAdatpter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }

    ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyzemanagment, container, false);
        bind = ButterKnife.bind(this, view);
        setupToolbar();
        setupView();
        return view;
    }

    public void setupView() {
        List<Room> roomList = LitePal.findAll(Room.class);
        setupView(roomList, true);
    }

    private void setupView(@NonNull List<Room> roomList, boolean needToSumary) {
        if (roomList.size() == 1 && !needToSumary) {
            tabLayout.setVisibility(View.GONE);
        } else tabLayout.setVisibility(View.VISIBLE);
        titleList = new ArrayList<>();
        fragmentList = new ArrayList<>();
        if (needToSumary) {
            titleList.add("全部");
            fragmentList.add(new TotalAnalyzeDetailFragment());
        }
        for (Room room : roomList) {
            titleList.add(room.getRoomNum());
            fragmentList.add(new SingelRoomAnalyzeFragment(room));
        }
        viewPager.setAdapter(new MyFragmentStateAdatpter(getActivity().getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        showRoomAndCharge(roomPos);
    }

    private void setupToolbar() {
        customToolbar.setTitle("历史记录");
        customToolbar.getToolbar().getMenu().add("查找").setIcon(R.drawable.ic_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        customToolbar.getToolbar().getMenu().add("刷新").setIcon(R.drawable.ic_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        customToolbar.getToolbar().setOnMenuItemClickListener(item -> {
            switch (item.toString()) {
                case "查找":
                    ((MainActivity) getActivity()).findCharge();
                    break;
                case "刷新":
                    setupView();
                    break;
            }
            return false;
        });
    }

    public ViewPager getViewPager() {
        return viewPager;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }

    public void showRoomAndCharge(int roomPos) {
        if (roomPos == -1) return;
        this.roomPos = roomPos;
        if (viewPager != null)
            viewPager.setCurrentItem(roomPos);
    }
}
