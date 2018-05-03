package com.app.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.app.R;
import com.app.adapter.FriendAdapter;
import com.app.sip.SipInfo;

import butterknife.Bind;
import butterknife.ButterKnife;


public class friendActivity extends AppCompatActivity {

    @Bind(R.id.t1)
    ListView t1;
    private FriendAdapter friendAdapter;
    private int lastPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        ButterKnife.bind(this);
        //Collections.sort(SipInfo.friends);
        Log.d("oncreate","1111111111");
        friendAdapter = new FriendAdapter(SipInfo.friends, this);
        t1.setAdapter(friendAdapter);
    }
}
