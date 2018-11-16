package com.example.huhep.litepaltest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.fragments.MainFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewRoomActivity extends BaseActivity {
    @BindView(R.id.newroom_toolBar)
    CustomToolbar toolbar;

    @BindView(R.id.newroom_roomsetspinner)
    Spinner roomSetSpinner;

    @BindView(R.id.newroom_roomnumber)
    CurrentMessageCollector roomnumber;

    @BindView(R.id.newroom_deposit)
    CurrentMessageCollector deposit;

    @BindView(R.id.newroom_username)
    CurrentMessageCollector username;

    @BindView(R.id.newroom_tel)
    CurrentMessageCollector tel;

    @BindView(R.id.newroom_roomsetbutton)
    Button roomSetButton;

    @BindView(R.id.newroom_newRoomButton)
    Button newRoomButton;

    @BindView(R.id.newroom_isoccupycheckbox)
    CheckBox isOccupyCheckBox;

    private EditText editTextForCreateDialog=null;
    public static final String USERNAME = "username";
    public static final String DEPOSIT = "deposit";
    public static final String ROOOMNUMBER = "roomnumber";
    public static final String PHONENUMBER = "tel";
    public static final String ROOMSETNAME = "roomsetname";
    public static final String OCCUPATION = "occupation";
    private String userNameEditText="";
    private String depositEditText = "200";
    private String roomNumberEditText = "";
    private String phoneNumberEditText = "";
    private String roomSetNameText = "";
    private boolean isOccupy = true;
    private boolean spinnerIsNull = false;
    long roomId;
    private static final String TAG = "PengPeng";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);
        ButterKnife.bind(this);
        Util.setFullScreen(this);
        getDataFromIntent();
        setlisteners();
        setupToolbar();
        setupSpinner();
        setupRoomNumber();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        roomId = intent.getLongExtra(BaseActivity.ROOM_ID, -1);
        if (roomId!=-1) {
            Room room = LitePal.find(Room.class, roomId);
            userNameEditText = room.getUsername();
            depositEditText = String.valueOf(room.getDeposit());
            roomNumberEditText = room.getRoomNum();
            phoneNumberEditText = room.getTel();
            roomSetNameText = Util.getRoomSetFromId(room.getRoomSet_id()).getRoomSetName();
            isOccupy = room.isOccupy();
        }
    }

    private void setlisteners() {
        roomSetSpinner.setOnLongClickListener(v -> {
            View view = LayoutInflater.from(BaseActivity.getContext()).inflate(R.layout.alerdialogconten, null,false);
            EditText editTextForRevisionDialog = view.findViewById(R.id.newroomset_alertdialog_edittext);
            String selectedItem = (String) roomSetSpinner.getSelectedItem();
            AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.getContext())
                    .setTitle("请问要把 " + selectedItem + " 修改为什么名:")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        String roomSetName = editTextForRevisionDialog.getText().toString();
                        RoomSet roomSetToUpdate = LitePal.where("roomSetName=?", selectedItem).find(RoomSet.class).get(0);
                        roomSetToUpdate.setRoomSetName(roomSetName);
                        if (roomSetToUpdate.save()) {
                            roomSetNameText = roomSetName;
                            BaseActivity.show("修改成功");
                            setupSpinner();
                        } else {
                            BaseActivity.show("修改失败，可能是有同名的组了");
                        }
                    }).setView(view)
                    .create();
            alertDialog.show();
            return true;
        });

        roomSetButton.setOnLongClickListener(v -> {
            String selectedItem = (String) roomSetSpinner.getSelectedItem();
            new AlertDialog.Builder(BaseActivity.getContext()).setTitle("是否真的要删除组别" + selectedItem + "?")
                    .setMessage("这会连同组下所有房间一起被删除")
                    .setPositiveButton("确定", (dialog, which) -> {
                RoomSet roomSet = LitePal.where("roomSetName=?", selectedItem).find(RoomSet.class).get(0);
                roomSet.delete();
                LitePal.deleteAll(Room.class, "roomSetId=?", String.valueOf(roomSet.getId()));
                BaseActivity.show("已经删除了组别" + roomSet.getRoomSetName());
                setupSpinner();
            }).setNegativeButton("取消", (dialog, which) -> BaseActivity.show("取消删除")).show();
            return true;
        });
        roomnumber.setListener(new MessageCollector.MessageCollectorListener() {
            @Override
            public void onEditing(MessageCollector messageCollector) {

            }

            @Override
            public void afterEditTextInputChanged(MessageCollector messageCollector,String s) {
                if (roomnumber.getEditText().getText().length() == 0||spinnerIsNull) {
                    newRoomButton.setEnabled(false);
                } else {
                    newRoomButton.setEnabled(true);
                }
            }

            @Override
            public void onFinishEditing(MessageCollector messageCollector) {
            }

            @Override
            public void onGetFocus(MessageCollector messageCollector) {

            }

            @Override
            public void onLoseFocus(MessageCollector messageCollector) {

            }
        });
    }

    private void setupRoomNumber() {
        roomnumber.setHint("房间号不能为空");
        final EditText editText = roomnumber.getEditText();
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(roomNumberEditText);
        newRoomButton.setEnabled(false);
        roomnumber.setTipsDrawable(android.R.color.transparent);

        deposit.getTextView().setText("押金");
        deposit.getEditText().setText(depositEditText);
        deposit.setTipsDrawable(android.R.color.transparent);

        username.getTextView().setText("住户名");
        username.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
        username.setTipsDrawable(android.R.color.transparent);
        username.getEditText().setText(userNameEditText);

        tel.getTextView().setText("联系电话");
        tel.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
        tel.setTipsDrawable(android.R.color.transparent);
        tel.getEditText().setText(phoneNumberEditText);

        isOccupyCheckBox.setChecked(isOccupy);
    }

    private void setupToolbar() {
        toolbar.setTitle("新增房间");
        toolbar.getToolbar().setNavigationIcon(R.drawable.ic_back);
        toolbar.getToolbar().setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        List<RoomSet> roomSets = LitePal.findAll(RoomSet.class);
        List<String> roomSetNames = new ArrayList<>();
        for (RoomSet roomSet : roomSets) {
            roomSetNames.add(roomSet.getRoomSetName());
        }
        if (roomSetNames.size() == 0) {
            roomSetNames.add("请新建一个组别");
            spinnerIsNull = true;
            newRoomButton.setEnabled(false);
        } else {
            spinnerIsNull = false;
            if (roomnumber.getEditText().getText().length()>0)
                newRoomButton.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomSetNames);
        roomSetSpinner.setAdapter(adapter);
        int i = roomSetNames.indexOf(roomSetNameText);
        if (i<0){
            i = roomSetNames.indexOf(getIntent().getStringExtra(MainFragment.PREROMESETNAME));
        }
        if (i<0) i = 0;
        roomSetSpinner.setSelection(i);

    }

    @OnClick(R.id.newroom_newRoomButton)
    public void newButtonClicked(View view) {
        Room room = LitePal.find(Room.class, roomId);
        if (room==null) room = new Room(roomnumber.getEditText().getText().toString());
        else room.setRoomNum(roomnumber.getEditText().getText().toString());
        room.setUsername(username.getEditText().getText().toString());
        room.setDeposit(Double.valueOf(deposit.getEditText().getText().toString()));
        room.setTel(tel.getEditText().getText().toString());
        long roomSetId = LitePal.where("roomSetName=?", roomSetSpinner.getSelectedItem().toString())
                .find(RoomSet.class).get(0).getId();
        room.setRoomSet_id(roomSetId);
        room.setOccupy(isOccupyCheckBox.isChecked());
        if (room.isOccupy())    room.setTimeToLiveIn(System.currentTimeMillis());
        else room.setTimeToMoveOut(System.currentTimeMillis());

        if (room.save()) {
            Log.d(TAG, "newButtonClicked: ");
            BaseActivity.show("房间创建/修改成功");
        } else {
            BaseActivity.show("房间创建失败，可能是有同名房间");
        }
        Intent intent = getIntent();
        intent.putExtra(MainFragment.PREROMESETNAME, roomSetSpinner.getSelectedItem().toString());
        setResult(BaseActivity.RESULT_FROM_NEWROOM_TO_MAINFRAGMENT, intent);
        finish();
    }

    @OnClick(R.id.newroom_roomsetbutton)
    public void roomsetButtonClicked(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog alertDialog = builder.setTitle("新建组别")
                .setPositiveButton("确定", (dialog, which) -> {
                    String roomSetName = editTextForCreateDialog.getText().toString();
                    RoomSet roomSet = new RoomSet(roomSetName);
                    if (roomSet.save()) {
                        BaseActivity.show("成功新建了分组:" + roomSetName);
                        roomSetNameText = roomSetName;
                        setupSpinner();
                    } else {
                        BaseActivity.show("已经有这个分组了");
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> BaseActivity.show("取消了新建分组")).setCancelable(false).create();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alerdialogconten, findViewById(R.id.dialog));
        alertDialog.setView(view);
        editTextForCreateDialog = view.findViewById(R.id.newroomset_alertdialog_edittext);
        alertDialog.show();
    }
}
