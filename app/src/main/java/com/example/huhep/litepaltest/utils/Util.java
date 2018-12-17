package com.example.huhep.litepaltest.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.bean.BillType;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.bean.Room;
import com.example.huhep.litepaltest.bean.RoomSet;

import org.litepal.LitePal;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import static com.example.huhep.litepaltest.bean.Bill.BILL_ALL_OK;
import static com.example.huhep.litepaltest.bean.Bill.BILL_TOO_MUCH;

public class Util {
    private static final String TAG = "PengPeng";

    public static void setFullScreen(Activity activity) {
        activity.getWindow().setBackgroundDrawableResource(R.color.backgroundColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.textViewBackground));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    public static <T> void sort(@NonNull List<T> list, Comparator<T> comparator) {
        Object[] objects = list.toArray();
        for (int i = 0; i < objects.length; i++) {
            for (int j = i; j > 0; j--) {
                if (comparator.compare((T) objects[j], (T) objects[j - 1]) < 0) {
                    swap(j, j - 1, objects);
                }
            }
        }
        ListIterator<T> i = list.listIterator();
        for (Object o : objects) {
            i.next();
            i.set((T) o);
        }
    }

    public static void sort(@NonNull List<?> roomList) {
        if (roomList.size() == 0) return;

        if (roomList.get(0) instanceof Room) {
            sort(roomList, (o1, o2) -> ((Room) o1).getRoomNum().compareTo(((Room) o2).getRoomNum()));
        } else if (roomList.get(0) instanceof BillType) {
            sort(roomList, (o3, o4) -> {
                BillType o1 = (BillType) o3;
                BillType o2 = (BillType) o4;
                if (o1.isChargeOnDegree() && !o2.isChargeOnDegree()) {
                    return 1;
                } else if (!o1.isChargeOnDegree() && o2.isChargeOnDegree()) {
                    return -1;
                } else {
                    return o1.getBillTypeName().compareTo(o2.getBillTypeName());
                }
            });
        }

    }

    private static void swap(int j, int i, Object[] objects) {
        Object temp = objects[j];
        objects[j] = objects[i];
        objects[i] = temp;
    }

    public static RoomSet getRoomSetFromId(long id) {
        RoomSet roomSet = LitePal.find(RoomSet.class, id);
        if (roomSet != null) {
            return roomSet;
        }
        return null;
    }

