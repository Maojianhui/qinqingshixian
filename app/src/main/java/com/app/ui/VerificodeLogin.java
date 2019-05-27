package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.db.DatabaseInfo;
import com.app.db.MyDatabaseHelper;
import com.app.db.SQLiteManager;
import com.app.friendCircleMain.domain.Alldevid;
import com.app.friendCircleMain.domain.Group;
import com.app.friendCircleMain.domain.GroupList;
import com.app.friendCircleMain.domain.UserFromGroup;
import com.app.friendCircleMain.domain.UserList;
import com.app.groupvoice.GroupInfo;
import com.app.http.GetPostUtil;
import com.app.http.RegexUtils;
import com.app.http.ToastUtils;
import com.app.http.VerifyCodeManager;
import com.app.http.VerifyCodeManager1;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.sip.KeepAlive;
import com.app.sip.SipDev;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.sip.SipUser;
import com.app.tools.ActivityCollector;
import com.app.view.CustomProgressDialog;
import com.app.views.CleanEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mob.MobSDK;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static com.amap.api.mapcore2d.p.i;
import static com.app.model.Constant.avatar;
import static com.app.model.Constant.devid1;
import static com.app.model.Constant.devid2;
import static com.app.model.Constant.devid3;
import static com.app.model.Constant.groupid1;
import static com.app.model.Constant.res;
import static java.lang.Thread.sleep;

public class VerificodeLogin extends Activity {
    private Context mContext;
    @Bind(R.id.num_input4)
    CleanEditText numInput4;
    @Bind(R.id.vericode_input)
    CleanEditText vericodeinput;
    @Bind(R.id.get_verificode)
    TextView getVerificode;
    @Bind(R.id.password_login)
    TextView passwordLogin;
    @Bind(R.id.btn_login2)
    Button btnLogin2;
    @Bind(R.id.iv_back5)
    ImageView ivBack5;
    @Bind(R.id.newAccount_register)
    TextView newAccountRegister;
    private VerifyCodeManager1 codeManager1;
    private EventHandler eventHandler;
    private String userinfomsg;
    //前一次的账号
    private String lastUserAccount;
    //密码错误次数
    private int errorTime = 0;
    //注册等待窗口
    private CustomProgressDialog registering;
    //网络连接失败窗口
    private AlertDialog newWorkConnectedDialog;

    private String TAG = getClass().getSimpleName();
    //账号不存在
    private AlertDialog accountNotExistDialog;
    //登陆超时
    private AlertDialog timeOutDialog;
    private List<String> list = new ArrayList<String>();
    private List<UserList> userList = new ArrayList<UserList>();
    private List<GroupList> groupList = new ArrayList<GroupList>();
    private String[] groupname = new String[3];
    private String[] groupid = new String[3];
    private String[] appdevid = new String[3];
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_verificode_login);
        mContext=this;
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.newbackground));
        }
        initData();
        codeManager1 = new VerifyCodeManager1(this, numInput4, getVerificode);
    }

    private void initData() {
        numInput4.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        getVerificode.setImeOptions(EditorInfo.IME_ACTION_NEXT);// 下一步
        getVerificode.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                // 点击虚拟键盘的done
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    login();
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
                handler1.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }



    @OnClick({R.id.get_verificode,R.id.password_login,R.id.btn_login2,R.id.newAccount_register})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.get_verificode:
