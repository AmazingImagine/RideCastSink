package com.ridehome.sinktestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;

import com.ridehome.castsink.CastSinkSDK;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private  CastSinkSDK mSinkSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requireSomePermission();

        mSinkSDK = new CastSinkSDK();
        mSinkSDK.init(this);
    }

    @AfterPermissionGranted(1000)
    private void requireSomePermission() {
        String[] perms = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
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
}