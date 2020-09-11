package com.qpsoft.smartcardreader;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.qpsoft.smartcardreader.bleservice.BlePeripheralUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class MyApp extends Application {

    private static MyApp instance ;

    public static MyApp getInstance() {
        return instance;
    }


    public BlePeripheralUtils getBlePeripheralUtils(Context context) {
        if (blePeripheralUtils == null) {
            blePeripheralUtils = new BlePeripheralUtils(context);
        }
        return blePeripheralUtils;
    }

    private BlePeripheralUtils blePeripheralUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Utils.init(this);
        LogUtils.getConfig().setLogSwitch(true);

        ZXingLibrary.initDisplayOpinion(this);
    }
}
