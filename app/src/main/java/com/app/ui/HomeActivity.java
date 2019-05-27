package com.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.R;
import com.app.model.Constant;
import com.app.service.BinderPoolService;
import com.app.service.NewsService;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.sip.SipUser;
import com.app.tools.ActivityCollector;
import com.app.ui.fragment.CommunityFragment;
import com.app.ui.fragment.LaoRenFragment;
import com.app.ui.fragment.MessageFragment;
import com.app.ui.fragment.PersonFragment;
//import com.punuo.sys.app.util.IntentUtil;

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

public class HomeActivity extends Activity implements View.OnClickListener, SipUser.LoginNotifyListener {
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
    @Bind(R.id.community)
    ImageButton community;
    @Bind(R.id.community_text)
    TextView communityText;
//    @Bind(R.id.shop)
//    ImageButton shop;
//    @Bind(R.id.shop_text)
//    TextView shopText;
    @Bind(R.id.old)
    ImageButton old;
    @Bind(R.id.old_text)
    TextView oldText;
    @Bind(R.id.menu_layout)
    LinearLayout menuLayout;
//    @Bind(R.id.count)
//    TextView messageCount;


    private FragmentManager fm;
    private FragmentTransaction ft;
    //个人中心界面
    private PersonFragment personFragment;
    //老人界面
    private LaoRenFragment laorenFragment;
    //聊天界面
    private MessageFragment messageFragment;
    //社区界面
    private CommunityFragment communityFragment;

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  //该参数指布局能延伸到navigationbar，我们场景中不应加这个参数
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT); //设置navigationbar颜色为透明
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        setButtonType(Constant.SAVE_FRAGMENT_SELECT_STATE);
//        SipInfo.lastestMsgs = DatabaseInfo.sqLiteManager.queryLastestMsg();
//        SipInfo.messageCount = 0;
//        for (int i = 0; i < SipInfo.lastestMsgs.size(); i++) {
//            if (SipInfo.lastestMsgs.get(i).getType() == 0) {
//                SipInfo.messageCount += SipInfo.lastestMsgs.get(i).getNewMsgCount();
//            }
//        }
//        if (SipInfo.messageCount != 0) {
//            messageCount.setVisibility(View.VISIBLE);
//            messageCount.setText(String.valueOf(SipInfo.messageCount));
//        } else {
//            messageCount.setVisibility(View.INVISIBLE);
//        }

    }

    private void init() {
        fm = getFragmentManager();

        setButtonType(Constant.MESSAGE);
        setButtonType(Constant.Person);

//        setButtonType(Constant.SHOP);
        setButtonType(Constant.COMMUNITY);
        setButtonType(Constant.OLD);
        setButtonType(Constant.SAVE_FRAGMENT_SELECT_STATE);

        message.setOnClickListener(this);
//        shop.setOnClickListener(this);
        community.setOnClickListener(this);
        person.setOnClickListener(this);
        old.setOnClickListener(this);
        sipUser.setLoginNotifyListener(this);
//        sipUser.setBottomListener(this);
        //启动语音电话服务
        //startService(new Intent(HomeActivity.this, SipService.class));
        //启动监听服务
        startService(new Intent(this, NewsService.class));
        //启动aidl接口服务
        startService(new Intent(this, BinderPoolService.class));
//        SipInfo.loginReplace = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
//                        SipInfo.user_from, BodyFactory.createLogoutBody()));
//                if ((groupid1 != null) && !("".equals(groupid1))) {
//                    sipDev.sendMessage(SipMessageFactory.createNotifyRequest(sipDev, SipInfo.dev_to,
//                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
//                }
//                //关闭语音电话服务
//                //stopService(new Intent(HomeActivity.this, SipService.class));
//                //关闭监听服务
//                stopService(new Intent(HomeActivity.this, NewsService.class));
//                //关闭PTT监听服务
////                stopService(new Intent(HomeActivity.this, PTTService.class));
//                //关闭aidl接口服务
//                stopService(new Intent(HomeActivity.this, BinderPoolService.class));
//                //关闭用户心跳
//                SipInfo.keepUserAlive.stopThread();
//                //关闭设备心跳
//                if ((groupid1 != null) && !("".equals(groupid1))) {
//                    SipInfo.keepDevAlive.stopThread();
//                }
//                running=false;
//                //重置登录状态
//                SipInfo.userLogined = false;
//                SipInfo.devLogined = false;
//                //关闭集群呼叫
////                GroupInfo.rtpAudio.removeParticipant();
////                if ((groupid1 != null) && !("".equals(groupid1))) {
////                    GroupInfo.groupUdpThread.stopThread();
////                    GroupInfo.groupKeepAlive.stopThread();
////                }
//                AlertDialog loginReplace = new AlertDialog.Builder(getApplicationContext())
//                        .setTitle("账号异地登录")
//                        .setMessage("请重新登录")
//                        .setPositiveButton("确定", null)
//                        .create();
//                loginReplace.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                loginReplace.show();
//                loginReplace.setCancelable(false);
//                loginReplace.setCanceledOnTouchOutside(false);
//                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                super.handleMessage(msg);
//            }
//        };
        SipInfo.loginReplace = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    sipDev.sendMessage(SipMessageFactory.createNotifyRequest(sipDev, SipInfo.dev_to,
                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                }
                //关闭语音电话服务
                //stopService(new Intent(HomeActivity.this, SipService.class));
                //关闭监听服务
                stopService(new Intent(HomeActivity.this, NewsService.class));
                //关闭PTT监听服务
//                stopService(new Intent(HomeActivity.this, PTTService.class));
                //关闭aidl接口服务
                stopService(new Intent(HomeActivity.this, BinderPoolService.class));
                //关闭用户心跳
                SipInfo.keepUserAlive.stopThread();
                //关闭设备心跳
                if ((groupid1 != null) && !("".equals(groupid1))) {
                    SipInfo.keepDevAlive.stopThread();
                }
                running = false;
                //重置登录状态
                SipInfo.userLogined = false;
                SipInfo.devLogined = false;
                //关闭集群呼叫
//                GroupInfo.rtpAudio.removeParticipant();
//                if ((groupid1 != null) && !("".equals(groupid1))) {
//                    GroupInfo.groupUdpThread.stopThread();
//                    GroupInfo.groupKeepAlive.stopThread();
//                }
                AlertDialog loginReplace = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("账号异地登录")
                        .setMessage("请重新登录")
                        .setPositiveButton("确定", null)
                        .create();
                loginReplace.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                loginReplace.show();
                loginReplace.setCancelable(false);
                loginReplace.setCanceledOnTouchOutside(false);
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
//                IntentUtil.jumpActivity(getApplicationContext(), LoginActivity.class);

                return true;
            }
        });
    }

    /**
     * 更改设置底部按钮样式
     */
    public void setButtonType(int id) {
        reSetButtonType();
        Constant.SAVE_FRAGMENT_SELECT_STATE = id;
//        int color = getResources().getColor(R.color.select);
        int color = getResources().getColor(R.color.reset1);
        switch (id) {
            case Constant.MESSAGE:
                message.setImageResource(R.drawable.ic_message1);
                messageText.setTextColor(color);
//                messageText.setTextColor(Color.parseColor("#474646"));
                showFragment(Constant.MESSAGE);
                break;
//            case Constant.CONTACT:
//                contacts.setImageResource(R.drawable.icon_contact_pressed);
//                contactsText.setTextColor(color);
//                showFragment(Constant.CONTACT);
//                break;
            case Constant.COMMUNITY:
                community.setImageResource(R.drawable.ic_community1);
                communityText.setTextColor(color);
                showFragment(Constant.COMMUNITY);
                break;
//            case Constant.SHOP:
//                shop.setImageResource(R.drawable.ic_mail1);
//                shopText.setTextColor(color);
////                showFragment(Constant.SHOP);
//                break;
            case Constant.OLD:
                old.setImageResource(R.drawable.ic_homepage1);
                oldText.setTextColor(color);
                showFragment(Constant.OLD);
                break;
            case Constant.Person:
                person.setImageResource(R.drawable.ic_myself1);
                personText.setTextColor(color);
                showFragment(Constant.Person);
                break;
        }
    }

    /**
     * 重置底部按钮样式
     */
    public void reSetButtonType() {
        int color1 = getResources().getColor(R.color.set);
        message.setImageResource(R.drawable.ic_message);
        messageText.setTextColor(color1);
//        messageText.setTextColor(Color.parseColor("#595959"));
//        shop.setImageResource(R.drawable.ic_mail);
//        shopText.setTextColor(color1);
        person.setImageResource(R.drawable.ic_myself);
        personText.setTextColor(color1);
        old.setImageResource(R.drawable.ic_homepage);
        oldText.setTextColor(color1);
        community.setImageResource(R.drawable.ic_community);
        communityText.setTextColor(color1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        ActivityCollector.removeActivity(this);
        ButterKnife.unbind(this);
        if ((groupid1 != null) && !("".equals(groupid1))) {
            SipInfo.keepUserAlive.stopThread();
            SipInfo.keepDevAlive.stopThread();
        }
        //关闭集群呼叫
//       GroupInfo.wakeLock.release();
//        if ((groupid1 != null) && !("".equals(groupid1))) {
//        GroupInfo.rtpAudio.removeParticipant();
//            GroupInfo.groupUdpThread.stopThread();
//            GroupInfo.groupKeepAlive.stopThread();
//        }
        SipInfo.userLogined = false;
        SipInfo.devLogined = false;
        SipInfo.loginReplace = null;
        //停止语音电话服务
        //stopService(new Intent(HomeActivity.this, SipService.class));
        //关闭监听服务
        stopService(new Intent(HomeActivity.this, NewsService.class));
        //停止PPT监听服务
//        stopService(new Intent(this, PTTService.class));
        //停止aidl接口服务
        stopService(new Intent(HomeActivity.this, BinderPoolService.class));
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
            case Constant.COMMUNITY:
                if (communityFragment != null) {
                    ft.show(communityFragment);
                } else {
                    communityFragment = new CommunityFragment();
                    ft.add(R.id.content_frame, communityFragment);
                }
                break;
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
        if (communityFragment != null) {
            if (Constant.SAVE_FRAGMENT_SELECT_STATE != Constant.COMMUNITY) {
                ft.hide(communityFragment);
            }
        }
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
//            case R.id.shop:
//                setButtonType(Constant.SHOP);
//                startActivity(new Intent(this,ShopActivity.class));
            case R.id.old:
                setButtonType(Constant.OLD);
                break;
            case R.id.person:
                setButtonType(Constant.Person);
                break;
            case R.id.community:
                setButtonType(Constant.COMMUNITY);
//                startActivity(new Intent(this,CommunityActivity.class));
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
//                            if ((groupid1 != null) && !("".equals(groupid1))) {
//                                GroupInfo.groupUdpThread.stopThread();
//                                GroupInfo.groupKeepAlive.stopThread();
//                            }
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

//    @Override
//    public void onReceivedBottomMessage(Msg msg) {
//        SipInfo.messageCount++;
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                messageCount.setVisibility(View.VISIBLE);
//                messageCount.setText(String.valueOf(SipInfo.messageCount));
//            }
//        });
//    }

//    @Override
//
//    public void onReceivedBottomFileshare(MyFile myfile) {
//
//    }


}
