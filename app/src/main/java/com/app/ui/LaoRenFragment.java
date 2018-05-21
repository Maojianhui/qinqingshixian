package com.app.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.app.LoadPicture;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.friendCircleMain.adapter.MyListAdapter;
import com.app.friendCircleMain.custonListView.CustomListView;
import com.app.friendCircleMain.domain.FirendMicroList;
import com.app.friendCircleMain.domain.FirendMicroListDatas;
import com.app.friendCircleMain.domain.FirendsMicro;
import com.app.friendcircle.PublishedActivity;
import com.app.model.Constant;
import com.app.model.MessageEvent;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.utils.GetPostUtil;
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

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static com.app.R.id.t1;



public class LaoRenFragment extends Fragment implements View.OnClickListener {

    TextView title;
    private Boolean shan = true;
    private CustomProgressDialog inviting;
    private Handler handlervideo = new Handler();
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String avaPath = SdCard + "/fanxin/Files/Camera/Image/";
    private static final String TAG = "MicroActivity";
    int now = 0;
    private int count = 1;
    List<FirendMicroListDatas> listdatas = new ArrayList<FirendMicroListDatas>();//json数据
    private View header;
    public CustomListView listview;
    public MyListAdapter mAdapter;//这是真正的
    //	private boolean flag=false;//是否是提交评论：false：第一次进入；true:提交评论的刷新进入；
    private CircleImageView MicroIcon;
    private CircleImageView alarm;
    private TextView MicroName;
    private ImageButton camera;
    //private static String res="";//json数据
    private LoadPicture avatarLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.micro_activity_main, container, false);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RelativeLayout lay = (RelativeLayout) getView().findViewById(
                R.id.main2);
        camera = (ImageButton) lay.findViewById(R.id.camera111);
        camera.setVisibility(View.VISIBLE);
        camera.setOnClickListener(this);
        title = (TextView) lay.findViewById(R.id.title);
        title.setText("亲情在线");
        avatarLoader = new LoadPicture(getActivity(), avaPath);
        init();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x111) {
                getData(msg.obj.toString());
            } else if (msg.what == 0x222) {
                getData(msg.obj.toString());
            }else if (msg.what == 666) {
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
           shan=true;
           new Thread(runnable).start();
       }
    }
    private void init() {
        EventBus.getDefault().register(this);  //注册
        String vatar_temp = LocalUserInfo.getInstance(getActivity())
                .getUserInfo("avatar");
        String nick = LocalUserInfo.getInstance(getActivity()).getUserInfo("nick");
        header = LayoutInflater.from(getActivity()).inflate(R.layout.micro_list_header, null);
        MicroIcon = (CircleImageView) header.findViewById(R.id.MicroIcon);
        MicroName = (TextView) header.findViewById(R.id.MicroName);
        alarm = (CircleImageView) header.findViewById(R.id.alarm);



        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    shan = false;

                alarm.setVisibility(View.INVISIBLE);
            }
        });
        MicroName.setText(nick);
        TextView t0 = (TextView) header.findViewById(R.id.t0);
        t0.setOnClickListener(this);
        TextView t1 = (TextView) header.findViewById(R.id.t1);
        t1.setOnClickListener(this);
        TextView t2 = (TextView) header.findViewById(R.id.t2);
        t2.setOnClickListener(this);
        TextView t3 = (TextView) header.findViewById(R.id.t3);
        t3.setOnClickListener(this);
        TextView t4 = (TextView) header.findViewById(R.id.t4);
        t4.setText(SipInfo.friends.size() + "位亲");
        t4.setOnClickListener(this);
        showUserAvatar(MicroIcon, vatar_temp);
        listview = (CustomListView) getActivity().findViewById(R.id.list);
        listview.setVerticalScrollBarEnabled(false);
        listview.setDivider(null);
        listview.addHeaderView(header);
        listview.setAdapter(mAdapter);
//		flag=false;
//		mAdapter = new MyListAdapter(this, listdatas,flag);
        mAdapter = new MyListAdapter(getActivity(), listdatas);
        listview.setAdapter(mAdapter);
listdatas.clear();
        getMicroList(0, true);
