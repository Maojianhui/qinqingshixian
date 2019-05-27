package com.punuo.sys.app.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.punuo.sys.app.R;

/**
 * Created by Wxcily on 16/1/5.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }
    /**
     * 复写返回键操作,返回true则不继续下发
     *
     * @return
     */
    protected boolean onPressBack() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (onPressBack()) {
            return;
        }
        try {
            super.onBackPressed();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        }
        overridePendingTransition(R.anim.push_right_in, R.anim.right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
