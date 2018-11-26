package com.example.huhep.litepaltest.utils;

import android.content.Context;
import android.view.View;

import com.example.huhep.litepaltest.bean.Bill;
import com.example.huhep.litepaltest.components.ItemComponents;
import com.example.huhep.litepaltest.fragments.PreviewFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BillFormater {
    private List<Bill> billList;
    private HashMap<String, List<Bill>> billByName;

    public BillFormater(List<Bill> billList) {
        this.billList = billList;
    }

    public HashMap<String, List<Bill>> getBillByName() {
        if (billByName == null) {
            billByName = new HashMap<>();
            for (Bill bill : billList) {
                List<Bill> bills = billByName.get(bill.getbillType().getBillTypeName());
                if (bills == null) {
                    bills = new ArrayList<>();
                    billByName.put(bill.getbillType().getBillTypeName(), bills);
                }
                bills.add(bill);
            }
        }
        return billByName;
    }

    public List<ItemComponents> getComponentsList(Context context) {
        List<ItemComponents> componentsList = new ArrayList<>();
        Set<Map.Entry<String, List<Bill>>> entries = getBillByName().entrySet();
        for (Map.Entry<String, List<Bill>> map : entries) {
            List<Bill> billList = map.getValue();
            ItemComponents itemComponents = new ItemComponents(context, null);
            if (billList.get(0).getToDegree() != 0) {
                itemComponents.setDuration("共计: " + Util.getTotalHowMuchOfBillList(billList) + " 元");
                itemComponents.setDetail("共用: " + Util.getHowDegreeOfBillList(billList) + " 度");
            } else {
                itemComponents.duration.setVisibility(View.GONE);
                itemComponents.setDetail("共计：" + Util.getTotalHowMuchOfBillList(billList) + " 元");
            }
            itemComponents.setTitle(map.getKey());
            componentsList.add(itemComponents);
        }
        componentsList.add(getTotalComponents(context));
        return componentsList;
    }

    public ItemComponents getTotalComponents(Context context) {
        ItemComponents totalComponents = new ItemComponents(context, null);
        totalComponents.setTitle("所有收费");
        totalComponents.duration.setVisibility(View.GONE);
        totalComponents.setDetail(Util.getTotalHowMuchOfBillList(PreviewFragment.billList) + "元");
        return totalComponents;
    }
}
