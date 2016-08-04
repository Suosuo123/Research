package com.bx.research.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.bx.research.R;
import com.lidroid.xutils.ViewUtils;

@SuppressLint("NewApi")
public class BaseActivity extends AppCompatActivity {

    protected Activity mActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mActivity = this;
        initWindow();

        onCreate();
        initView();
        initData();
    }

    protected void onCreate() {
    }

    protected void initView() {
    }

    protected void initData() {
    }


    private void initWindow() {

        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
//            window.setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
    }
}
