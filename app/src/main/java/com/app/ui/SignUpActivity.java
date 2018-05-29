package com.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.model.Constant;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.http.GetPostUtil;
import com.app.http.RegexUtils;
import com.app.http.ToastUtils;
import com.app.http.VerifyCodeManager;
import com.app.views.CleanEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mob.MobSDK;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;

/**
 * @desc 注册界面
 * 功能描述：一般会使用手机登录，通过获取手机验证码，跟服务器交互完成注册
 * Created by devilwwj on 16/1/24.
 */
public class SignUpActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SignupActivity";
    // 界面控件
    private CleanEditText phoneEdit;
    private CleanEditText passwordEdit;
    private CleanEditText againPassword;
    private CleanEditText verifyCodeEdit;
    private Button getVerifiCodeButton;
    private VerifyCodeManager codeManager;
    String response;
    private EventHandler eventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_signup);

        initViews();
        codeManager = new VerifyCodeManager(this, phoneEdit, getVerifiCodeButton);

    }

    /**
     * 通用findViewById,减少重复的类型转换
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class.", ex);
            throw ex;
        }
    }


    private void initViews() {

        getVerifiCodeButton = getView(R.id.btn_send_verifi_code);
        getVerifiCodeButton.setOnClickListener(this);
        phoneEdit = getView(R.id.et_phone);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        verifyCodeEdit = getView(R.id.et_verifiCode);
        verifyCodeEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        passwordEdit = getView(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        againPassword = getView(R.id.et_again);
        againPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        againPassword.setImeOptions(EditorInfo.IME_ACTION_GO);
        Button create = (Button) findViewById(R.id.btn_create_account);
        create.setOnClickListener(this);
        passwordEdit.setOnEditorActionListener(new OnEditorActionListener() {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
        ActivityCollector.removeActivity(this);
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

    //注册回调监听接口
    private void commit() {
        SipInfo.userAccount2 = phoneEdit.getText().toString().trim();
        SipInfo.passWord2 = passwordEdit.getText().toString().trim();
        final String again = againPassword.getText().toString().trim();
        String code = verifyCodeEdit.getText().toString().trim();
        if (checkInput(SipInfo.userAccount2, SipInfo.passWord2, code, again)) {
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
    //    private void beforeLogin() {
//        //registerid？
//        SipURL local = new SipURL("310023000000054992", SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
//        SipURL remote = new SipURL(SipInfo.SERVER_ID, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
//        SipInfo.user_from2 = new NameAddress(SipInfo.userAccount2, local);
//        SipInfo.user_to2 = new NameAddress(SipInfo.SERVER_NAME, remote);
//
//    }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_verifi_code:
                // TODO 请求接口发送验证码
                codeManager.getVerifyCode(VerifyCodeManager.REGISTER);
                break;
            case R.id.iv_cancel:
                ActivityCollector.removeActivity(this);
                finish();
                break;
            case R.id.btn_create_account:
                //创建账号
                final String phone = phoneEdit.getText().toString().trim();
                final String passWord = passwordEdit.getText().toString().trim();
                final String code = verifyCodeEdit.getText().toString().trim();
                final String again = againPassword.getText().toString().trim();
                if (checkInput(phone, passWord, code, again)) {
                    SMSSDK.submitVerificationCode("86", phone, code);
                }
                //commit();
                break;
            default:
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
                    final String phone = phoneEdit.getText().toString().trim();
                    final String passWord = passwordEdit.getText().toString().trim();
                    final String again = passwordEdit.getText().toString().trim();
                    String code = verifyCodeEdit.getText().toString().trim();
                    if (checkInput(phone, passWord, code, again)) {
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


