package com.example.huhep.litepaltest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {
    private static Context mcontext;
    private static Context appContext;
    private static SharedPreferences sharedPreferences;
    public static final int REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_NEWROOM = 123;
    public static final int REQUEST_FROM_MAINFRAGMENT_TO_NEWROOM_FOR_REVERROOM = 234;
    public static final int REQUEST_FROM_MAINFRAGMENT_TO_CHARGEMANAG = 345;
    public static final int REQUEST_FROM_CHARGEMANAG_TO_NEWCHARGE = 456;
    public static final int REQUEST_FROM_BILLMANAGE_TO_CHARGEMANAGE = 567;
    public static final int RESULT_FROM_NEWROOM_TO_MAINFRAGMENT=321;
    public static final int RESULT_FROM_NEWCHARGE_TO_CHARGEMANG = 654;
    public static final int RESULT_FROM_CHARGEMANG_TOMAINFRAGMENT = 543;
    public static final String WHEN_KEY_FOR_SHP = "when";
    public static final String ROOM_ID = "_room_id";
    public static final String FORCE_TO_TRUE = "force_to_true";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        appContext = getApplicationContext();
    }

    public static void show(String string) {
        Toast.makeText(mcontext, string, Toast.LENGTH_SHORT).show();
    }

    public static Context getContext() {
        return mcontext;
    }

    public static SharedPreferences getSP() {
        return mcontext.getSharedPreferences("bills", MODE_PRIVATE);
    }
}
