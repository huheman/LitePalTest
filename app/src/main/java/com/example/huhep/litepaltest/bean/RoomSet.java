package com.example.huhep.litepaltest.bean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class RoomSet extends LitePalSupport {
    private long id;
    @Column(unique = true)
    private String roomSetName;
    private List<Room> roomList=new ArrayList<>();
    private int isPublic;

    public boolean isPublic() {
        return isPublic==1;
    }

    public void setPublic(boolean aPublic) {
        if (aPublic == true) {
            isPublic = 1;
        }else
            isPublic = 2;
    }

    public RoomSet() {
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public RoomSet(String roomSetName) {
        this.roomSetName = roomSetName;
    }

    public String getRoomSetName() {
        return roomSetName;
    }

    public void setRoomSetName(String roomSetName) {
        this.roomSetName = roomSetName;
    }

    public long getId() {
        return id;
    }

    public void addRoom(Room room) {
        roomList.add(room);
    }

    public List<Room> getRoomList() {
        return roomList;
    }

}
