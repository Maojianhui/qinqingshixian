package com.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.RegexUtils;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.views.CleanEditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.SMSSDK;

public class SetNewPassword extends Activity {

    String response;

    @Bind(R.id.newpassword_set)
    CleanEditText newpasswordSet;
    @Bind(R.id.newpassword_confirm)
    CleanEditText newpasswordConfirm;
    @Bind(R.id.hidepassword1)
    ImageView hidepassword1;
    @Bind(R.id.showpassword1)
    ImageView showpassword1;
    @Bind(R.id.btn_down)
    Button btnDown;
    @Bind(R.id.iv_back4)
    ImageView ivBack4;
    @Bind(R.id.hidepassword2)
    ImageView hidepassword2;
    @Bind(R.id.showpassword2)
    ImageView showpassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_set_new_password);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }
    }

//    Handler myhandle = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 1) {
//                startActivity(new Intent(SetNewPassword.this, LoginActivity.class));
//            }
//        }
//    };

    @OnClick({R.id.hidepassword1, R.id.showpassword1,R.id.hidepassword2,
            R.id.showpassword2, R.id.btn_down, R.id.iv_back4})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hidepassword1:
                newpasswordSet.setTransformationMethod(PasswordTransformationMethod.getInstance());
                Toast.makeText(this, "隐藏密码", Toast.LENGTH_SHORT).show();
                hidepassword1.setVisibility(View.INVISIBLE);
                showpassword1.setVisibility(View.VISIBLE);
                break;
            case R.id.showpassword1:
                newpasswordSet.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                Toast.makeText(this,"显示密码",Toast.LENGTH_SHORT).show();
                showpassword1.setVisibility(View.INVISIBLE);
                hidepassword1.setVisibility(View.VISIBLE);
                break;
            case R.id.hidepassword2:
                newpasswordConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                Toast.makeText(this, "隐藏密码", Toast.LENGTH_SHORT).show();
                hidepassword2.setVisibility(View.INVISIBLE);
                showpassword2.setVisibility(View.VISIBLE);
                break;
            case R.id.showpassword2:
                newpasswordConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                Toast.makeText(this,"显示密码",Toast.LENGTH_SHORT).show();
                showpassword2.setVisibility(View.INVISIBLE);
                hidepassword2.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_down:
                SipInfo.passWord2 = newpasswordSet.getText().toString().trim();
                final String again = newpasswordConfirm.getText().toString().trim();
                if (checkInput(SipInfo.userAccount2, SipInfo.passWord2, SipInfo.code, again)) {
                    SMSSDK.submitVerificationCode("86", SipInfo.userAccount2, SipInfo.code);
                }
                break;
            case R.id.iv_back4:
                ActivityCollector.removeActivity(this);
                finish();
                break;
        }
    }



    private boolean checkInput(String phone, String password, String code, String again) {
        if (TextUtils.isEmpty(phone)) { // 电话号码为空
            ToastUtils.showShort(this, R.string.tip_phone_can_not_be_empty);
        } else {
            if (!RegexUtils.checkMobile(phone)) { // 电话号码格式有误
                ToastUtils.showShort(this, R.string.tip_phone_regex_not_right);
            } else if (TextUtils.isEmpty(code)) { // 验证码不正确
                ToastUtils.showShort(this, R.string.tip_please_input_code);
            } else if (password.length() < 6 || password.length() > 32
                    || TextUtils.isEmpty(password)) { // 密码格式
                ToastUtils.showShort(this,
                        R.string.tip_please_input_6_32_password);
            } else if (!password.equals(again)) {
                ToastUtils.showShort(this, "两次密码不一致");
            } else {
                return true;
            }
        }

        return false;
    }
}
