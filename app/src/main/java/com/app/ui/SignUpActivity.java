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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.RegexUtils;
import com.app.http.ToastUtils;
import com.app.http.VerifyCodeManager;
import com.app.http.VerifyCodeManager1;
import com.app.model.Constant;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.views.CleanEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mob.MobSDK;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class SignUpActivity extends Activity {

    @Bind(R.id.num_input)
    CleanEditText numInput;
    @Bind(R.id.verificode_input)
    CleanEditText verificodeInput;
    @Bind(R.id.get_verificode)
    TextView getVerificode;
    @Bind(R.id.password_set)
    CleanEditText passwordSet;
    @Bind(R.id.hidepassword)
    ImageView hidepassword;
    @Bind(R.id.showpassword)
    ImageView showpassword;
    @Bind(R.id.btn_register)
    Button btnRegister;
    @Bind(R.id.linearLayout)
    LinearLayout linearLayout;
    @Bind(R.id.goto_login)
    TextView gotoLogin;
    @Bind(R.id.iv_back6)
    ImageView ivBack6;
    private VerifyCodeManager1 codeManager1;
    String response;
    private EventHandler eventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up1);
        ActivityCollector.addActivity(this);
        ButterKnife.bind(this);
        initViews();
        codeManager1 = new VerifyCodeManager1(this, numInput, getVerificode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.newbackground));
        }
    }

    private void initViews() {
        numInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        verificodeInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        passwordSet.setOnEditorActionListener(new TextView.OnEditorActionListener() {

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
        MobSDK.init(this, "213c5d90b2394", "793f08e685abc8a57563a8652face144");
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                android.os.Message msg = new android.os.Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    Handler myhandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        }
    };

    private void commit() {
        SipInfo.userAccount2 = numInput.getText().toString().trim();
        SipInfo.passWord2 = passwordSet.getText().toString().trim();
        String code = verificodeInput.getText().toString().trim();
        if (checkInput(SipInfo.userAccount2, SipInfo.passWord2, code)) {
            // TODO:请求服务端注册账号
            new Thread() {
                @Override
                public void run() {
                    response = GetPostUtil.sendGet1111(Constant.URL_Register, "username=" + SipInfo.userAccount2 + "&" + "password=" + SipInfo.passWord2);
                    Log.i("jonsresponse", response);
                    if ((response != null) && !("".equals(response))) {
                        JSONObject obj = JSON.parseObject(response);
                        String msg = obj.getString("msg");
                        if (msg.equals("注册失败")) {
                            Looper.prepare();
                            ToastUtils.showShort(SignUpActivity.this, msg);
                            Looper.loop();
                            return;
                        } else if (msg.equals("手机号已注册")) {
                            Looper.prepare();
                            ToastUtils.showShort(SignUpActivity.this, msg);
                            Looper.loop();
                            return;
                        } else {
                            Looper.prepare();
                            ToastUtils.showShort(SignUpActivity.this, msg);
                            myhandle.sendEmptyMessage(1);
                            Looper.loop();
                            return;
                        }
                    }else {
                        Looper.prepare();
                        ToastUtils.makeShortText("请求无响应请重试", SignUpActivity.this);
                        Looper.loop();
                    }
                }
            }.start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private boolean checkInput(String phone, String password, String code) {
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
            } else {
                return true;
            }
        }

        return false;
    }

    @OnClick({R.id.get_verificode, R.id.hidepassword, R.id.showpassword,
            R.id.btn_register, R.id.goto_login,R.id.iv_back6})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_verificode:
                codeManager1.getVerifyCode(VerifyCodeManager.REGISTER);
                break;
            case R.id.hidepassword:
                passwordSet.setTransformationMethod(PasswordTransformationMethod.getInstance());
                Toast.makeText(this, "隐藏密码", Toast.LENGTH_SHORT).show();
                hidepassword.setVisibility(View.INVISIBLE);
                showpassword.setVisibility(View.VISIBLE);
                break;
            case R.id.showpassword:
                passwordSet.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                Toast.makeText(this,"显示密码",Toast.LENGTH_SHORT).show();
                showpassword.setVisibility(View.INVISIBLE);
                hidepassword.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_register:
                commit();
                break;
            case R.id.goto_login:
                ActivityCollector.removeActivity(this);
                finish();
                startActivity(new Intent(this,LoginActivity.class));
                break;
            case R.id.iv_back6:
                ActivityCollector.removeActivity(this);
                finish();
                break;
        }
    }
    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            Log.e("result", "result=" + result);
            // 短信注册成功后，返回LoginActivity,然后提示
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
//                    Toast.makeText(SignUpActivity.this, "验证成功",
//                            Toast.LENGTH_SHORT).show();
                    final String phone = numInput.getText().toString().trim();
                    final String passWord = passwordSet.getText().toString().trim();
                    String code = verificodeInput.getText().toString().trim();
                    if (checkInput(phone, passWord, code)) {
                        commit();
                    } else {
                        Toast.makeText(SignUpActivity.this, "填写信息格式不正确", Toast.LENGTH_SHORT).show();
                    }
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    Toast.makeText(getApplicationContext(), "验证码已经发送",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (result == SMSSDK.RESULT_ERROR) {
                Throwable throwable = (Throwable) data;
                throwable.printStackTrace();
                JsonObject obj = new JsonParser().parse(throwable.getMessage()).getAsJsonObject();
                String des = obj.get("detail").getAsString();//错误描述
                int status = obj.get("status").getAsInt();//错误代码
                if (status > 0 && !TextUtils.isEmpty(des)) {
                    Toast.makeText(SignUpActivity.this, des, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    };
}

