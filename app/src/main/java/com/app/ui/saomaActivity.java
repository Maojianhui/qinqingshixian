package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.friendCircleMain.domain.UserFromGroup;
import com.app.friendCircleMain.domain.UserList;
import com.app.groupvoice.GroupInfo;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;
import com.app.utils.GetPostUtil;
import com.app.utils.ToastUtils;
import com.app.view.CustomProgressDialog;
import com.app.zxing.android.CaptureActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.amap.api.mapcore2d.p.i;
import static com.app.model.Constant.devid1;
import static com.app.model.Constant.devid2;
import static com.app.model.Constant.devid3;
import static com.app.model.Constant.groupid;
import static com.app.model.Constant.groupid1;
import static com.app.model.Constant.groupid2;
import static com.app.model.Constant.groupid3;
import static com.app.sip.SipInfo.sipUser;

public class saomaActivity extends Activity {
    private CustomProgressDialog registering;
    private static final int REQUEST_CODE_SCAN1 = 0x0000;
    private static final int REQUEST_CODE_SCAN2 = 0x0001;
    private static final int REQUEST_CODE_SCAN3 = 0x0002;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    @Bind(R.id.ECoder_title)
    TextView ECoderTitle;
    @Bind(R.id.i1)
    ImageView i1;
    @Bind(R.id.b1)
    Button b1;
    @Bind(R.id.r1)
    RelativeLayout r1;
    @Bind(R.id.i2)
    ImageView i2;
    @Bind(R.id.b2)
    Button b2;
    @Bind(R.id.r2)
    RelativeLayout r2;
    @Bind(R.id.i3)
    ImageView i3;
    @Bind(R.id.b3)
    Button b3;
    @Bind(R.id.r3)
    RelativeLayout r3;
    @Bind(R.id.t1)
    TextView t1;
    @Bind(R.id.t2)
    TextView t2;
    @Bind(R.id.t3)
    TextView t3;
    @Bind(R.id.input1)
    Button input1;
    @Bind(R.id.input2)
    Button input2;
    @Bind(R.id.input3)
    Button input3;
    private String response;
    String devid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saoma);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);
        if (devid1 != null) {
            ECoderTitle.setText("当前设备号:  " + Constant.devid1);
        }
        t1.setText("设备1：  " + devid1);
        t2.setText("设备2：  " + devid2);
        t3.setText("设备3：  " + devid3);
        if ((devid1 != null) && !("".equals(devid1))){
            input1.setVisibility(View.INVISIBLE);
            b1.setVisibility(View.INVISIBLE);
        }
        if ((devid2 != null) && !("".equals(devid2))){
            input2.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
        }
        if ((devid3 != null) && !("".equals(devid3))){
            input3.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (resultCode == RESULT_OK) {
            if (data != null) {
                devid = data.getStringExtra(DECODED_CONTENT_KEY);
                //Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        response = GetPostUtil.sendGet1111(Constant.URL_InquireBind, "devid=" + devid);
                        Log.i("jonsresponse...........", response);
                        JSONObject obj1 = JSON.parseObject(response);
                        String msg1 = obj1.getString("msg");
                        if (msg1.equals("未绑定")) {
                            response = GetPostUtil.sendGet1111(Constant.URL_Bind, "id=" + Constant.id + "&devid=" + devid);
                            Log.i("jonsresponse...........", response);
                            JSONObject obj = JSON.parseObject(response);
                            String msg = obj.getString("msg");
                            if (msg.equals("success")) {
                                if (requestCode == REQUEST_CODE_SCAN1) {
                                    handler.sendEmptyMessage(1111);
                                } else if (requestCode == REQUEST_CODE_SCAN2) {
                                    handler.sendEmptyMessage(2222);
                                } else if (requestCode == REQUEST_CODE_SCAN3) {
                                    handler.sendEmptyMessage(3333);
                                }
                            } else if (msg.equals("已绑定")) {
                                handler.sendEmptyMessage(222);
                            } else  {
                                handler.sendEmptyMessage(333);
                            }
                        } else if (msg1.equals("已绑定")) {
                            //发消息给平台，转发给群组验证，通过后绑定
                            response = GetPostUtil.sendGet1111(Constant.URL_joinGroup, "devid=" + devid+"&id="+Constant.id);
                            Log.i("jonsresponse...........", response);
                            JSONObject obj2 = JSON.parseObject(response);
                            String msg2 = obj2.getString("msg");
                            if (msg2.equals("success")) {
                                if (requestCode == REQUEST_CODE_SCAN1) {
                                    handler.sendEmptyMessage(1111);
                                } else if (requestCode == REQUEST_CODE_SCAN2) {
                                    handler.sendEmptyMessage(2222);
                                } else if (requestCode == REQUEST_CODE_SCAN3) {
                                    handler.sendEmptyMessage(3333);
                                }
                            } else if (msg2.equals("已经加群")) {
                                handler.sendEmptyMessage(222);
                            } else  {
                                handler.sendEmptyMessage(333);
                            }
                        }
                    }
                }).start();
            }
            LocalUserInfo.getInstance(this).setUserInfo("devid", devid1);
            //qrCodeImage.setImageBitmap(bitmap);
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1111) {
                //ToastUtils.makeShortText("绑定设备成功请重新登录", saomaActivity.this);
                t1.setText("设备1：  " + devid);
                input1.setVisibility(View.INVISIBLE);
                b1.setVisibility(View.INVISIBLE);
                AlertDialog dialog = new AlertDialog.Builder(saomaActivity.this)
                        .setCancelable(false)
                        .setTitle("绑定设备成功")
                        .setMessage("请重新登录")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    SipInfo.sipDev.sendMessage(SipMessageFactory.createNotifyRequest(SipInfo.sipDev, SipInfo.dev_to,
                                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                                }
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    GroupInfo.groupUdpThread.stopThread();
                                    GroupInfo.groupKeepAlive.stopThread();
                                }
                                dialog.dismiss();
                                SipInfo.running=false;
                                ActivityCollector.finishToFirstView();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return;
            } else if (msg.what == 2222) {
                //ToastUtils.makeShortText("绑定设备成功请重新登录", saomaActivity.this);
                t2.setText("设备2：  " + devid);
                input2.setVisibility(View.INVISIBLE);
                b2.setVisibility(View.INVISIBLE);
                AlertDialog dialog = new AlertDialog.Builder(saomaActivity.this)
                        .setCancelable(false)
                        .setTitle("绑定设备成功")
                        .setMessage("请重新登录")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    SipInfo.sipDev.sendMessage(SipMessageFactory.createNotifyRequest(SipInfo.sipDev, SipInfo.dev_to,
                                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                                }
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    GroupInfo.groupUdpThread.stopThread();
                                    GroupInfo.groupKeepAlive.stopThread();
                                }
                                dialog.dismiss();
                                SipInfo.running=false;
                                ActivityCollector.finishToFirstView();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return;
            } else if (msg.what == 3333) {
                //ToastUtils.makeShortText("绑定设备成功请重新登录", saomaActivity.this);
                t2.setText("设备3：  " + devid);
                input3.setVisibility(View.INVISIBLE);
                b3.setVisibility(View.INVISIBLE);
                AlertDialog dialog = new AlertDialog.Builder(saomaActivity.this)
                        .setCancelable(false)
                        .setTitle("绑定设备成功")
                        .setMessage("请重新登录")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    SipInfo.sipDev.sendMessage(SipMessageFactory.createNotifyRequest(SipInfo.sipDev, SipInfo.dev_to,
                                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                                }
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    GroupInfo.groupUdpThread.stopThread();
                                    GroupInfo.groupKeepAlive.stopThread();
                                }
                                dialog.dismiss();
                                SipInfo.running=false;
                                ActivityCollector.finishToFirstView();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return;
            } else if (msg.what == 222) {
                ToastUtils.makeShortText("已经绑定过该设备", saomaActivity.this);
                return;
            } else if (msg.what == 333) {
                ToastUtils.makeShortText("绑定失败，不是一个合法的设备", saomaActivity.this);
                return;
            }
        }
    };

    @OnClick({R.id.b1, R.id.input1, R.id.r1, R.id.b2, R.id.input2, R.id.r2, R.id.b3, R.id.input3, R.id.r3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.b1:
                Intent intent = new Intent(saomaActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN1);
                break;
            case R.id.input1:
                final EditText editText = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入设备号").setIcon(
                        android.R.drawable.ic_dialog_info).setView(editText
                ).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        devid = editText.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                response = GetPostUtil.sendGet1111(Constant.URL_InquireBind, "devid=" + devid);
                                Log.i("jonsresponse...........", response);
                                JSONObject obj1 = JSON.parseObject(response);
                                String msg1 = obj1.getString("msg");
                                if (msg1.equals("未绑定")) {
                                    response = GetPostUtil.sendGet1111(Constant.URL_Bind, "id=" + Constant.id + "&devid=" + devid);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj = JSON.parseObject(response);
                                    String msg = obj.getString("msg");
                                    if (msg.equals("success")) {
                                        handler.sendEmptyMessage(1111);
                                    } else if (msg.equals("已绑定")) {
                                        handler.sendEmptyMessage(222);
                                    } else if (msg.equals("设备绑定用户失败")) {
                                        handler.sendEmptyMessage(333);
                                    }
                                } else if (msg1.equals("已绑定")) {
                                    //发消息给平台，转发给群主验证，通过后绑定
                                    response = GetPostUtil.sendGet1111(Constant.URL_joinGroup, "devid=" + devid+"&id="+Constant.id);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj2 = JSON.parseObject(response);
                                    String msg2 = obj2.getString("msg");
                                    if (msg2.equals("success")) {
                                            handler.sendEmptyMessage(1111);
                                    } else if (msg2.equals("已经加群")) {
                                        handler.sendEmptyMessage(222);
                                    } else  {
                                        handler.sendEmptyMessage(333);
                                    }
                                }
                            }
                        }).start();
                    }
                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();
                break;
            case R.id.r1:
                if (devid1 == null || "".equals(devid1)) {
                    ToastUtils.makeShortText("切换设备失败", saomaActivity.this);
                    return;
                } else {
                    registering = new CustomProgressDialog(saomaActivity.this);
                    registering.setCancelable(false);
                    registering.setCanceledOnTouchOutside(false);
                    registering.show();
                    Constant.groupid = groupid1;
                    SipInfo.paddevId=devid1;
                    ToastUtils.makeShortText("切换设备为" + devid1, saomaActivity.this);
                    ECoderTitle.setText("当前设备号:  " + Constant.devid1);
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            response = GetPostUtil.sendGet1111(Constant.URL_InquireUser, "groupid=" + Constant.groupid);
                            Log.i("jonsresponse...........", response);
                            if ((response != null) && !("".equals(response))) {
                                UserFromGroup userFromGroup = JSON.parseObject(response, UserFromGroup.class);
                                List<UserList> userList = userFromGroup.getUserList();
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
                                registering.dismiss();
                            } else {
                                Looper.prepare();
                                ToastUtils.makeShortText("获取用户数据失败请重试", saomaActivity.this);
                                registering.dismiss();
                                Looper.loop();
                            }
                        }
                    }).start();
                            }

                break;
            case R.id.b2:
                Intent intent2 = new Intent(saomaActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent2, REQUEST_CODE_SCAN2);
                break;
            case R.id.input2:
                final EditText editText2 = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入设备号").setIcon(
                        android.R.drawable.ic_dialog_info).setView(editText2
                ).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        devid = editText2.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                response = GetPostUtil.sendGet1111(Constant.URL_InquireBind, "devid=" + devid);
                                Log.i("jonsresponse...........", response);
                                JSONObject obj1 = JSON.parseObject(response);
                                String msg1 = obj1.getString("msg");
                                if (msg1.equals("未绑定")) {
                                    response = GetPostUtil.sendGet1111(Constant.URL_Bind, "id=" + Constant.id + "&devid=" + devid);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj = JSON.parseObject(response);
                                    String msg = obj.getString("msg");
                                    if (msg.equals("success")) {
                                        handler.sendEmptyMessage(1111);
                                    } else if (msg.equals("已绑定")) {
                                        handler.sendEmptyMessage(222);
                                    } else if (msg.equals("设备绑定用户失败")) {
                                        handler.sendEmptyMessage(333);
                                    }
                                } else if (msg1.equals("已绑定")) {
                                    //发消息给平台，转发给群主验证，通过后绑定
                                    response = GetPostUtil.sendGet1111(Constant.URL_joinGroup, "devid=" + devid+"&id="+Constant.id);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj2 = JSON.parseObject(response);
                                    String msg2 = obj2.getString("msg");
                                    if (msg2.equals("success")) {
                                        handler.sendEmptyMessage(2222);
                                    } else if (msg2.equals("已经加群")) {
                                        handler.sendEmptyMessage(222);
                                    } else  {
                                        handler.sendEmptyMessage(333);
                                    }
                                }
                            }
                        }).start();
                    }
                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();
                break;
            case R.id.r2:
                if (devid2 == null || "".equals(devid2)) {
                    ToastUtils.makeShortText("切换设备失败", saomaActivity.this);
                    return;
                } else {
                    registering = new CustomProgressDialog(saomaActivity.this);
                    registering.setCancelable(false);
                    registering.setCanceledOnTouchOutside(false);
                    registering.show();
                    groupid = groupid2;
                    SipInfo.paddevId=devid2;
                    ToastUtils.makeShortText("切换设备为" + devid2, saomaActivity.this);
                    ECoderTitle.setText("当前设备号:  " + Constant.devid2);
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            response = GetPostUtil.sendGet1111(Constant.URL_InquireUser, "groupid=" + Constant.groupid);
                            Log.i("jonsresponse...........", response);
                            if ((response != null) && !("".equals(response))) {
                                UserFromGroup userFromGroup = JSON.parseObject(response, UserFromGroup.class);
                                List<UserList> userList = userFromGroup.getUserList();
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
                                registering.dismiss();
                            } else {
                                Looper.prepare();
                                ToastUtils.makeShortText("获取用户数据失败请重试", saomaActivity.this);
                                registering.dismiss();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.b3:
                Intent intent3 = new Intent(saomaActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent3, REQUEST_CODE_SCAN3);
                break;
            case R.id.input3:
                final EditText editText1 = new EditText(this);
                new AlertDialog.Builder(this).setTitle("请输入设备号").setIcon(
                        android.R.drawable.ic_dialog_info).setView(editText1
                ).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        devid = editText1.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                response = GetPostUtil.sendGet1111(Constant.URL_InquireBind, "devid=" + devid);
                                Log.i("jonsresponse...........", response);
                                JSONObject obj1 = JSON.parseObject(response);
                                String msg1 = obj1.getString("msg");
                                if (msg1.equals("未绑定")) {
                                    response = GetPostUtil.sendGet1111(Constant.URL_Bind, "id=" + Constant.id + "&devid=" + devid);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj = JSON.parseObject(response);
                                    String msg = obj.getString("msg");
                                    if (msg.equals("success")) {
                                        handler.sendEmptyMessage(1111);
                                    } else if (msg.equals("已绑定")) {
                                        handler.sendEmptyMessage(222);
                                    } else if (msg.equals("设备绑定用户失败")) {
                                        handler.sendEmptyMessage(333);
                                    }
                                } else if (msg1.equals("已绑定")) {
                                    //发消息给平台，转发给群主验证，通过后绑定
                                    response = GetPostUtil.sendGet1111(Constant.URL_joinGroup, "devid=" + devid+"&id="+Constant.id);
                                    Log.i("jonsresponse...........", response);
                                    JSONObject obj2 = JSON.parseObject(response);
                                    String msg2 = obj2.getString("msg");
                                    if (msg2.equals("success")) {
                                        handler.sendEmptyMessage(3333);
                                    } else if (msg2.equals("已经加群")) {
                                        handler.sendEmptyMessage(222);
                                    } else  {
                                        handler.sendEmptyMessage(333);
                                    }
                                }
                            }
                        }).start();
                    }
                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();

                break;
            case R.id.r3:
                if (devid3 == null || "".equals(devid3)) {
                    ToastUtils.makeShortText("切换设备失败", saomaActivity.this);
                    return;
                } else {
                    registering = new CustomProgressDialog(saomaActivity.this);
                    registering.setCancelable(false);
                    registering.setCanceledOnTouchOutside(false);
                    registering.show();
                    groupid = groupid3;
                    SipInfo.paddevId=devid3;
                    ToastUtils.makeShortText("切换设备为" + devid3, saomaActivity.this);
                    ECoderTitle.setText("当前设备号:  " + Constant.devid3);
                   new Thread( new Runnable() {
                        @Override
                        public void run() {
                            response = GetPostUtil.sendGet1111(Constant.URL_InquireUser, "groupid=" + Constant.groupid);
                            Log.i("jonsresponse...........", response);
                            if ((response != null) && !("".equals(response))) {
                                UserFromGroup userFromGroup = JSON.parseObject(response, UserFromGroup.class);
                                List<UserList> userList = userFromGroup.getUserList();
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
                                registering.dismiss();
                            } else {
                                Looper.prepare();
                                ToastUtils.makeShortText("获取用户数据失败请重试", saomaActivity.this);
                                registering.dismiss();
                                Looper.loop();
                            }
                        }
                    }).start();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
