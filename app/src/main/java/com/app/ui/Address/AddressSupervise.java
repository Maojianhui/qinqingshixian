package com.app.ui.Address;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.adapter.AddressItemAdapter;
import com.app.http.GetPostUtil;
import com.app.model.Addressitem;
import com.app.model.Constant;
import com.app.model.MessageEvent;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static com.app.sip.SipInfo.addressList;

public class AddressSupervise extends Activity {
    @Bind(R.id.iv_back1)
    ImageView ivBack1;
    @Bind(R.id.titleset)
    TextView titleset;
    @Bind(R.id.rv_addressDispaly)
    RecyclerView rvAddressDispaly;
    @Bind(R.id.iv_addressicon)
    ImageView ivAddressicon;
    @Bind(R.id.tv_noAddress)
    TextView tvNoAddress;
    @Bind(R.id.btn_newAddress)
    Button btnNewAddress;


    private String mobile = "15990075781";
    private boolean hasAddress = false;
    String response;
    private String TAG = "AddressSupervise";
    private AddressItemAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_supervise);
        ActivityCollector.addActivity(this);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        init();

//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_addressDispaly);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvAddressDispaly.setLayoutManager(layoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }

    }

    private void init() {
        titleset.setText("地址管理");
        TextPaint tp = titleset.getPaint();
        tp.setFakeBoldText(true);
        new Thread(addressAdd).start();
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 0x555) {
                ivAddressicon.setVisibility(View.INVISIBLE);
                tvNoAddress.setVisibility(View.INVISIBLE);
                rvAddressDispaly.setVisibility(View.VISIBLE);
                adapter = new AddressItemAdapter(getApplicationContext(), addressList);
                rvAddressDispaly.setAdapter(adapter);
//                adapter.appendData(addressList);
            } else if (message.what == 0x444) {
                rvAddressDispaly.setVisibility(View.INVISIBLE);
                ivAddressicon.setVisibility(View.VISIBLE);
                tvNoAddress.setVisibility(View.VISIBLE);
            }
        }
    };

    //获取收货地址
    Runnable addressAdd = new Runnable() {
        @Override
        public void run() {
            response = GetPostUtil.sendGet1111(Constant.URL_getAddress, "id=" + Constant.id);
            if ((response != null) && !(("").equals(response))) {
                Log.d("获取地址", response);
                JSONObject obj= JSON.parseObject(response);
                String msg=obj.getString("msg");
                if(msg.equals("success")){
                    praseJSONWithGSON(response);
                }
                else if(msg.equals("地址为空")){
                    handler.sendEmptyMessage(0x444);
                }

            }

        }
    };

    private void praseJSONWithGSON(String response) {
        String jsonData = "[" + response.split("\\[")[1].split("\\]")[0] + "]";
        Gson gson = new Gson();
        addressList = gson.fromJson(jsonData, new TypeToken<List<Addressitem>>() {
        }.getType());
        for(int i=0;i<addressList.size();i++){
            Addressitem addressitem=new Addressitem();
            addressitem.setUserAddress(addressList.get(i).getUserAddress());
            addressitem.setDetailAddress(addressList.get(i).getDetailAddress());
            addressitem.setUserName(addressList.get(i).getUserName());
            addressitem.setUserPhoneNum(addressList.get(i).getUserPhoneNum());
            addressitem.setPosition(addressList.get(i).getPosition());
            addressitem.setIsDefault(addressList.get(i).getIsDefault());
        }
//        for (Addressitem addressitem : addressList) {
//            Log.d(TAG, "address is  " + addressitem.getUserAddress());
//            Log.d(TAG, "detailaddress is  " + addressitem.getDetailAddress());
//            Log.d(TAG, "username is  " + addressitem.getUserName());
//            Log.d(TAG, "userphonenum is  " + addressitem.getUserPhoneNum());
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x555);
            }
        }).start();
    }

    @OnClick({R.id.btn_newAddress, R.id.iv_back1})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_newAddress:
                SipInfo.isEditor = false;
                startActivity(new Intent(this, UserAddress.class));
                break;
            case R.id.iv_back1:
                ActivityCollector.removeActivity(this);
                finish();
                break;
//            case R.id.iv_addressEdit:
//                SipInfo.isEditor = true;
//                startActivity(new Intent(this, UserAddress.class));
//                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getMessage().equals("编辑"))
        {
            SipInfo.isEditor=true;
            startActivity(new Intent(this,UserAddress.class));
        }
        else if(event.getMessage().equals("刷新")){
            onFresh();
//            adapter.appendData(addressList);
        }
    }

    private void onFresh() {
        new Thread(addressAdd).start();
    }

}
