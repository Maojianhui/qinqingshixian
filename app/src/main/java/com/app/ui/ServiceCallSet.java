package com.app.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.R;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.sip.SipInfo.devName;

/**
 * Created by maojianhui on 2018/6/26.
 */

public class ServiceCallSet extends Activity {
    @Bind(R.id.bt_set1)
    Button bt_set1;
    @Bind(R.id.bt_set2)
    Button bt_set2;
    @Bind(R.id.bt_set3)
    Button bt_set3;
    //    @Bind(R.id.bt_set4)
//    Button bt_set4;
    @Bind(R.id.et_type1)
    TextView et_type1;
    @Bind(R.id.et_type2)
    TextView et_type2;
    @Bind(R.id.et_type3)
    TextView et_type3;
    @Bind(R.id.et_call1)
    EditText et_call1;
    @Bind(R.id.et_call2)
    EditText et_call2;
    @Bind(R.id.et_call3)
    EditText et_call3;
    @Bind(R.id.iv_back1)
    ImageView ivBack1;
    @Bind(R.id.titleset)
    TextView titleset;
    @Bind(R.id.Rl_service2)
    RelativeLayout RlService2;
    @Bind(R.id.Rl_service3)
    RelativeLayout RlService3;


    private SharedPreferences pref;
    private SharedPreferences.Editor editor1;
    private SharedPreferences.Editor editor2;
    private SharedPreferences.Editor editor3;
//    private SharedPreferences.Editor editor4;

    String devId = SipInfo.paddevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicecall1);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);
        titleset.setText("服务电话");
        TextPaint tp=titleset.getPaint();
        tp.setFakeBoldText(true);
        pref = PreferenceManager.getDefaultSharedPreferences(this);


//        String type1 = pref.getString("type1", "");
        String call1 = pref.getString("call1", "");
//        et_type1.setText(type1);
        et_call1.setText(call1);
//        String type2 = pref.getString("type2", "");
        String call2 = pref.getString("call2", "");
//        et_type2.setText(type2);
        et_call2.setText(call2);
//        String type3 = pref.getString("type3", "");
        String call3 = pref.getString("call3", "");
//        et_type3.setText(type3);
        et_call3.setText(call3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


    @OnClick({R.id.bt_set1, R.id.bt_set2, R.id.bt_set3, R.id.iv_back1})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.bt_set1:
                Log.i("111222", "123414");
                String type1 = et_type1.getText().toString();
                String call1 = et_call1.getText().toString();
                Log.i("mmma", type1);
                Log.i("ammm", call1);
//                if (type1.equals("") || type1 == null) {
//                    Toast.makeText(ServiceCallSet.this, "服务类型为空", Toast.LENGTH_SHORT).show();
//                } else
                if (call1.equals("") || call1 == null) {
                    Toast.makeText(ServiceCallSet.this, "电话号码为空", Toast.LENGTH_SHORT).show();
                } else {
                    editor1 = pref.edit();
//                    editor1.putString("type1", type1);
                    editor1.putString("call1", call1);
                    editor1.apply();
                    Toast.makeText(ServiceCallSet.this, "设置完成", Toast.LENGTH_SHORT).show();
//                    String devId1 = SipInfo.paddevId;
//                    devId = devId1.substring(0, devId1.length() - 4).concat("0160");//设备id后4位替换成0160
//                    String devName1 = "pad";
//                    final String devType1 = "2";
                    SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                    SipInfo.toDev = new NameAddress(devName, sipURL);
                    Message response = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                            SipInfo.user_from, BodyFactory.createServiceCall(type1, call1));
                    SipInfo.sipUser.sendMessage(response);
                }
                break;
            case R.id.bt_set2:
                String type2 = et_type2.getText().toString();
                String call2 = et_call2.getText().toString();
//                if (type2.equals("") || type2 == null) {
//                    Toast.makeText(ServiceCallSet.this, "服务类型为空", Toast.LENGTH_SHORT).show();
//                } else
                if (call2.equals("") || call2 == null) {
                    Toast.makeText(ServiceCallSet.this, "电话号码为空", Toast.LENGTH_SHORT).show();
                } else {
                    editor2 = pref.edit();
//                    editor2.putString("type2", type2);
                    editor2.putString("call2", call2);
                    editor2.apply();
                    Toast.makeText(ServiceCallSet.this, "设置完成", Toast.LENGTH_SHORT).show();
//                    String devId1 = SipInfo.paddevId;
//                    devId = devId1.substring(0, devId1.length() - 4).concat("0160");//设备id后4位替换成0160
//                    String devName1 = "pad";
//                    final String devType1 = "2";
                    SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                    SipInfo.toDev = new NameAddress(devName, sipURL);
                    Message response = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                            SipInfo.user_from, BodyFactory.createServiceCall(type2, call2));
                    SipInfo.sipUser.sendMessage(response);
                }
                break;
            case R.id.bt_set3:
                String type3 = et_type3.getText().toString();
                String call3 = et_call3.getText().toString();
//                if (type3.equals("") || type3 == null) {
//                    Toast.makeText(ServiceCallSet.this, "服务类型为空", Toast.LENGTH_SHORT).show();
//                } else
                if (call3.equals("") || call3 == null) {
                    Toast.makeText(ServiceCallSet.this, "电话号码为空", Toast.LENGTH_SHORT).show();
                } else {
                    editor3 = pref.edit();
//                    editor3.putString("type3", type3);
                    editor3.putString("call3", call3);
                    editor3.apply();
                    Toast.makeText(ServiceCallSet.this, "设置完成", Toast.LENGTH_SHORT).show();
//                    String devId1 = SipInfo.paddevId;
//                    devId = devId1.substring(0, devId1.length() - 4).concat("0160");//设备id后4位替换成0160
//                    String devName1 = "pad";
//                    final String devType1 = "2";
                    SipURL sipURL1 = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                    SipInfo.toDev = new NameAddress(devName, sipURL1);
                    Message response = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                            SipInfo.user_from, BodyFactory.createServiceCall(type3, call3));
                    SipInfo.sipUser.sendMessage(response);
                }
                break;
            case R.id.iv_back1:
                ActivityCollector.removeActivity(this);
                finish();
                break;
            default:
                break;
        }
    }
}








