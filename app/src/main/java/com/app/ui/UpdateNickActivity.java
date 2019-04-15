package com.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;
import com.app.http.GetPostUtil;
import com.app.http.ToastUtils;
import com.app.views.CleanEditText;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

import static com.app.sip.SipInfo.devName;


public class UpdateNickActivity extends Activity {
    String response;
    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ActivityCollector.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_nick);
        final String nick = LocalUserInfo.getInstance(UpdateNickActivity.this).getUserInfo("nick");
        final CleanEditText et_nick = (CleanEditText) this.findViewById(R.id.et_nick);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  //该参数指布局能延伸到navigationbar，我们场景中不应加这个参数
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT); //设置navigationbar颜色为透明
//        }


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

                        //通知平板更新昵称
                        String devId = SipInfo.paddevId;
                        SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                        SipInfo.toDev = new NameAddress(devName, sipURL);
                        org.zoolu.sip.message.Message query = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                                SipInfo.user_from, BodyFactory.createListUpdate("addsuccess"));
                        SipInfo.sipUser.sendMessage(query);
                        Looper.loop();
                    }
                }else {
                    myhandle.sendEmptyMessage(2);
                }
            }
        }.start();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}

