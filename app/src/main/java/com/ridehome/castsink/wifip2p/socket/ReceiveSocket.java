package com.ridehome.castsink.wifip2p.socket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ridehome.castsink.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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


    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 40:
                    if (mListener != null) {
                        mListener.onSatrt();
                    }
                    break;
            }
        }
    };

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


                // 客户端IP地址 : /192.168.49.74:60482
                //mInputStream = mSocket.getInputStream();
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
                                MainActivity.getInstance().StartPlay(rtspUrl);
                            }
                        }
                    }
                    Thread.sleep(30);
                }
            } catch (Exception e) {
                Log.e(TAG,e.toString());
                Log.e(TAG, "P2P信息交互出现问题");
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
     * 监听接收进度
     */
    private ProgressReceiveListener mListener;

    public void setOnProgressReceiveListener(ProgressReceiveListener listener) {
        mListener = listener;
    }

    public interface ProgressReceiveListener {

        //开始传输
        void onSatrt();

        //当传输进度发生变化时
        void onProgressChanged(File file, int progress);

        //当传输结束时
        void onFinished(File file);

        //传输失败回调
        void onFaliure(File file);
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
