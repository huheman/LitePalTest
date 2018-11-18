package com.example.huhep.litepaltest.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseIntArray;

import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Room extends LitePalSupport implements Parcelable {
    private static final String TAG = "PengPeng";
    private long id;
    private long timeToLiveIn;
    private long timeToMoveOut;
    @Column(unique = true)
    private String roomNum;
    private String memoForRoom;
    private String username;
    private String tel;
    private int isOccupy;
    private long roomSetId;
    private double deposit;
    private int paidOnWechat;

    public boolean isPaidOnWechat() {
        return paidOnWechat == 1;
    }

    public void setPaidOnWechat(boolean paidOnWechat) {
        if (paidOnWechat)
            this.paidOnWechat = 1;
        else
            this.paidOnWechat = 2;
    }

    public void setRoomSet_id(long roomSet_id) {
        this.roomSetId = roomSet_id;
    }

    protected Room(Parcel in) {
        id = in.readLong();
        timeToLiveIn = in.readLong();
        timeToMoveOut = in.readLong();
        roomNum = in.readString();
        memoForRoom = in.readString();
        username = in.readString();
        tel = in.readString();
        isOccupy = in.readInt() ;
        roomSetId = in.readLong();
        deposit = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(timeToLiveIn);
        dest.writeLong(timeToMoveOut);
        dest.writeString(roomNum);
        dest.writeString(memoForRoom);
        dest.writeString(username);
        dest.writeString(tel);
        dest.writeInt(isOccupy);
        dest.writeLong(roomSetId);
        dest.writeDouble(deposit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public double getDeposit() {
        return deposit;
    }

    public long getTimeToLiveIn() {
        return timeToLiveIn;
    }

    public void setTimeToLiveIn(long timeToLiveIn) {
        this.timeToLiveIn = timeToLiveIn;
    }

    public long getTimeToMoveOut() {
        return timeToMoveOut;
    }

    public void setTimeToMoveOut(long timeToMoveOut) {
        this.timeToMoveOut = timeToMoveOut;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public boolean isOccupy() {
        return isOccupy==1;
    }

    public void setOccupy(boolean occupy) {
        if (occupy)
            isOccupy = 1;
        else
            isOccupy = 2;
    }

    public long getRoomSet_id() {
        return roomSetId;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public String getMemoForRoom() {
        return memoForRoom;
    }

    public Charge getLastCharge() {
        List<Charge> charges = LitePal.where("roomId=?", String.valueOf(id)).find(Charge.class);
        if (charges.size()==0){
            Log.d(TAG, roomNum+"getLastCharge: is null");
            return null;
        }
        Log.d(TAG, "getLastCharge size: "+charges.size());
        Charge charge = charges.get(0);
        for (Charge charge1:charges)
            if (charge1.getCreateDate()>charge.getCreateDate())
                charge = charge1;
        return charge;
    }

    public String getDetail() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(username))
            sb.append("住户名:").append(username).append("   ");
        if (!TextUtils.isEmpty(tel))
            sb.append("电话：").append(tel);

        Charge lastCharge = getLastCharge();
        if (lastCharge != null) {
            List<Bill> billLasCheckOut = LitePal.where("charge_Id=?", String.valueOf(lastCharge.getId())).find(Bill.class);
            for (Bill bill : billLasCheckOut) {
                if (sb.length() > 0) sb.append("\n");
                BillType billType = bill.getbillType();
                sb.append(billType.getBillTypeName() + ":")
                        .append("\n" + bill.getDuration())
                        .append("\n" + bill.getDetail())
                        .append("\n");
            }
            if (billLasCheckOut.size()>0)
                sb.append("\n"+"共计: " + Util.getTotalHowMuchOfBillList(billLasCheckOut)+" 元\n");
        }

        if (sb.length() > 0) sb.append("\n");
        if (isOccupy()) {
            sb.append("入住日期：").append(Util.getDate(timeToLiveIn, "yyyy年MM月dd日")).append("，");
            sb.append("共住了").append(getDurationOfLiveIn());
        } else {
            sb.append("空置日期：").append(Util.getDate(timeToMoveOut, "yyyy年MM月dd日")).append("，");
            sb.append("已空置了").append(getDurationOfEmpty());
        }
        return sb.toString();
    }

    private String getDurationOfEmpty() {
        SparseIntArray duration = Util.duration(System.currentTimeMillis(), timeToMoveOut);
        return Util.formationDuration(duration);
    }

    private String getDurationOfLiveIn() {
        SparseIntArray duration = Util.duration(System.currentTimeMillis(), timeToLiveIn);
        return Util.formationDuration(duration);
    }

    public void setMemoForRoom(String memoForRoom) {
        this.memoForRoom = memoForRoom;
    }

    public Room(String roomNum) {
        this.roomNum = roomNum;
    }

    public List<BillType> getBillTypeList() {
        List<BillType> billTypeList = LitePal.where("belongTo=?", String.valueOf(id)).find(BillType.class);
        List<BillType> publicBillTypeList = LitePal.where("belongTo=?", "-1").find(BillType.class);
        for (BillType publicBillType : publicBillTypeList) {
            if (!publicBillType.isOneOf(billTypeList)) {
                billTypeList.add(publicBillType);
            }
        }
        return billTypeList;
    }

    public List<BillType> getCheckedBillTypeList() {
        List<BillType> billTypeList = getBillTypeList();
        Iterator<BillType> iterator = billTypeList.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isChecked()) {
                iterator.remove();
            }
        }
        return billTypeList;
    }
}
