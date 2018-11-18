package com.example.huhep.litepaltest.bean;

import android.support.annotation.NonNull;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class Charge extends LitePalSupport {
    private long id;
    private long createDate;
    private long roomId;
    private List<Bill> billList;


    public long getCreateDate() {
        return createDate;
    }

    public Charge() {
        this.createDate = System.currentTimeMillis();
    }

    public List<Bill> getBillList() {
        if (billList == null) {
            billList = LitePal.where("charge_Id=?",String.valueOf(id)).find(Bill.class);
        }
        return billList;
    }

    public void addBill(@NonNull Bill bill) {
        if (billList==null) billList = new ArrayList<>();
        billList.add(bill);
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
