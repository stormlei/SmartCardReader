package com.qpsoft.smartcardreader.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.qpsoft.smartcardreader.MyApp;


public class BtUtils {

    public static BluetoothAdapter getBtAdapter() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) MyApp.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter();
    }

    public static String getName() {
        BluetoothAdapter mBluetoothAdapter = getBtAdapter();
        return mBluetoothAdapter.getName();
    }

    public static void setName(String name) {
        String prefix = "qpsoft-";
        String bleName = prefix+name;
        BluetoothAdapter mBluetoothAdapter = getBtAdapter();
        mBluetoothAdapter.setName(bleName);
    }


    public static boolean isEnabled() {
        BluetoothAdapter mBluetoothAdapter = getBtAdapter();
        return mBluetoothAdapter.isEnabled();
    }
}
