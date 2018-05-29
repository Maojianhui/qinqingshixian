package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.R;
import com.app.db.DatabaseInfo;
import com.app.groupvoice.GroupInfo;
import com.app.model.Constant;
import com.app.model.Msg;
import com.app.model.MyFile;
import com.app.service.BinderPoolService;
import com.app.service.NewsService;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.sip.SipUser;
import com.app.tools.ActivityCollector;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.app.model.Constant.groupid1;
import static com.app.sip.SipInfo.running;
import static com.app.sip.SipInfo.sipDev;
import static com.app.sip.SipInfo.sipUser;

/**
 * Author chzjy
 * Date 2016/12/19.
 * 主界面
 */

public class Main extends Activity implements View.OnClickListener, SipUser.LoginNotifyListener, SipUser.BottomListener {
    private final String TAG = getClass().getSimpleName();
    @Bind(R.id.network_layout)
    LinearLayout networkLayout;
    @Bind(R.id.content_frame)
    FrameLayout contentFrame;
    @Bind(R.id.message)
    ImageButton message;
    @Bind(R.id.message_text)
    TextView messageText;
    @Bind(R.id.person)
    ImageButton person;
    @Bind(R.id.person_text)
    TextView personText;
    @Bind(R.id.shop)
    ImageButton shop;
    @Bind(R.id.shop_text)
    TextView shopText;
    @Bind(R.id.old)
    ImageButton old;
    @Bind(R.id.old_text)
    TextView oldText;
    @Bind(R.id.menu_layout)
    LinearLayout menuLayout;
    @Bind(R.id.count)
    TextView messageCount;


