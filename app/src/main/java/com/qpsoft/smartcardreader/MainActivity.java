package com.qpsoft.smartcardreader;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.qpsoft.smartcardreader.bleservice.BlePeripheralCallback;
import com.qpsoft.smartcardreader.bleservice.BlePeripheralUtils;
import com.qpsoft.smartcardreader.utils.BtUtils;
import com.reader.usbdevice.DeviceLib;
import com.reader.usbdevice.DeviceStatusCallback;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.UnsupportedEncodingException;

import fynn.app.PromptDialog;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llGroup;

    private DeviceLib mdev = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, BleService.class));
        initBleCallBack();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showQrCode();
            }
        }, 5000);

        InitEditText();
        InitButton();

        mdev = new DeviceLib(getApplicationContext(), new DeviceStatusCallback() {
            @Override
            public void UsbAttach() {
                showToast(getString(R.string.dev_link_succ));
                mdev.openDevice(100);
            }
            @Override
            public void UsbDeAttach() {
                showToast(getString(R.string.dev_link_error));
            }
        });
        mdev.openDevice(100);

        //openHttpServer();

        showString("等待获取读卡信息...");
    }

    private void showString(String msg) {
        TextView tv = new TextView(this);
        tv.setTextColor(getResources().getColor(R.color.color_00));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setText(msg);
        llGroup.addView(tv);
    }

    private void showToast(String msg) { Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show(); }

    private void InitEditText() {
        llGroup = (LinearLayout)findViewById(R.id.ll);
    }

    private void InitButton() {

    }

    private CardInfo idCardInfo;
    private void readIDCard() {
        if (!mdev.isOpen()) {
            //showString("设备未连接!"); return;
            mdev.openDevice(100);
        }
        llGroup.removeAllViews();
        String pkName=this.getPackageName();
        String show="";
        int nRt = mdev.PICC_ReadIDCardMsg(pkName);
        if( nRt != 0){
            showString("身份证读取失败，ret=" + nRt);
            return;
        }

        //beep
        mdev.ICC_PosBeep((byte) 10);

//        Bitmap bm1 = mdev.getBmpfile();
//        ImageView iv=new ImageView(this);
//        iv.setImageBitmap(bm1);
//        llGroup.addView(iv);

        if(mdev.GetCardType() == 0){
            showString("居民身份证\n");
            show = "姓名: "+ mdev.getName() +'\n'+'\n'
                    +"性别: "+ mdev.getSex() +'\n'+'\n'
                    +"民族: "+ mdev.getNation() +"族"+'\n'+'\n'
                    +"出生日期: "+ mdev.getBirth() +'\n'+'\n'
                    +"住址: "+ mdev.getAddress() +'\n'+'\n'
                    +"身份证号码: "+ mdev.getIDNo() +'\n'+'\n'
                    +"签发机关: "+ mdev.getDepartment() +'\n'+'\n'
                    +"有效日期: "+  mdev.getEffectDate()  + "至" +  mdev.getExpireDate() +'\n';
        }
        if(mdev.GetCardType() == 1){
            showString("外国人永久居留证");
            show ="中文姓名: "+ mdev.getName() +'\n'
                    +"英文姓名: "+ mdev.getEnName() +'\n'
                    +"性别: "+ mdev.getSex() +'\n'
                    +"国籍代码: "+ mdev.getNationalityCode() +'\n'
                    +"永久居留证号码: "+ mdev.getIDNo() +'\n'
                    +"出生日期: "+ mdev.getBirth() +'\n'
                    +"有效日期: "+  mdev.getEffectDate()  + "至" +  mdev.getExpireDate() +'\n';
        }
        if(mdev.GetCardType() == 2){
            showString("港澳台居民居住证");
            show = "姓名: "+ mdev.getName() +'\n'
                    +"性别: "+ mdev.getSex() +'\n'
                    +"出生日期: "+ mdev.getBirth() +'\n'
                    +"住址: "+ mdev.getAddress() +'\n'
                    +"身份证号码: "+ mdev.getIDNo() +'\n'
                    +"签发机关: "+ mdev.getDepartment() +'\n'
                    +"通行证号码: "+ mdev.getTXZHM() +'\n'
                    +"通行证签发次数: "+ mdev.getTXZQFCS() +'\n'
                    +"有效日期: "+  mdev.getEffectDate()  + "至" +  mdev.getExpireDate() +'\n';
        }
        showString(show);

        idCardInfo = new CardInfo();
        idCardInfo.setName(mdev.getName().trim());
        idCardInfo.setGender(mdev.getSex());
        idCardInfo.setNationality(mdev.getNation());
        idCardInfo.setBirthday(mdev.getBirth());
        idCardInfo.setAddress(mdev.getAddress());
        idCardInfo.setCardNumber(mdev.getIDNo());
        int nativePlaceCode = Integer.parseInt(mdev.getIDNo().substring(0, 6));
        idCardInfo.setNativePlace(NativePlace.getNativePlace(nativePlaceCode));
        idCardInfo.setIssuingAuthority(mdev.getDepartment());
        idCardInfo.setStartValidateDate(mdev.getEffectDate());
        idCardInfo.setEndValidateDate(mdev.getExpireDate());

        blePeripheralUtils.sendJson(JSON.toJSONString(idCardInfo));
    }

    private void readSiCard() {
        if (!mdev.isOpen()) {
            //showString("设备未连接!"); return;
            mdev.openDevice(100);
        }
        llGroup.removeAllViews();
        int nRt = -99;
        byte[] cardInfo = new byte[500];
        nRt = mdev.iReadSiCard((byte) 0x11, cardInfo);
        if (nRt != 0) {
            showString("读卡失败:"+ nRt); return;
        }
        try {
            //beep
            mdev.ICC_PosBeep((byte) 10);

            //showString("读卡成功："+ new String(cardInfo,"gbk"));

            String result = new String(cardInfo,"gbk");
            String[] ss = result.split("\\|");
            if (ss.length >= 8) {
                String khNumber = ss[0];
                String startValidateDate = ss[1];
                String endValidateDate = ss[2];
                String name = ss[3];
                String sex = ss[4];
                String nationality = ss[5];
                String cardNumber = ss[6];
                String birthday = ss[7];


                String show = "姓名: "+ name +'\n'+'\n'
                        +"卡号: "+ khNumber +'\n'+'\n'
                        //+"性别: "+ cardNumber +'\n'
                        //+"民族: "+ mdev.getNation() +"族"+'\n'
                        +"身份证号码: "+ cardNumber +'\n'+'\n'
                        +"出生日期: "+ birthday +'\n'+'\n'
                        +"发卡日期: "+  startValidateDate +'\n'+'\n'
                        +"卡有效期: "+  endValidateDate +'\n';

                showString("读卡成功：\n\n"+ show);


                idCardInfo = new CardInfo();
                idCardInfo.setName(name.trim());
                //idCardInfo.setGender();
                //idCardInfo.setNationality();
                idCardInfo.setBirthday(birthday);
                idCardInfo.setCardNumber(cardNumber);
                int nativePlaceCode = Integer.parseInt(cardNumber.substring(0, 6));
                idCardInfo.setNativePlace(NativePlace.getNativePlace(nativePlaceCode));
                idCardInfo.setStartValidateDate(startValidateDate);
                idCardInfo.setEndValidateDate(endValidateDate);
                idCardInfo.setKhNumber(khNumber);

                blePeripheralUtils.sendJson(JSON.toJSONString(idCardInfo));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void showQrCode() {
        ImageView ivQrCode = findViewById(R.id.ivQrCode);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", "读卡器");
        jsonObj.put("bluetooth_name", BtUtils.getName());
        jsonObj.put("name", "Q1");
        jsonObj.put("sn", Build.SERIAL);
        jsonObj.put("service_uuid", AppConfig.UUID_SERVER);
        jsonObj.put("notify_uuid", AppConfig.UUID_NOTIFY);
        jsonObj.put("write_uuid", AppConfig.UUID_WRITE);
        String txtStr = jsonObj.toJSONString();
        LogUtils.e("qrcode-----------"+txtStr);
        Bitmap qrBitmap = CodeUtils.createImage(txtStr, 400, 400, null);

        ivQrCode.setImageBitmap(qrBitmap);
    }


    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();
    private int listenPort = 5000;

    private void openHttpServer() {
        server.get("/getCardData", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send(JSON.toJSONString(idCardInfo));
                idCardInfo = null;
            }
        });

        // listen on port 5000
        server.listen(mAsyncServer, listenPort);
    }


    BlePeripheralUtils blePeripheralUtils;
    private void initBleCallBack() {
        blePeripheralUtils = MyApp.getInstance().getBlePeripheralUtils(this);
        //设置一个结果callback 方便把某些结果传到前面来
        blePeripheralUtils.setBlePeripheralCallback(callback);
    }

    BlePeripheralCallback callback = new BlePeripheralCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothDevice device, int status, final int newState) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newState == 2) {
                        //ToastUtils.showShort(device.getAddress()+"-----"+newState);
                        ToastUtils.showShort("连接成功");
                        sb.setLength(0);
                    } else {
                        ToastUtils.showShort("已断开");
                    }
                }
            });
        }
        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, final byte[] requestBytes) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = new String(requestBytes);
                    String result = sb.append(message).toString();
                    if (result.endsWith("}}")) {
                        parseData(result);
                        sb.setLength(0);
                    }

                }
            });
        }
    };
    private StringBuilder sb = new StringBuilder();

    private void parseData(String message) {
        if (message.contains("action")) {
            JSONObject jsonObj = JSON.parseObject(message);
            String action = jsonObj.getString("action");
            if ("command".equals(action)) {
                JSONObject payloadObj = jsonObj.getJSONObject("payload");
                String cmd = payloadObj.getString("cmd");
                if ("cert_card".equals(cmd)) {
                    readIDCard();
                } else if("si_card".equals(cmd)) {
                    readSiCard();
                }
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        if (mdev!=null) {
            mdev.unregisterUsbCallback();
        }
        if (server != null) {
            server.stop();
        }

        if (mAsyncServer != null) {
            mAsyncServer.stop();
        }

        stopService(new Intent(this, BleService.class));

        blePeripheralUtils.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private long exitTime = 0;

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }

    }


}
