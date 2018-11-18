package com.example.huhep.litepaltest.bean;

import android.util.Log;

import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

//收费名称，价钱，收费类型等
public  class BillType extends LitePalSupport {
    private long id;
    private double rentPrice;
    private double priceEachDegree;
    private int isChargeOnDegree;
    private List<Bill> billList=new ArrayList<>();
    private String billTypeName;
    private long belongTo;
    private int isChecked;
    private String memoForBillType="";
    private int loopThreshold;

    public int getLoopThreshold() {
        return loopThreshold;
    }

    public void setLoopThreshold(int loopThreshold) {
        writeInMemo("把最大表值设为"+loopThreshold);
        this.loopThreshold = loopThreshold;
    }

    public BillType(BillType currentBillType) {
        Room room = LitePal.find(Room.class, currentBillType.getBelongTo());
        String from = "公用的";
        if (room!=null) from = room.getRoomNum();
        writeInMemo("从" + from + "的" + currentBillType.getBillTypeName() + "新建而来");
        rentPrice = currentBillType.rentPrice;
        priceEachDegree = currentBillType.priceEachDegree;
        isChargeOnDegree = currentBillType.isChargeOnDegree;
        billList = currentBillType.billList;
        billTypeName = currentBillType.billTypeName;
        isChecked = currentBillType.isChecked;
        loopThreshold = currentBillType.loopThreshold;
    }

    public boolean isChargeOnDegree() {
        return isChargeOnDegree==1;
    }

    public void setChargeOnDegree(boolean chargeOnDegree) {
        if (isChargeOnDegree()==chargeOnDegree) return;
        if (chargeOnDegree){
            isChargeOnDegree = 1;
            writeInMemo("被设为按度数收费类型");
        }

        else {
            isChargeOnDegree = 2;
            writeInMemo("被设为按月收费类型");
        }
    }

    public List<Bill> getBillList() {
        return billList;
    }

    public void setBillList(List<Bill> billList) {
        this.billList = billList;
    }

    public void addBill(Bill rentBill) {
        billList.add(rentBill);
    }

    public double getPriceEachDegree() {
        return priceEachDegree;
    }

    public void setPriceEachDegree(double priceEachDegree) {
        if (this.priceEachDegree==priceEachDegree) return;

        writeInMemo("每度收费被设为"+priceEachDegree);
        this.priceEachDegree = priceEachDegree;
    }

    public BillType() {
        writeInMemo("新建类型");
        belongTo = -1;
        isChecked = 1;

    }
    public double getRentPrice() {
        return rentPrice;
    }

    public void setRentPrice(double rentPrice) {
        if (this.rentPrice==rentPrice) return;
        writeInMemo("每月费用设为"+rentPrice);
        this.rentPrice = rentPrice;
    }

    public String getMemoForBillType() {
        return memoForBillType;
    }
    public void setBelongTo(long belongTo) {
        if (this.belongTo==belongTo) return;
        String roomNumber;
        if (belongTo == -1) {
            roomNumber = "公共";
            setToDefault("belongTo");
        } else {
            this.belongTo = belongTo;
            roomNumber = LitePal.find(Room.class, belongTo).getRoomNum();
        }
        writeInMemo("被设为"+roomNumber+"所有");
    }

    public boolean isChecked() {
        return isChecked==1;
    }

    public boolean isPublic() {
        return belongTo==-1;
    }

    public void setChecked(boolean checked) {
        if (isChecked()==checked) return;
        if (checked){
            isChecked = 1;
            writeInMemo("被设为启用状态");
        }
        else{
            isChecked = 2;
            writeInMemo("被设为禁用状态");
        }
    }

    private void writeInMemo(String string) {
        memoForBillType += Util.getDate() + string+"\n";
    }

    public long getBelongTo() {
        return belongTo;
    }

    public String getBillTypeName() {
        return billTypeName;
    }

    public void setBillTypeName(String billTypeName) {
        if (this.billTypeName!=null && this.billTypeName.equals(billTypeName)) return;

        writeInMemo("把类型名改为"+billTypeName);
        this.billTypeName = billTypeName;
    }

    public long getId() {
        return id;
    }

    public boolean isOneOf(List<BillType> billTypeList) {
        for (BillType billType : billTypeList) {
            if (billTypeName.equals(billType.getBillTypeName())) {
                return true;
            }
        }
        return false;
    }

    private String getRoomName() {
        return LitePal.find(Room.class, getBelongTo()).getRoomNum();
    }

    protected String getMoreDetail() {
        if (isPublic()) {
            return "是公用的";
        } else {
            return "由" + getRoomName() + "专用";
        }
    }

    public void setPublic() {
        setBelongTo(-1);
    }

    public String getSubscrip() {
        String subscrip;
        if (isChargeOnDegree()) {
            subscrip = priceEachDegree + "元/度,";
            if (loopThreshold!=0)
                subscrip += "最大读数是" + loopThreshold+"度,";
        }else
            subscrip = rentPrice + "元/月，";
        return  subscrip+ getMoreDetail();
    }
}
