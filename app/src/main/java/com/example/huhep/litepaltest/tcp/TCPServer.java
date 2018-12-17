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
import java.net.ServerSocket;
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

public class TCPServer implements Runnable {
    private boolean isListening;
    private int port;
    private SSocket sSocket;
    private ServerSocket serverSocket;
    private ExecutorService exec = Executors.newCachedThreadPool();
    private BackupFragment.IListenerForTCP listener;
    private BackupFragment fragment;

    public SSocket getsSocket() {
        return sSocket;
    }

    public void setListener(BackupFragment.IListenerForTCP listener) {
        this.listener = listener;
    }


    public TCPServer(int port,BackupFragment fragment) {
        this.port = port;
        this.fragment = fragment;
        isListening = true;
    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            if (serverSocket != null)
                return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new MsgForBackupFragment(false, true, BaseActivity.getContext().getString(R.string.stopSyn), "正在等待客户端接入"));
        while (isListening) {
            Socket socket = getSocket(serverSocket);
            if (socket != null) {
                EventBus.getDefault().post(new MsgForBackupFragment(false, false, socket.getInetAddress().getHostAddress() + "已经接入"));
                sSocket = new SSocket(socket);
                exec.execute(sSocket);
                break;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new MsgForBackupFragment(true, true, BaseActivity.getContext().getString(R.string.beginSyn), BaseActivity.getContext().getString(R.string.normalhintText)));
    }

    public void send(byte[] msgByte) {
        sSocket.send(msgByte);
    }

    public void closeSelf() {
        setListening(false);
        if (sSocket != null)
            sSocket.closeSelf();
    }

    public void setListening(boolean listening) {
        isListening = listening;
    }

    public class SSocket implements Runnable {
        private OutputStream outputStream;
        private InputStream inputStream;
        private Socket socket;
        private boolean isRunning;

        public void closeSelf() {
            isRunning = false;
            EventBus.getDefault().post(new MsgForBackupFragment("正在关闭连接"));
        }

        public SSocket(Socket socket) {
            this.socket = socket;
            isRunning = true;
        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout(3000);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (isRunning && !socket.isInputShutdown()) {
                byte[] b = new byte[8];
                try {
                    int len = inputStream.read(b);
                    if (len > -1) {
                        int command = (int)Util.byteToLong(b);
                        switch (command) {
                            case COMMAND_COMPARE:
                                compareWithSelf();
                                break;
                            case COMMAND_RECEIVE:
                                if (listener != null)
                                    listener.receiveDBFromOutside(socket, inputStream);
                                break;
                            case COMMAND_SEND:
                                if (listener != null)
                                    listener.sendDBToOutSide(socket, outputStream);
                                break;
                        }
                    }else {
                        closeSelf();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
                EventBus.getDefault().post(new MsgForBackupFragment(true, true, "开始同步", "客户端已断开"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void compareWithSelf() {
            byte[] b = new byte[8];
            try {
                inputStream.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            long dateFromOutside = Util.byteToLong(b);
            List<Charge> charges = LitePal.select("createDate").order("createDate desc").limit(1).find(Charge.class);
            if (charges.size() != 0 && charges.get(0).getCreateDate() == dateFromOutside) {
                send(Util.longToByte(COMMAND_EQUAL));
                fragment.closeInSeconds("数据版本一致，无需更新",5);
            } else if (charges.size() != 0 && charges.get(0).getCreateDate() > dateFromOutside) {
                send(Util.longToByte(COMMAND_READYTORECEIVE));
                EventBus.getDefault().post(new MsgForBackupFragment("本机记录更新一点，无需更新"));
            } else {
                if (listener!=null)
                    listener.onReadyToReceive(socket.getInetAddress().getHostAddress());
            }
        }

        void send(byte[] msgByte) {
            if (outputStream == null) return;
            try {
                outputStream.write(msgByte);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
