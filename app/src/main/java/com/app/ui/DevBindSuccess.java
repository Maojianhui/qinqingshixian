package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.model.Constant.devid1;
import static com.app.model.Constant.groupid1;
import static com.app.sip.SipInfo.devName;


public class DevBindSuccess extends Activity {

    @Bind(R.id.iv_bindsuccess)
    ImageView ivBindsuccess;
    @Bind(R.id.tv_devname)
    TextView tvDevname;
    @Bind(R.id.tv_devnumber)
    TextView tvDevnumber;
    @Bind(R.id.bt_unbind1)
    Button btUnbind1;
    @Bind(R.id.iv_back8)
    ImageView ivBack8;
    @Bind(R.id.textView4)
    TextView textView4;

    private String response;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_bind_success);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);
        changStatusIconCollor(true);
        TextPaint tp=textView4.getPaint();
        tp.setFakeBoldText(true);
        if (devid1 != null) {
            tvDevnumber.setText(SipInfo.paddevId);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 444) {
                String devId = SipInfo.paddevId;
                SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                SipInfo.toDev = new NameAddress(devName, sipURL);
                org.zoolu.sip.message.Message query = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                        SipInfo.user_from, BodyFactory.createListUpdate("addsuccess"));
                SipInfo.sipUser.sendMessage(query);
                devid1 = "";
                Toast.makeText(getApplicationContext(),"解绑成功",Toast.LENGTH_SHORT).show();
                finish();
            }else if(message.what==555){
                ToastUtils.makeShortText("解绑失败",getApplicationContext());
            }
        }
    };

    @OnClick({R.id.bt_unbind1, R.id.iv_back8})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back8:
                ActivityCollector.removeActivity(this);
                finish();
            case R.id.bt_unbind1:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("是否解绑")
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if ((devid1 != null) && !("".equals(devid1))) {
                                    Log.i("jiebang", "111");
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            response = GetPostUtil.sendGet1111(Constant.URL_leaveGroup, "id=" + Constant.id + "&groupid=" + groupid1+"&devid="+devid1);
                                                        Log.i("jonsresponse...........", response);
                                                        JSONObject obj = JSON.parseObject(response);
                                                        String msg = obj.getString("msg");
                                                        if (msg.equals("success")) {
                                                            handler.sendEmptyMessage(444);
//                                Toast.makeText(saomaActivity.this,"解绑成功",Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            handler.sendEmptyMessage(555);
//                                Toast.makeText(saomaActivity.this,"解绑失败",Toast.LENGTH_SHORT).show();
                                                        }



//                                            response = GetPostUtil.sendGet1111(Constant.URL_queryCluster, "id=" + Constant.id);
//                                            Log.i("jonsresponse...........", response);
//                                            JSONObject obj = JSON.parseObject(response);
//                                            String msg = obj.getString("msg");
//                                            if (msg.equals("2")) {
//                                                new Thread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        response = GetPostUtil.sendGet1111(Constant.URL_UnBind, "id=" + Constant.id + "&devid=" + devid1);
//                                                        Log.i("jonsresponse...........", response);
//                                                        JSONObject obj = JSON.parseObject(response);
//                                                        String msg = obj.getString("msg");
//                                                        if (msg.equals("success")) {
//
//                                                            handler.sendEmptyMessage(444);
////                                Toast.makeText(saomaActivity.this,"解绑成功",Toast.LENGTH_SHORT).show();
//                                                        }
//                                                        else{
//                                                            handler.sendEmptyMessage(555);
//                                                        }
//                                                    }
//                                                }).start();
//
//                                            } else if (msg.equals("0")) {
//                                                new Thread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        response = GetPostUtil.sendGet1111(Constant.URL_leaveGroup, "id=" + Constant.id + "&groupid=" + groupid1);
//                                                        Log.i("jonsresponse...........", response);
//                                                        JSONObject obj = JSON.parseObject(response);
//                                                        String msg = obj.getString("msg");
//                                                        if (msg.equals("success")) {
//                                                            handler.sendEmptyMessage(444);
////                                Toast.makeText(saomaActivity.this,"解绑成功",Toast.LENGTH_SHORT).show();
//                                                        } else {
//                                                            handler.sendEmptyMessage(555);
////                                Toast.makeText(saomaActivity.this,"解绑失败",Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    }
//                                                }).start();
//
//                                            }
                                        }
                                    }).start();


                                }
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;
        }
    }

    public void changStatusIconCollor(boolean setDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                int vis = decorView.getSystemUiVisibility();
                if (setDark) {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
}
