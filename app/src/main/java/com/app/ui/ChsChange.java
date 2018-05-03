package com.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.app.R;
import com.app.groupvoice.GroupInfo;
import com.app.groupvoice.GroupKeepAlive;
import com.app.groupvoice.GroupSignaling;
import com.app.groupvoice.GroupUdpThread;
import com.app.sip.SipInfo;
import com.app.tools.MyToast;
import com.app.video.H264Sending;
import com.app.video.VideoInfo;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.sip.SipInfo.serverIp;

/**
 * Author chzjy
 * Date 2016/12/19.
 * 集群呼叫频道更换
 */
public class ChsChange extends Activity implements View.OnTouchListener {
    Button b1;
    @Bind(R.id.btnCall)
    ImageButton btnCall;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.btn1)
    Button btn1;
    @Bind(R.id.btn2)
    Button btn2;
    @Bind(R.id.btn3)
    Button btn3;
    @Bind(R.id.btn4)
    Button btn4;
    @Bind(R.id.btn5)
    Button btn5;
    @Bind(R.id.btn6)
    Button btn6;
    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        GroupInfo.rtpAudio.changeParticipant(serverIp, GroupInfo.port);
                        GroupInfo.groupUdpThread = new GroupUdpThread(serverIp, GroupInfo.port);
                        GroupInfo.groupUdpThread.startThread();
                        GroupInfo.groupKeepAlive = new GroupKeepAlive();
                        GroupInfo.groupKeepAlive.startThread();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                title.setText("频道更换(当前:频道" + (GroupInfo.port % 7000 + 1) + ")");
                                MyToast.show(ChsChange.this, "频道切换至" + (GroupInfo.port % 7000 + 1), Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }
            }.start();
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chschange);
        ButterKnife.bind(this);
        title.setText("频道更换(当前:频道" + (GroupInfo.port % 7000 + 1) + ")");
        b1=(Button)findViewById(R.id.b1) ;
        b1.setOnTouchListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6})
    public void onClick(View view) {
        GroupInfo.groupUdpThread.stopThread();
        GroupInfo.groupKeepAlive.stopThread();
        switch (view.getId()) {
            case R.id.btn1:
                GroupInfo.port = 7000;
                break;
            case R.id.btn2:
                GroupInfo.port = 7001;
                break;
            case R.id.btn3:
                GroupInfo.port = 7002;
                break;
            case R.id.btn4:
                GroupInfo.port = 7003;
                break;
            case R.id.btn5:
                GroupInfo.port = 7004;
                break;
            case R.id.btn6:
                GroupInfo.port = 7005;
                break;
        }
        handler.sendEmptyMessage(0x1111);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.b1) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                MyToast.show(this, "结束说话...", Toast.LENGTH_LONG);
                if (GroupInfo.rtpAudio != null) {
                    System.out.println(222);
                    GroupInfo.rtpAudio.pttChanged(false);
                    if (GroupInfo.isSpeak) {
                        GroupSignaling groupSignaling = new GroupSignaling();
                        groupSignaling.setEnd(SipInfo.devId);
                        String end = JSON.toJSONString(groupSignaling);
                        GroupInfo.groupUdpThread.sendMsg(end.getBytes());
                        waitFor();
                        if (VideoInfo.track != null) {
                            VideoInfo.track.play();
                        }
                        //发送消息通知H264Sending重新开启G711_encode线程
                        if (VideoInfo.handler != null)
                            VideoInfo.handler.sendEmptyMessage(0x1111);
                    }
                }
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                MyToast.show(this, "正在说话...", Toast.LENGTH_LONG);
                if (GroupInfo.rtpAudio != null) {
                    System.out.println(111);
                    GroupInfo.rtpAudio.pttChanged(true);
                    if (VideoInfo.track != null) {
                        VideoInfo.track.stop();
                    }
                    H264Sending.G711Running = false;
                    waitFor();
                    GroupSignaling groupSignaling = new GroupSignaling();
                    groupSignaling.setStart(SipInfo.devId);
                    groupSignaling.setLevel(GroupInfo.level);
                    String start = JSON.toJSONString(groupSignaling);
                    GroupInfo.groupUdpThread.sendMsg(start.getBytes());
                }
            }
        }
        return false;
    }

    private void waitFor() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
