package com.app.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.app.friendCircleMain.custonListView.CustomListView;
import com.app.friendCircleMain.domain.UserFromGroup;
import com.app.friendCircleMain.domain.UserList;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.model.MessageEvent;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.http.GetPostUtil;
import com.app.ui.FamilyCircle;
import com.app.ui.FriendCallActivity;
import com.app.ui.ShopActivity;
import com.app.ui.VideoDial;
import com.app.ui.VideoPlay;
import com.app.video.RtpVideo;
import com.app.video.SendActivePacket;
import com.app.video.VideoInfo;
import com.app.view.CircleImageView;
import com.app.view.CustomProgressDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import com.app.R;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import static com.amap.api.mapcore2d.p.i;
import static com.app.model.Constant.devid1;

import com.app.model.Constant;


public class LaoRenFragment extends Fragment implements View.OnClickListener {

    TextView title;
    private Boolean shan = true;
    private CustomProgressDialog inviting;
    private Handler handlervideo = new Handler();
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String TAG = "MicroActivity";
    private List<UserList> userList = new ArrayList<UserList>();
//    private CustomProgressDialog registering;//圈圈
    public CustomListView listview;
    private CircleImageView alarm;
    private ImageView camera;
    private RelativeLayout re_background;
    private RelativeLayout re_funcation;
    //private static String res="";//json数据


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.micro_list_header1, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏
//            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
        //得到当前界面的装饰视图
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getActivity().getWindow().getDecorView();
            //设置让应用主题内容占据状态栏和导航栏
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏和导航栏颜色为透明
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }


        init();
        int color = getResources().getColor(R.color.reset1);
        Window window = getActivity().getWindow();
        //如果系统5.0以上
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
//            View decorView = getActivity().getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
//            WindowManager.LayoutParams localLayoutParams = getActivity().getWindow().getAttributes();
//            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
//
//        }
//        changStatusIconCollor(true);
//        StatusBarUtil.statusBarLightMode(getActivity());
//        changeStatusBarTextColor(true);

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 666) {
                alarm.setVisibility(View.INVISIBLE);
            }else if (msg.what == 888) {
                alarm.setVisibility(View.VISIBLE);
            }
        }
    };
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            try {
                while (shan) {
                    handler.sendEmptyMessage(666);
                    Thread.sleep(500);
                    handler.sendEmptyMessage(888);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void aaa(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals("警报")){
            Log.d("111111   ","111111111");
            /*修改xml中某一区域的背景*/
            //方法一：
//            Resources resources = getActivity().getResources();
//            Drawable btnDrawable = resources.getDrawable(R.drawable.background2);
//            re_background.setBackgroundDrawable(btnDrawable);
            //方法二：
            re_background.setBackgroundResource(R.drawable.background2);
            shan=true;
            new Thread(runnable).start();
        }
    }
    private void init() {
        EventBus.getDefault().register(this);  //注册
        re_background=(RelativeLayout)getView().findViewById(R.id.re_background);
        re_funcation=(RelativeLayout)getView().findViewById(R.id.re_funcation);
        camera=(ImageView)re_background.findViewById(R.id.iv_camera);
        camera.setVisibility(View.VISIBLE);
        camera.setOnClickListener(this);

        alarm=(CircleImageView)re_background.findViewById(R.id.alarm1) ;
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shan = false;
                alarm.setVisibility(View.VISIBLE);
                re_background.setBackgroundResource(R.drawable.background1);
            }
        });
        ImageView application=(ImageView)re_funcation.findViewById(R.id.application);
        ImageView video=(ImageView)re_funcation.findViewById(R.id.video);
        ImageView browse=(ImageView)re_funcation.findViewById(R.id.browse);
        ImageView chat=(ImageView)re_funcation.findViewById(R.id.chat);
        application.setOnClickListener(this);
        video.setOnClickListener(this);
        browse.setOnClickListener(this);
        chat.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//取消注册
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera:
                startActivity(new Intent(getActivity(), FamilyCircle.class));
                break;
            case R.id.browse:
                if((devid1==null)||("".equals(devid1)))
                {
                    AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity())
                            .setTitle("请先绑定设备")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    dialog.show();

                }
                else {
                    SipInfo.single=true;
                    String devId = SipInfo.paddevId;
                    devId = devId.substring(0, devId.length() - 4).concat("0160");//设备id后4位替换成0160
                    String devName = "pad";
                    final String devType = "2";
                    SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                    SipInfo.toDev = new NameAddress(devName, sipURL);
                    org.zoolu.sip.message.Message response = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                            SipInfo.user_from, BodyFactory.createStartMonitor(true,SipInfo.devId,SipInfo.userId));
                    SipInfo.sipUser.sendMessage(response);
                    SipInfo.queryResponse = false;
                    SipInfo.inviteResponse = false;
                    inviting = new CustomProgressDialog(getActivity());
                    inviting.setCancelable(false);
                    inviting.setCanceledOnTouchOutside(false);
                    inviting.show();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                org.zoolu.sip.message.Message query = SipMessageFactory.createOptionsRequest(SipInfo.sipUser, SipInfo.toDev,
                                        SipInfo.user_from, BodyFactory.createQueryBody(devType));
                                outer:
                                for (int i = 0; i < 3; i++) {
                                    SipInfo.sipUser.sendMessage(query);
                                    for (int j = 0; j < 20; j++) {
                                        sleep(100);
                                        if (SipInfo.queryResponse) {
                                            break outer;
                                        }
                                    }
                                    if (SipInfo.queryResponse) {
                                        break;
                                    }
                                }
                                if (SipInfo.queryResponse) {
                                    org.zoolu.sip.message.Message invite = SipMessageFactory.createInviteRequest(SipInfo.sipUser,
                                            SipInfo.toDev, SipInfo.user_from, BodyFactory.createMediaBody(VideoInfo.resultion, "H.264", "G.711", devType));
                                    outer2:
                                    for (int i = 0; i < 3; i++) {
                                        SipInfo.sipUser.sendMessage(invite);
                                        for (int j = 0; j < 20; j++) {
                                            sleep(100);
                                            if (SipInfo.inviteResponse) {
                                                break outer2;
                                            }
                                        }
                                        if (SipInfo.inviteResponse) {
                                            break;
                                        }
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                inviting.dismiss();
                                if (SipInfo.queryResponse && SipInfo.inviteResponse) {
                                    Log.i("DevAdapter", "视频请求成功");
                                    SipInfo.decoding = true;
                                    try {
                                        VideoInfo.rtpVideo = new RtpVideo(VideoInfo.rtpIp, VideoInfo.rtpPort);
                                        VideoInfo.sendActivePacket = new SendActivePacket();
                                        VideoInfo.sendActivePacket.startThread();
                                        getActivity().startActivity(new Intent(getActivity(), VideoPlay.class));
                                    } catch (SocketException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.i("DevAdapter", "视频请求失败");
                                    handlervideo.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle("视频请求失败！")
                                                    .setMessage("请重新尝试")
                                                    .setPositiveButton("确定", null).show();

                                        }
                                    });
                                }
                            }
                        }
                    }.start();
                }
