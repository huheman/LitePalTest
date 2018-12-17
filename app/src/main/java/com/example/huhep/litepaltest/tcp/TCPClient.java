package com.example.huhep.litepaltest.tcp;

import android.util.Log;

import com.example.huhep.litepaltest.BaseActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.bean.Charge;
import com.example.huhep.litepaltest.fragments.BackupFragment;
import com.example.huhep.litepaltest.utils.MsgForBackupFragment;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static com.example.huhep.litepaltest.fragments.BackupFragment.COMMAND_COMPARE;
import static com.example.huhep.litepaltest.fragments.BackupFragment.COMMAND_EQUAL;
import static com.example.huhep.litepaltest.fragments.BackupFragment.COMMAND_READYTORECEIVE;
import static com.example.huhep.litepaltest.fragments.BackupFragment.COMMAND_RECEIVE;
import static com.example.huhep.litepaltest.fragments.BackupFragment.COMMAND_SEND;

public class TCPClient implements Runnable {
    private String IP;
    private int port;
    private Socket socket;
    private boolean isRunning;
    private BackupFragment fragment;
    private InputStream inputStream;
    private OutputStream outputStream;
    ExecutorService exec = Executors.newCachedThreadPool();
    private BackupFragment.IListenerForTCP listener;
    public void setListener(BackupFragment.IListenerForTCP listener) {
        this.listener = listener;
    }

    public TCPClient(String IP, int port,BackupFragment fragment) {
        this.IP = IP;
        this.port = port;
        isRunning = true;
        this.fragment = fragment;
    }

    public void send(byte[] msgByte) {
        if (outputStream==null) return;
        try {
            outputStream.write(msgByte);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(IP, port);
            socket.setSoTimeout(3000);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            EventBus.getDefault().post(new MsgForBackupFragment(false,true,"停止同步","正在与服务器通信"));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new MsgForBackupFragment(true,true,"开始同步","无法连接到服务器，请确保ip地址没写错"));
            return;
        }
        if (socket!=null) exec.execute(this::tellServerDate);
        while (isRunning && socket!=null &&!socket.isInputShutdown()) {
            byte[] b = new byte[8];
            try {
                int len = inputStream.read(b);
                if (len > -1) {
                    int command = (int)Util.byteToLong(b);
                    switch (command) {
                        case COMMAND_SEND:
                            if (listener != null) listener.sendDBToOutSide(socket, outputStream);
                            break;
                        case COMMAND_RECEIVE:
                            if (listener != null)
                                listener.receiveDBFromOutside(socket, inputStream);
                            break;
                        case COMMAND_READYTORECEIVE:
                            if (listener != null)
                                listener.onReadyToReceive(socket.getInetAddress().getHostAddress());
                            break;
                        case COMMAND_EQUAL:
                            fragment.closeInSeconds("数据版本一致，无需更新", 5);
                            break;
                    }
                } else {
                    closeSelf();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (inputStream!=null)
                inputStream.close();
            if (outputStream!=null)
                outputStream.close();
            if (socket!=null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new MsgForBackupFragment(true,true,"开始同步",BaseActivity.getContext().getString(R.string.normalhintText)));
    }

    private void tellServerDate() {
        List<Charge> charges = LitePal.select("createDate").order("createDate desc").limit(1).find(Charge.class);
        if (charges.size()==0) {
            if (listener!=null) listener.onReadyToReceive(socket.getInetAddress().getHostAddress());
            return;
        }
        send(Util.longToByte(COMMAND_COMPARE));
        send(Util.longToByte(charges.get(0).getCreateDate()));
    }

    public void closeSelf() {
        setRunning(false);
        EventBus.getDefault().post(new MsgForBackupFragment("正在断开连接..."));
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }


}
