package com.app.ui.Address;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.RegexUtils;
import com.app.http.ToastUtils;
import com.app.model.Addressitem;
import com.app.model.Constant;
import com.app.model.MessageEvent;
import com.app.sip.SipInfo;
import com.app.tools.ActivityCollector;
import com.app.views.CleanEditText;
import com.hengyi.wheelpicker.listener.OnCityWheelComfirmListener;
import com.hengyi.wheelpicker.ppw.CityWheelPickerPopupWindow;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.sip.SipInfo.addressList;


public class UserAddress extends Activity {
    @Bind(R.id.iv_back1)
    ImageView ivBack1;
    @Bind(R.id.titleset)
    TextView titleset;
    @Bind(R.id.tv_addressSelect)
    TextView tvAddressSelect;
    @Bind(R.id.Rl_address)
    RelativeLayout RlAddress;
    @Bind(R.id.et_detailAddress)
    CleanEditText etDetailAddress;
    @Bind(R.id.et_userName)
    CleanEditText etUserName;
    @Bind(R.id.et_userPhoneNum)
    CleanEditText etUserPhoneNum;
    @Bind(R.id.box1)
    CheckBox box1;
    @Bind(R.id.bt_addressSave)
    Button btAddressSave;

    String response;
    @Bind(R.id.rl_addressDelete)
    RelativeLayout rlAddressDelete;
    private boolean isdefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_address);
        ActivityCollector.addActivity(this);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        if (SipInfo.isEditor) {
            titleset.setText("编辑地址");
            etUserName.setText(addressList.get(SipInfo.listPosition).getUserName());
            etUserPhoneNum.setText(addressList.get(SipInfo.listPosition).getUserPhoneNum());
            etDetailAddress.setText(addressList.get(SipInfo.listPosition).getDetailAddress());
            tvAddressSelect.setText(addressList.get(SipInfo.listPosition).getUserAddress());
            if(SipInfo.isDefault==1){
                isdefault=true;
            }else if(SipInfo.isDefault==2){
                isdefault=false;
            }
            box1.setChecked(isdefault);
        } else {
            titleset.setText("新增地址");
            rlAddressDelete.setVisibility(View.INVISIBLE);
        }
        TextPaint tp = titleset.getPaint();
        tp.setFakeBoldText(true);
        box1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SipInfo.isDefault=1;
                    Log.d("是否默认",SipInfo.isDefault+"-----");
                }else
                    SipInfo.isDefault=2;
                Log.d("是否默认",SipInfo.isDefault+"-----");
            }
        });

        final CityWheelPickerPopupWindow wheelPickerPopupWindow = new CityWheelPickerPopupWindow(this);
        wheelPickerPopupWindow.setListener(new OnCityWheelComfirmListener() {
            @Override
            public void onSelected(String Province, String City, String District, String PostCode) {
                tvAddressSelect.setText(Province + " " + City + " " + District);
                Toast.makeText(getApplicationContext(), Province + City + District, Toast.LENGTH_LONG).show();
            }
        });

        RlAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheelPickerPopupWindow.show();
            }
        });
    }

    @OnClick({R.id.et_detailAddress, R.id.et_userPhoneNum, R.id.et_userName,
            R.id.iv_back1, R.id.bt_addressSave,R.id.rl_addressDelete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_detailAddress:
                break;
            case R.id.bt_addressSave:
                if (SipInfo.isEditor) {
                    addressUpdate();
                } else {
                    addressSave();
                }
                break;
            case R.id.rl_addressDelete:
                Dialog dialog=new AlertDialog.Builder(this)
                        .setMessage("确定要删除该地址吗？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addressDelete();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                dialog.show();
                break;
            case R.id.iv_back1:
                ActivityCollector.removeActivity(this);
                finish();
                break;
        }
    }

    private void addressDelete() {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    response = GetPostUtil.sendGet1111(Constant.URL_deleteAddress, "id=" + Constant.id +
                            "&position=" + SipInfo.addressPosition);
                    if ((response != null) && !(("").equals(response))) {
                        Log.d("删除地址", response);
                        JSONObject obj = JSON.parseObject(response);
                        String msg = obj.getString("msg");
                        if (msg.equals("success")) {
                            handler.sendEmptyMessage(0x111);
                        }
                    }
                }
            }).start();
        }


    private void addressUpdate() {
        SipInfo.userName = etUserName.getText().toString();
        SipInfo.userPhoneNum = etUserPhoneNum.getText().toString();
        SipInfo.userAddress = tvAddressSelect.getText().toString();
        SipInfo.detailAddress = etDetailAddress.getText().toString();
        if (checkInput(SipInfo.userAddress, SipInfo.detailAddress, SipInfo.userName, SipInfo.userPhoneNum)) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    response = GetPostUtil.sendGet1111(Constant.URL_updateAddress, "id=" + Constant.id + "&userAddress=" + SipInfo.userAddress
                            + "&detailAddress=" + SipInfo.detailAddress + "&userName=" + SipInfo.userName + "&userPhoneNum=" +
                            SipInfo.userPhoneNum + "&position=" + SipInfo.addressPosition+"&isDefault="+SipInfo.isDefault);
                    if ((response != null) && !(("").equals(response))) {
                        Log.d("更新地址", response);
                        JSONObject obj = JSON.parseObject(response);
                        String msg = obj.getString("msg");
                        if (msg.equals("success")) {
                            handler.sendEmptyMessage(0x111);
                        }
                    }
                }
            }).start();
        }
    }

    private void addressSave() {
        SipInfo.userName = etUserName.getText().toString();
        SipInfo.userPhoneNum = etUserPhoneNum.getText().toString();
        SipInfo.userAddress = tvAddressSelect.getText().toString();
        SipInfo.detailAddress = etDetailAddress.getText().toString();
        if(checkInput(SipInfo.userAddress, SipInfo.detailAddress, SipInfo.userName, SipInfo.userPhoneNum)){
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = GetPostUtil.sendGet1111(Constant.URl_addAddress, "id=" + Constant.id + "&userAddress=" + SipInfo.userAddress
                        + "&detailAddress=" + SipInfo.detailAddress + "&userName=" + SipInfo.userName + "&userPhoneNum=" +
                        SipInfo.userPhoneNum+"&isDefault="+SipInfo.isDefault);
                if ((response != null) && !(("").equals(response))) {
                    Log.d("添加地址", response);
                    JSONObject obj = JSON.parseObject(response);
                    String msg = obj.getString("msg");
                    if (msg.equals("success")) {
                        handler.sendEmptyMessage(0x111);
                    }
                }
            }
        }).start();
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 0x111) {
                EventBus.getDefault().post(new MessageEvent("刷新"));
                finish();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private boolean checkInput(String userAddress, String detailAddress, String userName, String userPhoneNum) {
        if(TextUtils.isEmpty(userPhoneNum)){
            ToastUtils.showShort(this,"手机号码不能为空");
        }else if(!RegexUtils.checkMobile(userPhoneNum)){
                ToastUtils.showShort(this,"手机号码格式不正确" );
            }else if(TextUtils.isEmpty(userName)){
                ToastUtils.showShort(this,"请输入收货人姓名");
            }else if(TextUtils.isEmpty(detailAddress)){
                ToastUtils.showShort(this,"请输入具体收货地址");
            }else  if(TextUtils.isEmpty(userAddress)){
                ToastUtils.showShort(this,"请选择所在地区");
            }else {
            return true;
        }
        return false;
    }
}


