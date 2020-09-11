package com.qpsoft.smartcardreader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.qpsoft.smartcardreader.bleservice.BlePeripheralUtils;

public class BleService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        //实例化工具类
        BlePeripheralUtils blePeripheralUtils = MyApp.getInstance().getBlePeripheralUtils(this);
        //初始化一下
        blePeripheralUtils.init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }
}