//		new MyTask().execute();
        //下拉刷新
        listview.setOnRefreshListener(new CustomListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                new Thread() {
                    @Override
                    public void run() {
                        count = 1;
                        Constant.res = GetPostUtil.sendGet1111(Constant.URL_getPostList, "id=" +
                                Constant.id + "&currentPage=" + count + "&groupid=" + Constant.groupid);
                        Log.i("jonsresponse...........", Constant.res + "");
                        Message msg = handler.obtainMessage();
                        msg.what = 0x111;
                        msg.obj = "下拉刷新";
                        handler.sendMessage(msg);

                    }
                }.start();
                //String s="下拉刷新";
                //getData(s);
            }

        });
        //上拉加载更多
        listview.setOnLoadListener(new CustomListView.OnLoadMoreListener() {

            public void onLoadMore() {
                new Thread() {
                    @Override
                    public void run() {
                        count++;
                        Constant.res = GetPostUtil.sendGet1111(Constant.URL_getPostList, "id=" +
                                Constant.id + "&currentPage=" + count + "&groupid=" + Constant.groupid);
                        Log.i("jonsresponse...........", Constant.res);
                        Message msg = handler.obtainMessage();
                        msg.what = 0x222;
                        msg.obj = "上拉加载更多";
                        handler.sendMessage(msg);

                    }
                }.start();
//                String s = "上拉加载更多";
//                getData(s);

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//取消注册
    }

    @Override
    public void onResume() {
        super.onResume();
        String vatar_temp = LocalUserInfo.getInstance(getActivity())
                .getUserInfo("avatar");
        String nick = LocalUserInfo.getInstance(getActivity()).getUserInfo("nick");
        MicroName.setText(nick);
        showUserAvatar(MicroIcon, vatar_temp);
    }

    private void showUserAvatar(ImageView iamgeView, String avatar) {
        final String url_avatar = Constant.URL_Avatar + Constant.id + "/" + avatar;
        iamgeView.setTag(url_avatar);
        if (avatar != null && !avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(iamgeView, url_avatar,
                    new LoadPicture.ImageDownloadedCallBack() {

                        @Override
                        public void onImageDownloaded(ImageView imageView,
                                                      Bitmap bitmap) {
                            if (imageView.getTag() == url_avatar) {
                                imageView.setImageBitmap(bitmap);

                            }
                        }

                    });
            if (bitmap != null)
                iamgeView.setImageBitmap(bitmap);

        }
    }

    /**
     * 得到主界面的列表
     *
     * @param i
     * @param has
     */
    private void getMicroList(final int i, boolean has) {
        if (TextUtils.isEmpty(Constant.res)) {

            return;
        }

        FirendsMicro fm = JSON.parseObject(Constant.res, FirendsMicro.class);
        //FirendMicroList fList=fm.getFriendPager();
        FirendMicroList fList = fm.getPostList();
        //if("0".equals(fm.getError())){

        if (i == 0) {
            listdatas.clear();

        }

        if (null == fList.getDatas() || fList.getDatas().size() == 0) {
            if (i == 0) {
                listview.onRefreshComplete();
            } else {
                listview.onLoadMoreComplete(false);
            }
        } else {
            if (i == 0) {
                listview.onRefreshComplete();
            } else {
                listview.onLoadMoreComplete();
            }
            listdatas.addAll(fList.getDatas());

        }
        int k = listdatas.size();
        now = k > 0 ? k - 1 : 0;
        mAdapter.notifyDataSetChanged();
        //}
    }

    private void getData(String s) {
        // TODO Auto-generated method stub
        if ("下拉刷新".equals(s)) {

            getMicroList(0, true);

            listview.onRefreshComplete();
        } else {
            getMicroList(now, true);

            listview.onLoadMoreComplete(); // 加载更多完成
        }
    }

    public void LoadList() {
        getMicroList(0, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera111:
                startActivity(new Intent(getActivity(), PublishedActivity.class));
                break;
            case t1:
                String devId = SipInfo.paddevId;
                devId = devId.substring(0, devId.length() - 4).concat("0160");//设备id后4位替换成0160
                String devName = "pad";
                final String devType = "2";
                SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                SipInfo.toDev = new NameAddress(devName, sipURL);
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
                break;
            case R.id.t2:
                startActivity(new Intent(getActivity(), ChsChange.class));
                break;
            case R.id.t3:
                //云相册
                break;
            case R.id.t4:
                startActivity(new Intent(getActivity(), friendActivity.class));
                break;
            case R.id.t0:
                //大事记
                break;
            default:
                break;
        }
    }

}


