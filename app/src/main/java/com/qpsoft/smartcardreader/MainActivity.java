package com.qpsoft.smartcardreader;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.reader.usbdevice.DeviceLib;
import com.reader.usbdevice.DeviceStatusCallback;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.UnsupportedEncodingException;

import fynn.app.PromptDialog;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llGroup;
    private LinearLayout llConnError;
    private LinearLayout llConnOk;

    private DeviceLib mdev = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                llConnOk.setVisibility(View.GONE);
                llConnError.setVisibility(View.VISIBLE);
            }
        });
        mdev.openDevice(100);

        openHttpServer();

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
        llConnError = (LinearLayout)findViewById(R.id.llConnError);
        llConnOk = (LinearLayout)findViewById(R.id.llConnOk);
    }

    private void InitButton() {
        TextView tvSiCardRead = (TextView)findViewById(R.id.tvSi);
        TextView tvCertCardRead = (TextView)findViewById(R.id.tvCert);
        TextView tvShowQrCode = (TextView)findViewById(R.id.tvShowQrCode);

        tvSiCardRead.setOnClickListener(new SampleOnClickListener());
        tvCertCardRead.setOnClickListener(new SampleOnClickListener());
        tvShowQrCode.setOnClickListener(new SampleOnClickListener());
    }

    private class SampleOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                switch (view.getId()) {
                    case R.id.tvCert:
                        readIDCard();
                        break;
                    case R.id.tvSi:
                        readSiCard();
                        break;
                    case R.id.tvShowQrCode:
                        showQrCode();
                        break;
                    default:
                        break;
                }
            } catch (SecurityException securityException) {
                showToast(getString(R.string.error));
            }
        }
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

        llConnOk.setVisibility(View.VISIBLE);
        llConnError.setVisibility(View.GONE);

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
        idCardInfo.setName(mdev.getName());
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

            llConnOk.setVisibility(View.VISIBLE);
            llConnError.setVisibility(View.GONE);

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
                idCardInfo.setName(name);
                //idCardInfo.setGender();
                //idCardInfo.setNationality();
                idCardInfo.setBirthday(birthday);
                idCardInfo.setCardNumber(cardNumber);
                int nativePlaceCode = Integer.parseInt(cardNumber.substring(0, 6));
                idCardInfo.setNativePlace(NativePlace.getNativePlace(nativePlaceCode));
                idCardInfo.setStartValidateDate(startValidateDate);
                idCardInfo.setEndValidateDate(endValidateDate);
                idCardInfo.setKhNumber(khNumber);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void showQrCode() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qrcode, null, false);
        new PromptDialog.Builder(MainActivity.this)
                .setTitle("小程序扫二维码")
                .setView(dialogView)
                .setCanceledOnTouchOutside(true)
                .show();

        ImageView ivQrCode = dialogView.findViewById(R.id.ivQrCode);
        String ip = NetworkUtils.getIPAddress(true);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("dataId", "32165488");
        jsonObj.put("type", "读卡器");
        jsonObj.put("name", "hd-100");
        jsonObj.put("endpoint", "http://"+ip+":"+listenPort+"/getCardData");
        String txtStr = jsonObj.toJSONString();
        LogUtils.e("qrcode-----------"+txtStr);
        Bitmap qrBitmap = CodeUtils.createImage(txtStr, 300, 300, null);

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
