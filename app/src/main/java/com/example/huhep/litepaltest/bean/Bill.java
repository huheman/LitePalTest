package com.example.huhep.litepaltest.bean;

import android.text.TextUtils;
import android.util.Log;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.fragments.CreateBillFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

public class Bill extends LitePalSupport {
    private long id;
    private long roomId;
    private long fromDate;
    private long toDate;
    private String memo;
    private long billTypeId;
    private double fromDegree;
    private double toDegree;
    private BillType billType;
    private long charge_Id;
    private Bill lastBill;
    public static final int BILL_ALL_OK = 0;
    public static final int BILL_NOTREADY_RENT = 5;
    public static final int BILL_SET_BASEDEGREE = 10;
    public static final int BILL_NOT_DEFINE = 20;
    public static final int BILL_NOT_INIT = 30;
    public static final int BILL_TOO_MUCH = 40;
    public static final int BILL_ERROR = 50;


    public long getBillType_id() {
        return billTypeId;
    }

    public void setBillType_id(long billType_id) {
        this.billTypeId = billType_id;
        billType = LitePal.find(BillType.class, billType_id);
    }

    public double getFromDegree() {
        return fromDegree;
    }

    public void setFromDegree(double fromDegree) {
        this.fromDegree = fromDegree;
    }

    public double getToDegree() {
        return toDegree;
    }

