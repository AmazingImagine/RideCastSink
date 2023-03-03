package com.ridehome.castsink;

import android.content.Context;

import com.ridehome.castsink.streamplayer.StreamPlayer;
import com.ridehome.castsink.wifip2p.WifiP2PController;

public class CastSinkSDK {
    private StreamPlayer mStreamPlayer;
    private WifiP2PController mWifiP2PController;

    private static CastSinkSDK instance_;
    public static CastSinkSDK getInstance(){
        return instance_;
    }

    public void init(Context context){
        instance_ = this;
        mStreamPlayer = new StreamPlayer(context);
        mWifiP2PController = new WifiP2PController(context);
        mWifiP2PController.Init();
        mWifiP2PController.Start();
    }

    public void unInit(){

    }

    public void OnException(){
        // 回收，释放资源
        mWifiP2PController.Stop();
        // 重新开始
        mWifiP2PController.Start();
    }

    public void Play(String strMRL){
        mStreamPlayer.Play(strMRL);
    }

    public void Stop(){
        mStreamPlayer.Stop();
    }
}
