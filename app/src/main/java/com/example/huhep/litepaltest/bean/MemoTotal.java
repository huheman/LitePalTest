package com.example.huhep.litepaltest.bean;

import org.litepal.crud.LitePalSupport;

public class MemoTotal extends LitePalSupport {
    private long id;
    private long room_id;
    private String memoContent;

    public long getId() {
        return id;
    }

    public long getRoom_id() {
        return room_id;
    }

    public void setRoom_id(long room_id) {
        this.room_id = room_id;
    }

    public String getMemoContent() {
        return memoContent;
    }

    public void setMemoContent(String memoContent) {
        this.memoContent = memoContent;
    }
}