    public void setToDegree(double toDegree) {
        this.toDegree = toDegree;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public long getRoom_id() {
        return roomId;
    }

    public void setRoom_id(long room_id) {
        this.roomId = room_id;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        if (!getbillType().isChargeOnDegree()) return BILL_ALL_OK;
        if (fromDegree == 0 && toDegree != 0) return BILL_SET_BASEDEGREE; //描述是billtype底被设为toDegree
        if ((fromDegree != 0 && toDegree == 0) || (fromDegree != 0 && fromDegree == toDegree))
            return BILL_NOT_DEFINE;      //没抄，写这个月没收X费
        if (toDegree < fromDegree && !isLoop() || getbillType().getLoopThreshold() != 0 && (int) toDegree > getbillType().getLoopThreshold())
            return BILL_ERROR;      //出错，红色表明错误要求检查
        if (fromDegree == 0 && toDegree == 0) return BILL_NOT_INIT;
        if (CreateBillFragment.tooMuch(getLastBill(), toDegree, fromDegree))
            return BILL_TOO_MUCH;    //相差太多
        if (fromDegree != 0 && toDegree != 0) return BILL_ALL_OK;   //正常
        return 5;   //不知道是啥错误
    }

    public Bill() {

    }

    public Bill(Room room, BillType billType) {
        roomId = room.getId();
        billTypeId = billType.getId();
        this.billType = billType;
        createDate(room, billType);
        if (billType.isChargeOnDegree()) {
            String fromDegreeStr = BaseActivity.getSP().getString(Util.getSPName(room, billType) + "_pre", "");
            if (fromDegreeStr.isEmpty()) fromDegree = 0;
            else fromDegree = Double.parseDouble(fromDegreeStr);
            String toDegreeStr = BaseActivity.getSP().getString(Util.getSPName(room, billType), "");
            if (toDegreeStr.isEmpty()) {
                if (fromDegreeStr.isEmpty())
                    toDegree = 0;
                else
                    toDegree = fromDegree;
            } else toDegree = Double.parseDouble(toDegreeStr);
        }
    }

    public long getCharge_Id() {
        return charge_Id;
    }

    private void createDate(Room room, BillType billType) {
        Bill lastBill = getLastBill();
        Charge lastCharge = null;
        if (lastBill != null)
            lastCharge = LitePal.find(Charge.class, lastBill.getCharge_Id());
        if (!billType.isChargeOnDegree()) {
            //按月收费无论什么情况都是以现在为开始，下个月为结束
            fromDate = System.currentTimeMillis();
            String nextMonty = Util.getNextMonty(System.currentTimeMillis());
            toDate = Util.getMillionsFromString(nextMonty);
        } else {
            if (lastBill==null) fromDate = 0;
            else if (lastCharge!=null && lastCharge.getCreateDateToString().equalsIgnoreCase(Util.getWhen()))
                fromDate = lastBill.getFromDate();
            else fromDate = lastBill.getToDate();

            toDate = System.currentTimeMillis();
        }
    }

    public double howMuch() {
        BillType billType = getbillType();
        if (billType.isChargeOnDegree()) {
            return howMuchDegree() * billType.getPriceEachDegree();
        } else {
            return Util.howManyMonth(toDate, fromDate) * billType.getRentPrice();
        }
    }

    public BillType getbillType() {
        if (billType == null) {
            billType = LitePal.find(BillType.class, billTypeId);
        } else if (billType.getBillTypeName() == null) {
            billType = LitePal.find(BillType.class, billTypeId);
        }
        return billType;
    }

    public String getDetailForImage() {
        if (getType()==BILL_NOT_DEFINE)
            return getTips();
        else if (getType() == BILL_TOO_MUCH) {
            return getNormalDetail();
        }
            return getDetail();
    }



    public String getDetail() {
        BillType billType = getbillType();
        if (billType.isChargeOnDegree()) {
            switch (getType()) {
                case BILL_ALL_OK:
                    return getNormalDetail();
                case BILL_SET_BASEDEGREE:
                    return getTips();
                case BILL_NOT_DEFINE:
                    return getTips();
                case BILL_ERROR:
                    return getTips();
                case BILL_TOO_MUCH:
                    String normalDetail = getNormalDetail();
                    return normalDetail + "\n"+getTips();
                case BILL_NOT_INIT:
                    return getTips();
                default:
                    return "未知错误";
            }
        } else {
            return  billType.getBillTypeName() + ": " + howMuch() + " 元";
        }
    }

    public String getTips() {
        BillType billType = getbillType();
        String toDegreeFormat = String.format("%.0f", toDegree);
        String fromDegreeFormat = String.format("%.0f", fromDegree);
        switch (getType()) {
            case BILL_ALL_OK:
                return "";
            case BILL_SET_BASEDEGREE:
                return "把" + billType.getBillTypeName() + "的底设置为" + toDegreeFormat;
            case BILL_NOT_DEFINE:
                return "这个月没收" + billType.getBillTypeName() + "\n读数依然为" + fromDegree;
            case BILL_ERROR:
                return "本月读数" + toDegreeFormat + "小于上月读数" + fromDegreeFormat + ",\n请返回修改！";
            case BILL_TOO_MUCH:
                return "上次数据是" + getLastBill().howMuchDegree() + "度，\n差距很大，请仔细确认";
            case BILL_NOT_INIT:
                return "还没设置读数，\n请前往“新建”标签设置读数";
            default:
                return "未知错误";
        }
    }

    public Bill getLastBill() {
        if (lastBill == null) {
            Room room = LitePal.find(Room.class, roomId);
            lastBill = Util.getLastBillOf(room, getbillType());
        }
        return lastBill;
    }

    public String getDuration() {
        String from = "未知时间";
        if (fromDate != 0) {
            if (!getbillType().isChargeOnDegree())
                from = Util.getWhen(fromDate);
            else
                from = Util.getWhenAccurately(fromDate);
        }
        if (!getbillType().isChargeOnDegree())
            return from + " ~ " + Util.getWhen(toDate);
        else
            return from + "~" + Util.getWhenAccurately(toDate);
    }

    public double howMuchDegree() {
        if (isLoop()) return getbillType().getLoopThreshold() + 1 - fromDegree + toDegree;
        else return toDegree - fromDegree;
    }

    public boolean isLoop() {
        BillType billType = getbillType();
        if (billType == null) return false;
        int loopThreshold = billType.getLoopThreshold();
        if (loopThreshold == 0) return false;
        if (toDegree < fromDegree && toDegree < 0.2 * loopThreshold && fromDegree > 0.8 * loopThreshold)
            return true;
        return false;
    }

    public String getNormalDetail() {
        BillType billType = getbillType();
        String toDegreeFormat = String.format("%.0f", toDegree);
        String fromDegreeFormat = String.format("%.0f", fromDegree);
        String howMuchDegreeFormat = String.format("%.0f", howMuchDegree());
        String howMuchFormat = String.format("%.2f", howMuch());
        String priceEachDegreeFormat = String.format("%.2f", billType.getPriceEachDegree());
        String showDegree;
        if (!isLoop()) {
            showDegree = toDegreeFormat + " - " + fromDegreeFormat + " = " + howMuchDegreeFormat + " 度";
        } else {
            showDegree = (billType.getLoopThreshold() + 1) + " - " + fromDegreeFormat + " + " + toDegreeFormat + " = " + howMuchDegreeFormat + " 度";
        }
        return "数量: " + showDegree + "\n" + "金额: " + howMuchDegreeFormat + " * " + priceEachDegreeFormat + " = " + howMuchFormat + " 元";
    }
}
