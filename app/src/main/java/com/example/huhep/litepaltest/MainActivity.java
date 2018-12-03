package com.example.huhep.litepaltest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.fragments.AnalyzeManagmentFragment;
import com.example.huhep.litepaltest.fragments.BillManageFragment;
import com.example.huhep.litepaltest.fragments.MainFragment;
import com.example.huhep.litepaltest.fragments.PreviewFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
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
                }
                transaction.show(activity.previewFragment).commit();
                break;
            case R.id.navigation_analize:
                if (activity.analyzeManagmentFragment == null) {
                    activity.analyzeManagmentFragment = new AnalyzeManagmentFragment();
                    transaction.add(R.id.main_constraintLayout, activity.analyzeManagmentFragment);
                }
                transaction.show(activity.analyzeManagmentFragment).commit();
                break;

            case R.id.navigation_backup:
                /*LitePal.deleteAll(Charge.class);
                LitePal.deleteAll(Bill.class);
                LitePal.deleteAll(RoomSet.class);
                LitePal.deleteAll(BillType.class);
                LitePal.deleteAll(Room.class);
                activity.mainFragment.setupTheRooms();*/
                break;
        }
        return true;
    };

    private void hideFragment(FragmentTransaction transaction) {
        if (mainFragment != null) transaction.hide(mainFragment);
        if (billManageFragment != null) transaction.hide(billManageFragment);
        if (previewFragment != null) transaction.hide(previewFragment);
        if (analyzeManagmentFragment != null) transaction.hide(analyzeManagmentFragment);
    }

    private FragmentManager fragmentManager;
    private MainFragment mainFragment;
    private BillManageFragment billManageFragment;
    private PreviewFragment previewFragment;
    private AnalyzeManagmentFragment analyzeManagmentFragment;
    @BindView(R.id.navigation)
    BottomNavigationView navigationView;

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    public BillManageFragment getBillManageFragment() {
        return billManageFragment;
    }

    public PreviewFragment getPreviewFragment() {
        return previewFragment;
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

    public void reflashBillManageFragment(long roomId) {
        navigationView.setSelectedItemId(R.id.navigation_newpage);
        billManageFragment.showViewPagerSelectRoom(roomId);
    }

    public void refreshAnalyzeFragment(int roomPos) {
        navigationView.setSelectedItemId(R.id.navigation_analize);
        if (analyzeManagmentFragment.getViewPager() != null) {
            analyzeManagmentFragment.setupView();
        }
        analyzeManagmentFragment.showRoomAndCharge(roomPos);
    }

    public void findCharge() {
        EditText editText = new EditText(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.leftMargin = 50;
        layoutParams.rightMargin = 50;
        editText.setLayoutParams(layoutParams);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER);
        new AlertDialog.Builder(getContext()).setTitle("请输入房间名或账单密码：")
                .setView(editText)
                .setPositiveButton("查找", (dialog, which) -> {
                    if (editText.getText().length() == 0) return;
                    String s = editText.getText().toString();
                    int roomPos = findRoomPos(s);
                    if (roomPos != -1) {
                        refreshAnalyzeFragment(roomPos);
                        return;
                    }
                    List<Charge> chargeList = LitePal.select("image").where("passWord=?",s).find(Charge.class);
                    if (chargeList.size() > 0) {
                        Charge charge = chargeList.get(0);
                        Bitmap bitmap = BitmapFactory.decodeFile(charge.getImage());
                        ImageView imageView = new ImageView(getContext());
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1,-2));
                        new AlertDialog.Builder(getContext()).setView(imageView).show();
                        return;
                    }
                    BaseActivity.show("没找到相关数据");
                }).setNegativeButton("取消", null).show();
    }

    public int findRoomPos(String s) {
        List<Room> roomList = LitePal.select("roomNum").find(Room.class);
        int roomPos = -1;
        for (int i = 0; i < roomList.size(); i++) {
            if (s.equals(roomList.get(i).getRoomNum())) {
                roomPos = i + 1;
                break;
            }
        }
        return roomPos;
    }
}