//                if (countSeconds == 60) {
//                    String mobile = numInput4.getText().toString();
//                    Log.e("tag", "mobile==" + mobile);
//
//                } else {
//                    Toast.makeText(VerificodeLogin.this, "不能重复发送验证码", Toast.LENGTH_SHORT).show();
//                }
                codeManager1.getVerifyCode(VerifyCodeManager.REGISTER);
                break;
            case R.id.password_login:
                startActivity(new Intent(this,LoginActivity.class));
                break;
            case R.id.btn_login2:
                SipInfo.passWord=null;//验证码登录没有设置密码，将密码设置为空
                final String phone = numInput4.getText().toString().trim();
                final String code = vericodeinput.getText().toString().trim();
                if (checkInput(phone,code)) {
                    SMSSDK.submitVerificationCode("86", phone, code);
                }
                break;
            case R.id.newAccount_register:
                startActivity(new Intent(this,SignUpActivity.class));
                break;
        }
    }

    private void login() {
        if (SipInfo.isNetworkConnected) {
            SipInfo.userAccount = numInput4.getText().toString();
            SipInfo.code = getVerificode.getText().toString();
            if (checkInput(SipInfo.userAccount, SipInfo.code)) {
                // TODO: 请求服务器登录账号
                if (!SipInfo.userAccount.equals(lastUserAccount)) {
                    errorTime = 0;
                }
                beforeLogin();
                registering = new CustomProgressDialog(VerificodeLogin.this);
                registering.setCancelable(false);
                registering.setCanceledOnTouchOutside(false);
                registering.show();
                SipInfo.isVericodeLogin=true;
                new Thread(connecting).start();
            }
        } else {
            //弹出网络连接失败窗口
            handler.post(networkConnectedFailed);
        }

    }



    private void beforeLogin() {
        SipInfo.isAccountExist = true;
        SipInfo.passwordError = false;
        SipInfo.userLogined = false;
        SipInfo.loginTimeout = true;
        SipURL local = new SipURL(SipInfo.REGISTER_ID, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
        SipURL remote = new SipURL(SipInfo.SERVER_ID, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
        SipInfo.user_from = new NameAddress(SipInfo.userAccount, local);
        SipInfo.user_to = new NameAddress(SipInfo.SERVER_NAME, remote);
        SipInfo.devLogined = false;
        SipInfo.dev_loginTimeout = true;

        SipURL remote_dev = new SipURL(SipInfo.SERVER_ID, SipInfo.serverIp, SipInfo.SERVER_PORT_DEV);

        SipInfo.dev_to = new NameAddress(SipInfo.SERVER_NAME, remote_dev);
    }

    // 网络是否连接
    private Runnable networkConnectedFailed = new Runnable() {
        @Override
        public void run() {
            if (newWorkConnectedDialog == null || !newWorkConnectedDialog.isShowing()) {
                newWorkConnectedDialog = new AlertDialog.Builder(VerificodeLogin.this)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent mIntent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(mIntent);
                            }
                        })
                        .setTitle("当前无网络,请检查网络连接")
                        .create();
                newWorkConnectedDialog.setCancelable(false);
                newWorkConnectedDialog.setCanceledOnTouchOutside(false);
                newWorkConnectedDialog.show();
            }
        }
    };

    private Runnable accountNotExist = new Runnable() {
        @Override
        public void run() {
            if (accountNotExistDialog == null || !accountNotExistDialog.isShowing()) {
                accountNotExistDialog = new AlertDialog.Builder(VerificodeLogin.this)
                        .setTitle("不存在该账号")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                accountNotExistDialog.show();
                accountNotExistDialog.setCancelable(false);
                accountNotExistDialog.setCanceledOnTouchOutside(false);
            }
        }
    };
    private Runnable timeOut = new Runnable() {
        @Override
        public void run() {
            if (timeOutDialog == null || !timeOutDialog.isShowing()) {
                timeOutDialog = new AlertDialog.Builder(VerificodeLogin.this)
                        .setTitle("连接超时,请检查网络")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                timeOutDialog.show();
                timeOutDialog.setCancelable(false);
                timeOutDialog.setCanceledOnTouchOutside(false);
            }
        }
    };

    private void showDialogTip(final int errorTime) {
        if (errorTime < 2) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(VerificodeLogin.this)
                            .setTitle("密码输入错误/还有" + (2 - errorTime) + "次输入机会")
                            .setPositiveButton("确定", null)
                            .create();
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(VerificodeLogin.this)
                            .setTitle("由于密码输入错误过多,该账号已被冻结")
                            .setPositiveButton("确定", null)//锁账号暂未完成
                            .create();
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);
                    Toast.makeText(getApplicationContext(), "该账号已被冻结", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    Runnable connecting = new Runnable() {
        @Override
        public void run() {
            try {

                int hostPort = new Random().nextInt(5000) + 2000;
                SipInfo.sipUser = new SipUser(null, hostPort, VerificodeLogin.this);
                org.zoolu.sip.message.Message register = SipMessageFactory.createRegisterRequest(
                        SipInfo.sipUser, SipInfo.user_to, SipInfo.user_from);
                SipInfo.sipUser.sendMessage(register);
                sleep(1000);
                for (int i = 0; i < 2; i++) {
                    if (!SipInfo.isAccountExist) {
                        //用户账号不存在
                        break;
                    }
                    if (SipInfo.passwordError) {
                        //密码错误
                        break;
                    }
                    if (!SipInfo.loginTimeout) {
                        //没有超时
                        break;
                    }
                    SipInfo.sipUser.sendMessage(register);
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

                if (!SipInfo.isAccountExist) {
                    registering.dismiss();
                    /**账号不存在提示*/
                    handler.post(accountNotExist);
                } else if (SipInfo.passwordError) {
                    //密码错误提示
                    registering.dismiss();
                    showDialogTip(errorTime++);
                    lastUserAccount = SipInfo.userAccount;
                } else if (SipInfo.loginTimeout) {
                    registering.dismiss();
                    //超时
                    handler.post(timeOut);
                } else {

                    if (SipInfo.userLogined) {
                        Log.i(TAG, "用户登录成功!");
                        //开启用户保活心跳包
                        SipInfo.keepUserAlive = new KeepAlive();
                        SipInfo.keepUserAlive.setType(0);
                        SipInfo.keepUserAlive.startThread();
                        //数据库
                        String dbPath = SipInfo.userId + ".db";
//                        deleteDatabase(dbPath);
                        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(VerificodeLogin.this, dbPath, null, 1);
                        DatabaseInfo.sqLiteManager = new SQLiteManager(myDatabaseHelper);

//                        SipInfo.applist.clear();
//                        //请求服务器上的app列表
//                        SipInfo.sipUser.sendMessage(SipMessageFactory.createSubscribeRequest(SipInfo.sipUser,
//                                SipInfo.user_to, SipInfo.user_from, BodyFactory.createAppsQueryBody()));
                        //启动设备注册线程
                        new Thread(getuserinfo).start();
                    }
                }
            }
        }
    };
    //获取用户数据线程
    String response = "";
    private Runnable getuserinfo = new Runnable() {
        @Override
        public void run() {
            response = GetPostUtil.sendGet1111(Constant.URL_GetUserInfo, "userid=" + SipInfo.userId);
//        LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("tiezi",
//                Constant.res);
            Log.i("jonsresponse...........", response);
            if ((response != null) && !("".equals(response))) {
                JSONObject obj = JSON.parseObject(response);
                String msg = obj.getString("msg");
                if ("success".equals(msg)) {
                    JSONObject user = obj.getJSONObject("user");
                    Constant.nick = user.getString("nickname");
                    Constant.avatar = user.getString("avatar");
                    Constant.id = user.getString("id");
                    Constant.phone = user.getString("name");
                    Log.e("msg.........", "获取用户数据成功   " + Constant.nick + "    " + avatar);
                    SipInfo.friends.clear();
                    new Thread(getgroupinfo).start();
                } else {
                    Looper.prepare();
                    ToastUtils.makeShortText("获取用户数据失败请重试", VerificodeLogin.this);
                    registering.dismiss();
                    Looper.loop();
                }
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", VerificodeLogin.this);
                registering.dismiss();
                Looper.loop();
            }
        }
    };
    //群组获取线程
    private Runnable getgroupinfo = new Runnable() {
        @Override
        public void run() {
            response = GetPostUtil.sendGet1111(Constant.URL_InquireGroup, "id=" + Constant.id);
            Log.i("jonsresponse...........", response);
            if ((response != null) && !("".equals(response))) {
                Group group = JSON.parseObject(response, Group.class);
                groupList = group.getGroupList();
                groupname[0] = null;
                groupname[1] = null;
                groupname[2] = null;
                groupid[0] = null;
                groupid[1] = null;
                groupid[2] = null;
                appdevid[0] = null;
                appdevid[1] = null;
                appdevid[2] = null;
                for (i = 0; i < groupList.size(); i++) {
                    groupname[i] = groupList.get(i).getGroup_name();
                    groupid[i] = groupList.get(i).getGroupid();
                }
                devid1 = groupname[0];
                devid2 = groupname[1];
                devid3 = groupname[2];

                Constant.groupid1 = groupid[0];
                Constant.groupid2 = groupid[1];
                Constant.groupid3 = groupid[2];
                Constant.groupid = groupid1;
                Log.i("dev1   ", "" + Constant.devid1);
                Log.i("dev2   ", "" + Constant.devid2);
                Log.i("dev3   ", "" + Constant.devid3);
                Log.i("group1   ", "" + Constant.groupid1);
                Log.i("group2   ", "" + Constant.groupid2);
                Log.i("group3   ", "" + Constant.groupid3);
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    SipInfo.paddevId = devid1;
                    response = GetPostUtil.sendGet1111(Constant.URL_getallDevidfromid, "id=" + Constant.id);
                    Log.i("jonsresponse...........", response);
                    new Thread(getalldevid).start();

                } else {
                    Constant.res = "";
                    LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("avatar",
                            Constant.avatar);
                    LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("nick",
                            Constant.nick);
                    LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("id",
                            Constant.id);
                    registering.dismiss();
                    startActivity(new Intent(VerificodeLogin.this, HomeActivity.class));
                }
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", VerificodeLogin.this);
                registering.dismiss();
                Looper.loop();
            }
        }
    };

    //群组用户信息获取
    private Runnable getuserfromgroup = new Runnable() {
        @Override
        public void run() {
            response = GetPostUtil.sendGet1111(Constant.URL_InquireUser, "groupid=" + Constant.groupid);
            Log.i("jonsresponse...........", response);
            if ((response != null) && !("".equals(response))) {
                UserFromGroup userFromGroup = JSON.parseObject(response, UserFromGroup.class);

                userList = userFromGroup.getUserList();

                for (i = 0; i < userList.size(); i++) {
                    Friend friend = new Friend();
                    friend.setNickName (userList.get(i).getNickname());
                    friend.setPhoneNum(userList.get(i).getName());
                    friend.setUserId(userList.get(i).getUserid());
                    friend.setId(userList.get(i).getId());
                    friend.setAvatar(userList.get(i).getAvatar());
                    SipInfo.friends.add(friend);
                }
                new Thread(getpostinfo).start();
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", VerificodeLogin.this);
                registering.dismiss();
                Looper.loop();
            }
        }
    };

    //帖子获取线程
    private Runnable getpostinfo = new Runnable() {
        @Override
        public void run() {
            Constant.res = GetPostUtil.sendGet1111(Constant.URL_getPostList, "id=" + Constant.id + "&currentPage=1" + "&groupid=" + Constant.groupid);
            Log.i("jonsresponse...........", Thread.currentThread().getName() + Constant.res + "");
            if ((Constant.res != null) && !("".equals(res))) {
                GroupInfo.groupNum = "7000";
                //String peer = peerElement.getFirstChild().getNodeValue();
                GroupInfo.ip = "101.69.255.134";
//                GroupInfo.port = 7000;
                GroupInfo.level = "1";
                SipInfo.devName = Constant.nick;
//
                LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("avatar",
                        Constant.avatar);
                LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("nick",
                        Constant.nick);
                LocalUserInfo.getInstance(VerificodeLogin.this).setUserInfo("id",
                        Constant.id);
                registering.dismiss();
                startActivity(new Intent(VerificodeLogin.this, HomeActivity.class));
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户帖子失败请重试", VerificodeLogin.this);
                registering.dismiss();
                Looper.loop();
            }
        }
    };
    //获取所有devid
    private Runnable getalldevid = new Runnable() {
        @Override
        public void run() {
            response = GetPostUtil.sendGet1111(Constant.URL_getallDevidfromid, "id=" + Constant.id);
            Log.i("jonsresponse...........", response);
            if ((response != null) && !("".equals(response))) {
                JSONObject jsonObject = JSONObject.parseObject(response);
                Alldevid alldevid = JSON.parseObject(response, Alldevid.class);
                list = alldevid.getDevid();

                appdevid[0] = list.get(0);

                Constant.appdevid1 = appdevid[0];


                if (appdevid[0] != null && !("".equals(appdevid[0]))) {

                    SipInfo.devId = appdevid[0];
                    Log.i("qwe",SipInfo.devId);
                    SipURL local_dev = new SipURL(SipInfo.devId, SipInfo.serverIp, SipInfo.SERVER_PORT_DEV);
                    SipInfo.dev_from = new NameAddress(SipInfo.devId, local_dev);
                    new Thread(devConnecting).start();
                }

                new Thread(getuserfromgroup).start();

            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户devid失败请重试", VerificodeLogin.this);
                registering.dismiss();
                Looper.loop();
            }
        }
    };

    //设备注册线程
    private Runnable devConnecting = new Runnable() {
        @Override
        public void run() {
            try {
                int hostPort = new Random().nextInt(5000) + 2000;
                SipInfo.sipDev = new SipDev(VerificodeLogin.this, null, hostPort);//无网络时在主线程操作会报异常
                org.zoolu.sip.message.Message register = SipMessageFactory.createRegisterRequest(
                        SipInfo.sipDev, SipInfo.dev_to, SipInfo.dev_from);

                for (int i = 0; i < 3; i++) {//如果没有回应,最多重发2次
                    SipInfo.sipDev.sendMessage(register);
                    sleep(2000);
                    if (!SipInfo.dev_loginTimeout) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (SipInfo.devLogined) {
                    Log.d(TAG, "设备注册成功!");
                    Log.d(TAG, "设备心跳包发送!");

                    //启动设备心跳线程
                    SipInfo.keepDevAlive = new KeepAlive();
                    SipInfo.keepDevAlive.setSipDev(SipInfo.sipDev);
                    SipInfo.keepDevAlive.setDev_from(SipInfo.dev_from);
                    SipInfo.keepDevAlive.setType(1);
                    SipInfo.keepDevAlive.startThread();

                } else {
                    Log.e(TAG, "设备注册失败!");
                    Looper.prepare();
                    ToastUtils.makeShortText("设备注册失败请重新登录", VerificodeLogin.this);
                    registering.dismiss();
                    Looper.loop();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registering != null) {
            registering.dismiss();
        }
        SMSSDK.unregisterEventHandler(eventHandler);
        ActivityCollector.removeActivity(this);
    }

    private boolean checkInput(String phone,  String code) {
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

    Handler handler1 = new Handler() {

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
                        login();
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
                    Toast.makeText(VerificodeLogin.this, des, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    };

}
