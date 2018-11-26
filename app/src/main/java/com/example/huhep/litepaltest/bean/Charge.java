package com.example.huhep.litepaltest.bean;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.huhep.litepaltest.components.ItemComponents;
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
    private int chargeType;
    private Room room;
    private List<Bill> usedBillList;
    private int state;

    public static final int TYPE_MOVE_OUT = 10;
    public static final int TYPE_LIVE_IN = 20;

    public String getPassWord() {
        return passWord;
    }


    public long getCreateDate() {
        return createDate;
    }

    public boolean haspainOnWechat() {
        return paidOnWechat == 1;
    }

    public Charge setPaidOnWechat(boolean hasPaidOnWechat) {
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
            billList = LitePal.where("charge_Id=?", String.valueOf(id)).find(Bill.class);
        }
        return billList;
    }

    public List<Bill> getUsedBillList() {
        if (usedBillList == null) {
            usedBillList = new ArrayList<>();
            Room room = getRoom();
            List<BillType> checkedBillTypeList = room.getCheckedBillTypeList();
            List<Bill> billList = getBillList();
            for (Bill bill : billList) {
                for (BillType billType : checkedBillTypeList) {
                    if (bill.getBillType_id() == billType.getId())
                        usedBillList.add(bill);
                }
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
        return getSameBill(bill) != null;
    }

    public Bill getSameBill(Bill bill) {
        List<Bill> billList = getBillList();
        for (Bill localBill : billList) {
            if (localBill.getBillType_id() == bill.getBillType_id())
                return localBill;
        }
        return null;
    }

    public Room getRoom() {
        if (room == null)
            room = LitePal.find(Room.class, roomId);
        return room;
    }

    public void createDescrib() {
        StringBuilder sb = new StringBuilder();
        Room room = getRoom();
        List<Bill> usedBillList = getUsedBillList();
        if (getChargeType() == TYPE_MOVE_OUT) {
            sb.append("退还押金:\n" + room.getDeposit() + "\n");
            usedBillList = removeAllMonthBill(usedBillList);
        }
        for (Bill bill : usedBillList) {
            int type = bill.getType();
            if (type != Bill.BILL_ALL_OK &&
                    type != Bill.BILL_TOO_MUCH &&
                    type != Bill.BILL_SET_BASEDEGREE &&
                    type != Bill.BILL_NOT_DEFINE)
                continue;
            if (sb.length() > 0) sb.append("\n");
            BillType billType = bill.getbillType();
            sb.append(billType.getBillTypeName() + ":")
                    .append("\n" + bill.getDuration())
                    .append("\n" + bill.getDetail())
                    .append("\n");
        }
        double finalPrice = 0;
        if (getChargeType() == TYPE_MOVE_OUT)
            finalPrice -= room.getDeposit();
        if (usedBillList.size() > 0) {
            finalPrice += Util.getTotalHowMuchOfBillList(usedBillList);
            if (finalPrice != 0)
                sb.append("\n" + "共计: " + finalPrice + " 元\n");
        }
        this.describe = sb.toString();
    }

    private List<Bill> removeAllMonthBill(List<Bill> usedBillList) {
        List<Bill> billsToReturn = new ArrayList<>();
        for (Bill usedBill : usedBillList) {
            if (usedBill.getbillType().isChargeOnDegree())
                billsToReturn.add(usedBill);
        }
        return billsToReturn;
    }

    public String getDescribe() {
        if (getChargeType() == TYPE_LIVE_IN) {
            StringBuilder sb = new StringBuilder();
            sb.append(getRoom().getUsername() + "搬入，押金为" + getRoom().getDeposit() + "\n");
            sb.append("收取的费用包括:");
            List<BillType> checkedBillTypeList = getRoom().getCheckedBillTypeList();
            for (BillType checkedBillType : checkedBillTypeList) {
                sb.append(checkedBillType.getBillTypeName() + "  ");
            }
            sb.append("\n");
            return sb.toString();
        }
        if (TextUtils.isEmpty(describe))
            createDescrib();
        return describe;
    }

    /**
     * 只有在退房的时候才用
     *
     * @param bill 最后一次正常charge中的bill
     */
    public Bill createBill(Bill bill, double toDegree) {
        if (chargeType == TYPE_MOVE_OUT) {
            Bill billCreated = new Bill();
            billCreated.setFromDate(bill.getToDate());
            billCreated.setFromDegree(bill.getToDegree());
            billCreated.setToDate(System.currentTimeMillis());
            billCreated.setToDegree(toDegree);
            billCreated.setBillType_id(bill.getBillType_id());
            billCreated.setRoom_id(bill.getRoom_id());
            billCreated.setMemo(bill.getMemo());
            addBill(billCreated);
            return billCreated;
        } else if (chargeType == TYPE_LIVE_IN) {
            return null;
        }
        return null;
    }

    public int getChargeType() {
        return chargeType;
    }

    public void setChargeType(int chargeType) {
        this.chargeType = chargeType;
    }

    public List<ItemComponents> getItemComponentsList(Context context) {
        List<ItemComponents> itemComponentsList = new ArrayList<>();
        List<Bill> usedBillList = getUsedBillList();
        for (Bill bill : usedBillList) {
            if (bill.getType() > state) state = bill.getType();
            ItemComponents itemComponent = new ItemComponents(context, null);
            itemComponent.setTitle(bill.getbillType().getBillTypeName());
            itemComponent.setDuration(bill.getDuration());
            itemComponent.setDetail(bill.getDetail());
            itemComponent.setDetailColor(bill.getType());
            itemComponentsList.add(itemComponent);
        }
        ItemComponents itemComponents = new ItemComponents(context, null);
        itemComponents.setTitle("合计");
        itemComponents.duration.setVisibility(View.GONE);
        double totalHowMuchOfBillList = Util.getTotalHowMuchOfBillList(usedBillList);
        itemComponents.setDetail(totalHowMuchOfBillList + " 元");
        itemComponentsList.add(itemComponents);

        return itemComponentsList;
    }

    public int getState() {
        return state;
    }
}
