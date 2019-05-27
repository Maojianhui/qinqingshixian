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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
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

public class ChangePassword1 extends Activity {
    private static final String TAG = "Changepassword1Activity";
    private EventHandler eventHandler;
    private VerifyCodeManager1 codeManager1;

    String response;
    @Bind(R.id.num_input3)
    CleanEditText numInput3;
    @Bind(R.id.verificode_input1)
    CleanEditText verificodeInput1;
    @Bind(R.id.verificode_get)
    TextView verificodeGet;
    @Bind(R.id.btn_nextstep)
    Button btnNextstep;
    @Bind(R.id.iv_back3)
    ImageView ivBack3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_change_password1);
        ButterKnife.bind(this);
        initViews();
        codeManager1 = new VerifyCodeManager1(this, numInput3, verificodeGet);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.newbackground));
        }
    }

    Handler myhandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                startActivity(new Intent(ChangePassword1.this, LoginActivity.class));
            }
//            else if(msg.what==2){
//                startActivity(new Intent(ChangePassword1.this,ChangePassword1.class));
//            }
        }
    };

    private void initViews() {
        numInput3.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        verificodeInput1.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        MobSDK.init(this, "213c5d90b2394", "793f08e685abc8a57563a8652face144");
         eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
//        注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
        ActivityCollector.removeActivity(this);
    }

    @OnClick({R.id.verificode_get,R.id.btn_nextstep,R.id.iv_back3})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.verificode_get:
                codeManager1.getVerifyCode(VerifyCodeManager.REGISTER);
                break;
            case R.id.btn_nextstep:
                SipInfo.code=verificodeInput1.getText().toString().trim();
                SipInfo.userAccount2=numInput3.getText().toString().trim();
                startActivity(new Intent(this,SetNewPassword.class));
                break;
            case R.id.iv_back3:
                ActivityCollector.removeActivity(this);
                finish();
                break;
        }
    }

    private boolean checkInput(String phone, String code) {
        if (TextUtils.isEmpty(phone)) { // 电话号码为空
            ToastUtils.showShort(this, R.string.tip_phone_can_not_be_empty);
        } else {
            if (!RegexUtils.checkMobile(phone)) { // 电话号码格式有误
                ToastUtils.showShort(this, R.string.tip_phone_regex_not_right);
            } else if (TextUtils.isEmpty(code)) { // 验证码不正确
                ToastUtils.showShort(this, R.string.tip_please_input_code);
            } else {
                return true;
            }
        }
        return false;
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
//                    final String phone = numInput3.getText().toString().trim();
//                    String code = verificodeInput1.getText().toString().trim();
//                    if (checkInput(phone, code)) {
                        commit();
//                    } else {
//                        Toast.makeText(ChangePassword1.this, "填写信息格式不正确", Toast.LENGTH_SHORT).show();
//                    }
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
                    Toast.makeText(ChangePassword1.this, des, Toast.LENGTH_SHORT).show();
//                    myhandle.sendEmptyMessage(2);
                    return;
                }
            }
        }
    };

    private void commit() {
//        Toast.makeText(this,"hahah",Toast.LENGTH_SHORT).show();
            // TODO:请求服务端注册账号
            new Thread() {
                @Override
                public void run() {
                    response = GetPostUtil.sendGet1111(Constant.URL_ChPaw, "tel_num=" + SipInfo.userAccount2 + "&" + "password=" + SipInfo.passWord2);
                    Log.i("jonsresponse", response);
                    if ((response != null) && !("".equals(response))) {
                        JSONObject obj = JSON.parseObject(response);
                        String msg = obj.getString("msg");
                        if (msg.equals("success")) {
                            Looper.prepare();
                            ToastUtils.showShort(ChangePassword1.this, "密码修改成功");
                            myhandle.sendEmptyMessage(1);
                            Looper.loop();
                            return;
                        } else {
                            ToastUtils.showShort(ChangePassword1.this, msg);
                            return;
                        }

                    } else {
                        Looper.prepare();
                        ToastUtils.makeShortText("请求无响应请重试",ChangePassword1.this);
                        Looper.loop();
                    }
                }
            }.start();


    }
}
