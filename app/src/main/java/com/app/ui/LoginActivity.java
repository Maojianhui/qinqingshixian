package com.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
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
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.sip.KeepAlive;
import com.app.sip.SipDev;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.sip.SipUser;
import com.app.tools.ActivityCollector;
import com.app.tools.PermissionUtils;
import com.app.view.CustomProgressDialog;
import com.app.views.CleanEditText;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.amap.api.mapcore2d.p.i;
import static com.app.groupvoice.GroupInfo.wakeLock;
import static com.app.model.Constant.avatar;
import static com.app.model.Constant.devid1;
import static com.app.model.Constant.devid2;
import static com.app.model.Constant.devid3;
import static com.app.model.Constant.groupid1;
import static com.app.model.Constant.res;
import static java.lang.Thread.sleep;

public class LoginActivity extends Activity {
    private static final String TAG ="LoginActivity";

//    private String[] groupname = new String[3];
//    private String[] groupid = new String[3];
//    private String[] appdevid = new String[3];
    private String groupname;
    private String groupid;
    private String appdevid;
    private Handler handler = new Handler();

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    //前一次的账号
    private String lastUserAccount;
    //网络连接失败窗口
    private AlertDialog newWorkConnectedDialog;
    //账号不存在
    private AlertDialog accountNotExistDialog;
    //登陆超时
    private AlertDialog timeOutDialog;
    //密码错误次数
    private int errorTime = 0;
    private CustomProgressDialog registering;
    protected CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Bind(R.id.num_input2)
    CleanEditText numInput2;
    @Bind(R.id.password_input)
    CleanEditText passwordInput;
    @Bind(R.id.hidepassword)
    ImageView hidepassword;
    @Bind(R.id.showpassword)
    ImageView showpassword;
    @Bind(R.id.vericode_login)
    TextView vericodeLogin;
    @Bind(R.id.password_forget)
    TextView passwordForget;
    @Bind(R.id.btn_login1)
    Button btnLogin1;
    @Bind(R.id.iv_back2)
    ImageView ivBack2;
    @Bind(R.id.tv_register)
    TextView tvRegister;
    @Bind(R.id.layout_root)
    RelativeLayout layoutRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_login1);
        ButterKnife.bind(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        setUpSplash();
        initViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.newbackground));
        }
    }

    private void setUpSplash() {
        Subscription splash = Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> requestPermission());
        mCompositeSubscription.add(splash);
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


    private void initViews() {
        numInput2.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        numInput2.setTransformationMethod(HideReturnsTransformationMethod
                .getInstance());
        String account=pref.getString("account","");
        numInput2.setText(account);

        passwordInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordInput.setImeOptions(EditorInfo.IME_ACTION_GO);
        passwordInput.setTransformationMethod(PasswordTransformationMethod
                .getInstance());
        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

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
        String password=pref.getString("password","");
        passwordInput.setText(password);
        SipInfo.localSdCard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/faxin/";
        isNetworkreachable();
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
    private void clickLogin() {
//        verifyStoragePermissions(this);
//        requestPower();
        if (SipInfo.isNetworkConnected) {
            SipInfo.userAccount = numInput2.getText().toString();
            SipInfo.passWord = passwordInput.getText().toString();
            editor=pref.edit();
            editor.putString("account",SipInfo.userAccount);
            editor.putString("password",SipInfo.passWord);
            editor.apply();
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

    private void beforeLogin() {
        SipInfo.phoneType= Build.MODEL;
        Log.i("手机型号","model"+SipInfo.phoneType);
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
                    /*账号不存在提示*/
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
    private Runnable    getuserinfo = new Runnable() {
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
                    Constant.sex=user.getString("gender");
                    Constant.isNotify=user.getString("notify");
                    Log.e("msg.........", "获取用户数据成功   " + Constant.nick + "    " + avatar);
                    SipInfo.friends.clear();
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
                List<GroupList> groupList = group.getGroupList();
                groupname=null;
                groupid=null;
                appdevid=null;
                for (i = 0; i < groupList.size(); i++) {
                    groupname= groupList.get(i).getGroup_name();
                    groupid = groupList.get(i).getGroupid();
                }
                devid1 = groupname;
                Constant.groupid1 = groupid;
                Constant.groupid = groupid1;
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    SipInfo.paddevId = devid1;
//                    response = GetPostUtil.sendGet1111(Constant.URL_getallDevidfromid, "id=" + Constant.id);
//                    Log.i("jonsresponse...........", response);
                    new Thread(getalldevid).start();

                } else {
                    Constant.res = "";
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("avatar",
                            Constant.avatar);
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("nick",
                            Constant.nick);
                    LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("id",
                            Constant.id);
                    registering.dismiss();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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

                List<UserList> userList = userFromGroup.getUserList();

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
            if ((Constant.res != null) && !("".equals(res))) {
                GroupInfo.groupNum = "7000";
                //String peer = peerElement.getFirstChild().getNodeValue();
                GroupInfo.ip = "101.69.255.134";
//                GroupInfo.port = 7000;
                GroupInfo.level = "1";
                SipInfo.devName = Constant.nick;
//
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("avatar",
                        Constant.avatar);
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("nick",
                        Constant.nick);
                LocalUserInfo.getInstance(LoginActivity.this).setUserInfo("id",
                        Constant.id);
                registering.dismiss();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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
                List<String> list = alldevid.getDevid();
                if(list.size()==0){
                    Looper.prepare();
                    ToastUtils.showShort(LoginActivity.this, "获取设备id失败");
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    Looper.loop();
                }else {
                    appdevid = list.get(0);
                    Constant.appdevid1 = appdevid;
                    if (appdevid != null && !("".equals(appdevid))) {

                        SipInfo.devId = appdevid;
                        Log.i("qwe",SipInfo.devId);
                        SipURL local_dev = new SipURL(SipInfo.devId, SipInfo.serverIp, SipInfo.SERVER_PORT_DEV);
                        SipInfo.dev_from = new NameAddress(SipInfo.devId, local_dev);
                        new Thread(devConnecting).start();
                    }

                    new Thread(getuserfromgroup).start();
                }
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

    private boolean checkInput(String userAccount, String passWord) {
        // 账号为空时提示
        if (userAccount == null || userAccount.trim().equals("")) {
            Toast.makeText(LoginActivity.this, R.string.tip_account_empty, Toast.LENGTH_LONG)
                    .show();
        } else {
            // 账号不匹配手机号格式（11位数字且以1开头）
//            if (!RegexUtils.checkMobile(account)) {
//                Toast.makeText(LoginActivity.this, R.string.tip_account_regex_not_right,
//                        Toast.LENGTH_LONG).show();
            if (passWord == null || passWord.trim().equals("")) {
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

    @Override
    protected void onResume() {
        super.onResume();
        if ((wakeLock != null) && (wakeLock.isHeld() == false)) {
            wakeLock.acquire();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registering != null) {
            registering.dismiss();
        }
        if(wakeLock!=null){
            wakeLock.release();
            wakeLock=null;
        }
        //ButterKnife.unbind(this);//空间解绑
    }
    @OnClick({R.id.num_input2,R.id.password_input,R.id.hidepassword,R.id.showpassword,
            R.id.vericode_login,R.id.password_forget,R.id.btn_login1,R.id.tv_register})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_login1:
                clickLogin();
                break;
            case R.id.hidepassword:
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                Toast.makeText(this,"隐藏密码",Toast.LENGTH_SHORT).show();
                hidepassword.setVisibility(View.INVISIBLE);
                showpassword.setVisibility(View.VISIBLE);
                break;
            case R.id.showpassword:
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                Toast.makeText(this,"显示密码",Toast.LENGTH_SHORT).show();
                showpassword.setVisibility(View.INVISIBLE);
                hidepassword.setVisibility(View.VISIBLE);
                break;
            case R.id.vericode_login:
                startActivity(new Intent(this,VerificodeLogin.class));
                break;
            case R.id.password_forget:
                startActivity(new Intent(this,ChangePassword1.class));
                break;
            case R.id.tv_register:
                startActivity(new Intent(this,SignUpActivity.class));
                break;
            case R.id.iv_back2:
                break;
        }
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.PermissionUtils.READ_EXTERNAL_STORAGE",
            "android.PermissionUtils.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限

            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.PermissionUtils.WRITE_EXTERNAL_STORAGE");
            if (permission != PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestPower() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "需要读写权限，请打开设置开启对应的权限", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    /**
     * onRequestPermissionsResult方法重写，Toast显示用户是否授权
     */
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        String requestPermissionsResult = "";
//        if (requestCode == 1) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PERMISSION_GRANTED) {
//                    requestPermissionsResult += permissions[i] + " 申请成功\n";
//                } else {
//                    requestPermissionsResult += permissions[i] + " 申请失败\n";
//                }
//            }
//        }
//        Toast.makeText(this, requestPermissionsResult, Toast.LENGTH_SHORT).show();
//    }

    private PermissionUtils.PermissionGrant mGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {

        }

        @Override
        public void onPermissionCancel() {
            Toast.makeText(LoginActivity.this, getString(R.string.alirtc_permission), Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    private void requestPermission(){
        PermissionUtils.requestMultiPermissions(this,
                new String[]{
                        PermissionUtils.PERMISSION_CAMERA,
                        PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE,
                        PermissionUtils.PERMISSION_RECORD_AUDIO,
                        PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE}, mGrant);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PermissionUtils.CODE_MULTI_PERMISSION){
            PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mGrant);
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PermissionUtils.REQUEST_CODE_SETTING){
            new Handler().postDelayed(this::requestPermission, 500);

        }

    }
}
