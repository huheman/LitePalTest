package com.example.huhep.litepaltest.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Charge extends LitePalSupport {
    private long id;
    private long createDate;
    private long roomId;
    private List<Bill> billList;
    private String passWord;
    private String describe;
    private int paidOnWechat;

    public String getPassWord() {
        return passWord;
    }


    public long getCreateDate() {
        return createDate;
    }

    public boolean haspainOnWechat(){
        return paidOnWechat == 1;
    }

    public Charge setPaidOnWechat(boolean hasPaidOnWechat){
        if (hasPaidOnWechat)
            paidOnWechat = 1;
        else
            paidOnWechat = 2;
        return this;
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

    public List<Bill> getUsedBillList() {
        List<Bill> usedBillList=new ArrayList<>();
        Room room = LitePal.find(Room.class, getRoomId());
        List<BillType> checkedBillTypeList = room.getCheckedBillTypeList();
        List<Bill> billList = getBillList();
        for (Bill bill : billList) {
            for (BillType billType : checkedBillTypeList) {
                if (bill.getBillType_id()==billType.getId())
                    usedBillList.add(bill);
            }
        }
        return usedBillList;
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

    public boolean containBill(Bill bill) {
        return getSameBill(bill)!=null;
    }

    public Bill getSameBill(Bill bill) {
        List<Bill> billList = getBillList();
        for (Bill localBill : billList) {
            if (localBill.getBillType_id()==bill.getBillType_id())
                return localBill;
        }
        return null;
    }

    public void ceateDescrib() {
        StringBuilder sb=new StringBuilder();
        List<Bill> usedBillList = getUsedBillList();
        for (Bill bill : usedBillList) {
            int type = bill.getType();
            if (type!=Bill.BILL_ALL_OK  &&
                    type!=Bill.BILL_TOO_MUCH &&
                    type!=Bill.BILL_SET_BASEDEGREE &&
                    type!=Bill.BILL_NOT_DEFINE)
                continue;
            if (sb.length() > 0) sb.append("\n");
            BillType billType = bill.getbillType();
            sb.append(billType.getBillTypeName() + ":")
                    .append("\n" + bill.getDuration())
                    .append("\n" + bill.getDetail())
                    .append("\n");
        }
        if (usedBillList.size()>0)
            sb.append("\n"+"共计: " + Util.getTotalHowMuchOfBillList(usedBillList)+" 元\n");
        this.describe = sb.toString();
    }

    public String getDescribe() {
        if (TextUtils.isEmpty(describe))
            ceateDescrib();
        return describe;
    }
}
