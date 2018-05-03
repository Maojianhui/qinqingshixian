package com.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.model.Constant;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.utils.GetPostUtil;
import com.app.utils.ToastUtils;


public class UpdateNickActivity extends Activity {
    String response;
    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ActivityCollector.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_nick);
        final String nick = LocalUserInfo.getInstance(UpdateNickActivity.this).getUserInfo("nick");
        final EditText et_nick = (EditText) this.findViewById(R.id.et_nick);
        et_nick.setText(nick);
        ImageView back = (ImageView) this.findViewById(R.id.iv_back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpdateNickActivity.this, MyUserInfoActivity.class));
                finish();
            }
        });
        TextView tv_save = (TextView) this.findViewById(R.id.tv_save);
        tv_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String newNick = et_nick.getText().toString().trim();
                if (nick.equals(newNick) || newNick.equals("") || newNick.equals("0")) {
                    return;
                }
                updateIvnServer(newNick);
            }

        });


    }

    Handler myhandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                dialog.dismiss();
                finish();
            }else if(msg.what==2){
                dialog.dismiss();
            }
        }
    };
    private void updateIvnServer(final String newNick) {
        dialog = new ProgressDialog(UpdateNickActivity.this);
        dialog.setMessage("正在更新...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                response = GetPostUtil.sendGet1111(Constant.URL_UPDATE_Nick, "userid=" + SipInfo.userId +
                        "&" + "name=" + newNick);

                Log.i("jonsresponse", response+"");
                if (null!=response&&!"".equals(response)) {
                    JSONObject obj = JSON.parseObject(response);

                    String msg = obj.getString("msg");
                    if (msg.equals("fail")) {
                        ToastUtils.showShort(UpdateNickActivity.this, msg);
                        myhandle.sendEmptyMessage(2);
                    } else if (msg.equals("success")) {
                        Looper.prepare();
                        ToastUtils.showShort(UpdateNickActivity.this, msg);
                        LocalUserInfo.getInstance(UpdateNickActivity.this).setUserInfo("nick", newNick);
                        myhandle.sendEmptyMessage(1);
                        Looper.loop();
                    }
                }else {
                    myhandle.sendEmptyMessage(2);
                }
            }
        }.start();
    }
}