//                SipCallMananger.getInstance().callVideoChat(getActivity(),true);
                break;
            case R.id.chat:
//                userList.clear();
//                SipInfo.friends.clear();
//                new Thread(getuserfromgroup).start();//亲聊模块获取用户信息得时候需要
                startActivity(new Intent(getActivity(), FriendCallActivity.class));
                break;
            case R.id.application:
//                应用
//                startActivity(new Intent(getActivity(),ApplicationActivity.class));
                startActivity(new Intent(getActivity(),ShopActivity.class));
                break;
            case R.id.video:
                SipInfo.single=false;
                String devId1 = SipInfo.paddevId;
//                    devId = devId1.substring(0, devId1.length() - 4).concat("0160");//设备id后4位替换成0160
                String devName1 = "pad";
                final String devType1 = "2";
                SipURL sipURL1 = new SipURL(devId1, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                SipInfo.toDev = new NameAddress(devName1, sipURL1);
                //视频
                org.zoolu.sip.message.Message query1 = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                        SipInfo.user_from, BodyFactory.createCallRequest("request",SipInfo.devId,SipInfo.userId));
                SipInfo.sipUser.sendMessage(query1);

                startActivity(new Intent(getActivity(),VideoDial.class));
                break;
            default:
                break;
        }
    }
    private void waitFor() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private Runnable getuserfromgroup = new Runnable() {
        @Override
        public void run() {
            String response = "";
            response = GetPostUtil.sendGet1111(Constant.URL_InquireUser, "groupid=" + Constant.groupid);
            Log.i("jonsresponse...........", response);
            if (( null!= response ) && !("".equals(response))) {
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
            } else {
                Looper.prepare();
                ToastUtils.makeShortText("获取用户数据失败请重试", getActivity());

                Looper.loop();
            }
        }
    };
    public void changStatusIconCollor(boolean setDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getActivity().getWindow().getDecorView();
            if (decorView != null) {
                int vis = decorView.getSystemUiVisibility();
                if (setDark) {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }

    private void changeStatusBarTextColor(boolean isBlack) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (isBlack) {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//设置状态栏黑色字体
            }else {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//恢复状态栏白色字体
            }
        }
    }
}


