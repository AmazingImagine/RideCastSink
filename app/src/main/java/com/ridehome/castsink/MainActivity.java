package com.ridehome.castsink;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ridehome.castsink.vlc.MyControlVlcVideoView;
import com.ridehome.castsink.wifip2p.activity.BaseActivity;
import com.ridehome.castsink.wifip2p.service.Wifip2pService;
import com.ridehome.castsink.wifip2p.socket.ReceiveSocket;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
public class MainActivity extends BaseActivity implements ReceiveSocket.ProgressReceiveListener,EasyPermissions.PermissionCallbacks, View.OnClickListener  {
    public static final String TAG = "MainActivity";

    private Wifip2pService.MyBinder mBinder;
    private Intent mIntent;

    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;
    private String mRtspUrl;

    public static String mPath01 = "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AAOEcdM.img";
    private RelativeLayout rootView;
    private MyControlVlcVideoView mPlayerView;
    private View.OnTouchListener onTouchVideoListener;

    private static MainActivity mInstance;
    public static MainActivity getInstance(){
        return mInstance;
    }

    public void StartPlay(String strUrl){
        mRtspUrl = strUrl;
        runOnUiThread(() -> {
            EditText urlText = (EditText) findViewById(R.id.rtspURL);
            urlText.setText(strUrl);
            // 播放RTSP
            mPlayerView.setStartLive(strUrl);
        });
    }

    private void initView() {
        mPlayerView = findViewById(R.id.player);
        //触摸控制亮度和声音,是否可触摸开关
        rootView = mPlayerView.getRootView();
        onTouchVideoListener = mPlayerView.getOnTouchVideoListener();
        rootView.setLongClickable(true);  //手势需要--能触摸
        rootView.setOnTouchListener(onTouchVideoListener);

        mPlayerView.setPlayerTitle("P2P投屏世界");

        mPlayerView.setVlcControllerLayoutListener(new MyControlVlcVideoView.onVlcControllerLayoutListener() {
            @Override
            public void finishActivity() {
                finish();
            }
        });

//        mPlayerView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //开始播放
//                mPlayerView.setStartLive(mPath01);
//            }
//        },500);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //调用服务里面的方法进行绑定
            mBinder = (Wifip2pService.MyBinder) service;
            mBinder.initListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //服务断开重新绑定
            bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInstance = this;

        requireSomePermission();

        Button btnCreate = (Button) findViewById(R.id.btn_create);
        Button btnRemove = (Button) findViewById(R.id.btn_remove);
        btnCreate.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        Button btnPlayRTSPP = (Button) findViewById(R.id.btn_playrtsp);
        Button btnStopRTSP = (Button) findViewById(R.id.btn_stoprtsp);
        btnPlayRTSPP.setOnClickListener(this);
        btnStopRTSP.setOnClickListener(this);

        initView();
    }

    @AfterPermissionGranted(1000)
    private void requireSomePermission() {
        String[] perms = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            //有权限
        } else {
            //没权限
            EasyPermissions.requestPermissions(this, "需要文件读取权限",
                    1000, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e(TAG,"权限申成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e(TAG,"权限申请失败");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSatrt() {

    }

    @Override
    public void onProgressChanged(File file, int progress) {

    }

    @Override
    public void onFinished(File file) {

    }

    @Override
    public void onFaliure(File file) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create:
                createGroup();
                break;
            case R.id.btn_remove:
                removeGroup();
                break;
            case R.id.btn_playrtsp: {
                EditText urlText = (EditText) findViewById(R.id.rtspURL);
                String strUrl = urlText.getText().toString();
                //mStreamUI.Start(strUrl);
            }
                break;
            case R.id.btn_stoprtsp:
                //mStreamUI.Stop();
                break;
        }
    }

    /**
     * 创建组群，等待连接
     */
    public void createGroup() {

        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "创建群组成功");

                mIntent = new Intent(MainActivity.this, Wifip2pService.class);
                //startService(mIntent);
                bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "创建群组失败: " + reason);                Toast.makeText(MainActivity.this, "创建群组失败,请移除已有的组群或者连接同一WIFI重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 移除组群
     */
    public void removeGroup() {
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "移除组群成功");
                Toast.makeText(MainActivity.this, "移除组群成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "移除组群失败");
                Toast.makeText(MainActivity.this, "移除组群失败,请创建组群重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         clear();
    }

    /**
     * 释放资源
     */
    private void clear() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        if (mIntent != null) {
            stopService(mIntent);
        }
    }
}