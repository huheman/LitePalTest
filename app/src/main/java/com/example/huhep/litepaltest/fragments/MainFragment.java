package com.example.huhep.litepaltest.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.ChargeManageActivity;
import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.MainActivity;
import com.example.huhep.litepaltest.NewRoomActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.MemoTotal;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.components.MainToolBar;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String PREROMESETNAME="preRoomSetName";
    public static final String FLAGISCREATE="flagisnew";
    private static final String TAG = "PengPeng";
    private String mParam1;
    private String mParam2;
    private String preRoomSetName="";
    private List<Fragment> fragments;
    private Unbinder bind;
    private MainFrgmentListener mainFragmentlistener;
    @BindView(R.id.mainfragment_memolistview)
    ListView memoListView;

    @BindView(R.id.mainfragment_noRoomTextView)
    TextView noRoomTextView;

    @BindView(R.id.mainfragment_coordinLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.mainfragment_tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.mainfragment_viewpager)
    ViewPager viewPager;

    @BindView(R.id.mainfragment_toobar)
    MainToolBar mainToolBar;

    private MainToolBar.MainToolBarListener listener=new MainToolBar.MainToolBarListener() {
        @Override
        public void onSearchClicked() {
            ((MainActivity) getActivity()).findCharge();
        }

        @Override
        public void onNewRoomClicked() {
            Intent intent = new Intent(getContext(), NewRoomActivity.class);
            intent.putExtra(PREROMESETNAME, preRoomSetName);//用来指定spinner的
            startActivityForResult(intent,BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_NEWROOM);
        }

        @Override
        public void onChargeManageClicked() {
            Intent intent = new Intent(getContext(), ChargeManageActivity.class);
            startActivityForResult(intent,BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_CHARGEMANAG);
        }

        @Override
        public void onnewMemoClicked() {

        }

        @Override
        public void onSpanClicked() {
            mainToolBar.getToolbar().getMenu().findItem(R.id.mainfragment_toolbar_span).setVisible(false);
            mainToolBar.getToolbar().getMenu().findItem(R.id.mianfragment_toolbar_screch).setVisible(true);
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    ((RoomFragment) fragment).setSpan(true);
                }
            }
        }

        @Override
        public void onScrechClicked() {
            mainToolBar.getToolbar().getMenu().findItem(R.id.mainfragment_toolbar_span).setVisible(true);
            mainToolBar.getToolbar().getMenu().findItem(R.id.mianfragment_toolbar_screch).setVisible(false);
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    ((RoomFragment) fragment).setSpan(false);
                }
            }
        }

    };

    public MainFragment() {
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static MainFragment newInstance() {
        return newInstance(null, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mview = inflater.inflate(R.layout.fragment_main, container, false);
        bind = ButterKnife.bind(this, mview);
        mainToolBar.setListener(listener);
        initView();
        return mview;
    }

    /**
     * 使listview的高度成为固定
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void initView() {
        setupTheMemoView();
        setupTheRooms();
    }

    public void setupTheRooms() {
        List<RoomSet> roomSets = LitePal.findAll(RoomSet.class);
        fragments = new ArrayList<>();
        List<String> titles=new ArrayList<>();
        if (roomSets.size()<=1) tabLayout.setVisibility(View.GONE);
        else tabLayout.setVisibility(View.VISIBLE);
        if (roomSets.size()==0) {
            coordinatorLayout.setVisibility(View.GONE);
            noRoomTextView.setVisibility(View.VISIBLE);
        }else {
            coordinatorLayout.setVisibility(View.VISIBLE);
            noRoomTextView.setVisibility(View.GONE);
        }
        for (int i = 0; i < roomSets.size(); i++) {
            RoomSet roomSet = roomSets.get(i);
            titles.add(roomSet.getRoomSetName());
            List<Room> rooms = LitePal.where("roomSetId=?",String.valueOf(roomSet.getId())).find(Room.class);
            RoomFragment fragment = RoomFragment.newInstance(rooms);
            fragments.add(fragment);
        }
        FragmentStatePagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return roomSets.size();
            }

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return roomSets.get(position).getRoomSetName();
            }
        };
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        int i = titles.indexOf(preRoomSetName);
        if (i<0) i = 0;
        viewPager.setCurrentItem(i,true);
    }

    public void setupTheMemoView() {
        LitePal.findAllAsync(MemoTotal.class).listen(memoTotals -> {
            List<String> memoList = new ArrayList<>();
            for (MemoTotal memoTotal : memoTotals) {
                memoList.add(memoTotal.getMemoContent());
            }
            ArrayAdapter<String> memoAdapter = new ArrayAdapter<>(BaseActivity.getContext(), android.R.layout.simple_list_item_1,memoList);
            memoListView.setAdapter(memoAdapter);
            setListViewHeightBasedOnChildren(memoListView);
            memoListView.setOnItemClickListener((parent, view, position, id) -> {
                long room_id = memoTotals.get(position).getRoom_id();
            });
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_NEWROOM:
                if (resultCode == BaseActivity.RESULT_FROM_NEWROOM_TO_MAINFRAGMENT) {
                    preRoomSetName = data.getStringExtra(MainFragment.PREROMESETNAME);
                    if (mainFragmentlistener!=null) mainFragmentlistener.onNewRoomCreated();
                }
                setupTheRooms();
                break;
            case BaseActivity.REQUEST_FROM_MAINFRAGMENT_TO_CHARGEMANAG:
                setupTheRooms();
                if (mainFragmentlistener!=null) mainFragmentlistener.onNewRoomCreated();
                break;
        }

    }

    public void setMainFragmentlistener(MainFrgmentListener mainFragmentlistener) {
        this.mainFragmentlistener = mainFragmentlistener;
    }

    public interface MainFrgmentListener{
        void onNewRoomCreated();
    }


}