    public static String getDate(long time, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getDate() {
        String date = getDate(System.currentTimeMillis(), "yyyy-MM-dd  HH:mm:ss");
        return date + " : ";
    }

    public static String getSPName(Room room, BillType billType) {
        return "roomId:" + room.getId() + "_billTypeId:" + billType.getId();
    }

    public static String getSPName(long roomId, long billTypeId) {
        return "roomId:" + roomId + "_billTypeId:" + billTypeId;
    }

    public static List<BillType> getAllBillTypeOf(long roomid) {
        if (roomid == -1) {
            return LitePal.where("belongTo=?", String.valueOf(roomid)).find(BillType.class);
        } else {
            return LitePal.find(Room.class, roomid).getBillTypeList();
        }
    }

    public static SparseIntArray duration(long later, long earlier) {
        if (later < earlier) throw new RuntimeException("计算日期间隔时，之后的数不能比之前的早");
        SparseIntArray array = new SparseIntArray();
        Calendar calendarLater = Calendar.getInstance();
        Calendar calendarEarlier = Calendar.getInstance();
        calendarLater.setTimeInMillis(later);
        calendarEarlier.setTimeInMillis(earlier);
        int year = calendarLater.get(Calendar.YEAR) - calendarEarlier.get(Calendar.YEAR);
        int month = calendarLater.get(Calendar.MONTH) - calendarEarlier.get(Calendar.MONTH);
        int day = calendarLater.get(Calendar.DAY_OF_MONTH) - calendarEarlier.get(Calendar.DAY_OF_MONTH);
        if (month < 0 && year > 0) {
            month += 12;
            year -= 1;
        }
        if (day < 0 && month > 0) {
            day += 30;
            month -= 1;
        }
        array.append(0, year);
        array.append(1, month);
        array.append(2, day);
        return array;
    }

    public static int howManyMonth(long later, long earlier) {
        if (later < earlier) return -1;
        SparseIntArray duration = duration(later, earlier);
        int year = duration.get(0);
        int month = duration.get(1);
        int day = duration.get(2);
        if (year > 0) month += (year * 12);
        if (day > 15) month += 1;
        return month;
    }

    public static String formationDuration(SparseIntArray duration) {
        return formationDuration(duration, "年", "月", "日");
    }

    public static String formationDuration(SparseIntArray duration, String yearUnit, String monthUnit, String dayUnit) {
        StringBuilder sb = new StringBuilder();
        int year = duration.get(0);
        int month = duration.get(1);
        int day = duration.get(2);
        if (year > 0) sb.append(year).append(yearUnit);
        if (month > 0) sb.append(month).append(monthUnit);
        sb.append(day).append(dayUnit);
        return sb.toString();
    }

    public static String getWhen() {
        return getWhen(System.currentTimeMillis());
    }

    public static String getWhen(long when) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(when);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day > 15) month += 1;
        if (month == 13) {
            year++;
            month = 1;
        }
        day = 1;
        return year + "年" + month + "月" + day + "日";
    }

    public static long getMillionsFromString(String parse) {
        if (parse.isEmpty()) new RuntimeException("时间数据不能为空");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            Date date = dateFormat.parse(parse);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getNextMonty(long when) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(when);
        calendar.add(Calendar.MONTH, 1);
        if (calendar.get(Calendar.MONTH) == 12){
            Log.d(TAG, "getNextMonty: ");
            calendar.set(Calendar.MONTH, 0);
        }
        return getWhen(calendar.getTimeInMillis());
    }

    public static String getWhenAccurately(long when) {
        return getDate(when,"yyyy年MM月dd日");
    }

    public static Bill getLastBillOf(@NonNull Room room, @NonNull BillType billType) {
        if (room == null || billType == null)   return null;
        List<Bill> bills = LitePal.where("roomId=? and billTypeId=?", String.valueOf(room.getId()),
                String.valueOf(billType.getId())).find(Bill.class);
        if (bills.size() == 0) {
            List<BillType> publiBillTypeList = LitePal.where("billTypeName=? and belongTo=-1", billType.getBillTypeName())
                    .find(BillType.class);
            if (publiBillTypeList.size() == 0) return null;
            BillType publiBillType = publiBillTypeList.get(0);
            bills = LitePal.where("roomId=? and billTypeId=?", String.valueOf(room.getId()),
                    String.valueOf(publiBillType.getId())).find(Bill.class);
        }
        if (bills.size() == 0) return null;
        Bill lastBill = bills.get(0);
        for (int i = 1; i < bills.size(); i++) {
            if (lastBill.getToDate() < bills.get(i).getToDate())
                lastBill = bills.get(i);
        }
        return lastBill;
    }

    public static double getTotalHowMuchOfBillList(List<Bill> billList) {
        double totalMoney = 0;
        for (Bill bill : billList) {
            if (bill.getbillType().isChargeOnDegree()) {
                if (bill.getType() == BILL_ALL_OK || bill.getType() == BILL_TOO_MUCH){
                    totalMoney += bill.howMuch();
                }
            } else {
                totalMoney += bill.howMuch();
            }
        }
        return totalMoney;
    }

    public static double getHowDegreeOfBillList(List<Bill> billList) {
        double totalDegree = 0;
        for (Bill bill : billList) {
            if (bill.getToDegree() != 0 && (bill.getType() == BILL_ALL_OK || bill.getType() == BILL_TOO_MUCH)) {
                totalDegree += (bill.getToDegree() - bill.getFromDegree());
            }
        }
        return totalDegree;
    }

    public static int mmToPix(double mm) {
        return inchToPix(mmToInch(mm));
    }

    public static double mmToInch(double mm) {
        return mm / 25.4;
    }

    public static int inchToPix(double inch) {
        double dpi = BaseActivity.getContext().getResources().getInteger(R.integer.imageDPI);
        return (int) (inch * dpi);
    }

    public static long getMinSearchCreateTime(long currentDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDate);
        int monthToShow = BaseActivity.getContext().getResources().getInteger(R.integer.MonthOnceShown);
        calendar.add(Calendar.MONTH, -monthToShow-1);
        calendar.add(Calendar.DAY_OF_MONTH, 16);
        return calendar.getTimeInMillis();
    }

    public static long getMaxSearchCreateTime(long currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        return calendar.getTimeInMillis();
    }

    public static File getDatabaseFile() {
        String parent = BaseActivity.getContext().getFilesDir().getParent();
        String s = parent + File.separator + "databases" + File.separator + "demo.db";
        return new File(s);

    }public static File getTempDatabaseFile() {
        String parent = BaseActivity.getContext().getFilesDir().getParent();
        String s = parent + File.separator + "databases" + File.separator + "demo_temp.db";
        return new File(s);
    }

    public static byte[] longToByte(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) ((num >> ((7 - i) * 8)) & 0xff);
        }
        return b;
    }

    public static long byteToLong(byte[] bytes) {
        long num = 0;
        for (int i = 0; i < 8; i++) {
            num <<= 8;
            num |= (bytes[i] & 0xff);
        }
        return num;
    }

    public static int findRoomPos(String s) {
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