    private FragmentManager fm;
    private FragmentTransaction ft;
    //个人中心界面
    private PersonFragment personFragment;
    //老人界面
    private LaoRenFragment laorenFragment;
    //聊天界面
    private MessageFragment messageFragment;
//    //语音呼叫界面
//    private AudioFragment audioFragment;
//    //联系人界面
//    private ContactFragment contactFragment;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //EventBus.getDefault().register(this);
        ActivityCollector.addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }




    @Override
    protected void onResume() {
        super.onResume();
        setButtonType(Constant.SAVE_FRAGMENT_SELECT_STATE);
        SipInfo.lastestMsgs = DatabaseInfo.sqLiteManager.queryLastestMsg();
        SipInfo.messageCount = 0;
        for (int i = 0; i < SipInfo.lastestMsgs.size(); i++) {
            if (SipInfo.lastestMsgs.get(i).getType() == 0) {
                SipInfo.messageCount += SipInfo.lastestMsgs.get(i).getNewMsgCount();
            }
        }
        if (SipInfo.messageCount != 0) {
            messageCount.setVisibility(View.VISIBLE);
            messageCount.setText(String.valueOf(SipInfo.messageCount));
        } else {
            messageCount.setVisibility(View.INVISIBLE);
        }

    }

    private void init() {
        fm = getFragmentManager();

        setButtonType(Constant.MESSAGE);
        setButtonType(Constant.Person);

        setButtonType(Constant.SHOP);
        setButtonType(Constant.OLD);
        setButtonType(Constant.SAVE_FRAGMENT_SELECT_STATE);

        message.setOnClickListener(this);
        shop.setOnClickListener(this);
        person.setOnClickListener(this);
        old.setOnClickListener(this);


        sipUser.setLoginNotifyListener(this);
        sipUser.setBottomListener(this);
        //启动语音电话服务
        //startService(new Intent(Main.this, SipService.class));
        //启动监听服务
        startService(new Intent(this, NewsService.class));
        //启动aidl接口服务
        startService(new Intent(this, BinderPoolService.class));
        SipInfo.loginReplace = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    sipDev.sendMessage(SipMessageFactory.createNotifyRequest(sipDev, SipInfo.dev_to,
                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                }
                //关闭语音电话服务
                //stopService(new Intent(Main.this, SipService.class));
                //关闭监听服务
                stopService(new Intent(Main.this, NewsService.class));
                //关闭PTT监听服务
//                stopService(new Intent(Main.this, PTTService.class));
                //关闭aidl接口服务
                stopService(new Intent(Main.this, BinderPoolService.class));
                //关闭用户心跳
                SipInfo.keepUserAlive.stopThread();
                //关闭设备心跳
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    SipInfo.keepDevAlive.stopThread();
                }
                running=false;
                //重置登录状态
                SipInfo.userLogined = false;
                SipInfo.devLogined = false;
                //关闭集群呼叫
                GroupInfo.rtpAudio.removeParticipant();
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    GroupInfo.groupUdpThread.stopThread();
                    GroupInfo.groupKeepAlive.stopThread();
                }
                AlertDialog loginReplace = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("账号异地登录")
                        .setMessage("请重新登录")
                        .setPositiveButton("确定", null)
                        .create();
                loginReplace.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                loginReplace.show();
                loginReplace.setCancelable(false);
                loginReplace.setCanceledOnTouchOutside(false);
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                super.handleMessage(msg);
            }
        };
    }

    /**
     * 更改设置底部按钮样式
     */
    public void setButtonType(int id) {
        reSetButtonType();
        Constant.SAVE_FRAGMENT_SELECT_STATE = id;
        int color = getResources().getColor(R.color.select);
        switch (id) {
            case Constant.MESSAGE:
                message.setImageResource(R.drawable.icon_message_pressed);
                messageText.setTextColor(color);
                showFragment(Constant.MESSAGE);
                break;
//            case Constant.CONTACT:
//                contacts.setImageResource(R.drawable.icon_contact_pressed);
//                contactsText.setTextColor(color);
//                showFragment(Constant.CONTACT);
//                break;
            case Constant.SHOP:
                shop.setImageResource(R.drawable.icon_phone_pressed);
                shopText.setTextColor(color);
                showFragment(Constant.SHOP);
                break;
            case Constant.OLD:
                old.setImageResource(R.drawable.icon_video_pressed);
                oldText.setTextColor(color);
                showFragment(Constant.OLD);
                break;
            case Constant.Person:
                person.setImageResource(R.drawable.icon_menu_pressed);
                personText.setTextColor(color);
                showFragment(Constant.Person);
                break;
        }
    }

    /**
     * 重置底部按钮样式
     */
    public void reSetButtonType() {
        message.setImageResource(R.drawable.icon_message_normal);
        messageText.setTextColor(Color.WHITE);
        shop.setImageResource(R.drawable.icon_phone_normal);
        shopText.setTextColor(Color.WHITE);
        person.setImageResource(R.drawable.icon_menu_normal);
        personText.setTextColor(Color.WHITE);
        old.setImageResource(R.drawable.icon_video_normal);
        oldText.setTextColor(Color.WHITE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityCollector.removeActivity(this);
        ButterKnife.unbind(this);
        if ((groupid1 != null) && !("".equals(groupid1))) {
            SipInfo.keepUserAlive.stopThread();
            SipInfo.keepDevAlive.stopThread();
        }
        //关闭集群呼叫
       GroupInfo.wakeLock.release();
        if ((groupid1 != null) && !("".equals(groupid1))) {
        GroupInfo.rtpAudio.removeParticipant();
            GroupInfo.groupUdpThread.stopThread();
            GroupInfo.groupKeepAlive.stopThread();
        }
        SipInfo.userLogined = false;
        SipInfo.devLogined = false;
        SipInfo.loginReplace = null;
        //停止语音电话服务
        //stopService(new Intent(Main.this, SipService.class));
        //关闭监听服务
        stopService(new Intent(Main.this, NewsService.class));
        //停止PPT监听服务
//        stopService(new Intent(this, PTTService.class));
        //停止aidl接口服务
        stopService(new Intent(Main.this, BinderPoolService.class));
        sipUser.setLoginNotifyListener(null);
        sipUser.setBottomListener(null);
        //关闭线程池
        sipUser.shutdown();
        if ((groupid1 != null) && !("".equals(groupid1))) {
            sipDev.shutdown();
        }
        //关闭监听线程
        sipUser.halt();
        if ((groupid1 != null) && !("".equals(groupid1))) {
            sipDev.halt();
        }
        System.gc();
        running=false;
    }

    /**
     * 显示Fragment
     */
    public void showFragment(int index) {
        ft = fm.beginTransaction();
        hideFragment(ft);
        switch (index) {
            case Constant.MESSAGE:
                if (messageFragment != null) {
                    ft.show(messageFragment);
                }else {
                    messageFragment = new MessageFragment();
                    ft.add(R.id.content_frame, messageFragment);
                }
                break;
//            case Constant.CONTACT:
//                if (contactFragment != null) {
//                    ft.show(contactFragment);
//                } else {
//                    contactFragment = new ContactFragment();
//                    ft.add(R.id.content_frame, contactFragment);
//                }
//                break;
            case Constant.Person:
                if (personFragment!= null) {
                    ft.show(personFragment);
                }else {
                    personFragment = new PersonFragment();
                    ft.add(R.id.content_frame, personFragment);
                }
                menuLayout.setVisibility(View.VISIBLE);
                break;
//            case Constant.PHONE:
//                if (audioFragment != null)
//                    ft.show(audioFragment);
//                else {
//                    audioFragment = new AudioFragment();
//                    ft.add(R.id.content_frame, audioFragment);
//                }
//                break;
            case Constant.OLD:
                if (laorenFragment != null)
                    ft.show(laorenFragment);
                else {
                    laorenFragment = new LaoRenFragment();
                    ft.add(R.id.content_frame, laorenFragment);
                }
                break;
        }
        ft.commitAllowingStateLoss();
    }

    /**
     * 隐藏Fragment
     */
    public void hideFragment(FragmentTransaction ft) {
        if (messageFragment != null) {
            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.MESSAGE) {
                ft.hide(messageFragment);
            }
        }
//        if (contactFragment != null) {
//            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.CONTACT) {
//                ft.hide(contactFragment);
//            }
//        }
        if (personFragment != null) {
            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.Person) {
                ft.hide(personFragment);
            }
        }

//        if (audioFragment != null) {
//            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.PHONE) {
//                ft.hide(audioFragment);
//            }
//        }
        if (laorenFragment != null) {
            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.OLD) {
                ft.hide(laorenFragment);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message:
                setButtonType(Constant.MESSAGE);
                break;
            case R.id.shop:
                setButtonType(Constant.SHOP);
                break;
            case R.id.old:
                setButtonType(Constant.OLD);
                break;
            case R.id.person:
                setButtonType(Constant.Person);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("keyCode = " + keyCode);
        if (keyCode == 82) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("注销账户?")
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
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
                            running=false;
                            ActivityCollector.finishToFirstView();
                        }
                    }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return true;
        }
        if (keyCode == 4) {
            setButtonType(Constant.OLD);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onDevNotify() {
//        laorenFragment.devNotify();
    }

    @Override
    public void onUserNotify() {
//        audioFragment.userNotify();
//        contactFragment.notifyFriendListChanged();
    }

    @Override
    public void onReceivedBottomMessage(Msg msg) {
        SipInfo.messageCount++;
        handler.post(new Runnable() {
            @Override
            public void run() {
                messageCount.setVisibility(View.VISIBLE);
                messageCount.setText(String.valueOf(SipInfo.messageCount));
            }
        });
    }

    @Override
    public void onReceivedBottomFileshare(MyFile myfile) {

    }


}
