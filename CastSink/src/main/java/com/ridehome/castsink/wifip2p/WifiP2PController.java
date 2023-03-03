package com.ridehome.castsink.wifip2p;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;

public class WifiP2PController implements Wifip2pActionListener {
    private final String TAG = "WifiP2PController";
    private Context mContext = null;
    public WifiP2pManager.Channel mChannel;
    public WifiP2pManager mWifiP2pManager;
    public Wifip2pReceiver mWifip2pReceiver;

    private ReceiveSocket mReceiveSocket = new ReceiveSocket();
    private Intent mIntent;

    private int mRetryCount = 0;

    private boolean mIsInGroup = false;

    public WifiP2PController(Context context){
        mContext = context;
    }

    public void Init(){
        //注册WifiP2pManager
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), this);

        //注册广播
        mWifip2pReceiver = new Wifip2pReceiver(mWifiP2pManager, mChannel, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mContext.registerReceiver(mWifip2pReceiver, intentFilter);
    }

    public void Start(){
        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mRetryCount = 0;

                Log.e(TAG, "创建群组成功");

                mIsInGroup = true;

                mReceiveSocket.createServerSocket();
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "创建群组失败: " + reason);
                mRetryCount++;
                if(mRetryCount<3){
                    Stop();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Start();
                }
            }
        });
    }

    public void Stop(){

            if(mIsInGroup){
                mIsInGroup = false;
                mReceiveSocket.clean();
            }

            mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e(TAG, "移除组群成功");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "移除组群失败");
                }
            });
    }


    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void wifiP2pEnabled(boolean enabled) {

    }

    @Override
    public void onConnection(WifiP2pInfo wifiP2pInfo) {

    }

    @Override
    public void onDisconnection() {

    }

    @Override
    public void onDeviceInfo(WifiP2pDevice wifiP2pDevice) {

    }

    @Override
    public void onPeersInfo(Collection<WifiP2pDevice> wifiP2pDeviceList) {

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //服务断开重新绑定
            mContext.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };
}
