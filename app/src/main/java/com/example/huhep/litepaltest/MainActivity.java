package com.example.huhep.litepaltest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.fragments.BillManageFragment;
import com.example.huhep.litepaltest.fragments.MainFragment;
import com.example.huhep.litepaltest.fragments.PreviewFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    private static final String TAG = "PengPeng";
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        MainActivity activity = MainActivity.this;
        FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
        hideFragment(transaction);
        switch (item.getItemId()) {
            case R.id.navigation_home:
                transaction.show(activity.mainFragment).commit();
                break;
            case R.id.navigation_newpage:
                if (activity.billManageFragment == null) {
                    activity.billManageFragment = new BillManageFragment();
                    transaction.add(R.id.main_constraintLayout, activity.billManageFragment);
                }
                transaction.show(activity.billManageFragment).commit();
                break;
            case R.id.navigation_output:
                //每次点进来都应该重新生成
                if (activity.previewFragment == null) {
                    activity.previewFragment = new PreviewFragment();
                    activity.previewFragment.setOnViewHolderClickedListener(activity::reflashBillManageFragment);
                    activity.previewFragment.setOnBillsSavedListener(() -> {
                        activity.navigationView.setSelectedItemId(R.id.navigation_home);
                        activity.mainFragment.setupTheRooms();
                        if (activity.billManageFragment != null) {
                            activity.billManageFragment.setupView();
                            activity.billManageFragment.setupToolBar();
                        }
                    });
                    transaction.add(R.id.main_constraintLayout, activity.previewFragment);
                } else {
                    activity.previewFragment.setupBillList();
                    activity.previewFragment.setupView();
                }
                transaction.show(activity.previewFragment).commit();
                break;
            case R.id.navigation_analize:
                break;

            case R.id.navigation_backup:
                LitePal.deleteAll(Charge.class);
                LitePal.deleteAll(Bill.class);
                LitePal.deleteAll(RoomSet.class);
                LitePal.deleteAll(BillType.class);
                LitePal.deleteAll(Room.class);
                activity.mainFragment.setupTheRooms();
                break;
        }
        return true;
    };

    private void hideFragment(FragmentTransaction transaction) {
        if (mainFragment != null) transaction.hide(mainFragment);
        if (billManageFragment != null) transaction.hide(billManageFragment);
        if (previewFragment != null) transaction.hide(previewFragment);
    }

    private FragmentManager fragmentManager;
    private MainFragment mainFragment;
    private BillManageFragment billManageFragment;
    private PreviewFragment previewFragment;
    @BindView(R.id.navigation)
    BottomNavigationView navigationView;

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }
    public BillManageFragment getBillManageFragment() {
        return billManageFragment;
    }
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
                billManageFragment.setupView();
                billManageFragment.setupToolBar();
            }
        });
        fragmentManager.beginTransaction().add(R.id.main_constraintLayout, mainFragment).show(mainFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbIF = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void reflashBillManageFragment(long roomId) {
        navigationView.setSelectedItemId(R.id.navigation_newpage);
        if (billManageFragment.getViewPager() == null)
            billManageFragment.setRoomIdToShow(roomId);
        else
            billManageFragment.showViewPagerSelectRoom(roomId);
    }
}
