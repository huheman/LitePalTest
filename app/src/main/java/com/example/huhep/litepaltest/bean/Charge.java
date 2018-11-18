package com.example.huhep.litepaltest.bean;

import android.support.annotation.NonNull;

import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Charge extends LitePalSupport {
    private long id;
    private long createDate;
    private long roomId;
    private List<Bill> billList;
    private String passWord;

    public String getPassWord() {
        return passWord;
    }


    public long getCreateDate() {
        return createDate;
    }

    public Charge() {
        this.createDate = System.currentTimeMillis();
        this.passWord = UUID.randomUUID().toString().substring(0, 6);
    }

    public String getCreateDateToString() {
        return Util.getWhen(createDate);
    }

    public List<Bill> getBillList() {
        if (billList == null) {
            billList = LitePal.where("charge_Id=?",String.valueOf(id)).find(Bill.class);
        }
        return billList;
    }

    public void addBill(@NonNull Bill bill) {
        getBillList().add(bill);
    }

    public Charge(@NonNull Room room) {
        this();
        roomId = room.getId();
    }

    public long getId() {
        return id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
