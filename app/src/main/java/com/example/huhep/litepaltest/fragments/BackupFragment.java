package com.example.huhep.litepaltest.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.load.data.BufferedOutputStream;
import com.example.huhep.litepaltest.MainActivity;
import com.example.huhep.litepaltest.R;
import com.example.huhep.litepaltest.tcp.TCPClient;
import com.example.huhep.litepaltest.tcp.TCPServer;
import com.example.huhep.litepaltest.utils.MsgForBackupFragment;
import com.example.huhep.litepaltest.utils.Util;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * A simple {@link Fragment} subclass.
 */
public class BackupFragment extends Fragment {
    @BindView(R.id.hintTV)
    TextView hintTV;

    @BindView(R.id.ipET)
    EditText ipET;

    @BindView(R.id.synBtn)
    Button synBtn;

    public static final int COMMAND_COMPARE = 1;
    public static final int COMMAND_RECEIVE = 2;
    public static final int COMMAND_SEND = 3;
    public static final int COMMAND_READYTORECEIVE = 4;
    public static final int COMMAND_EQUAL = 5;

    private ExecutorService exec = Executors.newCachedThreadPool();
    private Unbinder bind;
    private TCPServer tcpServer;
    private TCPClient tcpClient;
    private boolean inServerMode;
    public static MsgForBackupFragment currentState;

    public interface IListenerForTCP {
        void onReadyToReceive(String fromAddress);

        void receiveDBFromOutside(Socket socket, InputStream inputStream);

        void sendDBToOutSide(Socket socket, OutputStream outputStream);
    }

    abstract class ListenerForTCP implements IListenerForTCP {
        public void receiveDBFromOutside(Socket socket, InputStream inputStream) {
            EventBus.getDefault().post(new MsgForBackupFragment(false, false, "正在从" + socket.getInetAddress().getHostAddress() + "接收数据"));
            File tempDatabaseFile = Util.getTempDatabaseFile();
            if (tempDatabaseFile.exists()) tempDatabaseFile.delete();
            try {
                tempDatabaseFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(tempDatabaseFile);
                byte[] bForLength = new byte[8];
                inputStream.read(bForLength);
                long fileLen = Util.byteToLong(bForLength);
                long totalLen = 0;
                byte[] b = new byte[2048];
                int len;
                while ((len = inputStream.read(b)) > 0) {
                    fileOutputStream.write(b, 0, len);
                    totalLen += len;
                    if (totalLen>=fileLen) break;
                }
                EventBus.getDefault().post(new MsgForBackupFragment("接收了" + totalLen + "数据"));
                fileOutputStream.close();
                Util.getDatabaseFile().delete();
                Util.getTempDatabaseFile().renameTo(Util.getDatabaseFile());
                closeInSeconds("接收完成,请手动重启软件",5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendDBToOutSide(Socket socket, OutputStream outputStream) {
            EventBus.getDefault().post(new MsgForBackupFragment(false, false, "正在向" + socket.getInetAddress().getHostAddress() + "传输数据"));
            exec.execute(() -> {
                try {
                    File databaseFile = Util.getDatabaseFile();
                    if (!databaseFile.exists()) {
                        closeInSeconds("没有数据，传输失败",5);
                        return;
                    }

                    outputStream.write(Util.longToByte(COMMAND_RECEIVE));
                    outputStream.flush();
                    long totalSpace = databaseFile.length();
                    outputStream.write(Util.longToByte(totalSpace));
                    outputStream.flush();

                    FileInputStream fileInputStream = new FileInputStream(databaseFile);
                    int len;
                    byte[] b = new byte[2048];
                    while ((len = fileInputStream.read(b)) > 0) {
                        outputStream.write(b, 0, len);
                    }
                    outputStream.flush();
                    fileInputStream.close();
                    EventBus.getDefault().post(new MsgForBackupFragment("传输完成，所有数据是" + totalSpace+"字节"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup, container, false);
        bind = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        inServerMode = true;
        ipET.setOnEditorActionListener((v, actionId, event) -> {
            inServerMode = ipET.getText().length() == 0;
            return false;
        });
        return view;
    }

    @OnClick(R.id.synBtn)
    public void synBtnClicked(View view) {
        switch (synBtn.getText().toString()) {
            case "开始同步":
                if (inServerMode) {
                    ipET.setText(getHostIp());
                    if (tcpServer == null) {
                        tcpServer = new TCPServer(5173,this);
                        tcpServer.setListener(new ListenerForTCP() {
                            @Override
                            public void onReadyToReceive(String fromAddress) {
                                getActivity().runOnUiThread(() ->
                                        getReadyDialogBuilder(fromAddress).setPositiveButton("确定", (dialog, which) ->
                                                exec.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tcpServer.send(Util.longToByte(COMMAND_SEND));
                                                    }
                                                })).setNegativeButton("取消", null).show()
                                );
                            }
                        });
                    }
                    tcpServer.setListening(true);
                    exec.execute(tcpServer);
                } else {
                    if (tcpClient == null) {
                        tcpClient = new TCPClient(ipET.getText().toString(), 5173,this);
                        tcpClient.setListener(new ListenerForTCP() {
                            @Override
                            public void onReadyToReceive(String fromAddress) {
                                getActivity().runOnUiThread(() -> getReadyDialogBuilder(fromAddress)
                                        .setPositiveButton("确定", (dialog, which) ->
                                                exec.execute(() -> tcpClient.send(Util.longToByte(COMMAND_SEND))))
                                        .setNegativeButton("取消", null)
                                        .show());
                            }
                        });
                    }
                    tcpClient.setRunning(true);
                    exec.execute(tcpClient);
                }
                break;
            case "停止同步":
                if (inServerMode) {
                    tcpServer.closeSelf();
                } else {
                    tcpClient.closeSelf();
                }
                break;
        }
    }

    private AlertDialog.Builder getReadyDialogBuilder(String fromAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("正在更新")
                .setMessage("正在从" + fromAddress + "更新信息，是否确认？");
        return builder;
    }

    private String getHostIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet6Address) continue;
                    if (!"127.0.0.1".equals(inetAddress.getHostAddress()))
                        return inetAddress.getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void setHintTV(MsgForBackupFragment msg) {
        currentState = msg;
        ipET.setEnabled(msg.isEditTextCanEdit());
        synBtn.setEnabled(msg.isBtnIsEnable());
        if (msg.getBtnMsg() != null) synBtn.setText(msg.getBtnMsg());
        if (msg.getHintTVMsg() != null) hintTV.setText(msg.getHintTVMsg());
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
        bind.unbind();
    }

    public void closeInSeconds(String hint, int seconds) {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(new Runnable() {
            int left = seconds;
            @Override
            public void run() {
                if (left==0) {
                    if (tcpClient != null) tcpClient.closeSelf();
                    if (tcpServer != null) tcpServer.closeSelf();
                    timer.shutdownNow();
                }else {
                    EventBus.getDefault().post(new MsgForBackupFragment(hint+"\n即将在"+left+"秒后关闭连接"));
                    left--;
                }
            }
        },0,1,TimeUnit.SECONDS);
    }
}
