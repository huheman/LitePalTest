package com.example.huhep.litepaltest.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.components.ItemComponents;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.Inflater;

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
    private String image;
    private double WHratio;

    public static final int TYPE_MOVE_OUT = 10;
    public static final int TYPE_LIVE_IN = 20;

    public String getPassWord() {
        return passWord;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
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
            billList = LitePal.where("chargeId=?", String.valueOf(id)).find(Bill.class);
        }
        return billList;
    }

    public List<Bill> getUsedBillList() {
        if (usedBillList == null) {
            usedBillList = new ArrayList<>();
            Room room = getRoom();
            List<BillType> checkedBillTypeList = room.getCheckedBillTypeList();
            List<Bill> billList = getBillList();
            List<Bill> billsToRemove = new ArrayList<>();
            billsToRemove.addAll(billList);
            for (Bill bill : billList) {
                for (BillType billType : checkedBillTypeList) {
                    if (bill.getBillType_id() == billType.getId()){
                        usedBillList.add(bill);
                        billsToRemove.remove(bill);
                        break;
                    }
                }
            }
            //把不用的bill都删掉
            for (Bill billToRemove : billsToRemove){
                billToRemove.delete();
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

    public List<String> createDescrib() {
        List<String> nameToDescribe = new ArrayList<>();

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
            String s;
            if (bill.getbillType().isChargeOnDegree())
                s = billType.getBillTypeName() + "&" + bill.getDuration() + "\n" + bill.getDetailForImage();
            else{
                s = billType.getBillTypeName() + "&" + Util.getDate(bill.getFromDate(),"yyyy年MM月 ") + bill.getDetail();
            }
            nameToDescribe.add(s);
        }
        double finalPrice = 0;
        if (getChargeType() == TYPE_MOVE_OUT)
            finalPrice -= room.getDeposit();
        if (usedBillList.size() > 0) {
            finalPrice += Util.getTotalHowMuchOfBillList(usedBillList);
            sb.append("\n" + "共计: " + finalPrice + " 元");
            String s = "共计&" + finalPrice + "元";
            nameToDescribe.add(s);
        }
        this.describe = sb.toString();
        return nameToDescribe;
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
        int state = 0;
        List<Bill> usedBillList = getUsedBillList();
        for (Bill bill : usedBillList) {
            if (bill.getType() > state) state = bill.getType();
        }
        return state;
    }

    public String createImage(Context context) {
        View viewForBitmap = LayoutInflater.from(context).inflate(R.layout.charge_image, null, false);
        GridLayout gridLayout = viewForBitmap.findViewById(R.id.bitmap_gridLayout);
        TextView titleText = viewForBitmap.findViewById(R.id.bitmap_titleTextView);
        TextView codeText = viewForBitmap.findViewById(R.id.bitmap_codeTextView);
        TextView timeText = viewForBitmap.findViewById(R.id.bitmap_timeTextView);
        titleText.setTextSize(context.getResources().getDimension(R.dimen.titleTextSize));
        codeText.setTextSize(context.getResources().getDimension(R.dimen.secondaryTextSize));
        timeText.setTextSize(context.getResources().getDimension(R.dimen.secondaryTextSize));
        codeText.setTextColor(context.getResources().getColor(R.color.deepDeepDark));
        titleText.setTextColor(context.getResources().getColor(R.color.deepDeepDark));
        timeText.setTextColor(context.getResources().getColor(R.color.deepDeepDark));
        codeText.setText("Num：" + getPassWord());
        timeText.setText(getCreateDateToString());

        int broder = (int) context.getResources().getDimension(R.dimen.broder);
        gridLayout.setColumnCount(2);
        TextView roomNameTextView = new TextView(context);
        roomNameTextView.setTextSize(context.getResources().getDimension(R.dimen.primaryTextSize));
        roomNameTextView.setText(getRoom().getRoomNum()+" 房");
        roomNameTextView.setGravity(Gravity.CENTER);
        roomNameTextView.setTextColor(context.getResources().getColor(R.color.deepDeepDark));
        roomNameTextView.setBackgroundColor(context.getResources().getColor(R.color.textViewBackground));

        GridLayout.LayoutParams roomNameTVLayoutParams = new GridLayout.LayoutParams();
        roomNameTVLayoutParams.setMargins(broder, broder, broder, broder);
        roomNameTVLayoutParams.columnSpec = GridLayout.spec(0, 2);
        roomNameTVLayoutParams.rowSpec = GridLayout.spec(0, 1, 2);
        roomNameTVLayoutParams.setGravity(Gravity.FILL);
        gridLayout.addView(roomNameTextView, roomNameTVLayoutParams);

        List<String> describMap = createDescrib();
        int index = 0;
        for (String key : describMap) {
            String[] strings = key.split("&");
            String billTypeName = strings[0];
            String detail = strings[1];
            GridLayout.LayoutParams billTypeNameTVParams = new GridLayout.LayoutParams();
            billTypeNameTVParams.setMargins(broder, broder, broder, broder);
            billTypeNameTVParams.rowSpec = GridLayout.spec(index + 1, 1);
            billTypeNameTVParams.columnSpec = GridLayout.spec(index % 2, 1);
            billTypeNameTVParams.setGravity(Gravity.FILL);
            TextView billTypeNameTV = new TextView(context);
            billTypeNameTV.setBackgroundColor(context.getResources().getColor(R.color.textViewBackground));
            billTypeNameTV.setGravity(Gravity.CENTER);
            billTypeNameTV.setText(billTypeName);
            billTypeNameTV.setPadding(100, 0, 100, 0);
            billTypeNameTV.setTextSize(context.getResources().getDimension(R.dimen.primaryTextSize));
            gridLayout.addView(billTypeNameTV, billTypeNameTVParams);
            billTypeNameTV.setTextColor(context.getResources().getColor(R.color.deepDeepDark));


            GridLayout.LayoutParams billDetailTVParams = new GridLayout.LayoutParams();
            billDetailTVParams.setMargins(broder, broder, broder, broder);
            billDetailTVParams.rowSpec = GridLayout.spec(index + 1, 1);
            billDetailTVParams.columnSpec = GridLayout.spec((index + 1) % 2, 1);
            billDetailTVParams.setGravity(Gravity.FILL);
            TextView billDetailTV = new TextView(context);
            billDetailTV.setTextColor(context.getResources().getColor(R.color.deepDeepDark));
            billDetailTV.setPadding(100, 0, 100, 0);
            billDetailTV.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            billDetailTV.setBackgroundColor(context.getResources().getColor(R.color.textViewBackground));
            billDetailTV.setText(detail);
            billDetailTV.setTextSize(context.getResources().getDimension(R.dimen.secondaryTextSize));
            gridLayout.addView(billDetailTV, billDetailTVParams);
            if (describMap.indexOf(key) == describMap.size() - 1) {
                billDetailTV.setGravity(Gravity.CENTER);
                billDetailTV.setTextSize(context.getResources().getDimension(R.dimen.primaryTextSize));
            }
            index += 2;
        }


        viewForBitmap.measure(0, 0);
        viewForBitmap.layout(0, 0, viewForBitmap.getMeasuredWidth(), viewForBitmap.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(viewForBitmap.getWidth(), viewForBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        WHratio = viewForBitmap.getWidth() / viewForBitmap.getHeight();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(context.getResources().getColor(R.color.textViewBackground));
        viewForBitmap.draw(canvas);
        File file = new File(context.getCacheDir(), "_" + getCreateDateToString() + "_room_" + getRoom().getRoomNum()+".jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image = file.toString();
        return getImage();
    }

    public String getImage() {
        return image;
    }

    public double getWHratio() {
        return WHratio;
    }

    public void clearSP() {
        SharedPreferences.Editor editor = BaseActivity.getSP().edit();
        List<Bill> usedBillList = getUsedBillList();
        for (Bill bill : usedBillList) {
            editor.remove(Util.getSPName(getRoomId(), bill.getBillType_id()) + "_pre");
            editor.remove(Util.getSPName(getRoomId(), bill.getBillType_id()));
        }
        editor.apply();
    }
}
