package com.ridehome.castsink.wifip2p;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ridehome.castsink.CastSinkSDK;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * date：2018/2/24 on 16:59
 * description:服务端监听的socket
 */
public class ReceiveSocket {
    public static final String TAG = "ReceiveSocket";
    public static final int PORT = 12349;
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ConnectThread mConnectThread;

    private static ReceiveSocket instance_;

    public static ReceiveSocket getInstance(){
        return instance_;
    }

    public void OnMotionEvent(int mask, float x, float y){
        // send the motion to the sender
        new Thread(new Runnable(){
            @Override
            public void run() {
                if(null != mOutputStream){
                    String strMsg = "MotionEvent:"+mask+":"+x+":"+y+";\n";
                    try {
                        mOutputStream.write(strMsg.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    // start a thread to proc the connect
    private class ConnectThread extends Thread {
        private Boolean mRunFlag;

        @SuppressLint("MissingPermission")
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            mRunFlag = true;
            try {
                mServerSocket = new ServerSocket();
                mServerSocket.setReuseAddress(true);
                mServerSocket.bind(new InetSocketAddress(PORT));
                Log.e(TAG, "Listening : " + PORT);
                mSocket = mServerSocket.accept();
                Log.e(TAG, "客户端IP地址 : " + mSocket.getRemoteSocketAddress());

                mOutputStream = mSocket.getOutputStream();
                mInputStream = mSocket.getInputStream();

                String strMsg = "SinkOk;\n";
                mOutputStream.write(strMsg.getBytes());

                while (mRunFlag){
                    byte bytes[] = new byte[1024];
                    int x = mInputStream.read(bytes);
                    if(x>10){
                        String strRcv = new String(bytes,0,x);
                        Log.i(TAG, "接收到数据:"+strRcv);
                        if(strRcv.contains("StreamOk")){
                            // find the url
                            String[] strArray = strRcv.split(":|;");
                            if(strArray.length>=4){
                                String rtspUrl = strArray[1]+":"+strArray[2]+":"+strArray[3];
                                Log.i(TAG, "要播放RTSP:"+rtspUrl);
                                //MainActivity.getInstance().StartPlay(rtspUrl);
                                CastSinkSDK.getInstance().Play(rtspUrl);
                            }
                        }
                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                Log.e(TAG,e.toString());
                CastSinkSDK.getInstance().OnException();
            }
        }

        public void cancel() {
            mRunFlag = false;
        }
    }


    public void createServerSocket() {
        instance_ = this;

        if (null != mConnectThread) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    /**
     * 服务断开：释放内存
     */
    public void clean() {
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
