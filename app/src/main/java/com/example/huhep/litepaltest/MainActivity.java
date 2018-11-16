package com.example.huhep.litepaltest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.fragments.BillManageFragment;
import com.example.huhep.litepaltest.fragments.CreateBillFragment;
import com.example.huhep.litepaltest.fragments.MainFragment;
import com.example.huhep.litepaltest.fragments.PreviewDetailFragment;
import com.example.huhep.litepaltest.fragments.PreviewFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        MainActivity activity = MainActivity.this;
        if (item.getItemId() != R.id.navigation_newpage &&activity.billManageFragment != null) {
                activity.billManageFragment.saveAllMessage();
        }
        FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
        hideFragment(transaction);
        switch (item.getItemId()) {
            case R.id.navigation_home:
                transaction.show(activity.mainFragment).commit();
                break;
            case R.id.navigation_newpage:
                if (activity.billManageFragment == null) {
                    List<Room> all = LitePal.where("isOccupy=1").find(Room.class);
                    activity.billManageFragment = BillManageFragment.newInstance(all,-1);
                    transaction.add(R.id.main_constraintLayout, activity.billManageFragment);
                }
                transaction.show(activity.billManageFragment).commit();
                break;
            case R.id.navigation_output:
                //每次点进来都应该重新生成
                if (activity.previewFragment!=null){
                    transaction.remove(activity.previewFragment);
                    activity.previewFragment = null;
                }
                List<RoomSet> roomSetList = LitePal.findAll(RoomSet.class);
                Util.sort(roomSetList);
                List<Long> roomSetIdList = new ArrayList<>();
                for (RoomSet roomSet : roomSetList)
                    roomSetIdList.add(roomSet.getId());
                activity.previewFragment = new PreviewFragment(roomSetIdList);
                activity.previewFragment.setOnViewHolderClickedListener(roomId -> {
                    activity.navigationView.setSelectedItemId(R.id.navigation_newpage);
                    if (activity.billManageFragment.getViewPager()==null)
                        activity.billManageFragment.setRoomIdToShow(roomId);
                    else
                        activity.billManageFragment.showViewPagerSelectRoom(roomId);
                });
                transaction.add(R.id.main_constraintLayout, activity.previewFragment).show(activity.previewFragment).commit();
                break;
            case R.id.navigation_analize:

                break;

            case R.id.navigation_backup:
                LitePal.deleteAll(RoomSet.class);
                LitePal.deleteAll(Room.class);
                LitePal.deleteAll(BillType.class);
                activity.mainFragment.setupTheRooms();
                break;
        }
        return true;
    };

    private void hideFragment(FragmentTransaction transaction) {
        if (mainFragment != null) transaction.hide(mainFragment);
        if (billManageFragment != null) transaction.hide(billManageFragment);
        if (previewFragment!=null) transaction.hide(previewFragment);
    }

    private FragmentManager fragmentManager;
    private MainFragment mainFragment;
    private BillManageFragment billManageFragment;
    private PreviewFragment previewFragment;


    @BindView(R.id.navigation)
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Util.setFullScreen(this);
        fragmentManager = getSupportFragmentManager();
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Util.disableShiftMode(navigationView);//使bottomnavigation不再变大缩小


        mainFragment = MainFragment.newInstance();
        mainFragment.setMainFragmentlistener(() -> {
            if (billManageFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(billManageFragment).commit();
                billManageFragment = null;
            }
        });
        fragmentManager.beginTransaction().add(R.id.main_constraintLayout, mainFragment).show(mainFragment).commit();
    }
}
