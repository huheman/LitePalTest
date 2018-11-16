package com.example.huhep.litepaltest.bean;

import org.litepal.crud.LitePalSupport;

public class BillSet extends LitePalSupport {
    private long id;
    private long RoomId;
    private String DateOfBillSet;
    private long realTime;

    public BillSet() {
        realTime = System.currentTimeMillis();
    }
}
