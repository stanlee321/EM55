package com.spdata.em55.px.psam;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spdata.em55.R;
import com.spdata.em55.base.BaseAct;
import com.spdata.em55.px.print.utils.ApplicationContext;
import com.spdata.em55.view.ProgersssDialog;
import com.speedata.libutils.ConfigUtils;
import com.speedata.libutils.DataConversionUtils;
import com.speedata.libutils.ReadBean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import speedatacom.a3310libs.PsamManager;
import speedatacom.a3310libs.inf.IPsam;

public class PsamAct extends BaseAct implements View.OnClickListener {

    //19200 9600
    private Button btn1Activite, btn2Activite, btnGetRomdan, btnSendAdpu, btnClear;
    private EditText edvADPU;
    private TextView tvShowData;
    private int psamflag = 0;
    private Context mContext;
    private TextView tvVerson;
    private TextView tvConfig;
    private ImageView imgReset;
    private ProgersssDialog progersssDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ApplicationContext.getInstance().addActivity(PsamAct.this);
        mContext = this;
        progersssDialog = new ProgersssDialog(this);
        initUI();
        showConfig();
        initDevice();

    }


    private void showConfig() {

        String verson = getVersion();
//        tvVerson.setText("V" + verson);

        boolean isExit = ConfigUtils.isConfigFileExists();
        if (isExit)
            tvConfig.setText(getString(R.string.psam_flag2)+"\n");
        else
            tvConfig.setText(getString(R.string.psam_flag)+"：\n");
        ReadBean.PasmBean pasm = ConfigUtils.readConfig(this).getPasm();
        String gpio = "";
        List<Integer> gpio1 = pasm.getGpio();
        for (Integer s : gpio1) {
            gpio += s + ",";
        }
        tvConfig.append(getString(R.string.psam_serialport) + pasm.getSerialPort() + getString(R.string.psam_baurate) + pasm.getBraut() + getString(R.string.psam_powertype) +
                pasm.getPowerType() + " GPIO:" + gpio + getString(R.string.psam_reset_gpio)  + pasm.getResetGpio());
    }


    private void initUI() {
        setContentView(R.layout.activity_main);
        imgReset = (ImageView) findViewById(R.id.img_reset);
        imgReset.setOnClickListener(this);
        tvConfig = (TextView) findViewById(R.id.tv_config);
        btn1Activite = (Button) findViewById(R.id.btn1_active);
        btn2Activite = (Button) findViewById(R.id.btn2_active);
        btnGetRomdan = (Button) findViewById(R.id.btn_get_ramdon);
        btnSendAdpu = (Button) findViewById(R.id.btn_send_adpu);
        btnClear = (Button) findViewById(R.id.btn_clear);
        tvVerson = (TextView) findViewById(R.id.tv_verson);
        btn1Activite.setOnClickListener(this);
        btn2Activite.setOnClickListener(this);
        btnGetRomdan.setOnClickListener(this);
        btnSendAdpu.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        tvShowData = (TextView) findViewById(R.id.tv_show_message);
        tvShowData.setMovementMethod(ScrollingMovementMethod.getInstance());
        edvADPU = (EditText) findViewById(R.id.edv_adpu_cmd);
        edvADPU.setText("0084000008");
        edvADPU.setText("0020000003");
        edvADPU.setText("80f0800008122a31632a3bafe0");
        edvADPU.setText("00A404000BA000000003454E45524759");
    }


    String send_data = "";
    //获取psam实例
    IPsam psamIntance = PsamManager.getPsamIntance();


    @Override
    public void onClick(View v) {
        if (v == imgReset) {
            psamIntance.resetDev();//复位
//            SystemClock.sleep(3000);
//            showToast("复位成功");

        } else if (v == btn1Activite) {
            psamflag = 1;
            byte[] data = psamIntance.PsamPower(IPsam.PowerType.Psam1);
            if (data != null)
                tvShowData.setText("Psam1 activite \n" + DataConversionUtils.byteArrayToString
                        (data));
            else {
                tvShowData.setText("failed");
            }
        } else if (v == btn2Activite) {
            psamflag = 2;
            byte[] data = psamIntance.PsamPower(IPsam.PowerType.Psam2);
            if (data != null)
                tvShowData.setText("Psam2 activite \n" + DataConversionUtils.byteArrayToString
                        (data));
            else {
                tvShowData.setText("failed");
            }
        } else if (v == btnGetRomdan) {
            if (psamflag == 1) {
                try {
                    tvShowData.setText("Psam1 Send data：00 84 00 00 08\n");
                    //读取长度20   最大延时10ms  此方法  耗时应该在15ms左右
                    byte[] data = psamIntance.WriteCmd(new byte[]{0x00, (byte) 0x84, 0x00, 0x00,
                            0x08}, IPsam
                            .PowerType.Psam1, 20, 10);
                    if (data != null)
                        tvShowData.append("rece->" + DataConversionUtils.byteArrayToString(data));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (psamflag == 2) {
                try {
                    tvShowData.setText("Psam2 Send data：00 84 00 00 08\n");
                    byte[] data = psamIntance.WriteCmd(new byte[]{0x00, (byte) 0x84, 0x00, 0x00,
                            0x08}, IPsam
                            .PowerType.Psam2);
                    if (data != null)
                        tvShowData.append("rece->" + DataConversionUtils.byteArrayToString(data));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnSendAdpu) {
            String temp_cmd = edvADPU.getText().toString();
            if ("".equals(temp_cmd) || temp_cmd.length() % 2 > 0 || temp_cmd.length() < 4) {
                Toast.makeText(mContext, "Please enter a valid instruction！", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            send_data = temp_cmd;
            if (psamflag == 1) {
                tvShowData.setText("Psam1 Send data：\n" + send_data + "\n");
                try {
                    byte[] data = psamIntance.WriteCmd(DataConversionUtils
                            .HexString2Bytes(temp_cmd), IPsam.PowerType.Psam1);
                    if (data != null)
                        tvShowData.append("rece->" + DataConversionUtils.byteArrayToString(data));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else if (psamflag == 2) {
                tvShowData.setText("Psam2 Send data：\n" + send_data + "\n");
                try {
                    byte[] data = psamIntance.WriteCmd(DataConversionUtils
                            .HexString2Bytes(temp_cmd), IPsam.PowerType.Psam2);
                    if (data != null)
                        tvShowData.append("rece->" + DataConversionUtils.byteArrayToString(data));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnClear) {
            tvShowData.setText("");
        }

    }


    private void initDevice() {
        try {
            psamIntance.initDev(this);//初始化设备
            progersssDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    psamIntance.resetDev();//复位
                    SystemClock.sleep(3000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progersssDialog.dismiss();
                        }
                    });
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            psamIntance.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 获取当前应用程序的版本号
//     */
//    private String getVersion() {
//        PackageManager pm = getPackageManager();
//        try {
//            PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
//            String version = packinfo.versionName;
//            return version;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return "版本号错误";
//        }
//    }
}
