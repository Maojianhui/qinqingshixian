package com.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.views.CleanEditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;

/**
 * Author chzjy
 * Date 2016/12/19.
 */

public class ChangePassword extends Activity implements View.OnClickListener {
    private static final String TAG = "ChangepasswordActivity";
    @Bind(R.id.iv_back1)
    ImageView ivBack1;
    @Bind(R.id.titleset)
    TextView titleset;
    @Bind(R.id.btn_revise)
    Button btnrevise;

    String response;
    @Bind(R.id.oldpassword_input)
    CleanEditText oldpasswordInput;
    @Bind(R.id.newpassword_input)
    CleanEditText newpasswordInput;
    @Bind(R.id.newpassword_again)
    CleanEditText newpasswordAgain;
    private EventHandler eventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_frogetpwd);
        ButterKnife.bind(this);
        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }
    }


    private void initViews() {
        titleset.setText("修改密码");
        oldpasswordInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        newpasswordInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        newpasswordAgain.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        newpasswordAgain.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                // 点击虚拟键盘的done
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    commit();
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    Handler myhandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {

                startActivity(new Intent(ChangePassword.this, LoginActivity.class));
            }
        }
    };

    //注册回调监听接口
    private void commit() {
        final String old = oldpasswordInput.getText().toString().trim();
        SipInfo.passWord2 = newpasswordInput.getText().toString().trim();
        final String again = newpasswordAgain.getText().toString().trim();
        if (checkInput(old, SipInfo.passWord2, again)) {
            // TODO:请求服务端注册账号
            new Thread() {
                @Override
                public void run() {
                    response = GetPostUtil.sendGet1111(Constant.URL_ChPaw, "tel_num=" + SipInfo.userAccount + "&" + "password=" + SipInfo.passWord2);
                    Log.i("jonsresponse", response);
                    if ((response != null) && !("".equals(response))) {
                        JSONObject obj = JSON.parseObject(response);
                        String msg = obj.getString("msg");
                        if (msg.equals("success")) {
                            Looper.prepare();
                            ToastUtils.showShort(ChangePassword.this, "密码修改成功");
                            myhandle.sendEmptyMessage(1);
                            Looper.loop();
                            return;
                        } else {
                            ToastUtils.showShort(ChangePassword.this, msg);
                            return;
                        }

                    } else {
                        Looper.prepare();
                        ToastUtils.makeShortText("请求无响应请重试", ChangePassword.this);
                        Looper.loop();
                    }
                }
            }.start();
        }

    }


    private boolean checkInput(String old, String password, String again) {
        if (SipInfo.isVericodeLogin){
            if (password.length()<6||password.length()>32
                    || TextUtils.isEmpty(password)){
                ToastUtils.showShort(this,
                        R.string.tip_please_input_6_32_password);
            }else if (!password.equals(again)) {
                ToastUtils.showShort(this, "两次密码不一致");
            } else {
                return true;
            }
        }
        else if (!(old.equals(SipInfo.passWord))) { // 旧密码输入错误
            ToastUtils.showShort(this, R.string.tip_password_not_same);
        } else if (password.length() < 6 || password.length() > 32
                || TextUtils.isEmpty(password)) { // 密码格式
            ToastUtils.showShort(this,
                    R.string.tip_please_input_6_32_password);
        } else if (!password.equals(again)) {
            ToastUtils.showShort(this, "两次密码不一致");
        } else {
            return true;
        }
        return false;
    }

    @OnClick({R.id.iv_back1, R.id.btn_revise})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back1:
                ActivityCollector.removeActivity(this);
                finish();
                break;
            case R.id.btn_revise:
//                final String old= oldpasswordInput.getText().toString().trim();
//                final String newpassWord = newpasswordInput.getText().toString().trim();
//                final String again = newpasswordAgain.getText().toString().trim();
//                if (checkInput(old, newpassWord, again)) {
//
//                }
                commit();
                break;
            default:
                break;
        }
    }
}
