package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.app.groupvoice.GroupKeepAlive;
import com.app.groupvoice.GroupUdpThread;
import com.app.groupvoice.RtpAudio;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.sip.KeepAlive;
import com.app.sip.SipDev;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.sip.SipUser;
import com.app.tools.ActivityCollector;
import com.app.utils.GetPostUtil;
import com.app.utils.ToastUtils;
import com.app.view.CustomProgressDialog;
import com.app.views.CleanEditText;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.OnClickListener;
import static com.amap.api.mapcore2d.p.i;
import static com.app.model.Constant.avatar;
import static com.app.model.Constant.devid1;
import static com.app.model.Constant.devid2;
import static com.app.model.Constant.devid3;
import static com.app.model.Constant.groupid1;
import static java.lang.Thread.sleep;

/**
 * @desc 登录界面
 * Created by echo on 18/1/24.
 */
public class LoginActivity extends Activity implements OnClickListener {

    // 界面控件
    private CleanEditText accountEdit;
    private CleanEditText passwordEdit;
    private List<String> list = new ArrayList<String>();
    private List<UserList> userList = new ArrayList<UserList>();
    private List<GroupList> groupList = new ArrayList<GroupList>();
    private String SdCard;
    private String[] groupname = new String[3];
    private String[] groupid = new String[3];
    private String[] appdevid = new String[3];
    private Handler handler = new Handler();
    //网络连接失败窗口
    private AlertDialog newWorkConnectedDialog;
    //账号不存在
    private AlertDialog accountNotExistDialog;
    //登陆超时
    private AlertDialog timeOutDialog;
    //密码错误次数
    private int errorTime = 0;
    //前一次的账号
    private String lastUserAccount;
    //注册等待窗口
    private CustomProgressDialog registering;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_login);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNetworkreachable();
        closeKeyboard(LoginActivity.this, getWindow().getDecorView());
    }

    //检查网络是否连接
    public boolean isNetworkreachable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            SipInfo.isNetworkConnected = false;
        } else {
            SipInfo.isNetworkConnected = info.getState() == NetworkInfo.State.CONNECTED;
        }
        return SipInfo.isNetworkConnected;
    }

    /**
     * 强制关闭键盘
     */
    public void closeKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // 网络是否连接
    private Runnable networkConnectedFailed = new Runnable() {
        @Override
        public void run() {
            if (newWorkConnectedDialog == null || !newWorkConnectedDialog.isShowing()) {
                newWorkConnectedDialog = new AlertDialog.Builder(LoginActivity.this)
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

    /**
     * 初始化
     */
    private void initViews() {
        accountEdit = (CleanEditText) this.findViewById(R.id.et_email_phone);
        accountEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        accountEdit.setTransformationMethod(HideReturnsTransformationMethod
                .getInstance());
        accountEdit.setText("15757174780");

        passwordEdit = (CleanEditText) this.findViewById(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod
                .getInstance());
        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    clickLogin();
                }
                return false;
            }
        });
        passwordEdit.setText("123456");
        SipInfo.localSdCard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faxin/";
        isNetworkreachable();
    }

    private void clickLogin() {
        if (SipInfo.isNetworkConnected) {
            SipInfo.userAccount = accountEdit.getText().toString();
            SipInfo.passWord = passwordEdit.getText().toString();
            if (checkInput(SipInfo.userAccount, SipInfo.passWord)) {
                // TODO: 请求服务器登录账号
                if (!SipInfo.userAccount.equals(lastUserAccount)) {
                    errorTime = 0;
                }
                beforeLogin();
                registering = new CustomProgressDialog(LoginActivity.this);
                registering.setCancelable(false);
                registering.setCanceledOnTouchOutside(false);
                registering.show();

                new Thread(connecting).start();
            }
        } else {
            //弹出网络连接失败窗口
            handler.post(networkConnectedFailed);
        }
    }

    Runnable connecting = new Runnable() {
        @Override
        public void run() {
            try {

                int hostPort = new Random().nextInt(5000) + 2000;
                SipInfo.sipUser = new SipUser(null, hostPort, LoginActivity.this);
                Message register = SipMessageFactory.createRegisterRequest(
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
                        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(LoginActivity.this, dbPath, null, 1);
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
                    new Thread(getgroupinfo).start();
                } else {
                    Looper.prepare();
                    ToastUtils.makeShortText("获取用户数据失败请重试", LoginActivity.this);
                    registering.dismiss();
                    Looper.loop();
                }
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", LoginActivity.this);
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
                groupname[0]=null;
                groupname[1]=null;
                groupname[2]=null;
                groupid[0]=null;
                groupid[1]=null;
                groupid[2]=null;
                appdevid[0]=null;
                appdevid[1]=null;
                appdevid[2]=null;
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
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("avatar",
                            Constant.avatar);
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("nick",
                            Constant.nick);
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("id",
                            Constant.id);
                    registering.dismiss();
                    startActivity(new Intent(LoginActivity.this, Main.class));
                }
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", LoginActivity.this);
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
                SipInfo.friends.clear();
                for (i = 0; i < userList.size(); i++) {
                    Friend friend = new Friend();
                    friend.setNickName(userList.get(i).getNickname());
                    friend.setPhoneNum(userList.get(i).getName());
                    friend.setUserId(userList.get(i).getUserid());
                    friend.setId(userList.get(i).getId());
                    friend.setAvatar(userList.get(i).getAvatar());
                    SipInfo.friends.add(friend);
                }
                new Thread(getpostinfo).start();
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", LoginActivity.this);
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
            if ((response != null) && !("".equals(response))) {
                GroupInfo.groupNum = "7000";
                //String peer = peerElement.getFirstChild().getNodeValue();
                GroupInfo.ip = "101.69.255.134";
                GroupInfo.port = 7000;
                GroupInfo.level = "1";
                SipInfo.devName = Constant.nick;
                Thread groupVoice = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GroupInfo.rtpAudio = new RtpAudio(SipInfo.serverIp, GroupInfo.port);
                            GroupInfo.groupUdpThread = new GroupUdpThread(SipInfo.serverIp, GroupInfo.port);
                            GroupInfo.groupUdpThread.startThread();
                            GroupInfo.groupKeepAlive = new GroupKeepAlive();
                            GroupInfo.groupKeepAlive.startThread();
//                        Intent PTTIntent = new Intent(LoginActivity.this, PTTService.class);
//                        LoginActivity.this.startService(PTTIntent);
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, "groupVoice");
                groupVoice.start();
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("avatar",
                        Constant.avatar);
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("nick",
                        Constant.nick);
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("id",
                        Constant.id);
                registering.dismiss();
                startActivity(new Intent(LoginActivity.this, Main.class));
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户帖子失败请重试", LoginActivity.this);
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
                for (int i = 0; i < list.size(); i++) {
                    appdevid[i] = list.get(i);
                }
                Constant.appdevid1 = appdevid[0];
                Constant.appdevid2 = appdevid[1];
                Constant.appdevid3 = appdevid[2];
                for (int i = 0; i < 3; i++) {
                    if (appdevid[i] != null && !("".equals(appdevid[i]))) {
                        try {
                            SipInfo.devId = appdevid[i];
                            SipURL local_dev = new SipURL(SipInfo.devId, SipInfo.serverIp, SipInfo.SERVER_PORT_DEV);
                            SipInfo.dev_from = new NameAddress(SipInfo.devId, local_dev);
                            new Thread(devConnecting).start();
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                new Thread(getuserfromgroup).start();

            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户devid失败请重试", LoginActivity.this);
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
                SipInfo.sipDev = new SipDev(LoginActivity.this, null, hostPort);//无网络时在主线程操作会报异常
                Message register = SipMessageFactory.createRegisterRequest(
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
                    ToastUtils.makeShortText("设备注册失败请重新登录", LoginActivity.this);
                    registering.dismiss();
                    Looper.loop();
                }
            }
        }
    };

    private void showDialogTip(final int errorTime) {
        if (errorTime < 2) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
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
                    AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
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

    private Runnable accountNotExist = new Runnable() {
        @Override
        public void run() {
            if (accountNotExistDialog == null || !accountNotExistDialog.isShowing()) {
                accountNotExistDialog = new AlertDialog.Builder(LoginActivity.this)
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
                timeOutDialog = new AlertDialog.Builder(LoginActivity.this)
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

    /**
     * 检查输入
     *
     * @param account
     * @param password
     * @return
     */
    public boolean checkInput(String account, String password) {
        // 账号为空时提示
        if (account == null || account.trim().equals("")) {
            Toast.makeText(LoginActivity.this, R.string.tip_account_empty, Toast.LENGTH_LONG)
                    .show();
        } else {
            // 账号不匹配手机号格式（11位数字且以1开头）
//            if (!RegexUtils.checkMobile(account)) {
//                Toast.makeText(LoginActivity.this, R.string.tip_account_regex_not_right,
//                        Toast.LENGTH_LONG).show();
            if (password == null || password.trim().equals("")) {
                Toast.makeText(LoginActivity.this, R.string.tip_password_can_not_be_empty,
                        Toast.LENGTH_LONG).show();
            } else {
                return true;
            }
        }

        return false;
    }

    private void showTip(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //创建文件夹
    private boolean createDirs(String dir) {
        try {
            File dirPath = new File(dir);
            if (!dirPath.exists()) {
                dirPath.mkdirs();
            }
        } catch (Exception e) {
            showTip(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registering != null) {
            registering.dismiss();
        }
        ActivityCollector.removeActivity(this);
        //ButterKnife.unbind(this);//空间解绑
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.btn_login:
                clickLogin();
                break;
            case R.id.tv_create_account:
                enterRegister();
                break;
            case R.id.tv_forget_password:
                enterForgetPwd();
                break;
            default:
                break;
        }
    }


    /**
     * 跳转到忘记密码
     */
    private void enterForgetPwd() {
        Intent intent = new Intent(this, ChangePassword.class);
        startActivity(intent);
    }

    /**
     * 跳转到注册页面
     */
    private void enterRegister() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, 0x001);
    }

}

