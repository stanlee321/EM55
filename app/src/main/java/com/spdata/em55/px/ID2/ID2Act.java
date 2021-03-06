package com.spdata.em55.px.ID2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.spdata.em55.R;
import com.spdata.em55.base.BaseAct;
import com.spdata.em55.px.print.utils.ApplicationContext;
import com.speedata.libid2.IDInfor;
import com.speedata.libid2.IDManager;
import com.speedata.libid2.IDReadCallBack;
import com.speedata.libid2.IID2Service;
import com.speedata.libutils.ConfigUtils;
import com.speedata.libutils.ReadBean;

import java.io.IOException;
import java.util.List;

public class ID2Act extends BaseAct {

    private static final String TAG = "ID_DEV";
    private static final String SERIALPORT_PATH = "/dev/ttyMT2";
    private ToggleButton btnStarRead;
    private Button findBtn;
    private Button chooseBtn;
    private Button readBtn;
    private Button sendBtn;
    private Button btnReadCard;
    private TextView contView;
    private EditText EditTextsend;
    private ImageView mImageViewPhoto;
    private TextView mtextname;
    private TextView mtextsex;
    private TextView mtextminzu;
    private TextView mtextyear;
    private TextView mtextmouth;
    private TextView mtextday;
    private TextView mtextaddr;
    private TextView mtextnum;
    private TextView mtextqianfa;
    private TextView mtextqixian;
    private TextView tvConfig;
    private IDInfor idInfor;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            iid2Service.getIDInfor(false, btnStarRead.isChecked());
            idInfor = (IDInfor) msg.obj;
            if (idInfor.isSuccess()) {
                play(1, 0);
//                try {
//                    iid2Service.releaseDev();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                initID();
                mtextsex.setText(idInfor.getSex());
                mtextname.setText(idInfor.getName());
                mtextaddr.setText(idInfor.getAddress());
                mtextminzu.setText(idInfor.getNation());
                mtextyear.setText(idInfor.getYear());
                mtextmouth.setText(idInfor.getMonth());
                mtextday.setText(idInfor.getDay());
                mtextnum.setText(idInfor.getNum());
                String sss = idInfor.getNum();
                Log.i(TAG, "handleMessage: " + sss);
                mtextqianfa.setText(idInfor.getQianFa());
                mtextqixian.setText(idInfor.getDeadLine());
                mImageViewPhoto.setImageBitmap(idInfor.getBmps());
                if (idInfor.isWithFinger()) {
                    //有zhiwen
                    byte[] fp = new byte[1024];
                    fp = idInfor.getFingerprStringer();
                    Toast.makeText(ID2Act.this, "该身份证有指纹！", Toast.LENGTH_SHORT).show();
                }
            } else {
//                play(3, 0);
//                contView.setText(idInfor.getErrorMsg());
//                initID2Info();
            }

        }
    };
    private IID2Service iid2Service;
    private ProgressDialog progressDialog;

    private void initID2Info() {
        mtextsex.setText("男");
        mtextname.setText("张三");
        mtextaddr.setText("北京市海淀区上地六街28致远大厦");
        mtextminzu.setText("汉");
        mtextyear.setText("2016");
        mtextmouth.setText("12");
        mtextday.setText("21");
        mtextnum.setText("101101199509084323");
        mtextqianfa.setText("北京市公安局");
        mtextqixian.setText("2016.01.01-2026.01.01");
        mImageViewPhoto.setImageBitmap(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationContext.getInstance().addActivity(ID2Act.this);
        setContentView(R.layout.act_id2);
        initUI();
        initID2Info();

        Log.i(TAG, "==onCreate==");
    }

    @Override
    protected void onResume() {
        super.onResume();

        initIDService();
        boolean isExit = ConfigUtils.isConfigFileExists();
        if (isExit)
            tvConfig.setText("定制配置：\n");
        else
            tvConfig.setText("标准配置：\n");
        ReadBean.Id2Bean pasm = ConfigUtils.readConfig(this).getId2();
        String gpio = "";
        List<Integer> gpio1 = pasm.getGpio();
        for (Integer s : gpio1) {
            gpio += s + ",";
        }
        tvConfig.append("串口:" + pasm.getSerialPort() + "  波特率：" + pasm.getBraut() + " 上电类型:" +
                pasm.getPowerType() + " GPIO:" + gpio);
    }


    private void initUI() {
        tvConfig = (TextView) findViewById(R.id.tv_id2_verson);
        findBtn = (Button) findViewById(R.id.button_find);
        chooseBtn = (Button) findViewById(R.id.button_choose);
        readBtn = (Button) findViewById(R.id.button_read);
        btnReadCard = (Button) findViewById(R.id.button_finger);
//        findBtn.setOnClickListener(this);
//        chooseBtn.setOnClickListener(this);
//        readBtn.setOnClickListener(this);
//        btnReadCard.setOnClickListener(this);
        contView = (TextView) findViewById(R.id.tv_content);
        mtextname = (TextView) findViewById(R.id.textname);
        mtextsex = (TextView) findViewById(R.id.textsex);
        mtextminzu = (TextView) findViewById(R.id.textminzu);
        mtextyear = (TextView) findViewById(R.id.textyear);
        mtextmouth = (TextView) findViewById(R.id.textmouth);
        mtextday = (TextView) findViewById(R.id.textday);
        mtextaddr = (TextView) findViewById(R.id.textaddr);
        mtextnum = (TextView) findViewById(R.id.textsfz);
        mtextqianfa = (TextView) findViewById(R.id.textqianfa);
        mtextqixian = (TextView) findViewById(R.id.textqixian);
        mImageViewPhoto = (ImageView) findViewById(R.id.imageViewPortrait);
        btnStarRead = (ToggleButton) findViewById(R.id.button_startread);
        btnStarRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                iid2Service.getIDInfor(false, isChecked);
                if (!isChecked) {
                    initID2Info();
                }
            }
        });
//        try {
//            iid2Service.initDev(ID2Act.this, new IDReadCallBack() {
//                @Override
//                public void callBack(IDInfor infor) {
//                 //// TODO: 2017/6/6   接收数据
//                }
//            },SERIAL_TTYMT2,115200, DeviceControl.PowerType.MAIN_AND_EXPAND,88,7);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    private int dia=1;
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == dia) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在初始化");
            progressDialog.setCancelable(false);
        }
        return progressDialog;
    }

    /**
     * 初始化二代证模块   失败退出
     */
    public void initIDService() {
        iid2Service = IDManager.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在初始化");
        progressDialog.setCancelable(false);
        progressDialog.show();
//        showDialog(dia);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean result = iid2Service.initDev(ID2Act.this, new
                            IDReadCallBack() {
                                @Override
                                public void callBack(IDInfor infor) {
                                    Message message = new Message();
                                    message.obj = infor;
                                    handler.sendMessage(message);
                                }
                            });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            dismissDialog(dia);
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                                progressDialog.cancel();
                            }
                            if (!result) {
                                new AlertDialog.Builder(ID2Act.this).setCancelable(false)
                                        .setMessage("二代证模块初始化失败")
                                        .setPositiveButton("确定", new DialogInterface
                                                .OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialogInterface,
                                                                int i) {
                                                finish();
                                            }
                                        }).show();
                            } else {
                                showToast("初始化成功");
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            iid2Service.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog.cancel();
//            progressDialog=null;
        }
        super.onDestroy();
        try {
            if (iid2Service != null)
                iid2Service.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
