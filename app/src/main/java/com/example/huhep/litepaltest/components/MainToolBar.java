package com.example.huhep.litepaltest.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.example.huhep.litepaltest.CustomToolbar;
import com.example.huhep.litepaltest.R;

public class MainToolBar extends CustomToolbar {

    public interface MainToolBarListener {
        void onSearchClicked();

        void onNewRoomClicked();

        void onChargeManageClicked();

        void onnewMemoClicked();
    }

    MainToolBarListener listener;
    public MainToolBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTitle("房间概况");
        getToolbar().inflateMenu(R.menu.mainfragment_toolbarmenu);

        getToolbar().setOnMenuItemClickListener(item -> {
            if (listener == null) {
                return false;
            }
            switch (item.getItemId()) {
                case R.id.mainfragment_toolbar_searchroom:
                    listener.onSearchClicked();
                    break;
                case R.id.mainfragment_toolbar_newRoom:
                    listener.onNewRoomClicked();
                    break;
                case R.id.mainfragment_toolbar_chargemangment:
                    listener.onChargeManageClicked();
                    break;
                case R.id.mainfragment_toolbar_note:
                    listener.onnewMemoClicked();
                    break;
            }
            return false;
        });
    }

    public void setListener(MainToolBarListener listener) {
        this.listener = listener;
    }
}
