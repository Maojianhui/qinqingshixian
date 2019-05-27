package com.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.R;
import com.app.tools.ActivityCollector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddressManage extends AppCompatActivity {

    @Bind(R.id.iv_back1)
    ImageView ivBack1;
    @Bind(R.id.titleset)
    TextView titleset;
    @Bind(R.id.btn_addaddress)
    Button btnAddaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_manage);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);
        init();
    }

    private void init() {
        titleset.setText("地址管理");
    }



    @OnClick({R.id.iv_back1,R.id.btn_addaddress})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_addaddress:
                startActivity(new Intent(this,AddressManage.class));
                break;
            case R.id.iv_back1:
                ActivityCollector.removeActivity(this);
                finish();
        }
    }
}
